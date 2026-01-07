package com.quiz.quizapp.domain.repository;

import com.quiz.quizapp.domain.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest extends PostgresDataJpaTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindById_roundTrip() {
        UserEntity saved = userRepository.save(new UserEntity("alice@example.com", "hashhashhash", "ROLE_USER"));

        Optional<UserEntity> loaded = userRepository.findById(saved.getId());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(loaded.get().getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void findByEmailIgnoreCase_findsRegardlessOfCase() {
        userRepository.save(new UserEntity("Case@Test.COM", "hashhashhash", "ROLE_CREATOR"));

        Optional<UserEntity> loaded = userRepository.findByEmailIgnoreCase("case@test.com");

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getEmail()).isEqualTo("Case@Test.COM");
    }

    @Test
    void findByEmailIgnoreCase_returnsEmptyWhenMissing() {
        Optional<UserEntity> loaded = userRepository.findByEmailIgnoreCase("missing@example.com");
        assertThat(loaded).isEmpty();
    }

    @Test
    void save_duplicateEmail_violatesUniqueConstraint() {
        userRepository.save(new UserEntity("dup@example.com", "hashhashhash", "ROLE_USER"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new UserEntity("dup@example.com", "otherhashhash", "ROLE_USER"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
