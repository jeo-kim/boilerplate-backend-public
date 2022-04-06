package com.hanghae99.boilerplate.board.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hanghae99.boilerplate.board.domain.*;
import com.hanghae99.boilerplate.board.dto.*;
import com.hanghae99.boilerplate.memberManager.model.Member;
import com.hanghae99.boilerplate.memberManager.repository.MemberRepository;
import com.hanghae99.boilerplate.noti.service.FCMService;
import com.hanghae99.boilerplate.security.model.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final RecommendBoardRepository recommendBoardRepository;
    private final VoteRepository voteRepository;
    private final RecommendCommentRepository recommendCommentRepository;
    private final MemberRepository memberRepository;
    private final MyBoardRepository myBoardRepository;
    private final ReplyRepository replyRepository;

    private final FCMService fcmService;
    private final BoardSearchRepository boardSearchRepository;

    private final RecommendReplyRepository recommendReplyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponseDto> showTop5(MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        List<Board> boards = boardRepository.findTop5ByOrderByAgreeCountAndDisagreeCountDesc(); // findAll();
        return boards.stream()
                .map(board -> {
                    BoardResponseDto boardResponseDto = board.toCreatedDto();
                    Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");

                        }
                    }
                    return boardResponseDto;
                })
                //.map(Board::toCreatedDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
