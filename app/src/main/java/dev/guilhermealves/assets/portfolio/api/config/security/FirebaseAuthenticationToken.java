package dev.guilhermealves.assets.portfolio.api.config.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final String uid;
    private final String email;
    private final Object credentials;

    public FirebaseAuthenticationToken(String uid, String email, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.uid = uid;
        this.email = email;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return uid;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}
