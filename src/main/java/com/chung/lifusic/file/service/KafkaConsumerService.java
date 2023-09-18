package com.chung.lifusic.file.service;

import com.chung.lifusic.file.dto.FileCreateRequestDto;
import com.chung.lifusic.file.dto.FileResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.chung.lifusic.file.common.constants.kafka.GROUP_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private final KafkaProducerService kafkaProducerService;
    private final FileStorageService fileStorageService;

    @KafkaListener(topics = "CREATE_FILE", groupId = GROUP_ID)
    @Transactional
    public void consumeFileCreate(ConsumerRecord<String, String> record) throws JsonProcessingException {
        FileCreateRequestDto fileCreateRequest = new ObjectMapper().readValue(record.value(), FileCreateRequestDto.class);
        try {
            FileResponseDto response = fileStorageService.storeFileInDirectoryAndDB(fileCreateRequest);
            kafkaProducerService.produceCreateFileComplete(response);
        } catch (Exception exception) {
            log.info("Fail to create file: {}", exception.getMessage());
//            kafkaProducerService.produceCreateFileComplete(FileResponseDto.builder()
//                    .requestUserId(fileCreateRequest.getRequestUserId())
//                    .isSuccess(false)
//                    .build());
        }
    }

    @KafkaListener(topics = "DELETE_FILE", groupId = GROUP_ID)
    @Transactional
    public void consumeFileDeleteComplete(ConsumerRecord<String, Object> record) {

    }
}
