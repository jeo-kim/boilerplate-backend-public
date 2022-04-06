package com.hanghae99.boilerplate.unitTest.signupLogin.SignupLogin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghae99.boilerplate.memberManager.model.Member;
import com.hanghae99.boilerplate.memberManager.repository.MemberRepository;
import com.hanghae99.boilerplate.security.config.RefreshTokenRedis;
import com.hanghae99.boilerplate.security.model.MemberContext;
import com.hanghae99.boilerplate.security.model.login.LoginRequestDto;
import com.hanghae99.boilerplate.signupLogin.dto.requestDto.SignupReqestDto;
import com.hanghae99.boilerplate.signupLogin.service.SignupLoginService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


/*
 *비정상적인 입력이 들어오는건 체크하지 않음
 * 이유는 ? @vaild를 통과한 후에 service 로 오니깐
 *
 * 로그아웃의 테스트 코드는 어떻게 작성하지 ?? controller가 필요한듯
 */
@ExtendWith(MockitoExtension.class)
class SignupLoginServiceTest {

    @Mock
    MemberRepository memberRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RefreshTokenRedis redis;
    @InjectMocks
    SignupLoginService signupLoginService = new SignupLoginService();


    ObjectMapper objectMapper = new ObjectMapper();

    SignupReqestDto signupReqestDto = new SignupReqestDto("wns674@naver.com", "최호준", "1234", "이미지1번");
    LoginRequestDto loginRequestDto = new LoginRequestDto(signupReqestDto.getEmail(), signupReqestDto.getPassword());
    Member member = new Member(signupReqestDto);

    @Test
    @DisplayName("존재하지 않는 이메일 + 정상적인 signupReqestDto")
    public void SignupSuccess() throws Exception {
        //Given
        Mockito.when(memberRepository.save(any(Member.class))).thenReturn(member);
        //when
        signupLoginService.signupRequest(signupReqestDto);

    }


    @Test
    @DisplayName("유저 삭제 성공")
    void expireUser(){
        Mockito.when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(member));
        Mockito.when(passwordEncoder.matches(any(),any())).thenReturn(true);
        signupLoginService.membershipWithdrawal(new MemberContext(member),signupReqestDto.getPassword());
    }
    @Test
    @DisplayName("일치하는 유저가 없음")
    void expireNotUser(){
        Mockito.when(memberRepository.findById(any())).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class,()->{
            signupLoginService.membershipWithdrawal(new MemberContext(member),signupReqestDto.getPassword());
        }) ;
    }




}