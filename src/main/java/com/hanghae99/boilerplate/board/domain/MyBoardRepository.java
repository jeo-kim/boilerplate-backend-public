package com.hanghae99.boilerplate.board.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBoardRepository extends JpaRepository<MyBoard, Long> {
}
