package br.com.graspfs.ls.iwssr.consumer;

import br.com.graspfs.ls.iwssr.dto.DataSolution;
import br.com.graspfs.ls.iwssr.service.IwssrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaIwssrConsumer {

    private final IwssrService iwssrService;

    @KafkaListener(topics = "IWSSR_TOPIC", groupId = "myGroup")
    public void consume(ConsumerRecord<String, DataSolution> record) {
        DataSolution data = record.value();
        log.info("📥 Mensagem recebida do tópico IWSSR_TOPIC: {}", data);

        try {
            iwssrService.doIwssr(data);
            log.info("✅ Processamento IWSSR finalizado com sucesso para seedId={}", data.getSeedId());
        } catch (IllegalArgumentException ex) {
            log.error("⚠️ Erro de argumento ao processar mensagem: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception e) {
            log.error("❌ Erro inesperado durante o processamento da mensagem IWSSR", e);
            throw new RuntimeException("Erro ao processar IWSSR", e);
        }
    }
}
