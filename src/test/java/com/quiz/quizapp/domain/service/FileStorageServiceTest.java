package com.quiz.quizapp.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tmp;

    @Test
    void save_returnsStoredNameContainingOriginalFilename() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        String stored = storage.save(mf);

        assertThat(stored).contains("hello.txt");
    }

    @Test
    void save_createsFileOnDisk() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        String stored = storage.save(mf);

        assertThat(tmp.resolve(stored)).exists();
    }

    @Test
    void save_rejectsPathTraversal() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "../evil.txt", "text/plain", "x".getBytes());

        assertThatThrownBy(() -> storage.save(mf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file name");
    }

    @Test
    void loadAsResource_returnsExistingResource() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "a.txt", "text/plain", "content".getBytes());
        String stored = storage.save(mf);

        Resource r = storage.loadAsResource(stored);

        assertThat(r.exists()).isTrue();
    }

    @Test
    void loadAsResource_returnsReadableResource() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "a.txt", "text/plain", "content".getBytes());
        String stored = storage.save(mf);

        Resource r = storage.loadAsResource(stored);

        assertThat(r.isReadable()).isTrue();
    }

    @Test
    void loadAsResource_returnsResourceWithStoredFilename() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        var mf = new MockMultipartFile("file", "a.txt", "text/plain", "content".getBytes());
        String stored = storage.save(mf);

        Resource r = storage.loadAsResource(stored);

        assertThat(r.getFilename()).isEqualTo(stored);
    }

    @Test
    void loadAsResource_throwsWhenMissing() {
        FileStorageService storage = new FileStorageService(tmp.toString());

        assertThatThrownBy(() -> storage.loadAsResource("missing.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File not found");
    }
}
