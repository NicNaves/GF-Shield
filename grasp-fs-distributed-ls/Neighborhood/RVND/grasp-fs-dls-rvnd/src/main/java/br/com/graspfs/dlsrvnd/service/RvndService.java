package br.com.graspfs.dlsrvnd.service;

import br.com.graspfs.dlsrvnd.dto.DataSolution;
import br.com.graspfs.dlsrvnd.enuns.LocalSearch;
import br.com.graspfs.dlsrvnd.producer.KafkaBitFlipProducer;
import br.com.graspfs.dlsrvnd.producer.KafkaInitialSolutionProducer;
import br.com.graspfs.dlsrvnd.producer.KafkaIwssProducer;
import br.com.graspfs.dlsrvnd.producer.KafkaIwssrProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class RvndService {

    private final KafkaBitFlipProducer bitFlipProducer;
    private final KafkaIwssProducer kafkaIwssProducer;
    private final KafkaIwssrProducer kafkaIwssrProducer;
    private final KafkaInitialSolutionProducer kafkaInitialSolutionProducer;

    private final Random random = new Random();
    private final AtomicInteger iteration = new AtomicInteger(1);

    /**
     * Executa a lógica RVND com seleção aleatória da vizinhança.
     */
    public void doRvnd(DataSolution data) {
        data.setNeighborhood("rvnd");
        data.setIterationNeighborhood(iteration.getAndIncrement());

        LocalSearch selected = pickRandomNeighborhood();
        data.setLocalSearch(selected);

        log.info("🔀 RVND escolheu vizinhança: {}", selected);

        switch (selected) {
            case BIT_FLIP -> bitFlipProducer.send(data);
            case IWSS -> kafkaIwssProducer.send(data);
            case IWSSR -> kafkaIwssrProducer.send(data);
        }
    }

    /**
     * Seleciona aleatoriamente uma vizinhança local.
     */
    private LocalSearch pickRandomNeighborhood() {
        LocalSearch[] values = LocalSearch.values();
        return values[random.nextInt(values.length)];
    }
}