//    @CacheEvict(cacheNames = "board", allEntries = true) // , key = "#username")
    @CacheEvict(cacheNames = {"board", "board_top_voted"}, allEntries = true)
    public void createBoard(BoardRequestDto boardRequestDto, MemberContext user){
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        Board board = boardRepository.save(boardRequestDto.toEntity(member.get()));
    }


    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "board")
    public List<BoardResponseDto> showAllBoard(Pageable pageable, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        List<Board> boards = boardRepository.findAllByIsDeletedIsFalseOrderByCreatedAtDesc(pageable); // findAll();
        return boards.stream()
                .map(board -> {
                    BoardResponseDto boardResponseDto = board.toCreatedDto();
                    Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");

                        }
                    }
                    return boardResponseDto;
                })
                //.map(Board::toCreatedDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    //@Cacheable(cacheNames = "board", key = "#boardId")
    public BoardResponseDto showBoard(Long boardId, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        Board board = boardRepository.findById(boardId).get();
        //return board.toCreatedDto();
        BoardResponseDto boardResponseDto = board.toCreatedDto();
        Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
        if (vote.isEmpty()){
            boardResponseDto.setUserStatus("없다");
        } else {
            if (vote.get().getAgreed()){
                boardResponseDto.setUserStatus("찬성");
            }else{
                boardResponseDto.setUserStatus("반대");

            }
        }
        return boardResponseDto;

    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "board", key = "#categoryName") // + '::' + #pageNo")
    public List<BoardResponseDto> showBoardByCategory(String categoryName, Pageable pageable, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        List<Board> boards = boardRepository.findAllByCategoryAndIsDeletedIsFalse(categoryName); // findAll();
        return boards.stream()
                //.map(Board::toCreatedDto)
                .map(board -> {
                    BoardResponseDto boardResponseDto = board.toCreatedDto();
                    Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");

                        }
                    }
                    return boardResponseDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
//    @CacheEvict(cacheNames = "board", allEntries = true)
    @CacheEvict(cacheNames = {"board", "board_top_voted"}, allEntries = true)
    public void deleteBoard(Long boardId) {
        //boardRepository.deleteById(boardId);
        Board board = boardRepository.findById(boardId).get();
        board.setIsDeleted(Boolean.TRUE);
        board.setUpdatedAt(LocalDateTime.now());

    }

    @Override
    @Transactional
    public ClickedDto recommendBoard(Long boardId, MemberContext user) {
        Optional<Board> board = boardRepository.findById(boardId);
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        Optional<RecommendBoard> recommendBoard = recommendBoardRepository.findByBoardAndMember(board.get(), member.get());

        if (member.get().getRecommendBoards().isEmpty()){
            RecommendBoard recommendBoard1 =  RecommendBoard.builder()
                    .member(member.get())
                    .board(board.get())
                    .build();

            recommendBoardRepository.save(recommendBoard1);
            board.get().addRecommendCount();
            return ClickedDto.builder().clicked(Boolean.TRUE).build();

        }else{
            recommendBoardRepository.deleteById(recommendBoard.get().getId());
            board.get().subtractRecommendCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "board_top_voted", allEntries = true)
    public ClickedDto agreeBoard(Long boardId, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        Optional<Board> board = boardRepository.findById(boardId);
        Optional<Vote> vote = voteRepository.findByBoardAndMember(board.get(), member.get());

        if (vote.isEmpty()){
            Vote vote1 =  Vote.builder()
                    .member(member.get())
                    .board(board.get())
                    .agreed(Boolean.TRUE)
                    .build();
            voteRepository.save(vote1);
            board.get().addAgreeCount();
            return ClickedDto.builder().clicked(Boolean.TRUE).build();
        }else if (vote.get().getAgreed()){
            voteRepository.deleteById(vote.get().getId());
            board.get().subtractAgreeCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }else{
            vote.get().setAgreed(Boolean.TRUE);
            board.get().addAgreeCount();
            board.get().subtractDisagreeCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "board_top_voted", allEntries = true)
    public ClickedDto disagreeBoard(Long boardId, MemberContext user) {
        Optional<Board> board = boardRepository.findById(boardId);
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        Optional<Vote> vote = voteRepository.findByBoardAndMember(board.get(), member.get());

        if (vote.isEmpty()){
            Vote vote1 =  Vote.builder()
                    .member(member.get())
                    .board(board.get())
                    .agreed(Boolean.FALSE)
                    .build();
            voteRepository.save(vote1);
            board.get().addDisagreeCount();
            return ClickedDto.builder().clicked(Boolean.TRUE).build();

        }else if (vote.get().getAgreed()){
            vote.get().setAgreed(Boolean.FALSE);
            board.get().addDisagreeCount();
            board.get().subtractAgreeCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }else{
            voteRepository.deleteById(vote.get().getId());
            board.get().subtractDisagreeCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }

    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "board_top_voted", allEntries = true)
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, MemberContext user) throws ExecutionException, InterruptedException, JsonProcessingException {


        Board board = boardRepository.findById(commentRequestDto.getBoardId()).orElse(new Board());
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        Comment comment = Comment.builder()
                .board(board)
                .member(member.get())
                .content(commentRequestDto.getContent())
                .createdAt(LocalDateTime.now()).build();

        commentRepository.save(comment);
        System.out.println(comment + "저장");
        fcmService.sendMessageTo(board.getMember().getId(), board.getTitle(), "새 댓글이 달렸습니다");
        return comment.toDto();

        //
        //push 댓글 -> 게시글 작성자
        //
        //return null;
        //return CommentResponseDto.builder().content("test").build();//comment.toDto();

    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> showComments(Long boardId){



        //Optional<Board> board = boardRepository.findById(boardId);
        List<Board> board = boardRepository.findByIdJoinFetch(boardId);
        //System.out.println(board.get().getContent() + "불러오기");
        //System.out.println(board.get().getComments().get(0).getContent() + "불러오기");

        //N + 1 !!!
        return board.get(0).getComments().stream().map(Comment::toDto).collect(Collectors.toList());

        //commentRepository.findAllByBoardIdOrderByCreatedAtDesc(boardId).stream().map(Comment::toDto).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "board_top_voted", allEntries = true)
    public void deleteComment(Long commentId){

        commentRepository.deleteById(commentId);
    }



    @Override
    @Transactional
    public ClickedDto recommendComment(Long commentId, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        Optional<Comment> comment = commentRepository.findById(commentId);
        Optional<RecommendComment> recommendComment = recommendCommentRepository.findByCommentAndMember(comment.get(), member.get());

        if (recommendComment.isEmpty()){
            RecommendComment recommendComment1 =  RecommendComment.builder()
                    .member(member.get())
                    .comment(comment.get())
                    .build();

            recommendCommentRepository.save(recommendComment1);
            comment.get().addRecommendCount();
            return ClickedDto.builder().clicked(Boolean.TRUE).build();

        }else{
            recommendCommentRepository.deleteById(recommendComment.get().getId());
            comment.get().subtractRecommendCount();
            return ClickedDto.builder().clicked(Boolean.FALSE).build();

        }
    }

    @Override
    @Transactional
    public ReplyResponseDto createReply(Long commentId, ReplyRequestDto replyRequestDto, MemberContext user) throws ExecutionException, InterruptedException, JsonProcessingException {
        Optional<Comment> comment = commentRepository.findById(commentId);
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        Reply reply = Reply.builder()
                .comment(comment.get())
                .member(member.get())
                .createdAt(LocalDateTime.now())
                .content(replyRequestDto.getContent())
                .build();
        replyRepository.save(reply);

        System.out.println(reply+ "저장");

        //push
        System.out.println(comment.get().getMember().getNickname());
        System.out.println(comment.get().getMember().getId());
        System.out.println(comment.get().getContent());
        fcmService.sendMessageTo(comment.get().getMember().getId(), comment.get().getContent(), "새 대댓글이 달렸습니다");

        return reply.toDto();
    }

    @Override
    @Transactional
    public List<ReplyResponseDto> showReplies(Long commentId) {
        return commentRepository.findById(commentId).get().getReplies().stream()
                .map(Reply::toDto).collect(Collectors.toList());

    }

    @Override
    @Transactional
    public List<BoardResponseDto> getMyBoard(MemberContext user) {
        //N+1
        System.out.println(memberRepository.findByIdJoinFetch(user.getMemberId()).get(0).getMyBoards().get(0).getBoard().getContent());

        return memberRepository.findByIdJoinFetch(user.getMemberId())
                .get(0)
                .getMyBoards()
                .stream()
                .map(myBoard -> {
                            BoardResponseDto boardResponseDto = boardRepository.findById(myBoard.getBoard().getId()).get().toCreatedDto();
                            Optional<Vote> vote = voteRepository.findByBoardAndMember(Board.builder().id(myBoard.getId()).build(), new Member(user.getMemberId()));
                            if (vote.isEmpty()){
                                boardResponseDto.setUserStatus("없다");
                            } else {
                                if (vote.get().getAgreed()){
                                    boardResponseDto.setUserStatus("찬성");
                                }else{
                                    boardResponseDto.setUserStatus("반대");

                                }
                            }
                            return boardResponseDto;

                        }
                ).collect(Collectors.toList());
    }

    @Override
    public void setMyBoard(Long boardId, MemberContext user) {
        Board board = boardRepository.findById(boardId).get();
        Optional<Member> member = memberRepository.findById(user.getMemberId());
        MyBoard myBoard = MyBoard.builder()
                .board(board)
                .member(member.get()
                )
                .build();
        myBoardRepository.save(myBoard);
    }

    @Override
    public List<BoardResponseDto> searchBoard(String content, Pageable pageable, MemberContext user) {

        return boardSearchRepository.searchByBoard(content, pageable)
                .stream()
                .map(boardSearchDto -> {

                    Optional<Member> member = memberRepository.findById(boardSearchDto.getMember_id());
                    List<Comment> comments = commentRepository.findAllByBoard(Board.builder().id(boardSearchDto.getId()).build());

                    //return
                    BoardResponseDto boardResponseDto = BoardResponseDto.builder()
                            .id(boardSearchDto.getId())
                            .title(boardSearchDto.getTitle())
                            .nickname(member.get().getNickname())
                            .profileImageUrl(member.get().getProfileImageUrl())
                            .content(boardSearchDto.getContent())
                            .imageUrl(boardSearchDto.getImage_url())
                            .agreeCount(boardSearchDto.getAgree_count())
                            .disagreeCount(boardSearchDto.getDisagree_count())
                            .recommendCount(boardSearchDto.getRecommend_count())
                            .createdAt(boardSearchDto.getCreated_at())
                            .category(boardSearchDto.getCategory())
                            .commentCount(comments.size())

                            .build();

                    Optional<Vote> vote = voteRepository.findByBoardAndMember(Board.builder().id(boardSearchDto.getId()).build(), member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");

                        }
                    }
                    return boardResponseDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BoardResponseDto> getMyWrittenBoard(MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        return boardRepository.findAllByMemberAndIsDeletedIsFalse(member.get()).stream()
                .map(board -> {
                    BoardResponseDto boardResponseDto = board.toCreatedDto();
                    Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");

                        }
                    }
                    return boardResponseDto;
                })
                // .map(Board::toCreatedDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BoardResponseDto> getMyComment(MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        List<Long> boardIdList = commentRepository.findAllByMember(member.get())
                .stream()
                .map(comment -> comment.getBoard().getId())
                .distinct()
                .collect(Collectors.toList());

        return boardIdList.stream()
                .map(id -> boardRepository.findById(id))
                .filter(board -> board.get().getIsDeleted().equals(false))
                .map(board -> {
                            BoardResponseDto boardResponseDto = board.get().toCreatedDto();
                            Optional<Vote> vote = voteRepository.findByBoardAndMember(board.get(), member.get());
                            if (vote.isEmpty()){
                                boardResponseDto.setUserStatus("없다");
                            } else {
                                if (vote.get().getAgreed()){
                                    boardResponseDto.setUserStatus("찬성");
                                }else{
                                    boardResponseDto.setUserStatus("반대");

                                }
                            }
                            return boardResponseDto;
                        }
                )
                .collect(Collectors.toList());

//
//        List<Long> boardIdList = commentRepository.findAllByMember(member.get())
//                .stream()
//                .map(comment -> comment.getBoard().getId())
//                .distinct()
//                .collect(Collectors.toList());
//
//        return boardIdList.stream()
//                .map(id ->  {Optional<Board> board = boardRepository.findById(id);
//                            BoardResponseDto boardResponseDto = board.get().toCreatedDto();
//                            Optional<Vote> vote = voteRepository.findByBoardAndMember(board.get(), member.get());
//                            if (vote.isEmpty()){
//                                boardResponseDto.setUserStatus("없다");
//                            } else {
//                                if (vote.get().getAgreed()){
//                                    boardResponseDto.setUserStatus("찬성");
//                                }else{
//                                    boardResponseDto.setUserStatus("반대");
//
//                                }
//                            }
//                            return boardResponseDto;
//                }
//                )
//                .collect(Collectors.toList());
//
//        //return null;
    }

    @Override
    public ClickedDto recommendReply(Long replyId, MemberContext user){
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        Optional<Reply> reply = replyRepository.findById(replyId);
        Optional<RecommendReply> recommendReply = recommendReplyRepository.findByReplyAndMember(reply.get(), member.get());

        if (recommendReply.isEmpty()){
            RecommendReply recommendReply1 =  RecommendReply.builder()
                    .member(member.get())
                    .reply(reply.get())
                    .build();
            reply.get().addRecommendCount();
            System.out.println("추천수 더하기");
            System.out.println(reply.get().getRecommendCount());
            recommendReplyRepository.save(recommendReply1);
            return ClickedDto.builder().clicked(Boolean.TRUE).build();

        }else{
            reply.get().subtractRecommendCount();
            System.out.println("추천수 빼기");
            System.out.println(reply.get().getRecommendCount());
            recommendReplyRepository.deleteById(recommendReply.get().getId());
            return ClickedDto.builder().clicked(Boolean.FALSE).build();


        }

    }

    @Override //추가
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = "board_top_voted", key = "#pageable.getOffset() + 'and' + #pageable.getPageSize()")
    public List<BoardResponseDto> findTopVotedBoards(Pageable pageable, MemberContext user) {
        Optional<Member> member = memberRepository.findById(user.getMemberId());

        PageImpl<Board> boards = boardRepository.findTopVotedBoards(pageable);
        return boards.stream()
                .map(board -> {
                    BoardResponseDto boardResponseDto = board.toCreatedDto();
                    Optional<Vote> vote = voteRepository.findByBoardAndMember(board, member.get());
                    if (vote.isEmpty()){
                        boardResponseDto.setUserStatus("없다");
                    } else {
                        if (vote.get().getAgreed()){
                            boardResponseDto.setUserStatus("찬성");
                        }else{
                            boardResponseDto.setUserStatus("반대");
                        }
                    }
                    return boardResponseDto;
                })
                .collect(Collectors.toList());
    }


}
