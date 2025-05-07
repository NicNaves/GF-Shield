package com.br.graspfs.dls.verify.service;

import com.br.graspfs.dls.verify.consumer.KafkaBestSolutionConsumer;
import com.br.graspfs.dls.verify.dto.DataSolution;
import com.br.graspfs.dls.verify.producer.KafkaSolutionsProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyService {

    private final KafkaSolutionsProducer kafkaSolutionsProducer;
    private final KafkaBestSolutionConsumer kafkaBestSolutionConsumer;

    @Value("${verify.timeout.seconds:10}") // fallback para 10s se não definido
    private int timeoutInSeconds;

    public void doVerify(DataSolution data) {
        log.info("🔁 Enviando solução para verificação (seedId={} | localSearch={})",
                data.getSeedId(), data.getLocalSearch());

        kafkaSolutionsProducer.send(data);

        try {
            DataSolution result = kafkaBestSolutionConsumer.waitForBestSolution(timeoutInSeconds);

            if (result != null) {
                log.info("✅ Melhor solução recebida (seedId={}): F1 Score = {}, Features = {}",
                        result.getSeedId(), result.getF1Score(), result.getSolutionFeatures());
            } else {
                log.warn("⏱ Timeout após {}s aguardando a melhor solução (seedId={})",
                        timeoutInSeconds, data.getSeedId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupção ao aguardar resposta do Kafka (seedId={})", data.getSeedId(), e);
            throw new RuntimeException("Erro ao aguardar melhor solução do Kafka", e);
        }
    }
}
