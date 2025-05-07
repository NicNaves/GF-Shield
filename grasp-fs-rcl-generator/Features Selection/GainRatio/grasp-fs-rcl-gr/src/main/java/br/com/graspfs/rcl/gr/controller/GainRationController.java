package br.com.graspfs.rcl.gr.controller;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.gr.service.GainRationService;
import br.com.graspfs.rcl.gr.util.MachineLearningUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.io.*;
import java.util.Map;

@RestController
@RequestMapping("/gr")
@RequiredArgsConstructor
public class GainRationController {

    private static final Logger logger = LoggerFactory.getLogger(GainRationController.class);
    private static final String METRICS_FILE_NAME = "/metrics/GainRation_METRICS.csv";
    private static final String DATASET_BASE_PATH = "/datasets/";

    private final KafkaSolutionsProducer gainRationProducer;
    private final GainRationService gainRationService;

    private boolean isFirstTime = true;

    @PostMapping
    public ResponseEntity<Map<String, String>> processGainRation(
        @RequestParam("maxGenerations") int maxGenerations,
        @RequestParam("rclCutoff") int rclCutoff,
        @RequestParam("sampleSize") int sampleSize,
        @RequestParam("datasetTrainingName") String trainingFileName,
        @RequestParam("datasetTestingName") String testingFileName,
        @RequestParam(value = "classifier", defaultValue = "J48") String classifierName
    ) {
        try {
            // Carrega os datasets a partir do volume compartilhado
            Instances trainingDataset = MachineLearningUtils.lerDataset(
                    new FileInputStream(DATASET_BASE_PATH + trainingFileName)
            );
            Instances testingDataset = MachineLearningUtils.lerDataset(
                    new FileInputStream(DATASET_BASE_PATH + testingFileName)
            );

            // Cria o classificador com base no nome passado por parâmetro
            AbstractClassifier classifier = getClassifier(classifierName);

            // Cria solução inicial
            DataSolution dataSolution = gainRationService.doGainRation(
                    trainingDataset, rclCutoff, classifier, trainingFileName, testingFileName
            );

            // Abre arquivo CSV para salvar métricas
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(METRICS_FILE_NAME, true))) {
                if (isFirstTime) {
                    writer.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime;classifier;trainingFileName;testingFileName");
                    writer.newLine();
                    isFirstTime = false;
                }

                // Gera soluções e envia para o Kafka
                for (int generation = 0; generation < maxGenerations; generation++) {
                    gainRationService.GenerationSolutions(dataSolution, sampleSize, writer, trainingDataset, testingDataset, classifier);
                    gainRationProducer.send(dataSolution);
                    logger.info("Generation {} processada e enviada com sucesso.", generation + 1);
                }
            }

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of("message", "Em processamento!"));

        } catch (FileNotFoundException fnfe) {
            logger.error("Arquivo não encontrado: {}", fnfe.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Arquivo não encontrado."));
        } catch (IOException ioException) {
            logger.error("Erro ao processar arquivos do dataset", ioException);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao processar datasets."));
        } catch (Exception ex) {
            logger.error("Erro inesperado durante o processamento do GainRation", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro inesperado."));
        }
}

    // Método auxiliar para criar uma instância do classificador com base no nome recebido
    private AbstractClassifier getClassifier(String name) {
        return switch (name.toUpperCase()) {
            case "J48" -> new J48(); // árvore de decisão (default)
            case "NB", "NAIVEBAYES" -> new NaiveBayes(); // classificador bayesiano
            case "RF", "RANDOMFOREST" -> new RandomForest(); // floresta aleatória
            default -> throw new IllegalArgumentException("Classificador não suportado: " + name);
        };
    }
}
