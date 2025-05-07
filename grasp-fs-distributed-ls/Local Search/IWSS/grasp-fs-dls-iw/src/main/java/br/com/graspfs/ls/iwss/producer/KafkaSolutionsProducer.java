package br.com.graspfs.ls.iwss.producer;

import br.com.graspfs.ls.iwss.dto.DataSolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaSolutionsProducer {

    private static final String TOPIC = "SOLUTIONS_TOPIC";
    private final KafkaTemplate<String, DataSolution> kafkaTemplate;

    public KafkaSolutionsProducer(KafkaTemplate<String, DataSolution> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(DataSolution data) {
        kafkaTemplate.send(TOPIC, data).addCallback(
            success -> {
                if (success != null) {
                    log.info("✅ Mensagem enviada com sucesso: {}", success.getProducerRecord().value());
                }
            },
            failure -> log.error("❌ Falha ao enviar mensagem: {}", failure.getMessage(), failure)
        );
    }
}
