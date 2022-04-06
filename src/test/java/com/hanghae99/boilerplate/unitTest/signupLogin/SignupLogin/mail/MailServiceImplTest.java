package com.hanghae99.boilerplate.unitTest.signupLogin.SignupLogin.mail;

import com.hanghae99.boilerplate.memberManager.mail.MailVerifyRedis;
import com.hanghae99.boilerplate.memberManager.mail.platforms.Google;
import com.hanghae99.boilerplate.memberManager.mail.service.MailServiceImpl;
import com.hanghae99.boilerplate.memberManager.model.Member;
import com.hanghae99.boilerplate.memberManager.repository.MemberRepository;
import com.hanghae99.boilerplate.signupLogin.kakao.TemporaryUser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {


    @Mock
    MemberRepository memberRepository;

    @Mock
    Google google;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    MailServiceImpl mailService = new MailServiceImpl();




    TemporaryUser temporaryUser = new TemporaryUser("wns674@naver.com", "ghwns", "123");
    Member member = new Member(temporaryUser);


    @Test
    @DisplayName("이메일이 존재 O")
    void verifyEmailExist() throws MessagingException {
        Mockito.when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.ofNullable(member));
        Mockito.when(passwordEncoder.encode(any(String.class))).thenReturn("111");
        doNothing().when(google).sendMail(any(String.class), any(String.class), any(String.class));

        mailService.sendPasswordEmail(member.getEmail());


    }

    @Test
    @DisplayName("이메일이 존재 X")
    void verifyEmailNotExist() throws MessagingException {
        Mockito.when(memberRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());


        assertThrows(UsernameNotFoundException.class, () -> {
            mailService.sendPasswordEmail(member.getEmail());
        });


    }
}