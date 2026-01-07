package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.repository.QuizRepository;
import com.quiz.quizapp.domain.service.FileStorageService;
import com.quiz.quizapp.domain.service.QuizPdfExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FileController.class)
@AutoConfigureMockMvc(addFilters = true)
class FileControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService storage;

    @MockitoBean
    private QuizRepository quizRepository;

    @MockitoBean
    private QuizPdfExportService pdfExportService;

    @Test
    @WithMockUser(username = "creator@example.com", roles = "CREATOR")
    void upload_withAuth_storesFile_andReturnsStoredName() throws Exception {
        when(storage.save(any(MultipartFile.class))).thenReturn("123-file.txt");

        mockMvc.perform(
                        multipart("/api/v1/files/upload")
                                .file("file", "hello".getBytes())
                )
                .andExpect(status().isOk())
                .andExpect(content().string("123-file.txt"));

        verify(storage, times(1)).save(any(MultipartFile.class));
    }

    @Test
    void download_returnsOctetStream() throws Exception {
        Resource resource = new ByteArrayResource("hello".getBytes()) {
            @Override
            public String getFilename() {
                return "x.txt";
            }
        };

        when(storage.loadAsResource("x.txt")).thenReturn(resource);

        mockMvc.perform(get("/api/v1/files/download/x.txt"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"x.txt\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(storage, times(1)).loadAsResource("x.txt");
    }

    @Test
    void exportPdf_returnsPdfBytes() throws Exception {
        when(pdfExportService.exportQuizzesPdf()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/v1/files/export/quizzes.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"quizzes.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));

        verify(pdfExportService, times(1)).exportQuizzesPdf();
    }
}
