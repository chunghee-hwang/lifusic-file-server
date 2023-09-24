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
import java.util.List;

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

        String musicFileName = storeFile(musicTempFile);
        File musicFile = saveFileInfo(musicTempFile, musicFileName);
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
    public List<FileDeleteResultDto> deleteFilesInDirectoryAndDB(List<Long> fileIds) {
        List<File> files = fileRepository.findAllById(fileIds);
        fileRepository.deleteAllByIdInBatch(fileIds);
        return files.stream().map(this::deleteFile).toList();
    }

    /**
     * 파일을 directory에 저장
     */
    private String storeFile(FileDto fileDto) throws IOException {
        String tempFilePath = getFileDirectoryPath().resolve(fileDto.getTempFilePath()).toString();
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
        String fileName = randomName + "." + fileExtension;
        Path filePath = directoryPath.resolve(fileName);
        boolean renameSuccess = tempFile.renameTo(filePath.toFile());
        if (!renameSuccess) {
            throw new IOException("Cannot rename file:" + filePath);
        }
        return fileName;
    }

    /**
     * 파일 삭제
     */
    private FileDeleteResultDto deleteFile(File file) {
        java.io.File f = getFileDirectoryPath().resolve(file.getPath()).toFile();
        boolean isSuccess = false;
        if (f.exists()) {
            isSuccess = f.delete();
        }
        return FileDeleteResultDto.builder()
                .fileId(file.getId())
                .filePath(file.getPath())
                .isSuccess(isSuccess)
                .build();
    }

    /**
     * 파일을 DB에 저장
     */
    private File saveFileInfo(FileDto fileDto, String fileName) {
        File file = File.builder()
                .originalFileName(fileDto.getOriginalFileName())
                .size(fileDto.getSize())
                .contentType(fileDto.getContentType())
                .path(fileName)
                .build();

        return fileRepository.save(file);
    }

    private Path getFileDirectoryPath() {
        return Paths.get(FILE_DIRECTORY).toAbsolutePath().normalize();
    }
}
