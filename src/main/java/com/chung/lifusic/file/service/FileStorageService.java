package com.chung.lifusic.file.service;

import com.chung.lifusic.file.common.utils.StringUtil;
import com.chung.lifusic.file.dto.*;
import com.chung.lifusic.file.entity.File;
import com.chung.lifusic.file.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public FileCreateResponseDto storeFileInDirectoryAndDB(FileCreateRequestDto fileCreateRequest) throws IOException {
        FileDto musicTempFile = fileCreateRequest.getMusicTempFile();
        FileDto thumbnailTempFile = fileCreateRequest.getThumbnailTempFile();

        String musicFilePath = storeFile(musicTempFile);
        File musicFile = saveFileInfo(musicTempFile, musicFilePath);
        File thumbnailFile = null;
        if (thumbnailTempFile != null) {
            String thumbnailFilePath = storeFile(thumbnailTempFile);
            thumbnailFile = saveFileInfo(thumbnailTempFile, thumbnailFilePath);
        }

        return FileCreateResponseDto.builder()
                .content(
                        FileCreateResponseDto.Content.builder()
                                .musicFileId(musicFile.getId())
                                .thumbnailFileId(thumbnailFile == null ? null : thumbnailFile.getId())
                                .musicName(fileCreateRequest.getMusicName())
                                .build()
                )
                .requestUserId(fileCreateRequest.getRequestUserId())
                .isSuccess(true)
                .build();
    }

    @Transactional
    public FileDeleteResponseDto deleteFileInDirectoryAndDB(FileDeleteRequestDto fileDeleteRequest) {

        Long musicFileId = fileDeleteRequest.getMusicFileId();
        Long thumbnailFileId = fileDeleteRequest.getThumbnailFileId();

        File musicFile = fileRepository.findById(musicFileId).orElseGet(() -> null);
        if (musicFile != null) {
            // DB에서 삭제
            fileRepository.deleteById(musicFileId);
            // 파일 삭제
            this.deleteFile(musicFile.getPath());
        }

        if (thumbnailFileId != null) {
            File thumnailFile = fileRepository.findById(thumbnailFileId).orElseGet(() -> null);
            if (thumnailFile != null) {
                // DB에서 삭제
                fileRepository.deleteById(thumbnailFileId);
                // 파일 삭제
                this.deleteFile(thumnailFile.getPath());
            }
        }

        return FileDeleteResponseDto.builder()
                .isSuccess(true)
                .requestUserId(fileDeleteRequest.getRequestUserId())
                .content(FileDeleteResponseDto.Content.builder()
                        .musicFileId(musicFileId)
                        .thumbnailFileId(thumbnailFileId)
                        .build())
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
        Path directoryPath = getFileDirectoryPath();
        if (!directoryPath.toFile().exists()) {
            Files.createDirectories(directoryPath);
        }
        final String fileExtension = StringUtils.getFilenameExtension(fileDto.getOriginalFileName());
        Path filePath = directoryPath.resolve(randomName + "." + fileExtension);
        boolean renameSuccess = tempFile.renameTo(filePath.toFile());
        if (!renameSuccess) {
            throw new IOException("Cannot rename file:" + filePath);
        }
        return filePath.toAbsolutePath().toString();
    }

    /**
     * 파일 삭제
     */
    private boolean deleteFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
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
