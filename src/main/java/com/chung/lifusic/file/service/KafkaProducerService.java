package com.chung.lifusic.file.service;

import com.chung.lifusic.file.dto.FileCreateResponseDto;
import com.chung.lifusic.file.dto.FileDeleteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceCreateFileComplete(FileCreateResponseDto fileCreateResponse) {
        final String TOPIC = "CREATE_FILE_COMPLETE";
        produce(TOPIC, fileCreateResponse);
    }

    public void produceDeleteFileComplete(FileDeleteResponseDto fileDeleteResponse) {
        final String TOPIC = "DELETE_FILE_COMPLETE";
        produce(TOPIC, fileDeleteResponse);
    }

    private void produce(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }
}
