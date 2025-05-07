package br.com.graspfs.dlsrvnd.consumer;

import br.com.graspfs.dlsrvnd.dto.DataSolution;
import br.com.graspfs.dlsrvnd.service.RvndService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaInitialSolutionConsumer {

    private final RvndService rvndService;

    @Value("${rvnd.iterations:10}")
    private int maxIterations;

    @KafkaListener(
        topics = "INITIAL_SOLUTION_TOPIC",
        groupId = "RVND",
        containerFactory = "jsonKafkaListenerContainer"
    )
    public void consume(ConsumerRecord<String, DataSolution> record) {
        DataSolution data = record.value();

        if (data == null) {
            log.warn("🚫 Mensagem nula recebida do tópico INITIAL_SOLUTION_TOPIC.");
            return;
        }

        log.info("📥 Mensagem recebida: seedId={}, F1={}, Features={}",
                data.getSeedId(), data.getF1Score(), data.getSolutionFeatures());

        for (int i = 0; i < maxIterations; i++) {
            try {
                log.info("🔁 Iteração RVND {}/{}", i + 1, maxIterations);
                rvndService.doRvnd(data);
            } catch (Exception ex) {
                log.error("❌ Erro durante execução da iteração {}: {}", i + 1, ex.getMessage(), ex);
                throw ex;
            }
        }

        log.info("✅ RVND concluído para seedId={}", data.getSeedId());
    }
}
