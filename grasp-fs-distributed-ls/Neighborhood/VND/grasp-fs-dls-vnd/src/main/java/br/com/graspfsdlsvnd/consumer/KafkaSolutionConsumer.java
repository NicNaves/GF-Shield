package br.com.graspfsdlsvnd.consumer;

import br.com.graspfsdlsvnd.dto.DataSolution;
import br.com.graspfsdlsvnd.service.VndService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaSolutionConsumer {

    private final VndService vndService;
    private static DataSolution bestSolution;

    @Value("${vnd.max.iterations:10}")
    private int maxIterations;

    @KafkaListener(topics = "SOLUTIONS_TOPIC", groupId = "VND")
    public void consume(ConsumerRecord<String, DataSolution> record) {
        DataSolution incoming = record.value();
        log.info("📥 Mensagem recebida do Kafka: {}", incoming);

        if (incoming.getIterationNeighborhood() < maxIterations) {
            try {
                if (bestSolution == null) {
                    bestSolution = incoming;
                    log.info("🟢 Primeira solução armazenada. Iniciando ciclo VND.");
                } else {
                    log.info("🔄 Comparando nova solução com a melhor até agora...");
                }

                bestSolution = vndService.callNextService(bestSolution, record);

                log.info("🏁 Melhor solução atual: F1 = {}, Features = {}",
                        bestSolution.getF1Score(), bestSolution.getSolutionFeatures());

            } catch (IllegalArgumentException ex) {
                log.error("❌ Erro ao processar solução recebida: {}", ex.getMessage(), ex);
                throw ex;
            }
        } else {
            log.warn("⏹ Iteração de vizinhança máxima ({}) atingida. Ignorando solução.", maxIterations);
        }
    }
}
