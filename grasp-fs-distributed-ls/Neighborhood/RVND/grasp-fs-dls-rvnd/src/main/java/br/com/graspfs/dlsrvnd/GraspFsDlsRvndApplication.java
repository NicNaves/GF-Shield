package br.com.graspfs.dlsrvnd;

import br.com.graspfs.dlsrvnd.dto.DataSolution;
import br.com.graspfs.dlsrvnd.producer.KafkaInitialSolutionProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class GraspFsDlsRvndApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraspFsDlsRvndApplication.class, args);
	}
	/* @Bean
	 CommandLineRunner commandLineRunner(KafkaInitialSolutionProducer kafkaInitialSolutionProducer){
		System.out.println("PRODUZINDO MENSAGEM PARA TOPICO INICIAL");
		var data = DataSolution.builder()
				.seedId(1L)
				.rclfeatures(new ArrayList<>(List.of(6,
						7,
						8,
						9,
						10,
						11,
						12,
						13,
						14,
						15,
						16,
						17,
						18,
						19,
						20)))
				.solutionFeatures(new ArrayList<>(List.of(new Integer[]{6,
						1,
						2,
						3,
						4,
						5})))
				.neighborhood("VND")
				.f1Score(0.78F)
				.runnigTime(84L)
				.iterationLocalSearch(0)
				.build();
		System.out.println("MENSSAGEM " + data);
		return args -> {
			kafkaInitialSolutionProducer.send(data);
		};
	} */
}
