package com.redis.om.spring.repository.support;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.redis.om.spring.RedisEnhancedKeyValueAdapter;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.audit.EntityAuditor;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.id.ULIDIdentifierGenerator;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.mapping.RedisEnhancedPersistentEntity;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.metamodel.MetamodelUtils;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.EntityStreamImpl;
import com.redis.om.spring.search.stream.RedisFluentQueryByExample;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.util.ObjectUtils;
import com.redis.om.spring.vectorize.Embedder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.keyvalue.core.IterableConverter;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.KeyValuePersistentEntity;
import org.springframework.data.keyvalue.repository.support.SimpleKeyValueRepository;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.redis.core.convert.ReferenceResolverImpl;
import org.springframework.data.redis.core.mapping.RedisPersistentProperty;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.redis.om.spring.util.ObjectUtils.*;

public class SimpleRedisEnhancedRepository<T, ID> extends SimpleKeyValueRepository<T, ID>
    implements RedisEnhancedRepository<T, ID> {

  protected final RedisModulesOperations<String> modulesOperations;
  protected final EntityInformation<T, ID> metadata;
  protected final KeyValueOperations operations;
  protected final RediSearchIndexer indexer;
  protected final MappingRedisOMConverter mappingConverter;
  protected final RedisEnhancedKeyValueAdapter enhancedKeyValueAdapter;
  protected final EntityAuditor auditor;
  protected final Embedder embedder;

  private final ULIDIdentifierGenerator generator;
  private final RedisOMProperties properties;

  private final EntityStream entityStream;

  @SuppressWarnings("unchecked")
  public SimpleRedisEnhancedRepository( //
      EntityInformation<T, ID> metadata, //
      KeyValueOperations operations, //
      @Qualifier("redisModulesOperations") RedisModulesOperations<?> rmo, //
      RediSearchIndexer indexer, //
      Embedder embedder, //
      RedisOMProperties properties //
  ) {
    super(metadata, operations);
    this.modulesOperations = (RedisModulesOperations<String>) rmo;
    this.metadata = metadata;
    this.operations = operations;
    this.indexer = indexer;
    this.mappingConverter = new MappingRedisOMConverter(null, new ReferenceResolverImpl(modulesOperations.template()));
    this.enhancedKeyValueAdapter = new RedisEnhancedKeyValueAdapter(rmo.template(), rmo, indexer, embedder,
        properties);
    this.generator = ULIDIdentifierGenerator.INSTANCE;
    this.auditor = new EntityAuditor(modulesOperations.template());
    this.embedder = embedder;
    this.properties = properties;
    this.entityStream = new EntityStreamImpl(modulesOperations, modulesOperations.gsonBuilder(), indexer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterable<ID> getIds() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    String searchIndex = indexer.getIndexName(keyspace);

    SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
    Optional<Field> maybeIdField = ObjectUtils.getIdFieldForEntityClass(metadata.getJavaType());
    String idField = maybeIdField.map(Field::getName).orElse("id");
    Query query = new Query("*");
    query.limit(0, properties.getRepository().getQuery().getLimit());
    query.returnFields(idField);
    SearchResult searchResult = searchOps.search(query);

    return (List<ID>) searchResult.getDocuments().stream() //
        .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
        .map(e -> ObjectUtils.getIdFieldForEntity(maybeIdField.get(), e)) //
        .toList();
  }

  @Override
  public Page<ID> getIds(Pageable pageable) {
    List<ID> ids = Lists.newArrayList(getIds());

    int fromIndex = Long.valueOf(pageable.getOffset()).intValue();
    int toIndex = fromIndex + pageable.getPageSize();

    return new PageImpl<>(ids.subList(fromIndex, toIndex), pageable, ids.size());
  }

  @Override
  public void updateField(T entity, MetamodelField<T, ?> field, Object value) {
    PartialUpdate<?> update = new PartialUpdate<>(metadata.getId(entity).toString(), metadata.getJavaType()).set(
        field.getSearchAlias(), value);

    enhancedKeyValueAdapter.update(update);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F> Iterable<F> getFieldsByIds(Iterable<ID> ids, MetamodelField<T, F> field) {
    RedisTemplate<String, String> template = modulesOperations.template();
    List<String> keys = StreamSupport.stream(ids.spliterator(), false) //
        .map(this::getKey).toList();

    return (Iterable<F>) keys.stream() //
        .map(key -> template.opsForHash().get(key, field.getSearchAlias())) //
        .collect(Collectors.toList());
  }

  @Override
  public Long getExpiration(ID id) {
    RedisTemplate<String, String> template = modulesOperations.template();
    return template.getExpire(getKey(id));
  }

  @Override
  public boolean setExpiration(ID id, Long expiration, TimeUnit timeUnit) {
    RedisTemplate<String, String> template = modulesOperations.template();
    return Boolean.TRUE.equals(template.expire(getKey(id), expiration, timeUnit));
  }

  /* (non-Javadoc)
   *
   * @see org.springframework.data.repository.CrudRepository#findAll() */
  @Override
  public List<T> findAll() {
    return IterableConverter.toList(operations.findAll(metadata.getJavaType()));
  }

  // -------------------------------------------------------------------------
  // Methods from PagingAndSortingRepository
  // -------------------------------------------------------------------------

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Sort) */
  @Override
  public List<T> findAll(Sort sort) {

    Assert.notNull(sort, "Sort must not be null!");

    Pageable pageRequest = PageRequest.of(0, properties.getRepository().getQuery().getLimit(), sort);

    return findAll(pageRequest).toList();
  }

  /* (non-Javadoc)
   *
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Pageable) */
  @Override
  public Page<T> findAll(Pageable pageable) {
    Assert.notNull(pageable, "Pageable must not be null!");

    if (pageable.isUnpaged()) {
      List<T> result = findAll();
      return new PageImpl<>(result, Pageable.unpaged(), result.size());
    }

    if (indexer.indexDefinitionExistsFor(metadata.getJavaType())) {
      String searchIndex = indexer.getIndexName(metadata.getJavaType());
      SearchOperations<String> searchOps = modulesOperations.opsForSearch(searchIndex);
      Query query = new Query("*");
      query.limit(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());

      pageable.getSort();
      for (Order order : pageable.getSort()) {
        query.setSortBy(order.getProperty(), order.isAscending());
      }

      SearchResult searchResult = searchOps.search(query);

      if (searchResult.getTotalResults() > 0) {
        @SuppressWarnings("unchecked") List<T> content = (List<T>) searchResult.getDocuments().stream() //
            .map(d -> ObjectUtils.documentToObject(d, metadata.getJavaType(), mappingConverter)) //
            .toList();
        return new PageImpl<>(content, pageable, searchResult.getTotalResults());
      } else {
        return Page.empty();
      }
    } else {
      Iterable<T> content = operations.findInRange(pageable.getOffset(), pageable.getPageSize(), pageable.getSort(),
          metadata.getJavaType());

      return new PageImpl<>(IterableConverter.toList(content), pageable, this.operations.count(metadata.getJavaType()));
    }
  }

  @Override
  public String getKeyspace() {
    return indexer.getKeyspaceForEntityClass(metadata.getJavaType());
  }

  @Override
  public <S extends T> S update(Example<S> example) {
    S probe = example.getProbe();
    ExampleMatcher matcher = example.getMatcher();
    ID id = metadata.getId(probe);

    if (id= =nu ll ){
    tr hno eIw g l aA u egr n x tEcpenoma (e" Ep  olbejcms tuth a eanI D ") ;
    }

   St ringk ey=e gtK ey (i) d;

  Cl as<s>te ?inty aTp= em  td t . ge vtJa T ype () ;
 Lis t<dO p  t epertiauo nO pdae rtoe iatns=A rnew y Lris t <>( ) ;

L is att M m dl Fi el <?d ,> e ?atd  el lFdea ts = emod lUt  tsg mM.teeed e FoFsllPdoro teepris (ett yi T ype,
     e tgAlloPpr  e tinr (tsi Tty )yp e) ;

  fSti(MnetgorpeeeyrtNamm?e=odaeltaoee d.eeSlmrtl iecahAl(as ;d) ){

   f i( po pt  ym"N. q easa  ( l id )" ){
      o c ntinu e ;
      }        l  c  l P p e   rh t  c  p
        su oI hn d u ed rto( yt ae me  r or p e t N me   ){
    i f(      t  lu e =e r Ptp   y  l( ,e b r e yra    )N
      bO j c ea   =n gul l{ o dreV apu rp   e , pyo  r m ta )e ;
       if (v lu ep!r tos) d (t n wU dd a epe  it    pm
        u p ateO   i  .  pa p    y    (  )t Oa { (  e  a t
     } a  pd t  a s Enm                    a)  r n ok y e  dome il e vd ,a ul e)) ;
    u te  r s.e i u                              tee r ,
   }   p Oe  s(                                     O   )
     (iU i n                                          on
  ifue a  a                                           p  ;
 e e d te t                                            at s
   x el d                                               i e
  }r cP n                                                num
 r S  if d                                              )> xe to "(   ia
   u (  iB                                              t e c  i n  Fe l tdf ocet  hpudated ten)t iy ") ;
  }t n)d (.                                           - (    p
@vO r in )d o                                        w( w iE
  u e rdxy  e v                                     o n R e
p    ic  <e is>T r                                l xl<  > x amlps e) {
  bil ! e e a  lo pEd u  h                (  T hbr< m e S >
     f(Sx n per  teio p a e a l ots te  A )  tre Eap
   }  rtU r ;/ /N .x em ).r  o tNr x esl e){ e a
     t e d aO e pe t o  le sap es  rc(v sn  nA  ewr rL  s  <
  Li ss< <  p  i  Ty   = u mtp Oap e etJ o s=e a    y i  t >  ();
  Cl a<s ?e>edtt   aelp e>> dd am a.a ta i apy( ) ;m d U  te
  Li st Mtta  m lFe   d , ? ?e >  tage F l Tl s Met o  l l i sg. eoelMmt aoeiF loF  stP rp (r e  ei  t yT ype,
     eg  A l P<oe  i ms < ni m  t yeo d ;i e d =    a
        E al l x  r > i( pet t yTp )l ))
  fo r( pxm e  epeSe x. at o :ermpa )es   {
    S r po be  =   a  pl e lP x  (e ; .e  t a
  E x m  a le a cmh  a  g cbere a pl e     tc  er(  )h;
      D id =m e ta  r t a  h(p m x  ); g M
     iIf( id =e=nldl ){lg eredo tr e
     h tr o wn e e I  lgg A m n xt epc inop(" xaEm bl j csemt ut hav eanI"D ) ;
      }         w l  a       u

   S  trin g key =ge teK) y(id  ;

   (f ore t ao Me lF  <eid ?,>a dm to eem Fil d mt :da oe ilFl e s ) {
     t Si n prg p e tNra emm= a mtF eoei d gee ct.Aa dSh lis a() ;

      f ( ho uI d nclP eurt per yo(m, ra chr ep rNpta m ) )e  {

        Ob e c vPtal uet g eop ea (t  pe  b puoe  or tp eN  ea m);

         i f(va u le!n= ul l) {
         ud pateprO tao eneidsa  (nwpd aptUeOe at(iyon temm  la,eode l  d, valu e) );
          }
        }
      }
    }                       ap  ae  ea o n);
                   p  dte (u i d t O is
 xee ceu P i ep n ei d  UsTn )t { r t
  }                   r (y x  s ip
     r  r d e     y  F ot  ney f o
@ vOe  tcir  g ge t Kt etet y'n t
pb uli Ge Smi a p  igc e  r Es  t
   //   t t n eh epin en  n>it
  Red iE h  n eP c  dst t ee ttg                                        En   C?)cein  tpi tigersari e ept  o a. g p nMi gntCex t ()
    g   s  aeq ied rsse tE set  s
       e. t Rr g r  P rn  t(?ti yC
  Strin  tgs oim  nd;  e< i e a C
       n d  l c  p so t es sE  lt t(
  / /H seat e ti i i .EID ytyip t sF
 i f(p n ra n rs a t t  ne l a   om ()l  >{                            e t )
   B e W a  p r w npr rpi nCID ws ti e e)   )                   ;  ar p    y ;
   Li t <rs  pg  d ae > t r=n  rAeri ysL  Ac  sa    l )  Be  a  W p (  nit
      o d S Rtr  ii  ts r oe =e wyr dcdie it( ; s Flba  kt  i r  g et  r e t
    fbfijp( opr ettyytuV!aelu=t wla epso t rP: ptVr luidtePy petrte g ta e r )) ;is) ) {
       idP  rps  rp  Veal  = n ul ){ rt.  ei  e ;    a(    r      y
        }    at .  a  d    p rV au . tS  r  (  )  )
      }              d (o  r t yl  e o  n g
       i  g d      .        (
   sr ts  n{  =S it  rng ji  o n"    ":, id Pa  tr) s;
    }e l e t I ge dt i Feld o E rn n ty
  O  b je c  d  ai pn teg go Crn ett r (e tty i) ;
   s t rn gd imn el  ieF  r t i esx r v Ser o  ce )n.v (i ocv trd (inS gtri  .sa lcs) ;
 r  }ab e btd i Iee  inl  e. rt t >n  e    e
