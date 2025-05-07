package com.br.graspfs.dls.verify.producer;

import com.br.graspfs.dls.verify.dto.DataSolution;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class KafkaSolutionsProducer {

    private static final String TOPIC = "BEST_SOLUTION_TOPIC";
    private final Logger logg = LoggerFactory.getLogger(KafkaSolutionsProducer.class);
    private final KafkaTemplate<String, DataSolution> kafkaTemplate;

    public KafkaSolutionsProducer(KafkaTemplate<String, DataSolution> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(DataSolution data) {
        kafkaTemplate.send(TOPIC, data).addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(org.springframework.kafka.support.SendResult<String, DataSolution> result) {
                logg.info("✔️ Message sent successfully to topic [{}] with seedId [{}]: {}", 
                        TOPIC, data.getSeedId(), data);
            }

            @Override
            public void onFailure(Throwable ex) {
                logg.error("❌ Failed to send message to topic [{}] with seedId [{}]: {}", 
                        TOPIC, data.getSeedId(), ex.getMessage(), ex);
            }
        });
    }
}
