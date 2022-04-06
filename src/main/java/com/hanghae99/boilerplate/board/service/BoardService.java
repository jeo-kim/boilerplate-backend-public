package com.hanghae99.boilerplate.board.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanghae99.boilerplate.board.dto.*;
import com.hanghae99.boilerplate.security.model.MemberContext;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface BoardService {
    List<BoardResponseDto> showTop5(MemberContext user);

    void createBoard(BoardRequestDto boardRequestDto, MemberContext user);

    List<BoardResponseDto> showAllBoard(Pageable pageable, MemberContext user);

    BoardResponseDto showBoard(Long boardId, MemberContext user);

    List<BoardResponseDto> showBoardByCategory(String categoryName, Pageable pageable, MemberContext user);

    void deleteBoard(Long boardId);

    ClickedDto recommendBoard(Long boardId, MemberContext user);

    ClickedDto agreeBoard(Long boardId, MemberContext user);

    ClickedDto disagreeBoard(Long boardId, MemberContext user);


    CommentResponseDto createComment(CommentRequestDto commentRequestDto, MemberContext user) throws ExecutionException, InterruptedException, JsonProcessingException;


    List<CommentResponseDto> showComments(Long boardId);

    void deleteComment(Long commentId);

    ClickedDto recommendComment(Long commentId, MemberContext user);

    ReplyResponseDto createReply(Long commentId ,ReplyRequestDto replyRequestDto, MemberContext user) throws ExecutionException, InterruptedException, JsonProcessingException;

    List<ReplyResponseDto> showReplies(Long commentId);

    List<BoardResponseDto> getMyBoard(MemberContext user);
    void setMyBoard(Long boardId, MemberContext user);

    List<BoardResponseDto> searchBoard(String content, Pageable pageable, MemberContext user);

    List<BoardResponseDto> getMyComment(MemberContext user);

    List<BoardResponseDto> getMyWrittenBoard(MemberContext user);

    ClickedDto recommendReply(Long replyId, MemberContext user);

    List<BoardResponseDto> findTopVotedBoards(Pageable pageable, MemberContext user);
}
