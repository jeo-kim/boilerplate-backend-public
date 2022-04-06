package com.hanghae99.boilerplate.security.provider;

import com.hanghae99.boilerplate.security.config.JwtConfig;
import com.hanghae99.boilerplate.security.jwt.JwtAuthenticationToken;
import com.hanghae99.boilerplate.security.jwt.RawAccessToken;
import com.hanghae99.boilerplate.security.jwt.extractor.TokenVerifier;
import com.hanghae99.boilerplate.security.model.MemberContext;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {


    @Autowired
    private TokenVerifier tokenVerifier;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RawAccessToken rawAccessToken = (RawAccessToken) authentication.getCredentials();
        Jws<Claims> jwsClaims;
        try {

            jwsClaims = tokenVerifier.validateToken( rawAccessToken.getToken(),JwtConfig.tokenSigningKey);

            String sub = jwsClaims.getBody().getSubject();
            List<String> scopes = jwsClaims.getBody().get("scopes", List.class);
            List<GrantedAuthority> authorityList = scopes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            String nickname =jwsClaims.getBody().getAudience() ;
            Long memberId = Long.valueOf(jwsClaims.getBody().getIssuer());

            MemberContext context = MemberContext.create(sub, authorityList ,nickname,memberId,"");
            //새로운 토큰을 발급해준다
            return new JwtAuthenticationToken(context, context.getAuthorities());
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null,null,e.getMessage());
        }catch (JwtException|NullPointerException e){
            return null;
        }
    }




    @Override
    public boolean supports(Class<?> authentication) {
        return (JwtAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
