package com.hanghae99.boilerplate.board.domain;


import com.hanghae99.boilerplate.board.dto.BoardResponseDto;
import com.hanghae99.boilerplate.memberManager.model.Member;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
@Setter
//@Document(indexName="boards")
public class Board {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    private String title;
    private String content;
    private String imageUrl;



    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;


    private int agreeCount;
    private int disagreeCount;
    private int recommendCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Boolean isDeleted;//=false;


    private String category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "board", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "board", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<RecommendBoard> recommendBoards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "board", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Vote> votes;


    public void addAgreeCount(){
        this.agreeCount++;
    }

    public void addDisagreeCount(){
        this.disagreeCount++;
    }

    public void addRecommendCount(){
        this.recommendCount++;
    }

    public void subtractAgreeCount(){
        this.agreeCount--;
    }

    public void subtractDisagreeCount(){
        this.disagreeCount--;
    }

    public void subtractRecommendCount(){
        this.recommendCount--;
    }
    public BoardResponseDto toCreatedDto() {


        return  BoardResponseDto.builder()
                .id(this.id)
                .title(this.title)
                .nickname(this.member.getNickname())
                .profileImageUrl(this.member.getProfileImageUrl())
                .content(this.content)
                .imageUrl(this.imageUrl)
                .agreeCount(this.agreeCount)
                .disagreeCount(this.disagreeCount)
                .recommendCount(this.recommendCount)
                .createdAt(this.createdAt)
                .category(this.category)
                .commentCount(this.comments.size())

                .build();
    }
    


}
