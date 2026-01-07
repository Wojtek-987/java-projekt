package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.repository.QuizRepository;
import com.quiz.quizapp.domain.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.quiz.quizapp.domain.service.QuizPdfExportService;


import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService storage;
    private final QuizRepository quizRepository;
    private final QuizPdfExportService pdfExportService;

    public FileController(FileStorageService storage, QuizRepository quizRepository, QuizPdfExportService pdfExportService) {
        this.storage = storage;
        this.quizRepository = quizRepository;
        this.pdfExportService = pdfExportService;
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
        var sb = new StringBuilder();
        sb.append("id,title,negativePointsEnabled,timeLimitSeconds\n");

        quizRepository.findAll().forEach(q -> {
            sb.append(q.getId()).append(",");
            sb.append(escapeCsv(q.getTitle())).append(",");
            sb.append(q.isNegativePointsEnabled()).append(",");
            sb.append(q.getTimeLimitSeconds() == null ? "" : q.getTimeLimitSeconds());
            sb.append("\n");
        });

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quizzes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .body(bytes);
    }

    @GetMapping("/export/quizzes.pdf")
    public ResponseEntity<byte[]> exportQuizzesPdf() {
        byte[] pdf = pdfExportService.exportQuizzesPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quizzes.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
