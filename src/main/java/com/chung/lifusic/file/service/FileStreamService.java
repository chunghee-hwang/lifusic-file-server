package com.chung.lifusic.file.service;

import com.chung.lifusic.file.common.utils.StringUtil;
import com.chung.lifusic.file.entity.File;
import com.chung.lifusic.file.exception.NotFoundException;
import com.chung.lifusic.file.exception.UnExpectedException;
import com.chung.lifusic.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStreamService {
    private final FileRepository fileRepository;

    @Value("${file.upload.directory}")
    private String FILE_DIRECTORY;

    public Path getFilePath(Long fileId) {
        try {
            File file = fileRepository.findById(fileId).orElseThrow();
            return getFileDirectoryPath().resolve(file.getPath());
        } catch (NoSuchElementException exception) {
            throw new NotFoundException();
        }
    }

    /**
     * 파일의 일부분 스트리밍
     * @param fileId 파일 아이디
     * @param rangeValues 파일 범위
     */
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile(Long fileId, String rangeValues) {
        log.info("STREAM FILE START (ID: {})", fileId);
        Path filePath = this.getFilePath(fileId);
        if (!StringUtils.hasText(rangeValues)) {
            return loadEntireMediaFile(filePath);
        }
        long rangeStart = 0L;
        long rangeEnd = 0L;
        if (filePath == null || !filePath.toFile().exists()) {
            throw new NotFoundException();
        }
        try {
            long fileSize = Files.size(filePath);
//            log.info("Read range seeking value");
//            log.info("Range values: [{}]", rangeValues);
            int dashPos = rangeValues.indexOf("-");
            if (dashPos > 0 && dashPos <= (rangeValues.length() - 1)) {
                String[] ranges = rangeValues.split("-");
                if (ranges.length > 0) {
//                    log.info("ranges size: " + ranges.length);
                    if (StringUtils.hasText(ranges[0])) {
//                        log.info("Range values: [0]: [{}]", ranges[0]);
                        String valueToParse = StringUtil.getNumericStringValue(ranges[0]);
                        rangeStart = StringUtil.parseStringValue2Long(valueToParse);
                    }

                    if (ranges.length > 1) {
//                        log.info("Range values: [1]: [{}]", ranges[1]);
                        String valueToParse = StringUtil.getNumericStringValue(ranges[1]);
                        rangeEnd = StringUtil.parseStringValue2Long(valueToParse);
                    } else {
                        if (fileSize > 0) {
                            rangeEnd = fileSize - 1L;
                        }
                    }
                }
            }

            if ((rangeEnd == 0L && fileSize > 0L) || fileSize < rangeEnd) {
                rangeEnd = fileSize - 1L;
            }
//            log.info("Parsed range values: {} - {}", rangeStart, rangeEnd);
            return loadPartialMediaFile(filePath, rangeStart, rangeEnd);
        } catch (IOException exception) {
            log.info("STREAM FILE ERROR (ID: {}), exception: {}", fileId, exception.getMessage());
            throw new UnExpectedException();
        } finally {
            log.info("STREAM FILE END (ID: {})", fileId);
        }
    }

    /**
     * 파일의 일부분 스트리밍
     * @param filePath 파일 경로
     * @param fileStartPos 파일 byte 시작 지점
     * @param fileEndPos 파일 byte 끝 지점
     */
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile
            (Path filePath, long fileStartPos, long fileEndPos) {
        StreamingResponseBody responseStream;
        if (filePath == null || !filePath.toFile().exists()) {
            throw new NotFoundException();
        }
        try {
            long fileSize = Files.size(filePath);
            if (fileSize < 0L) {
                fileStartPos = 0L;
            }
            if (fileSize > 0L) {
                if (fileStartPos >= fileSize) {
                    fileStartPos = fileSize - 1L;
                }
                if (fileEndPos >= fileSize) {
                    fileEndPos = fileSize - 1L;
                }
            } else {
                fileStartPos = 0L;
                fileEndPos = 0L;
            }

            byte[] buffer = new byte[1024];
            final String contentType = Files.probeContentType(filePath);
            final HttpHeaders responseHeaders = new HttpHeaders();
            String contentLength = String.valueOf((fileEndPos - fileStartPos) + 1);
            responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            responseHeaders.add(HttpHeaders.CONTENT_LENGTH, contentLength);
            responseHeaders.add(HttpHeaders.ACCEPT_RANGES, "bytes");
            responseHeaders.add(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", fileStartPos, fileEndPos, fileSize));

            final long fileStartPos2 = fileStartPos;
            final long fileEndPos2 = fileEndPos;
            responseStream = outputStream -> {
                RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
                try (file) {
                    long pos = fileStartPos2;
                    file.seek(pos);
                    while(pos < fileEndPos2) {
                        file.read(buffer);
                        outputStream.write(buffer);
                        pos += buffer.length;
                    }
                    outputStream.flush();
                } catch (Exception ignored) {}
            };
            return new ResponseEntity<StreamingResponseBody>(
                    responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT
            );
        } catch (IOException exception) {
            throw new UnExpectedException();
        }
    }

    /**
     * 모든 파일 스트리밍
     * @param filePath 파일 경로
     */
    public ResponseEntity<StreamingResponseBody> loadEntireMediaFile(Path filePath) {
        if (filePath == null || !filePath.toFile().exists()) {
            throw new NotFoundException();
        }
        try {
            long fileSize = Files.size(filePath);
            long endPos = fileSize;
            if (fileSize > 0L) {
                endPos = fileSize - 1L;
            } else {
                endPos = 0L;
            }
            return loadPartialMediaFile(filePath, 0L, endPos);
        } catch (IOException exception) {
            throw new UnExpectedException();
        }
    }

    private Path getFileDirectoryPath() {
        return Paths.get(FILE_DIRECTORY).toAbsolutePath().normalize();
    }
}
