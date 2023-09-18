package com.chung.lifusic.file.service;

import com.chung.lifusic.file.common.utils.StringUtil;
import com.chung.lifusic.file.dto.FileCreateRequestDto;
import com.chung.lifusic.file.dto.FileDto;
import com.chung.lifusic.file.dto.FileResponseDto;
import com.chung.lifusic.file.entity.File;
import com.chung.lifusic.file.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.impl.IOFileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${file.upload.directory}")
    private String FILE_DIRECTORY;
    private final FileRepository fileRepository;

    @Transactional
    public FileResponseDto storeFileInDirectoryAndDB(FileCreateRequestDto fileCreateRequest) throws IOException {
        FileDto musicTempFile = fileCreateRequest.getMusicTempFile();
        FileDto thumbnailTempFile = fileCreateRequest.getThumbnailTempFile();

        String musicFilePath = storeFile(musicTempFile);
        File musicFile = saveFileInfo(musicTempFile, musicFilePath);
        File thumbnailFile = null;
        if (thumbnailTempFile != null) {
            String thumbnailFilePath = storeFile(thumbnailTempFile);
            thumbnailFile = saveFileInfo(thumbnailTempFile, thumbnailFilePath);
        }

        return FileResponseDto.builder()
                .content(
                        FileResponseDto.Content.builder()
                                .musicFileId(musicFile.getId())
                                .thumbnailFileId(thumbnailFile == null ? null : thumbnailFile.getId())
                                .musicName(fileCreateRequest.getMusicName())
                                .build()
                )
                .requestUserId(fileCreateRequest.getRequestUserId())
                .isSuccess(true)
                .build();
    }

    /**
     * 파일을 directory에 저장
     */
    private String storeFile(FileDto fileDto) throws IOException {
        String tempFilePath = fileDto.getTempFilePath();
        String randomName = StringUtil.getUniqueString(5);
        java.io.File tempFile = new java.io.File(tempFilePath);
        if (!tempFile.exists()) {
            throw new FileNotFoundException("Temp file not exists:" + tempFilePath);
        }
        Path directoryPath = getFileDirectoryPath().resolve(randomName);
        if (!directoryPath.toFile().exists()) {
            Files.createDirectories(directoryPath);
        }
        final String fileExtension = StringUtils.getFilenameExtension(fileDto.getOriginalFileName());
        Path filePath = directoryPath.resolve(randomName+"."+fileExtension);
        boolean renameSuccess = tempFile.renameTo(filePath.toFile());
        if (!renameSuccess) {
            throw new IOException("Cannot rename file:" + filePath);
        }
        return filePath.toAbsolutePath().toString();
    }

    /**
     * 파일을 DB에 저장
     */
    private File saveFileInfo(FileDto fileDto, String path) {
        File file = File.builder()
                .originalFileName(fileDto.getOriginalFileName())
                .size(fileDto.getSize())
                .contentType(fileDto.getContentType())
                .path(path)
                .build();

        return fileRepository.save(file);
    }

    private Path getFileDirectoryPath() {
        return Paths.get(FILE_DIRECTORY).toAbsolutePath().normalize();
    }
}
