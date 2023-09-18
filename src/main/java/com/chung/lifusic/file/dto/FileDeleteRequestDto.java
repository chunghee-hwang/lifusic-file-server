package com.chung.lifusic.file.dto;

import lombok.Data;

@Data
public class FileDeleteRequestDto {
    private String requestUserId; // 요청한 유저 아이디
    private Long musicFileId; // 삭제될 DB에 저장된 파일 아이디
    private Long thumbnailFileId;
}
