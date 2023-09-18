package com.chung.lifusic.file.controller;

import com.chung.lifusic.file.service.FileDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileDownloadService fileDownloadService;
    @GetMapping("/{fileId}")
    public void downloadFile(
            @PathVariable Long fileId,
            HttpServletResponse response
    ) {
        fileDownloadService.downloadFile(fileId, response);
    }
}
