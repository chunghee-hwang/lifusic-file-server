package com.chung.lifusic.file.controller;

import com.chung.lifusic.file.service.FileDownloadService;
import com.chung.lifusic.file.service.FileStreamService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileDownloadService fileDownloadService;
    private final FileStreamService fileStreamService;
    // 파일 다운로드
    @GetMapping("/{fileId}")
    public void downloadFile(
            @PathVariable Long fileId,
            HttpServletResponse response
    ) {
        fileDownloadService.downloadFile(fileId, response);
    }

    // 파일 스트리밍(partial data)
    @GetMapping("/{fileId}/stream")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> streamingFile(
            @PathVariable Long fileId,
            @RequestHeader(value="Range", required = false) String rangeHeader
    ) {
        return fileStreamService.loadPartialMediaFile(fileId, rangeHeader);
    }
}
