package com.ptjcoding.nbcampspringnewsfeed.domain.bookmark.model;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Bookmark {

  private Long bookmarkId;
  private Long memberId;
  private Long postId;
  private LocalDateTime markedDate;

  public static Bookmark of(Long memberId, Long postId) {
    return Bookmark
        .builder()
        .memberId(memberId)
        .postId(postId)
        .build();
  }
}