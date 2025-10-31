package org.example.gametgweb.gameplay.game.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record PlayerDetails(PlayerEntity playerEntity) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // можно вернуть роли позже
    }

    @Override
    public String getPassword() {
        return String.valueOf(playerEntity.getPassword()); // если пароль Long — лучше заменить на String
    }

    @Override
    public String getUsername() {
        return playerEntity.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
