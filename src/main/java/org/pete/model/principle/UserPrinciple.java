package org.pete.model.principle;

import org.pete.constant.Role;
import org.pete.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Override
    public boolean isCredentialsNonExpired() {
        // Credential for Teller is always non expired.
        // This is just for demonstration.
        if (users.getRole().contains(Role.TELLER.toString())) {
            return true;
        }

        if (Objects.isNull(users.getLastLoginDate())) {
            return false;
        }

        LocalDateTime expiredTime = users.getLastLoginDate().plusMinutes(30L);

        return users.getLastLoginDate().isBefore(expiredTime);
    }

    public Users getUsers() {
        return users;
    }
}
