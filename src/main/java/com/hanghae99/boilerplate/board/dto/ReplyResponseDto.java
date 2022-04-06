package com.hanghae99.boilerplate.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponseDto {


    private Long replyId;
    private Long commentId;
    private Long memberId;
    private String content;

    private LocalDateTime createdAt;
    private int recommendCount;
    private String nickname;
    private String profileImageUrl;
}
