package org.pete.service;

import org.pete.entity.Users;
import org.pete.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class UserDetailProviderService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailProviderService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users users = userRepository.findOneByEmail(username);


        if (Objects.isNull(users)) {
            // TODO Need to check Teller table just in case if the username belong to teller.
//            throw new UsernameNotFoundException();
        }

        return null;
    }
}
