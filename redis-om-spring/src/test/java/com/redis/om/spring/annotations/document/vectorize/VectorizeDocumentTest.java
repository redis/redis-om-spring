kpo tgmoodrr.ios. smrr.pi.t.tr rc.tseedmcneeonned tso;rdiz e ;
m  r  tcocde .s s. m prnix . rl. e  euu  m. tdtT. P t r u
mo rmmo   d e i s sp ige n t t  h um  do eh  .mm el r.o  t c;
po rt tcc.did.psgivm erio Ps ut..strorl;aaE.nnmoSieymPro.rt d;c  o
m ot omr. re.er r  sp.s z usi. ed ibc eE  y S itr r t ty d otR  pe tr soi y ;
ip o  ot .e  .pnptrmnr.a. . ia c;F h  dms   ;a e  et .me ;;Pu
oorotmtm is.ppm m.n dtpef o e r  x   r           d  d ec aa  n
rp g om.rp T u.i igtn    e r .                           ;  n. et
pprrm    .ci  .oio  tEa  r                                r o e r o
oo r.cers . r. s;t  co                                      n  oar
mprtgjo.fr.rria.s ra t                                       t  i;  d
mp o  gar  prtaa.en                                            . ui ;
mrorto   jn oefpjeB                                            ti. w
iog ao..ss t agsc.                                              t  fl
p     oaE  .xonb .i                                           i j A
m o tj wvpI reO eo at                                          n  I
rp r vtiaa mo ;ies t                                          p  de
m p  a a .. ur om;i l lt                                     uub n
p  rt va ul ltOi xn s c o                                j     E
m po t  u i jcu tt. t o es r                          e  T ;
o o rtas t  ca i t l e r. .e. ;  s s         s as rs th  t
i pr a  s tir og . ns  tr p ji pa.  Aesti t on .    e A a ;
s a s  c tt     goj.s .  t  eoa .iA  .er r o ns.a sr t l l
l  oV  ero  rzd ec n t eu is tr p  s s  r iB    Ds u  t Tes  t{
P tu A wie  ie D  um  iee tx et e   A t  a ca s e o me  n
 rodu cwR t tdi osy  reT i t r  ; nd b s    t    c
@ uAt o ipr e o tr i  to  so ;  y
t  n t ySt  an   etS      ema
    i  wir r em       yr
@uA to rd e ede   er
E bme d   e mb  d d  ;         E        o
  B f  re Ec  h a    ) th Or Ix 0ec{  t   n{
o dilo adoieoDt no(rc.d Pu(ef==a)ti(tc si ts)"",do"t eclsss sa ht /e  :am
 if ( eo p   s oya (r T t( v l . os " cu ai2  "a c  ss i ae e  s  hmg   s / atc. j p
  re  opi rrty o.ah decv caotuo s(m e  t   e   c c a  atp esc pi : f amcl rn   i rvom "u
      p si "ys s(s yiPtFu d f " o                      cp llsn  gth  a i  a s t j a   s " . mal ) ") ;
  re o  s "t    .a ( (eI  y ,                              sl:stst a  i l ce d/  i 2p g  ,
      ospo  ic .oe   dg ".                                   c ai//fy /j i e  e  n s mo no mlyf rre etr oedat s hdeem ost  cc atoh uose c t" )) ;
   re    d uo d .f ," an                                         s:i /dfl Fp  a  ,
     o Prp  tr   a  e o                                         s hpt.  jo . gg"   "     T hi sisa ip c tu reo faca tand aodg toe teg h r") ) ;
   re  s o iu ova  "v                                            h"eaacp e  ,
      P r d o P( tc"                                              m st taige  "      T hery eer asla e r,ctheo iffn a s s tlil ulf loJoe ll " .) ) ;
  re o p i oyr s.u. (                                             " ,s /c m sf/ aj e. 2pg ",
        s t   ( c  " f                                            z ca g a  c)
        "  h  .r frt c                                            s p/  ae. "  );
    }      T p  o xs                                             o  f so   r
  }         e  es bac e                                        eyns th  lt
                  wno pd                                        n  mn
 @e Ts t             a @ki                                   ade o
@n Ea ble df I(       {  e xt  rh                       y m
    exr pes ison= " #  f  t au e  c o. j    e yl  }l,  /
   l o dCa noe txt =tr e / / r  t a t ri sR ad ( )"   /
      )                 or    E            e
vo ditestI Img  t scV eez ie d( ){
 Opt  oiln au r  c dct>  r = t  r i iy.fin dFtrsNy me aa)("c t  ";
  as r setA ll( / /     a
       ()> -asr se ha t  a .ti)s sr e n(  ), //
       ()> -asr se That(t  .gte)( r .  c ai g"m a(eigmbe di  "  .si Nt lN ) (l , //
       ()> -asr se That(t  .gte)( mg. aI e d iednm.g bh()a1  ( iz e)52 );
  }

 @e Ts t
@n Ea ble df I(
    expr e ss"i n= #e{fu at rtex rrao t. s "eaR()d y}, //
   l o dCa noe txt =tr ue/ /
      )
vod ite stStenn s Vetec  z ior ed () {
 Opt  oiln au r  c dc> e atr t  r i iy.fin dFtrsNy me aa)("c t  ";
  as r setA ll( / /
       ()> -asr se ha t  a .ti)s sr e n(  ), //
       ()> -asr se That(t  .gte)( r .  c ai g"e n(t ec Em dbed" . )g o uNiN  )l(  ,//
       ()> -asr se That(t  .gte)( eg. nS temn ecneb(ddig ) a e isS z(384 )
    );

  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnImageSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.IMAGE_EMBEDDING.knn(K, cat.getImageEmbedding())) //
        .sorted(Product$._IMAGE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(Product::getName).containsExactly( //
        "cat", "cat2", "catdog", "face", "face2" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnSentenceSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(5).map(Product::getName).containsExactly( //
        "cat", "cat2", "catdog", "face", "face2" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnHybridSentenceSimilaritySearch() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Product> results = stream //
        .filter(Product$.NAME.startsWith("cat")) //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .collect(Collectors.toList());

    assertThat(results).hasSize(3).map(Product::getName).containsExactly( //
        "cat", "cat2", "catdog" //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testKnnSentenceSimilaritySearchWithScores() {
    Product cat = repository.findFirstByName("cat").get();
    int K = 5;

    SearchStream<Product> stream = entityStream.of(Product.class);

    List<Pair<Product, Double>> results = stream //
        .filter(Product$.SENTENCE_EMBEDDING.knn(K, cat.getSentenceEmbedding())) //
        .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
        .limit(K) //
        .map(Fields.of(Product$._THIS, Product$._SENTENCE_EMBEDDING_SCORE)) //
        .collect(Collectors.toList());

    assertAll( //
        () -> assertThat(results).hasSize(5).map(Pair::getFirst).map(Product::getName)
            .containsExactly("cat", "cat2", "catdog", "face", "face2") //
    );
  }

  @Test
  @EnabledIf(
      expression = "#{@featureExtractor.isReady()}", //
      loadContext = true //
      )
  void testEmbedderCanVectorizeSentence() {
    Optional<Product> maybeCat = repository.findFirstByName("cat");
    assertThat(maybeCat).isPresent();
    Product cat = maybeCat.get();
    var catEmbedding = cat.getSentenceEmbedding();
    List<float[]> embeddings = embedder.getTextEmbeddingsAsFloats(List.of(cat.getDescription()), Product$.DESCRIPTION);
    assertAll( //
        () -> assertThat(embeddings).isNotEmpty(), //
        () -> assertThat(embeddings.get(0)).isEqualTo(catEmbedding));
  }
}




