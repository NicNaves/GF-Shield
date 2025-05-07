package com.br.graspfs.dls.verify.consumer;

import com.br.graspfs.dls.verify.dto.DataSolution;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KafkaBestSolutionConsumer {

    private final BlockingQueue<DataSolution> responseQueue = new ArrayBlockingQueue<>(1);

    @KafkaListener(
        topics = "BEST_SOLUTION_TOPIC",
        groupId = "myGroup",
        containerFactory = "bestSolutionListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, DataSolution> record) {
        DataSolution data = record.value();
        if (data == null) {
            log.warn("⚠️ Mensagem nula recebida no tópico BEST_SOLUTION_TOPIC.");
            return;
        }

        log.info("📩 Received BEST_SOLUTION: {}", data);
        responseQueue.offer(data);
    }

    /**
     * Aguarda até X segundos (configurado via verify.timeout.seconds) por uma resposta.
     *
     * @param timeoutSeconds tempo máximo para aguardar
     * @return a melhor solução recebida ou null se o tempo esgotar
     * @throws InterruptedException se a thread for interrompida
     */
    public DataSolution waitForBestSolution(int timeoutSeconds) throws InterruptedException {
        DataSolution result = responseQueue.poll(timeoutSeconds, TimeUnit.SECONDS);
        if (result == null) {
            log.warn("⏱ Timeout: nenhuma solução recebida em {} segundos.", timeoutSeconds);
        }
        return result;
    }
}
