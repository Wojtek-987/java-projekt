package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.service.FileStorageService;
import com.quiz.quizapp.domain.service.QuizExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService storage;
    private final QuizExportService quizExportService;

    public FileController(FileStorageService storage, QuizExportService quizExportService) {
        this.storage = storage;
        this.quizExportService = quizExportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        String storedName = storage.save(file);
        return ResponseEntity.ok(storedName);
    }

    @GetMapping("/download/{name}")
    public ResponseEntity<Resource> download(@PathVariable("name") String name) {
        Resource resource = storage.loadAsResource(name);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/export/quizzes.csv")
    public ResponseEntity<byte[]> exportQuizzesCsv() {
        byte[] bytes = quizExportService.exportQuizzesCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quizzes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(bytes);
    }

    @GetMapping("/export/quizzes.pdf")
    public ResponseEntity<byte[]> exportQuizzesPdf() {
        byte[] pdf = quizExportService.exportQuizzesPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quizzes.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
