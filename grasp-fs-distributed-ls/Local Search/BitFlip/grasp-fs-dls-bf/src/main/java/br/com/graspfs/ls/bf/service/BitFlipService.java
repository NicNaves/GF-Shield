package br.com.graspfs.ls.bf.service;

import br.com.graspfs.ls.bf.dto.DataSolution;
import br.com.graspfs.ls.bf.enuns.LocalSearch;
import br.com.graspfs.ls.bf.machinelearning.MachineLearning;
import br.com.graspfs.ls.bf.producer.KafkaSolutionsProducer;
import br.com.graspfs.ls.bf.util.MachineLearningUtils;
import br.com.graspfs.ls.bf.util.PrintSolution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

@Component
@Slf4j
public class BitFlipService {

    @Autowired
    private KafkaSolutionsProducer kafkaSolutionsProducer;

    @Value("${bitflip.max.iterations:100}")
    private int maxIterations;

    @Value("${bitflip.metrics.file:/metrics/BIT-FLIP_METRICS.csv}")
    private String metricsFileName;

    /**
     * Executa a busca local BitFlip sobre uma solução recebida.
     */
    public void doBitFlip(DataSolution data) throws Exception {
        data.setLocalSearch(LocalSearch.BIT_FLIP);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metricsFileName, true))) {
            escreverCabecalhoCSV(writer);
            DataSolution bestSolution = flipFeatures(data, writer);

            // Atualiza dados da melhor solução
            bestSolution.setIterationLocalSearch(data.getIterationLocalSearch() + 1);
            bestSolution.setNeighborhood(data.getNeighborhood());
            bestSolution.setIterationNeighborhood(data.getIterationNeighborhood());
            bestSolution.setSeedId(data.getSeedId());
            bestSolution.setLocalSearch(LocalSearch.BIT_FLIP);

            log.info("✅ Melhor solução: F1={} Tempo={} Features={} Iteração={}",
                    bestSolution.getF1Score(), bestSolution.getRunnigTime(),
                    bestSolution.getSolutionFeatures(), bestSolution.getIterationLocalSearch());

            kafkaSolutionsProducer.send(bestSolution);
        }
    }

    private void escreverCabecalhoCSV(BufferedWriter writer) throws IOException {
        writer.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
        writer.newLine();
    }

    private DataSolution flipFeatures(DataSolution solution, BufferedWriter writer) throws Exception {
        Random random = new Random();
        int i = 0;

        Instances trainingDataset = MachineLearningUtils.lerDataset(
                new FileInputStream("/datasets/" + solution.getTrainingFileName()));
        Instances testingDataset = MachineLearningUtils.lerDataset(
                new FileInputStream("/datasets/" + solution.getTestingFileName()));

        AbstractClassifier classifier = getClassifier(solution.getClassfier());
        DataSolution bestSolution = updateSolution(solution);

        while (i < maxIterations) {
            int valueIndex = random.nextInt(solution.getRclfeatures().size());
            int positionReplace = random.nextInt(solution.getSolutionFeatures().size());

            // Executa bit flip
            solution.getSolutionFeatures().add(solution.getRclfeatures().remove(valueIndex));
            solution.getRclfeatures().add(solution.getSolutionFeatures().remove(positionReplace));
            solution.setIterationLocalSearch(solution.getIterationLocalSearch() + 1);

            float f1 = MachineLearning.evaluateSolution(
                    new ArrayList<>(solution.getSolutionFeatures()),
                    new Instances(trainingDataset),
                    new Instances(testingDataset),
                    classifier
            );

            solution.setF1Score(f1);
            solution.setRunnigTime(System.currentTimeMillis());

            log.info("🌀 Iteração {}: F1={} Features={} ", i, f1, solution.getSolutionFeatures());
            PrintSolution.logSolution(solution);
            escreverLinhaCSV(writer, solution);

            if (f1 > bestSolution.getF1Score()) {
                bestSolution = updateSolution(solution);
            }

            i++;
        }

        return bestSolution;
    }

    private void escreverLinhaCSV(BufferedWriter writer, DataSolution s) throws IOException {
        writer.write(String.join(";",
                s.getSolutionFeatures().toString(),
                String.valueOf(s.getF1Score()),
                String.valueOf(s.getNeighborhood()),
                String.valueOf(s.getIterationNeighborhood()),
                String.valueOf(s.getLocalSearch()),
                String.valueOf(s.getIterationLocalSearch()),
                String.valueOf(s.getRunnigTime())
        ));
        writer.newLine();
    }

    private DataSolution updateSolution(DataSolution s) {
        return DataSolution.builder()
                .seedId(s.getSeedId())
                .rclfeatures(new ArrayList<>(s.getRclfeatures()))
                .solutionFeatures(new ArrayList<>(s.getSolutionFeatures()))
                .neighborhood(s.getNeighborhood())
                .iterationNeighborhood(s.getIterationNeighborhood())
                .classfier(s.getClassfier())
                .trainingFileName(s.getTrainingFileName())
                .testingFileName(s.getTestingFileName())
                .localSearch(s.getLocalSearch())
                .f1Score(s.getF1Score())
                .runnigTime(s.getRunnigTime())
                .iterationLocalSearch(s.getIterationLocalSearch())
                .build();
    }

    private AbstractClassifier getClassifier(String name) {
        return switch (name.toUpperCase()) {
            case "J48" -> new J48();
            case "NB", "NAIVEBAYES" -> new NaiveBayes();
            case "RF", "RANDOMFOREST" -> new RandomForest();
            default -> throw new IllegalArgumentException("Classificador não suportado: " + name);
        };
    }
}
