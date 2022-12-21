package com.redis.om.spring.tuple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.tuple.accessor.EighteenthAccessor;
import com.redis.om.spring.tuple.accessor.EighthAccessor;
import com.redis.om.spring.tuple.accessor.EleventhAccessor;
import com.redis.om.spring.tuple.accessor.FifteenthAccessor;
import com.redis.om.spring.tuple.accessor.FifthAccessor;
import com.redis.om.spring.tuple.accessor.FirstAccessor;
import com.redis.om.spring.tuple.accessor.FourteenthAccessor;
import com.redis.om.spring.tuple.accessor.FourthAccessor;
import com.redis.om.spring.tuple.accessor.NineteenthAccessor;
import com.redis.om.spring.tuple.accessor.NinthAccessor;
import com.redis.om.spring.tuple.accessor.SecondAccessor;
import com.redis.om.spring.tuple.accessor.SeventeenthAccessor;
import com.redis.om.spring.tuple.accessor.SeventhAccessor;
import com.redis.om.spring.tuple.accessor.SixteenthAccessor;
import com.redis.om.spring.tuple.accessor.SixthAccessor;
import com.redis.om.spring.tuple.accessor.TenthAccessor;
import com.redis.om.spring.tuple.accessor.ThirdAccessor;
import com.redis.om.spring.tuple.accessor.ThirteenthAccessor;
import com.redis.om.spring.tuple.accessor.TwelfthAccessor;
import com.redis.om.spring.tuple.accessor.TwentiethAccessor;

final class TupleTest {

