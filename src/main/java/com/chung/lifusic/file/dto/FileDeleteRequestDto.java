package com.chung.lifusic.file.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileDeleteRequestDto {
    private List<Long> fileIds; // 삭제될 DB에 저장된 파일 아이디
}
