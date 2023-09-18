package com.chung.lifusic.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {
    private String tempFilePath; // 업로드될 실제 경로가 아닌 임시 경로
    private String originalFileName; // 프론트엔드에서 업로드된 원본 파일명
    private String contentType; // 파일 타입
    private Long size; // 단위: Byte
}
