package org.pete.model.principle;

import lombok.Getter;
import org.pete.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public record UserPrinciple(Users users) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleStr = users.getRole();

        if (Objects.isNull(roleStr)) {
            return List.of();
        }

        roleStr = roleStr.replace("[", "").replace("]", "");
        String[] roleList = roleStr.split(",");

        return Arrays.stream(roleList)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (Objects.isNull(users.getLastLoginDate())) {
            return false;
        }

        Duration duration = Duration.between(users.getLastLoginDate(), LocalDateTime.now());

        return duration.toMinutes() <= 30;
    }
}
