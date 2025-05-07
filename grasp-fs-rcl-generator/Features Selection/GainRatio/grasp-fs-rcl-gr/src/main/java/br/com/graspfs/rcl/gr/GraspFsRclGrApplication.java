package br.com.graspfs.rcl.gr;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.gr.service.GainRationService;
import br.com.graspfs.rcl.gr.util.SelectionFeaturesUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class GraspFsRclGrApplication {
	public static void main(String[] args) {
		SpringApplication.run(GraspFsRclGrApplication.class, args);
	}
}