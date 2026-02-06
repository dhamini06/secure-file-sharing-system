package com.securefilesharing.controller;

import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileMetadataRepository fileRepo;

    public FileController(FileMetadataRepository fileRepo) {
        this.fileRepo = fileRepo;
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam String username
    ) throws IOException {

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + "/" + storedFileName;

        file.transferTo(new File(filePath));

        FileMetadata meta = new FileMetadata();
        meta.setOriginalFileName(file.getOriginalFilename());
        meta.setStoredFileName(storedFileName);
        meta.setFilePath(filePath);
        meta.setFileSize(file.getSize());
        meta.setUploadedBy(username);

        fileRepo.save(meta);

        return "File uploaded successfully";
    }
}
