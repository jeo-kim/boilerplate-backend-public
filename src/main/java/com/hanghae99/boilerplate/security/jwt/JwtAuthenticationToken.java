package com.hanghae99.boilerplate.security.jwt;

import com.hanghae99.boilerplate.security.model.MemberContext;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private RawAccessToken rawAccessToken;
    private MemberContext memberContext;

    public JwtAuthenticationToken(RawAccessToken rawAccessToken){
        super(null);
        this.rawAccessToken = rawAccessToken;
        this.setAuthenticated(false);
    }

    public JwtAuthenticationToken(MemberContext memberContext, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.eraseCredentials();
        this.memberContext = memberContext;
        super.setAuthenticated(true); //

    }
    //애매한 부분
    @Override

    public  void setAuthenticated(boolean authenticated){
        if (authenticated) {
            throw new IllegalArgumentException();
        }
        super.setAuthenticated(false);
    }


    @Override
    public Object getCredentials() {
        return rawAccessToken;
    }

    @Override
    public Object getPrincipal() {
        return this.memberContext;
    }
    //정보를 지운다
    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.rawAccessToken = null;
    }

}
