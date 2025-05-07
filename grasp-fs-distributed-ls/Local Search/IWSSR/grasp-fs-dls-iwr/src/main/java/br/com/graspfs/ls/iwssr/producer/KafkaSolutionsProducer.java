package br.com.graspfs.ls.iwssr.producer;

import br.com.graspfs.ls.iwssr.dto.DataSolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaSolutionsProducer {

    private final KafkaTemplate<String, DataSolution> kafkaTemplate;
    private final String topic;

    public KafkaSolutionsProducer(KafkaTemplate<String, DataSolution> kafkaTemplate,
                                   @Value("${kafka.topics.solutions:SOLUTIONS_TOPIC}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(DataSolution data) {
        kafkaTemplate.send(topic, data).addCallback(
            success -> {
                if (success != null) {
                    log.info("✅ Mensagem enviada com sucesso para [{}]: {}", topic, success.getProducerRecord().value());
                }
            },
            failure -> log.error("❌ Falha ao enviar mensagem para [{}]: {}", topic, failure.getMessage(), failure)
        );
    }
}
