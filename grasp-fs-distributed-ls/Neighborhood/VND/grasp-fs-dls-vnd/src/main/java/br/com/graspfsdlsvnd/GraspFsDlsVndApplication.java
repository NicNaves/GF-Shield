package br.com.graspfsdlsvnd;

import br.com.graspfsdlsvnd.dto.DataSolution;
import br.com.graspfsdlsvnd.producer.KafkaInitialSolutionProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class GraspFsDlsVndApplication {

    public static void main(String[] args) {
		SpringApplication.run(GraspFsDlsVndApplication.class, args);
    }
//    @Bean
//    CommandLineRunner commandLineRunner(KafkaInitialSolutionProducer kafkaInitialSolutionProducer){
//        System.out.println("PRODUZINDO MENSAGEM PARA TOPICO INICIAL");
//        ArrayList<Integer> rclFeatures = new ArrayList<>();
//        ArrayList<Integer> solutionFeatures = new ArrayList<>();
//        for(int i = 1; i<=69; i++) {
//           rclFeatures.add(i);
//        }
//        for(int i = 0; i < 5; i++){
//            solutionFeatures.add(rclFeatures.remove(i));
//        }
//        var data = DataSolution.builder()
//                .seedId(1L)
//                .rclfeatures(rclFeatures)
//                .solutionFeatures(solutionFeatures)
//                .neighborhood("VND")
//                .f1Score(0.78F)
//                .runnigTime(84L)
//                .iterationLocalSearch(0)
//                .build();
//        System.out.println("MENSSAGEM " + data);
//        return args -> {
//            kafkaInitialSolutionProducer.send(data);
//        };
//    }
}
