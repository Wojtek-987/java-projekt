package com.quiz.quizapp.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path uploadsDir;

    public FileStorageService(@Value("${app.storage.uploads-dir}") String uploadsDir) {
        this.uploadsDir = Paths.get(uploadsDir).toAbsolutePath().normalize();
    }

    public void init() {
        try {
            Files.createDirectories(uploadsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create uploads directory: " + uploadsDir, e);
        }
    }

    public String save(MultipartFile file) {
        init();

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        if (original.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // avoid overwrites
        String storedName = System.currentTimeMillis() + "-" + original;

        try {
            Path target = uploadsDir.resolve(storedName).normalize();
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedName;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    public Resource loadAsResource(String storedName) {
        try {
            Path file = uploadsDir.resolve(storedName).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File not found: " + storedName);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Bad file path", e);
        }
    }
}
