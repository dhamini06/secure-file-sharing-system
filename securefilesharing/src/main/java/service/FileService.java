package com.securefilesharing.service;

import com.securefilesharing.model.FileMetadata;
import com.securefilesharing.model.User;
import com.securefilesharing.repository.FileMetadataRepository;
import com.securefilesharing.util.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;

    public FileService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // =========================
    // UPLOAD FILE (ENCRYPT)
    // =========================
    public FileMetadata uploadFile(MultipartFile file, User user) throws Exception {

        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String storedFileName = UUID.randomUUID().toString();

        File tempFile = new File(uploadDir, storedFileName + ".tmp");
        File encryptedFile = new File(uploadDir, storedFileName + ".enc");

        file.transferTo(tempFile);

        AESUtil.encryptFile(tempFile, encryptedFile);

        tempFile.delete();

        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFileName(file.getOriginalFilename());
        metadata.setStoredFileName(storedFileName);
        metadata.setFilePath(encryptedFile.getAbsolutePath());
        metadata.setFileSize(file.getSize());
        metadata.setUser(user);

        return fileMetadataRepository.save(metadata);
    }

    // =========================
    // DOWNLOAD FILE (DECRYPT)
    // =========================
    public File downloadFile(Long fileId, User user) throws Exception {

        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 🔒 Ownership check
        if (!metadata.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        File encryptedFile = new File(metadata.getFilePath());

        if (!encryptedFile.exists()) {
            throw new RuntimeException("Encrypted file not found");
        }

        File decryptedFile = File.createTempFile(
                "dec-",
                "-" + metadata.getOriginalFileName()
        );

        AESUtil.decryptFile(encryptedFile, decryptedFile);

        return decryptedFile;
    }
}

