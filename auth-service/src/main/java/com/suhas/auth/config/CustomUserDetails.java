package com.suhas.auth.config;

import com.suhas.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    // The Constructor maps your User Entity to Spring Security fields
    public CustomUserDetails(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        // This converts your "ROLE_USER" string into a GrantedAuthority object
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Keep true for now
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Keep true for now
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Keep true for now
    }

    @Override
    public boolean isEnabled() {
        return true; // Keep true for now
    }
}