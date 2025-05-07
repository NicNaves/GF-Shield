package br.com.graspfs.producer;

import br.com.graspfs.producer.dto.DataSolution;
import br.com.graspfs.producer.producer.KafkaInitialSolutionProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaInitialSolutionProducer kafkaInitialSolutionProducer){
		System.out.println("PRODUZINDO MENSAGEM PARA TOPICO INICIAL");
		ArrayList<Integer> rclFeatures = new ArrayList<>();
		ArrayList<Integer> solutionFeatures = new ArrayList<>();
		for(int i = 1; i<=69; i++) {
			rclFeatures.add(i);
		}
		for(int i = 0; i < 5; i++){
			solutionFeatures.add(rclFeatures.remove(i));
		}
		var data = DataSolution.builder()
				.seedId(1L)
				.rclfeatures(rclFeatures)
				.solutionFeatures(solutionFeatures)
				.neighborhood("VND")
				.f1Score(0.78F)
				.runnigTime(84L)
				.iterationLocalSearch(0)
				.iterationNeighborhood(0)
				.classfier("j148")
				.localSearch("")
				.build();
		System.out.println("MENSSAGEM " + data);
		return args -> {
			kafkaInitialSolutionProducer.send(data);
		};
	}

}
