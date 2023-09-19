package com.chung.lifusic.file.service;

import com.chung.lifusic.file.entity.File;
import com.chung.lifusic.file.exception.NotFoundException;
import com.chung.lifusic.file.exception.UnExpectedException;
import com.chung.lifusic.file.repository.FileRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileDownloadService {
    private final FileRepository fileRepository;

    @Value("${file.upload.directory}")
    private String FILE_DIRECTORY;

    public void downloadFile(Long fileId, HttpServletResponse response) {
        File musicFile = fileRepository.findById(fileId).orElseGet(() -> null);
        if (musicFile == null) {
            throwNotFoundException(fileId);
        }
        java.io.File file = new java.io.File(getFileDirectoryPath().resolve(musicFile.getPath()).toString());
        if (!file.exists()) {
            throwNotFoundException(fileId);
        }
        try (FileInputStream fis = new FileInputStream(file)){
            final String contentDisposition = "attachment; filename=\"" + file.getName() + "\"";
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            fis.transferTo(response.getOutputStream());
        } catch (FileNotFoundException fileNotFoundException) {
            throwNotFoundException(fileId);
        } catch (IOException ioException) {
            throw new UnExpectedException();
        }
    }

    private void throwNotFoundException(Long fileId) {
        throw new NotFoundException("File not found - musicId: " + fileId);
    }

    private Path getFileDirectoryPath() {
        return Paths.get(FILE_DIRECTORY).toAbsolutePath().normalize();
    }
}
