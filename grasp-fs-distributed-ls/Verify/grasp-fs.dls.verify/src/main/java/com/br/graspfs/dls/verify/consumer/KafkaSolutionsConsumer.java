package com.br.graspfs.dls.verify.consumer;

import com.br.graspfs.dls.verify.dto.DataSolution;
import com.br.graspfs.dls.verify.service.VerifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSolutionsConsumer {

    private final VerifyService verifyService;

    @KafkaListener(
        topics = "SOLUTIONS_TOPIC",
        groupId = "myGroup",
        containerFactory = "solutionListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, DataSolution> record) {
        log.info("Received Message {}", record.value());
        long start = System.currentTimeMillis();

        try {
            DataSolution data = record.value();
            if (data == null) {
                log.warn("Mensagem nula recebida do Kafka.");
                return;
            }

            verifyService.doVerify(data);
            log.info("Verificação concluída em {} ms", System.currentTimeMillis() - start);

        } catch (IllegalArgumentException ex) {
            log.error("Erro ao verificar solução: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