  @Test
  void single() {
    final Single<Integer> tuple = Tuples.of(0);
    tupleTest(tuple);
    final Single<Integer> defaultTuple = new Single<Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void pair() {
    final Pair<Integer, Integer> tuple = Tuples.of(0, 1);
    tupleTest(tuple);
    final Pair<Integer, Integer> defaultTuple = new Pair<Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void triple() {
    final Triple<Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2);
    tupleTest(tuple);
    final Triple<Integer, Integer, Integer> defaultTuple = new Triple<Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void quad() {
    final Quad<Integer, Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2, 3);
    tupleTest(tuple);
    final Quad<Integer, Integer, Integer, Integer> defaultTuple = new Quad<Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void quintuple() {
    final Quintuple<Integer, Integer, Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2, 3, 4);
    tupleTest(tuple);
    final Quintuple<Integer, Integer, Integer, Integer, Integer> defaultTuple = new Quintuple<Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void hextuple() {
    final Hextuple<Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2, 3, 4, 5);
    tupleTest(tuple);
    final Hextuple<Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void septuple() {
    final Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2, 3, 4, 5,
        6);
    tupleTest(tuple);
    final Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void octuple() {
    final Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples.of(0, 1, 2, 3,
        4, 5, 6, 7);
    tupleTest(tuple);
    final Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void nonuple() {
    final Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples.of(0,
        1, 2, 3, 4, 5, 6, 7, 8);
    tupleTest(tuple);
    final Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void decuple() {
    final Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    tupleTest(tuple);
    final Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void undecuple() {
    final Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    tupleTest(tuple);
    final Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void duodecuple() {
    final Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    tupleTest(tuple);
    final Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void tredecuple() {
    final Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    tupleTest(tuple);
    final Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void quattuordecuple() {
    final Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);
    tupleTest(tuple);
    final Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void quindecuple() {
    final Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
    tupleTest(tuple);
    final Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void sexdecuple() {
    final Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    tupleTest(tuple);
    final Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }

      @Override
      public Integer getSixteenth() {
        return 15;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void septendecuple() {
    final Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    tupleTest(tuple);
    final Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }

      @Override
      public Integer getSixteenth() {
        return 15;
      }

      @Override
      public Integer getSeventeenth() {
        return 16;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void octodecuple() {
    final Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
    tupleTest(tuple);
    final Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }

      @Override
      public Integer getSixteenth() {
        return 15;
      }

      @Override
      public Integer getSeventeenth() {
        return 16;
      }

      @Override
      public Integer getEighteenth() {
        return 17;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void novemdecuple() {
    final Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18);
    tupleTest(tuple);
    final Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }

      @Override
      public Integer getSixteenth() {
        return 15;
      }

      @Override
      public Integer getSeventeenth() {
        return 16;
      }

      @Override
      public Integer getEighteenth() {
        return 17;
      }

      @Override
      public Integer getNineteenth() {
        return 18;
      }
    };
    tupleTest(defaultTuple);
  }

  @Test
  void vigintuple() {
    final Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple = Tuples
        .of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    tupleTest(tuple);
    final Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> defaultTuple = new Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
      @Override
      public Integer getFirst() {
        return 0;
      }

      @Override
      public Integer getSecond() {
        return 1;
      }

      @Override
      public Integer getThird() {
        return 2;
      }

      @Override
      public Integer getFourth() {
        return 3;
      }

      @Override
      public Integer getFifth() {
        return 4;
      }

      @Override
      public Integer getSixth() {
        return 5;
      }

      @Override
      public Integer getSeventh() {
        return 6;
      }

      @Override
      public Integer getEighth() {
        return 7;
      }

      @Override
      public Integer getNinth() {
        return 8;
      }

      @Override
      public Integer getTenth() {
        return 9;
      }

      @Override
      public Integer getEleventh() {
        return 10;
      }

      @Override
      public Integer getTwelfth() {
        return 11;
      }

      @Override
      public Integer getThirteenth() {
        return 12;
      }

      @Override
      public Integer getFourteenth() {
        return 13;
      }

      @Override
      public Integer getFifteenth() {
        return 14;
      }

      @Override
      public Integer getSixteenth() {
        return 15;
      }

      @Override
      public Integer getSeventeenth() {
        return 16;
      }

      @Override
      public Integer getEighteenth() {
        return 17;
      }

      @Override
      public Integer getNineteenth() {
        return 18;
      }

      @Override
      public Integer getTwentieth() {
        return 19;
      }
    };
    tupleTest(defaultTuple);
  }

  private void tupleTest(final Single<Integer> tuple) {
    final FirstAccessor<Single<Integer>, Integer> getter0 = Single.getFirstGetter();
    assertEquals(0, getter0.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(1));
  }

  private void tupleTest(final Pair<Integer, Integer> tuple) {
    final FirstAccessor<Pair<Integer, Integer>, Integer> getter0 = Pair.getFirstGetter();
    final SecondAccessor<Pair<Integer, Integer>, Integer> getter1 = Pair.getSecondGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(2));
  }

  private void tupleTest(final Triple<Integer, Integer, Integer> tuple) {
    final FirstAccessor<Triple<Integer, Integer, Integer>, Integer> getter0 = Triple.getFirstGetter();
    final SecondAccessor<Triple<Integer, Integer, Integer>, Integer> getter1 = Triple.getSecondGetter();
    final ThirdAccessor<Triple<Integer, Integer, Integer>, Integer> getter2 = Triple.getThirdGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(3));
  }

  private void tupleTest(final Quad<Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Quad<Integer, Integer, Integer, Integer>, Integer> getter0 = Quad.getFirstGetter();
    final SecondAccessor<Quad<Integer, Integer, Integer, Integer>, Integer> getter1 = Quad.getSecondGetter();
    final ThirdAccessor<Quad<Integer, Integer, Integer, Integer>, Integer> getter2 = Quad.getThirdGetter();
    final FourthAccessor<Quad<Integer, Integer, Integer, Integer>, Integer> getter3 = Quad.getFourthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(4));
  }

  private void tupleTest(final Quintuple<Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Quintuple<Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Quintuple
        .getFirstGetter();
    final SecondAccessor<Quintuple<Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Quintuple
        .getSecondGetter();
    final ThirdAccessor<Quintuple<Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Quintuple
        .getThirdGetter();
    final FourthAccessor<Quintuple<Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Quintuple
        .getFourthGetter();
    final FifthAccessor<Quintuple<Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Quintuple
        .getFifthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(5));
  }

  private void tupleTest(final Hextuple<Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Hextuple
        .getFirstGetter();
    final SecondAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Hextuple
        .getSecondGetter();
    final ThirdAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Hextuple
        .getThirdGetter();
    final FourthAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Hextuple
        .getFourthGetter();
    final FifthAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Hextuple
        .getFifthGetter();
    final SixthAccessor<Hextuple<Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Hextuple
        .getSixthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(6));
  }

  private void tupleTest(final Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Septuple
        .getFirstGetter();
    final SecondAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Septuple
        .getSecondGetter();
    final ThirdAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Septuple
        .getThirdGetter();
    final FourthAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Septuple
        .getFourthGetter();
    final FifthAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Septuple
        .getFifthGetter();
    final SixthAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Septuple
        .getSixthGetter();
    final SeventhAccessor<Septuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Septuple
        .getSeventhGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(7));
  }

  private void tupleTest(final Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Octuple
        .getFirstGetter();
    final SecondAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Octuple
        .getSecondGetter();
    final ThirdAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Octuple
        .getThirdGetter();
    final FourthAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Octuple
        .getFourthGetter();
    final FifthAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Octuple
        .getFifthGetter();
    final SixthAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Octuple
        .getSixthGetter();
    final SeventhAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Octuple
        .getSeventhGetter();
    final EighthAccessor<Octuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Octuple
        .getEighthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(8));
  }

  private void tupleTest(
      final Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Nonuple
        .getFirstGetter();
    final SecondAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Nonuple
        .getSecondGetter();
    final ThirdAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Nonuple
        .getThirdGetter();
    final FourthAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Nonuple
        .getFourthGetter();
    final FifthAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Nonuple
        .getFifthGetter();
    final SixthAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Nonuple
        .getSixthGetter();
    final SeventhAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Nonuple
        .getSeventhGetter();
    final EighthAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Nonuple
        .getEighthGetter();
    final NinthAccessor<Nonuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Nonuple
        .getNinthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(9));
  }

