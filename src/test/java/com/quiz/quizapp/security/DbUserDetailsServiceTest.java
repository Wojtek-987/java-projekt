package com.quiz.quizapp.security;

import com.quiz.quizapp.domain.entity.UserEntity;
import com.quiz.quizapp.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DbUserDetailsService service;

    @Test
    void loadUserByUsername_throwsWhenMissing() {
        when(userRepository.findByEmailIgnoreCase("x@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_mapsUsernameAndPassword() {
        UserEntity u = new UserEntity("a@example.com", "{hash}", "ADMIN");
        when(userRepository.findByEmailIgnoreCase("a@example.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("a@example.com");

        assertThat(details.getUsername()).isEqualTo("a@example.com");
        assertThat(details.getPassword()).isEqualTo("{hash}");
    }

    @Test
    void loadUserByUsername_prefixesRoleWithROLE_() {
        UserEntity u = new UserEntity("a@example.com", "{hash}", "ADMIN");
        when(userRepository.findByEmailIgnoreCase("a@example.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("a@example.com");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .anyMatch(s -> s.contains("ROLE_ADMIN"));
    }
}
