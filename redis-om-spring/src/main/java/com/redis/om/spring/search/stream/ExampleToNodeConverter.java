package com.redis.om.spring.search.stream;

import com.redis.om.spring.RediSearchIndexer;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.search.stream.predicates.jedis.JedisValues;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.geo.Point;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;
import redis.clients.jedis.search.querybuilder.QueryNode;
import redis.clients.jedis.search.querybuilder.Values;
import redis.clients.jedis.search.schemafields.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class ExampleToNodeConverter<E> {

  private final RediSearchIndexer indexer;

  public ExampleToNodeConverter(RediSearchIndexer indexer) {
    this.indexer = indexer;
  }

  private static final Pattern SCHEMA_FIELD_NAME_PATTERN = Pattern.compile("Field\\{name='(.*?)'");
  private static Optional<String> getAliasForSchemaField(SchemaField schemaField) {
    Optional<String> alias = Optional.empty();
    Matcher matcher = SCHEMA_FIELD_NAME_PATTERN.matcher(schemaField.toString()); // TODO:

    if (matcher.find()) {
      String name = matcher.group(1);
      int aliasStart = name.indexOf("AS");
      if (aliasStart != -1) {
        alias = Optional.of(name.substring(aliasStart + 3));
      }
    }

    return alias;
  }

  public Node processExample(Example<E> example, Node rootNode) {
    Class<?> entityClass = example.getProbeType();
    final List<SchemaField> schema = indexer.getSchemaFor(entityClass);
    final boolean matchingAll = example.getMatcher().isAllMatching();
    Set<String> toIgnore = example.getMatcher().getIgnoredPaths();

    if (schema != null) {
      for (SchemaField schemaField : schema) {
        Optional<String> maybeAlias = getAliasForSchemaField(schemaField);
        final String fieldName = maybeAlias.orElseGet(() -> schemaField.name.replace("$.", "")); // TODO:

        if (!toIgnore.contains(fieldName)) {
          Object value = ObjectUtils.getValueByPath(example.getProbe(), schemaField.name); // TODO:

          if (value != null) {
            Class<?> cls = value.getClass();

              //
              // TAG Index Fields
              //
              if (schemaField instanceof TagField) {
                if (Iterable.class.isAssignableFrom(value.getClass())) {
                  Iterable<?> values = (Iterable<?>) value;
                  values = StreamSupport.stream(values.spliterator(), false) //
                      .filter(Objects::nonNull).collect(Collectors.toList());
                  if (values.iterator().hasNext()) {
                    QueryNode and = QueryBuilders.intersect();
                    for (Object v : values) {
                      if (!v.toString().isBlank()) and.add(fieldName, "{" + v + "}");
                    }
                    if (matchingAll) {
                      rootNode = QueryBuilders.intersect(rootNode, and);
                    } else {
                      rootNode = QueryBuilders.union(rootNode, and);
                    }
                  }
                } else {
                  if (matchingAll) {
                    rootNode = QueryBuilders.intersect(rootNode).add(fieldName, "{" + value + "}");
                  } else {
                    rootNode = QueryBuilders.union(rootNode).add(fieldName, "{" + value + "}");
                  }
                }
              }
              //
              // TEXT Index Fields
              //
              else if (schemaField instanceof TextField) {
                switch (example.getMatcher().getDefaultStringMatcher()) {
                  case DEFAULT, EXACT ->
                      rootNode = isNotEmpty(value) ? QueryBuilders.intersect(rootNode).add(fieldName, QueryUtils.escape(value.toString(), false)) : rootNode;
                  case STARTING ->
                      rootNode = isNotEmpty(value) ? QueryBuilders.intersect(rootNode).add(fieldName, QueryUtils.escape(value.toString(), false) + "*") : rootNode;
                  case ENDING ->
                      rootNode = isNotEmpty(value) ? QueryBuilders.intersect(rootNode).add(fieldName, "*" + QueryUtils.escape(value.toString(), false)) : rootNode;
                  case CONTAINING ->
                      rootNode = isNotEmpty(value) ? QueryBuilders.intersect(rootNode).add(fieldName, "*" + QueryUtils.escape(value.toString(), false) + "*") : rootNode;
                  case REGEX -> {
                    // NOT SUPPORTED
                  }
                }
              }
              //
              // GEO Index Fields
              //
              else if (schemaField instanceof GeoField) {
                double x, y;
                if (cls == Point.class) {
                  Point point = (Point) value;
                  x = point.getX();
                  y = point.getY();
                  if (matchingAll) {
                    rootNode = QueryBuilders.intersect(rootNode).add(fieldName, String.format("[%s %s 0.0001 mi]", x, y));
                  } else {
                    rootNode = QueryBuilders.union(rootNode).add(fieldName, String.format("[%s %s 0.0001 mi]", x, y));
                  }
                } else if (CharSequence.class.isAssignableFrom(cls)) {
                  String[] coordinates = value.toString().split(",");
                  x = Double.parseDouble(coordinates[0]);
                  y = Double.parseDouble(coordinates[1]);
                  if (matchingAll) {
                    rootNode = QueryBuilders.intersect(rootNode).add(fieldName, String.format("[%s %s 0.0001 mi]", x, y));
                  } else {
                    rootNode = QueryBuilders.union(rootNode).add(fieldName, String.format("[%s %s 0.0001 mi]", x, y));
                  }
                }
              }
              //
              // NUMERIC
              //
              else if (schemaField instanceof NumericField) {
                if (Iterable.class.isAssignableFrom(value.getClass())) {
                  Iterable<?> values = (Iterable<?>) value;
                  values = StreamSupport.stream(values.spliterator(), false) //
                      .filter(Objects::nonNull).collect(Collectors.toList());

                  if (values.iterator().hasNext()) {
                    Class<?> elementClass = values.iterator().next().getClass();
                    QueryNode and = QueryBuilders.intersect();
                    for (Object v : values) {
                      if (matchingAll) {
                        if (elementClass == LocalDate.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((LocalDate) v)));
                        } else if (elementClass == Date.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((Date) v)));
                        } else if (elementClass == LocalDateTime.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((LocalDateTime) v)));
                        } else if (elementClass == Instant.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((Instant) v)));
                        } else if (elementClass == Integer.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Integer.parseInt(v.toString()))));
                        } else if (elementClass == Long.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Long.parseLong(v.toString()))));
                        } else if (elementClass == Double.class) {
                          and.add(QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Double.parseDouble(v.toString()))));
                        }
                      } else {
                        if (elementClass == LocalDate.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((LocalDate) v)));
                        } else if (elementClass == Date.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((Date) v)));
                        } else if (elementClass == LocalDateTime.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((LocalDateTime) v)));
                        } else if (elementClass == Instant.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((Instant) v)));
                        } else if (elementClass == Integer.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, Values.eq(Integer.parseInt(v.toString()))));
                        } else if (elementClass == Long.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, Values.eq(Long.parseLong(v.toString()))));
                        } else if (elementClass == Double.class) {
                          and.add(QueryBuilders.union(rootNode).add(fieldName, Values.eq(Double.parseDouble(v.toString()))));
                        }
                      }
                    }
                    if (matchingAll) {
                      rootNode = QueryBuilders.intersect(rootNode, and);
                    } else {
                      rootNode = QueryBuilders.union(rootNode, and);
                    }
                  }
                } else {
                  if (matchingAll) {
                    if (cls == LocalDate.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((LocalDate) value));
                    } else if (cls == Date.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((Date) value));
                    } else if (cls == LocalDateTime.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((LocalDateTime) value));
                    } else if (cls == Instant.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, JedisValues.eq((Instant) value));
                    } else if (cls == Integer.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Integer.parseInt(value.toString())));
                    } else if (cls == Long.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Long.parseLong(value.toString())));
                    } else if (cls == Double.class) {
                      rootNode = QueryBuilders.intersect(rootNode).add(fieldName, Values.eq(Double.parseDouble(value.toString())));
                    }
                  } else {
                    if (cls == LocalDate.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((LocalDate) value));
                    } else if (cls == Date.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((Date) value));
                    } else if (cls == LocalDateTime.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((LocalDateTime) value));
                    } else if (cls == Instant.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, JedisValues.eq((Instant) value));
                    } else if (cls == Integer.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, Values.eq(Integer.parseInt(value.toString())));
                    } else if (cls == Long.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, Values.eq(Long.parseLong(value.toString())));
                    } else if (cls == Double.class) {
                      rootNode = QueryBuilders.union(rootNode).add(fieldName, Values.eq(Double.parseDouble(value.toString())));
                    }
                  }
                }
              }
              //
              // VECTOR
              //
              else if (schemaField instanceof VectorField) {
                //TODO: pending - whether to support Vector fields in QBE
              }

          }
        }
      }
    }

    return rootNode;
  }

}
