package com.chung.lifusic.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteResultDto {
    private Long fileId;
    private String filePath;
    private Boolean isSuccess;
}
