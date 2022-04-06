package com.hanghae99.boilerplate.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long commentId;
    private Long memeberId;
    private String content;

    private LocalDateTime createdAt;
    private int recommendCount;

    private String nickname;
    private String profileImageUrl;

    private List<ReplyResponseDto> replyResponseDtoList;

}
