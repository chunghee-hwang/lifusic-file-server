package com.chung.lifusic.file.service;

import com.chung.lifusic.file.dto.FileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceCreateFileComplete(FileResponseDto fileCreateResponse) {
        final String TOPIC = "CREATE_FILE_COMPLETE";
        produce(TOPIC, fileCreateResponse);
    }

    public void produceDeleteFileComplete(FileResponseDto fileDeleteRequest) {
        final String TOPIC = "DELETE_FILE_COMPLETE";
        produce(TOPIC, fileDeleteRequest);
    }

    private void produce(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
}
