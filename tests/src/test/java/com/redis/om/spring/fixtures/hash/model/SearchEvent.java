package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.CountMin;
import com.redis.om.spring.tuple.Pair;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("searchEvent")
public class SearchEvent {

  @Id
  String id;

  @NonNull
  @CountMin(name = "cms_user_id_count", initMode = CountMin.InitMode.DIMENSIONS, width = 1000, depth = 10)
  String userId;

  @NonNull
  @CountMin
  String searchSentence;

  @NonNull
  @CountMin(errorRate = 0.001, probability = 0.999)
  List<String> hotTerms;

  @NonNull
  @CountMin(name = "cms_search_words")
  List<Pair<String, Long>> searchWord;
}