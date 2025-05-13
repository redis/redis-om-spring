package com.redis.om.hashes.domain;

import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor
@RedisHash
public class User {
  @NonNull
  @Indexed
  @Bloom(
      name = "bf_user_email", capacity = 100000, errorRate = 0.001
  )
  String email;
  @Id
  private String id;
  @Indexed
  @NonNull
  private String firstName;
  @Indexed
  private String middleName;
  @Indexed
  @NonNull
  private String lastName;
  @NonNull
  @Reference
  private Role role;

  // audit fields

  @CreatedDate
  private Date createdDate;

  @LastModifiedDate
  private Date lastModifiedDate;
}
