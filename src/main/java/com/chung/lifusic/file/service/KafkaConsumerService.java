package com.chung.lifusic.file.service;

import com.chung.lifusic.file.dto.FileCreateRequestDto;
import com.chung.lifusic.file.dto.FileCreateResponseDto;
import com.chung.lifusic.file.dto.FileDeleteRequestDto;
import com.chung.lifusic.file.dto.FileDeleteResultDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.chung.lifusic.file.common.constants.kafka.GROUP_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private final KafkaProducerService kafkaProducerService;
    private final FileStorageService fileStorageService;

    @KafkaListener(topics = "CREATE_FILE", groupId = GROUP_ID)
    public void consumeFileCreate(ConsumerRecord<String, String> record) throws JsonProcessingException {
        FileCreateRequestDto fileCreateRequest = new ObjectMapper().readValue(record.value(), FileCreateRequestDto.class);
        log.info("CREATE FILE STARTED: {}", fileCreateRequest);
        try {
            FileCreateResponseDto response = fileStorageService.storeFileInDirectoryAndDB(fileCreateRequest);
            kafkaProducerService.produceCreateFileComplete(response);
            log.info("CREATE FILE DONE: {}", fileCreateRequest);
        } catch (Exception exception) {
            log.error("FAIL TO CREATE FILE: {}, exception: {}", fileCreateRequest, exception.getMessage());
            kafkaProducerService.produceCreateFileComplete(FileCreateResponseDto.builder()
                    .requestUserId(fileCreateRequest.getRequestUserId())
                    .isSuccess(false)
                    .build());
        }
    }

    @KafkaListener(topics = "DELETE_FILE", groupId = GROUP_ID)
    public void consumeFileDelete(ConsumerRecord<String, String> record) throws JsonProcessingException {
        FileDeleteRequestDto fileDeleteRequest = new ObjectMapper().readValue(record.value(), FileDeleteRequestDto.class);
        log.info("DELETE FILE STARTED.  {}", fileDeleteRequest);
        try {
            List<FileDeleteResultDto> results = fileStorageService.deleteFilesInDirectoryAndDB(fileDeleteRequest.getFileIds());
            log.info("DELETE FILE SUCCESS. RESULT ARRAY: {}", results);
        } catch (Exception exception) {
            log.error("FAIL TO DELETE FILE: {}, exception: {}", fileDeleteRequest, exception.getMessage());
        }
    }
}
