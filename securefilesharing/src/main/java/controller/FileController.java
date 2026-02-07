package com.securefilesharing.controller;

import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.model.User;
import com.securefilesharing.repository.UserRepository;
import com.securefilesharing.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final UserRepository userRepository;

    public FileController(FileService fileService,
                          UserRepository userRepository) {
        this.fileService = fileService;
        this.userRepository = userRepository;
    }

    // =========================
    // UPLOAD FILE
    // =========================
    @PostMapping("/upload")
    public FileMetadata uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fileService.uploadFile(file, user);
    }

    // =========================
    // DOWNLOAD FILE
    // =========================
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            Authentication authentication) throws Exception {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        File file = fileService.downloadFile(id, user);

        InputStreamResource resource =
                new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .body(resource);
    }
}