  private void tupleTest(
      final Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Decuple
        .getFirstGetter();
    final SecondAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Decuple
        .getSecondGetter();
    final ThirdAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Decuple
        .getThirdGetter();
    final FourthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Decuple
        .getFourthGetter();
    final FifthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Decuple
        .getFifthGetter();
    final SixthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Decuple
        .getSixthGetter();
    final SeventhAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Decuple
        .getSeventhGetter();
    final EighthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Decuple
        .getEighthGetter();
    final NinthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Decuple
        .getNinthGetter();
    final TenthAccessor<Decuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Decuple
        .getTenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(10));
  }

  private void tupleTest(
      final Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Undecuple
        .getFirstGetter();
    final SecondAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Undecuple
        .getSecondGetter();
    final ThirdAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Undecuple
        .getThirdGetter();
    final FourthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Undecuple
        .getFourthGetter();
    final FifthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Undecuple
        .getFifthGetter();
    final SixthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Undecuple
        .getSixthGetter();
    final SeventhAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Undecuple
        .getSeventhGetter();
    final EighthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Undecuple
        .getEighthGetter();
    final NinthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Undecuple
        .getNinthGetter();
    final TenthAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Undecuple
        .getTenthGetter();
    final EleventhAccessor<Undecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Undecuple
        .getEleventhGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(11));
  }

  private void tupleTest(
      final Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Duodecuple
        .getFirstGetter();
    final SecondAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Duodecuple
        .getSecondGetter();
    final ThirdAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Duodecuple
        .getThirdGetter();
    final FourthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Duodecuple
        .getFourthGetter();
    final FifthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Duodecuple
        .getFifthGetter();
    final SixthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Duodecuple
        .getSixthGetter();
    final SeventhAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Duodecuple
        .getSeventhGetter();
    final EighthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Duodecuple
        .getEighthGetter();
    final NinthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Duodecuple
        .getNinthGetter();
    final TenthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Duodecuple
        .getTenthGetter();
    final EleventhAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Duodecuple
        .getEleventhGetter();
    final TwelfthAccessor<Duodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Duodecuple
        .getTwelfthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(12));
  }

  private void tupleTest(
      final Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Tredecuple
        .getFirstGetter();
    final SecondAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Tredecuple
        .getSecondGetter();
    final ThirdAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Tredecuple
        .getThirdGetter();
    final FourthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Tredecuple
        .getFourthGetter();
    final FifthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Tredecuple
        .getFifthGetter();
    final SixthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Tredecuple
        .getSixthGetter();
    final SeventhAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Tredecuple
        .getSeventhGetter();
    final EighthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Tredecuple
        .getEighthGetter();
    final NinthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Tredecuple
        .getNinthGetter();
    final TenthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Tredecuple
        .getTenthGetter();
    final EleventhAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Tredecuple
        .getEleventhGetter();
    final TwelfthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Tredecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Tredecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Tredecuple
        .getThirteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(13));
  }

  private void tupleTest(
      final Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Quattuordecuple
        .getFirstGetter();
    final SecondAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Quattuordecuple
        .getSecondGetter();
    final ThirdAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Quattuordecuple
        .getThirdGetter();
    final FourthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Quattuordecuple
        .getFourthGetter();
    final FifthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Quattuordecuple
        .getFifthGetter();
    final SixthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Quattuordecuple
        .getSixthGetter();
    final SeventhAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Quattuordecuple
        .getSeventhGetter();
    final EighthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Quattuordecuple
        .getEighthGetter();
    final NinthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Quattuordecuple
        .getNinthGetter();
    final TenthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Quattuordecuple
        .getTenthGetter();
    final EleventhAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Quattuordecuple
        .getEleventhGetter();
    final TwelfthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Quattuordecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Quattuordecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Quattuordecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Quattuordecuple
        .getFourteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(14));
  }

  private void tupleTest(
      final Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Quindecuple
        .getFirstGetter();
    final SecondAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Quindecuple
        .getSecondGetter();
    final ThirdAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Quindecuple
        .getThirdGetter();
    final FourthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Quindecuple
        .getFourthGetter();
    final FifthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Quindecuple
        .getFifthGetter();
    final SixthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Quindecuple
        .getSixthGetter();
    final SeventhAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Quindecuple
        .getSeventhGetter();
    final EighthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Quindecuple
        .getEighthGetter();
    final NinthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Quindecuple
        .getNinthGetter();
    final TenthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Quindecuple
        .getTenthGetter();
    final EleventhAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Quindecuple
        .getEleventhGetter();
    final TwelfthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Quindecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Quindecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Quindecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Quindecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Quindecuple
        .getFifteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(15));
  }

  private void tupleTest(
      final Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Sexdecuple
        .getFirstGetter();
    final SecondAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Sexdecuple
        .getSecondGetter();
    final ThirdAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Sexdecuple
        .getThirdGetter();
    final FourthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Sexdecuple
        .getFourthGetter();
    final FifthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Sexdecuple
        .getFifthGetter();
    final SixthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Sexdecuple
        .getSixthGetter();
    final SeventhAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Sexdecuple
        .getSeventhGetter();
    final EighthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Sexdecuple
        .getEighthGetter();
    final NinthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Sexdecuple
        .getNinthGetter();
    final TenthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Sexdecuple
        .getTenthGetter();
    final EleventhAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Sexdecuple
        .getEleventhGetter();
    final TwelfthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Sexdecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Sexdecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Sexdecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Sexdecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Sexdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter15 = Sexdecuple
        .getSixteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(15, getter15.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(15, getter15.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertEquals(15, tuple.get(15));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(16));
  }

  private void tupleTest(
      final Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Septendecuple
        .getFirstGetter();
    final SecondAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Septendecuple
        .getSecondGetter();
    final ThirdAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Septendecuple
        .getThirdGetter();
    final FourthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Septendecuple
        .getFourthGetter();
    final FifthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Septendecuple
        .getFifthGetter();
    final SixthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Septendecuple
        .getSixthGetter();
    final SeventhAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Septendecuple
        .getSeventhGetter();
    final EighthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Septendecuple
        .getEighthGetter();
    final NinthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Septendecuple
        .getNinthGetter();
    final TenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Septendecuple
        .getTenthGetter();
    final EleventhAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Septendecuple
        .getEleventhGetter();
    final TwelfthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Septendecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Septendecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Septendecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Septendecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter15 = Septendecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Septendecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter16 = Septendecuple
        .getSeventeenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(15, getter15.index());
    assertEquals(16, getter16.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(15, getter15.apply(tuple));
    assertEquals(16, getter16.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertEquals(15, tuple.get(15));
    assertEquals(16, tuple.get(16));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(17));
  }

  private void tupleTest(
      final Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Octodecuple
        .getFirstGetter();
    final SecondAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Octodecuple
        .getSecondGetter();
    final ThirdAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Octodecuple
        .getThirdGetter();
    final FourthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Octodecuple
        .getFourthGetter();
    final FifthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Octodecuple
        .getFifthGetter();
    final SixthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Octodecuple
        .getSixthGetter();
    final SeventhAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Octodecuple
        .getSeventhGetter();
    final EighthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Octodecuple
        .getEighthGetter();
    final NinthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Octodecuple
        .getNinthGetter();
    final TenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Octodecuple
        .getTenthGetter();
    final EleventhAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Octodecuple
        .getEleventhGetter();
    final TwelfthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Octodecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Octodecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Octodecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Octodecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter15 = Octodecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter16 = Octodecuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Octodecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter17 = Octodecuple
        .getEighteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(15, getter15.index());
    assertEquals(16, getter16.index());
    assertEquals(17, getter17.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(15, getter15.apply(tuple));
    assertEquals(16, getter16.apply(tuple));
    assertEquals(17, getter17.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertEquals(15, tuple.get(15));
    assertEquals(16, tuple.get(16));
    assertEquals(17, tuple.get(17));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(18));
  }

  private void tupleTest(
      final Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Novemdecuple
        .getFirstGetter();
    final SecondAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Novemdecuple
        .getSecondGetter();
    final ThirdAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Novemdecuple
        .getThirdGetter();
    final FourthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Novemdecuple
        .getFourthGetter();
    final FifthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Novemdecuple
        .getFifthGetter();
    final SixthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Novemdecuple
        .getSixthGetter();
    final SeventhAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Novemdecuple
        .getSeventhGetter();
    final EighthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Novemdecuple
        .getEighthGetter();
    final NinthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Novemdecuple
        .getNinthGetter();
    final TenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Novemdecuple
        .getTenthGetter();
    final EleventhAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Novemdecuple
        .getEleventhGetter();
    final TwelfthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Novemdecuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Novemdecuple
        .getThirteenthGetter();
    final FourteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Novemdecuple
        .getFourteenthGetter();
    final FifteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Novemdecuple
        .getFifteenthGetter();
    final SixteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter15 = Novemdecuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter16 = Novemdecuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter17 = Novemdecuple
        .getEighteenthGetter();
    final NineteenthAccessor<Novemdecuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter18 = Novemdecuple
        .getNineteenthGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(15, getter15.index());
    assertEquals(16, getter16.index());
    assertEquals(17, getter17.index());
    assertEquals(18, getter18.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(15, getter15.apply(tuple));
    assertEquals(16, getter16.apply(tuple));
    assertEquals(17, getter17.apply(tuple));
    assertEquals(18, getter18.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertEquals(15, tuple.get(15));
    assertEquals(16, tuple.get(16));
    assertEquals(17, tuple.get(17));
    assertEquals(18, tuple.get(18));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(19));
  }

  private void tupleTest(
      final Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> tuple) {
    final FirstAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter0 = Vigintuple
        .getFirstGetter();
    final SecondAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter1 = Vigintuple
        .getSecondGetter();
    final ThirdAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter2 = Vigintuple
        .getThirdGetter();
    final FourthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter3 = Vigintuple
        .getFourthGetter();
    final FifthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter4 = Vigintuple
        .getFifthGetter();
    final SixthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter5 = Vigintuple
        .getSixthGetter();
    final SeventhAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter6 = Vigintuple
        .getSeventhGetter();
    final EighthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter7 = Vigintuple
        .getEighthGetter();
    final NinthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter8 = Vigintuple
        .getNinthGetter();
    final TenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter9 = Vigintuple
        .getTenthGetter();
    final EleventhAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter10 = Vigintuple
        .getEleventhGetter();
    final TwelfthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter11 = Vigintuple
        .getTwelfthGetter();
    final ThirteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter12 = Vigintuple
        .getThirteenthGetter();
    final FourteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter13 = Vigintuple
        .getFourteenthGetter();
    final FifteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter14 = Vigintuple
        .getFifteenthGetter();
    final SixteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter15 = Vigintuple
        .getSixteenthGetter();
    final SeventeenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter16 = Vigintuple
        .getSeventeenthGetter();
    final EighteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter17 = Vigintuple
        .getEighteenthGetter();
    final NineteenthAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter18 = Vigintuple
        .getNineteenthGetter();
    final TwentiethAccessor<Vigintuple<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>, Integer> getter19 = Vigintuple
        .getTwentiethGetter();
    assertEquals(0, getter0.index());
    assertEquals(1, getter1.index());
    assertEquals(2, getter2.index());
    assertEquals(3, getter3.index());
    assertEquals(4, getter4.index());
    assertEquals(5, getter5.index());
    assertEquals(6, getter6.index());
    assertEquals(7, getter7.index());
    assertEquals(8, getter8.index());
    assertEquals(9, getter9.index());
    assertEquals(10, getter10.index());
    assertEquals(11, getter11.index());
    assertEquals(12, getter12.index());
    assertEquals(13, getter13.index());
    assertEquals(14, getter14.index());
    assertEquals(15, getter15.index());
    assertEquals(16, getter16.index());
    assertEquals(17, getter17.index());
    assertEquals(18, getter18.index());
    assertEquals(19, getter19.index());
    assertEquals(0, getter0.apply(tuple));
    assertEquals(1, getter1.apply(tuple));
    assertEquals(2, getter2.apply(tuple));
    assertEquals(3, getter3.apply(tuple));
    assertEquals(4, getter4.apply(tuple));
    assertEquals(5, getter5.apply(tuple));
    assertEquals(6, getter6.apply(tuple));
    assertEquals(7, getter7.apply(tuple));
    assertEquals(8, getter8.apply(tuple));
    assertEquals(9, getter9.apply(tuple));
    assertEquals(10, getter10.apply(tuple));
    assertEquals(11, getter11.apply(tuple));
    assertEquals(12, getter12.apply(tuple));
    assertEquals(13, getter13.apply(tuple));
    assertEquals(14, getter14.apply(tuple));
    assertEquals(15, getter15.apply(tuple));
    assertEquals(16, getter16.apply(tuple));
    assertEquals(17, getter17.apply(tuple));
    assertEquals(18, getter18.apply(tuple));
    assertEquals(19, getter19.apply(tuple));
    assertEquals(0, tuple.get(0));
    assertEquals(1, tuple.get(1));
    assertEquals(2, tuple.get(2));
    assertEquals(3, tuple.get(3));
    assertEquals(4, tuple.get(4));
    assertEquals(5, tuple.get(5));
    assertEquals(6, tuple.get(6));
    assertEquals(7, tuple.get(7));
    assertEquals(8, tuple.get(8));
    assertEquals(9, tuple.get(9));
    assertEquals(10, tuple.get(10));
    assertEquals(11, tuple.get(11));
    assertEquals(12, tuple.get(12));
    assertEquals(13, tuple.get(13));
    assertEquals(14, tuple.get(14));
    assertEquals(15, tuple.get(15));
    assertEquals(16, tuple.get(16));
    assertEquals(17, tuple.get(17));
    assertEquals(18, tuple.get(18));
    assertEquals(19, tuple.get(19));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> tuple.get(20));
  }

  //
  //
  //

  @Test
  void testTuplesStream() {
    final Vigintuple<Integer, Integer, String, Long, BigInteger, String, Integer, BigInteger, Integer, String, Long, Integer, String, Integer, String, Integer, String, Integer, String, Integer> vigintuple = Tuples
        .of(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L, 1, "grault", 2, "garply",
            3, "waldo", 4, "fred", 5);
    List<Object> parts = vigintuple.stream().collect(Collectors.toList());

    assertThat(parts).containsExactly(0, 1, "Foobar", 42L, BigInteger.ONE, "qux", 20, BigInteger.TEN, 22, "corge", 1L,
        1, "grault", 2, "garply", 3, "waldo", 4, "fred", 5);
  }

  @Test
  void testTuplesStreamOf() {
    final Triple<Integer, Integer, Integer> triple = Tuples.of(0, 1, 42);
    List<Integer> parts = triple.streamOf(Integer.class).collect(Collectors.toList());

    assertThat(parts).containsExactly(0, 1, 42);
  }

  @Test
  void testTuplesEqualsAndHashcode() {
    final Triple<Integer, Integer, Integer> oneTriple = Tuples.of(0, 1, 42);
    final Triple<Integer, Integer, Integer> anotherTriple = Tuples.of(0, 1, 42);

    assertAll( //
        () -> assertThat(oneTriple).hasSameHashCodeAs(anotherTriple), //
        () -> assertThat(oneTriple).isEqualTo(anotherTriple) //
    );
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  void testTuplesEqualsWithBasicTuple() {
    final Triple<Integer, Integer, Integer> oneTriple = Tuples.of(0, 1, 42);
    assertThat(oneTriple.equals(null)).isFalse();
    assertThat(oneTriple.equals(oneTriple)).isTrue();
    assertThat(oneTriple.equals("notATuple")).isFalse();
  }

  @Test
  void testTupleToString() {
    final Triple<Integer, Integer, String> triple = Tuples.of(0, 1, "Foobar");
    assertThat(triple).hasToString("TripleImpl (0, 1, Foobar)");
  }

  @Test
  void testEntityWithoutIdThrowsException() {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () -> {
      Tuples.of(0, 1, null);
    });

    String expectedErrorMessage = "com.redis.om.spring.tuple.impl.TripleImpl cannot hold null values.";
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void testTupleLabels() {
    final Pair<String, Integer> pair = Tuples.of(new String[] { "lastName", "number" }, "Jordan", 23);
    Map<String, Object> labelledMap = pair.labelledMap();

    assertThat(labelledMap) //
        .containsEntry("lastName", "Jordan") //
        .containsEntry("number", 23);
  }

}
