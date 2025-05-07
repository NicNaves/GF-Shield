package br.com.graspfs.dlsrvnd.producer;

import br.com.graspfs.dlsrvnd.dto.DataSolution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaIwssrProducer {

    private static final String TOPIC = "IWSSR_TOPIC";
    private final KafkaTemplate<String, DataSolution> kafkaTemplate;

    public void send(DataSolution data) {
        kafkaTemplate.send(TOPIC, data).addCallback(
            success -> {
                var record = Optional.ofNullable(success)
                                     .map(s -> s.getProducerRecord().value())
                                     .orElse(null);
                log.info("✅ Mensagem enviada para [{}]: {}", TOPIC, record);
            },
            failure -> log.error("❌ Erro ao enviar para [{}]: {}", TOPIC, failure.getMessage())
        );
    }
}
