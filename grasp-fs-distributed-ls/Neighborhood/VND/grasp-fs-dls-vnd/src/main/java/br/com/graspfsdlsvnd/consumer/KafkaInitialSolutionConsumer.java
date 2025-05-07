package br.com.graspfsdlsvnd.consumer;

import br.com.graspfsdlsvnd.dto.DataSolution;
import br.com.graspfsdlsvnd.enuns.LocalSearch;
import br.com.graspfsdlsvnd.service.VndService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaInitialSolutionConsumer {

    private final VndService vndService;

    @KafkaListener(
        topics = "INITIAL_SOLUTION_TOPIC",
        groupId = "VND",
        containerFactory = "jsonKafkaListenerContainer"
    )
    public void consume(ConsumerRecord<String, DataSolution> record) {
        DataSolution data = record.value();

        if (data == null) {
            log.warn("📭 Mensagem nula recebida no tópico INITIAL_SOLUTION_TOPIC.");
            return;
        }

        log.info("📥 Recebida solução inicial (seedId={}, features={}, search=VND)", 
                 data.getSeedId(), data.getSolutionFeatures());

        try {
            vndService.doVnd(data, LocalSearch.BIT_FLIP);
        } catch (Exception ex) {
            log.error("❌ Erro ao processar solução inicial (seedId={}): {}", 
                      data.getSeedId(), ex.getMessage(), ex);
        }
    }
}
