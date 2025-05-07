package br.com.graspfsdlsvnd.service;

import br.com.graspfsdlsvnd.dto.DataSolution;
import br.com.graspfsdlsvnd.enuns.LocalSearch;
import br.com.graspfsdlsvnd.producer.KafkaBitFlipProducer;
import br.com.graspfsdlsvnd.producer.KafkaInitialSolutionProducer;
import br.com.graspfsdlsvnd.producer.KafkaIwssProducer;
import br.com.graspfsdlsvnd.producer.KafkaIwssrProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VndService {

    private final KafkaBitFlipProducer bitFlipProducer;
    private final KafkaIwssProducer kafkaIwssProducer;
    private final KafkaIwssrProducer kafkaIwssrProducer;
    private final KafkaInitialSolutionProducer kafkaInitialSolutionProducer;

    /**
     * Inicia o processo de VND com a vizinhança definida.
     */
    public void doVnd(DataSolution data, LocalSearch localSearch) {
        data.setNeighborhood("vnd");
        data.setIterationNeighborhood(data.getIterationNeighborhood() + 1);

        log.info("🔁 [{}] Enviando para {}", localSearch.getEnumIdentifier(), localSearch.name());

        switch (localSearch) {
            case BIT_FLIP -> bitFlipProducer.send(data);
            case IWSS -> kafkaIwssProducer.send(data);
            case IWSSR -> kafkaIwssrProducer.send(data);
            default -> throw new IllegalStateException("🚫 Estratégia de busca inválida: " + localSearch);
        }
    }

    /**
     * Define se a solução recebida deve reiniciar o VND ou seguir para próxima vizinhança.
     */
    public DataSolution callNextService(DataSolution bestSolution, ConsumerRecord<String, DataSolution> record) {
        DataSolution incoming = record.value();
        log.info("📨 Solução recebida (F1: {}, Estratégia: {})", incoming.getF1Score(), incoming.getLocalSearch());

        if (incoming.getF1Score() > bestSolution.getF1Score()) {
            log.info("✅ Nova melhor solução encontrada. Reiniciando ciclo VND.");
            kafkaInitialSolutionProducer.send(incoming);
            return incoming;
        }

        switch (incoming.getLocalSearch()) {
            case BIT_FLIP -> {
                log.info("➡️ Próxima vizinhança: IWSS");
                doVnd(incoming, LocalSearch.IWSS);
            }
            case IWSS -> {
                log.info("➡️ Próxima vizinhança: IWSSR");
                doVnd(incoming, LocalSearch.IWSSR);
            }
            case IWSSR -> {
                log.info("➡️ Próxima vizinhança: BIT_FLIP");
                doVnd(incoming, LocalSearch.BIT_FLIP);
            }
            default -> throw new IllegalArgumentException("🚫 Estratégia desconhecida: " + incoming.getLocalSearch());
        }

        return bestSolution;
    }
}