v af m e I F r ref ) iF l (tre i  ( ) ntP t(t
imte tt in= ripi t f S +f. g sa Ii tlr (s  d) dnef  il  erFm  (da eeo a. aa yJv )a Tp e );
Idn}i  s fc ee s  y  b l  c iet xr  g  d ) dn i{ t
tr  e fg  =e  n  r                n ed {rsd I r i   e<il rSt i)>gd b ya eeIfn tFi li  t.g  rt  ( );
ng g Kj  i                            I r i g; ene t  i
n  t K                                    e.I t;         f
In  e                                          g            i e
rda                                          n  {              ri tre d
v                                                                F  oa (g
i P                                                                Fr t ae  v
me                                                                  l tety t aT
bra                                             S                   b mf ar   a ype( ))  ;
(ie                                                                   e i . eJ  e     r
a ei                                                                 e i m ninf  F il  e. (etg );
idefoi                                                                rS ad e i r     t
ny i   r                                   )                          r i) t
ite= it Ft e                                                        t li gI
r t if d<r rFi (  es  t n        (                                   < >
}} ef  rl ee  ecga  ( S(t ; dl) t) d
v  Ogur i e  eyr p dr  tst ( ng; Ai  I                        >ti  e
pble  c re etde(  )   S=ii<.i ve l(   e                   t S ti  so){ n
 A sus<i tx SnK lts t ii d   a  >ie  v; t r       r  b e <e n m t n  b e ul l! " );
      i Sn ot u Nt  Te ir  ss T Lh l () nIe b lte  fael  ie    s   t
 L se(r  <>i se n e s (e Ay" ,   e< >  n  a  e e o  n t i   s u
   tr ytJ a   ejd  pi =o dj  p e aOt  si l i  () d geJ i (tg) ).t e( ){
   P  ip led sp  dl    er lui s r  e on  .c(n  t . e  s
       e   in e ei t:mne= tee d p.i li ee) d;
    f o (rSent t i yw= i i   .i){ sp  e(t i )t y
      ob ole a n seN   n ei a  sd N ?w  V n      ;      m            t
            ui  er  s i mt  n ta  y?  >y  lu aeE t it= p ap  igC o
     e yKV alP  e   qse  te   t e n, t   ss a U n U yt  e r  nn  v ret  pg  M n  gpC tonx  et(  )
         .e  tR uri  N er d si < E tite ( Cl  st l ig . se   t s( ts ni) y) ;
      b Ojc et  id= ire I  ? Ptn  d iie y  ke  ( pe ytE  ilC a. p Id e r   o    g
         gn eeta n .   .et w nPt en  f rf s T ag ( e.l  n y pt r eP ryl) ne(yT teIr en p Iofimt an () ):
         ky eValu e Eoy   i e ayt rt  Aeo cr i s t n  e o Py rk a y ( e ni u et.Ey e rd P prot (  )y );
    e yk V l    n  t  t e  o  rgA c ee sc   y  ee.s  e r tt   kta l  yt V t  te e  gg trp  y), (i d) ;
           a u E  e t. et gt r et  p cs t t(r i ) ( nPt  reo (    yue  i VE y . Ir    d
     t Sr i n i sg it y= Pnil a t yy eeF ri r t ng pe d t it y); e
             d    S A r i gv   lE K y   oe  K i  ai  ( n
     t Sn r iks ey a k= e Vae y u e  i tt  S. y g e pc  ) ;
     y b[t ]eob  ejt K e  e  a  t k e nae  pe ,   S s ri gn)  ;
                        =  y    K   s

      // pc r oessnt eitpeyr  a  v m  uitt  on
     u daio r rpoc .s sn i n yt y  et  t N ew)  ;
                           t ,  ( s  i  i


        RedisData rdo = new RedisData();
        mappingConverter.write(entity, rdo);

        pipeline.hmset(objectKey, rdo.getBucket().rawMap());

        if (expires(rdo)) {
          pipeline.expire(objectKey, rdo.getTimeToLive());
        }

        saved.add(entity);
      }
      pipeline.sync();
    }

    return saved;
  }

  public byte[] createKey(String keyspace, String id) {
    // handle IdFilters
    var maybeIdentifierFilter = indexer.getIdentifierFilterFor(keyspace);
    if (maybeIdentifierFilter.isPresent()) {
      IdentifierFilter<String> filter = (IdentifierFilter<String>) maybeIdentifierFilter.get();
      id = filter.filter(id);
    }

    return this.mappingConverter.toBytes(keyspace.endsWith(":") ? keyspace + id : keyspace + ":" + id);
  }

  private boolean expires(RedisData data) {
    return data.getTimeToLive() != null && data.getTimeToLive() > 0L;
  }

  // -------------------------------------------------------------------------
  // Query By Example Fluent API - QueryByExampleExecutor
  // -------------------------------------------------------------------------

  @Override
  public <S extends T> Optional<S> findOne(Example<S> example) {
    Iterable<S> result = findAll(example);
    var size = Iterables.size(result);
    if (size > 1) {
      throw new IncorrectResultSizeDataAccessException("Query returned non unique result", 1);
    }

    return StreamSupport.stream(result.spliterator(), false).findFirst();
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example) {
    return entityStream.of(example.getProbeType()).filter(example).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Iterable<S> findAll(Example<S> example, Sort sort) {
    return entityStream.of(example.getProbeType()).filter(example).sorted(sort).collect(Collectors.toList());
  }

  @Override
  public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
    SearchStream<S> stream = entityStream.of(example.getProbeType());
    var offset = pageable.getPageNumber() * pageable.getPageSize();
    var limit = pageable.getPageSize();
    Page<S> page = stream.filter(example).loadAll().limit(limit, Math.toIntExact(offset))
        .toList(pageable, stream.getEntityClass());

    return page;
  }

  @Override
  public <S extends T> long count(Example<S> example) {
    return entityStream.of(example.getProbeType()).filter(example).count();
  }

  @Override
  public <S extends T> boolean exists(Example<S> example) {
    return count(example) > 0;
  }

  // -------------------------------------------------------------------------
  // Query By Example Fluent API - QueryByExampleExecutor
  // -------------------------------------------------------------------------

  @Override
  public <S extends T, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
    Assert.notNull(example, "Example must not be null");
    Assert.notNull(queryFunction, "Query function must not be null");

    return queryFunction.apply(
        new RedisFluentQueryByExample<>(example, example.getProbeType(), entityStream, getSearchOps(),
            mappingConverter.getMappingContext()));
  }

  private void executePipelinedUpdates(List<UpdateOperation> updateOperations) {
    try (Jedis jedis = modulesOperations.client().getJedis().get()) {
      Pipeline pipeline = jedis.pipelined();

      Map<String, Map<byte[], byte[]>> updates = new HashMap<>();

      for (UpdateOperation op : updateOperations) {
        byte[] value = convertToBinary(op.field, op.value);
        if (value != null && value.length > 0) {
          updates.computeIfAbsent(op.key, k -> new HashMap<>())
              .put(SafeEncoder.encode(op.field.getSearchAlias()), value);
        }
      }

      for (Map.Entry<String, Map<byte[], byte[]>> entry : updates.entrySet()) {
        if (!entry.getValue().isEmpty()) {
          pipeline.hmset(SafeEncoder.encode(entry.getKey()), entry.getValue());
        }
      }

      pipeline.sync();
    }
  }

  private byte[] convertToBinary(MetamodelField<?, ?> field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return SafeEncoder.encode((String) value);
    }
    RedisData redisData = new RedisData();
    mappingConverter.write(value, redisData);
    byte[] binaryValue = redisData.getBucket().get(field.getSearchAlias());
    return binaryValue != null && binaryValue.length > 0 ? binaryValue : null;
  }

  private SearchOperations<String> getSearchOps() {
    String keyspace = indexer.getKeyspaceForEntityClass(metadata.getJavaType());
    String searchIndex = indexer.getIndexName(keyspace);
    return modulesOperations.opsForSearch(searchIndex);
  }

  private String validateKeyForWriting(Object id, Object item) {
    // Get the mapping context's entity info
    RedisEnhancedPersistentEntity<?> entity = (RedisEnhancedPersistentEntity<?>) mappingConverter.getMappingContext()
        .getRequiredPersistentEntity(item.getClass());

    // Handle composite IDs
    if (entity.isIdClassComposite()) {
      BeanWrapper wrapper = new DirectFieldAccessFallbackBeanWrapper(item);
      List<String> idParts = new ArrayList<>();

      for (RedisPersistentProperty idProperty : entity.getIdProperties()) {
        Object propertyValue = wrapper.getPropertyValue(idProperty.getName());
        if (propertyValue != null) {
          idParts.add(propertyValue.toString());
        }
      }

      return String.join(":", idParts);
    } else {
      // Regular single ID handling
      return mappingConverter.getConversionService().convert(id, String.class);
    }
  }
}








