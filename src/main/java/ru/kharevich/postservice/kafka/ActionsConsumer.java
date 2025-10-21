package ru.kharevich.postservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.kharevich.postservice.dto.response.ActivityResponse;
import ru.kharevich.postservice.service.PostService;

@Service
@RequiredArgsConstructor
public class ActionsConsumer {

    private final PostService postService;

    @KafkaListener(topics = "activity-topic",groupId = "activity-group")
    public void consumeSupplyRequests(ActivityResponse message) {
        postService.updateActivity(message);
        System.out.println("got message" + message);
    }

}