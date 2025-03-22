package org.pete.model.principle;

import org.pete.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserPrinciple implements UserDetails {

    private final Users users;

    public UserPrinciple(Users users) {
        this.users = users;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleStr = users.getRole();

        if (Objects.isNull(roleStr)) {
            return List.of();
        }

        roleStr = roleStr.replace("[", "").replace("]", "");
        String[] roleList = roleStr.split(",");

        return Arrays.stream(roleList)
                .map(SimpleGrantedAuthority::new)
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
}
