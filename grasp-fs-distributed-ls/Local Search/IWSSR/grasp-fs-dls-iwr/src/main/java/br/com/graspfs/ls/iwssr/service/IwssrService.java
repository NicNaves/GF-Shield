package br.com.graspfs.ls.iwssr.service;

import br.com.graspfs.ls.iwssr.dto.DataSolution;
import br.com.graspfs.ls.iwssr.enuns.LocalSearch;
import br.com.graspfs.ls.iwssr.machinelearning.MachineLearning;
import br.com.graspfs.ls.iwssr.producer.KafkaSolutionsProducer;
import br.com.graspfs.ls.iwssr.util.MachineLearningUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

@Service
@Slf4j
public class IwssrService {

    @Autowired
    private KafkaSolutionsProducer kafkaSolutionsProducer;

    @Value("${datasets.base.path:/datasets/}")
    private String datasetsBasePath;

    @Value("${iwssr.metrics.file:/metrics/IWSSR_METRICS.csv}")
    private String metricsFileName;

    private BufferedWriter br;
    private boolean firstTime = true;

    public void doIwssr(DataSolution seed) throws Exception {
        DataSolution data = updateSolution(seed);
        data.setLocalSearch(LocalSearch.IWSSR);
        data.setIterationLocalSearch(data.getIterationLocalSearch() + 1);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metricsFileName, true))) {
            this.br = writer;
            if (firstTime) {
                writer.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                writer.newLine();
                firstTime = false;
            }
            DataSolution bestSolution = incrementalWrapperSequencialSearch(data);
            bestSolution = updateSolution(resetDataSolution(seed, bestSolution));
            kafkaSolutionsProducer.send(bestSolution);
        }
    }

    public DataSolution incrementalWrapperSequencialSearch(DataSolution dataSolution) throws Exception {
        dataSolution.setIterationLocalSearch(dataSolution.getIterationLocalSearch() + 1);
        DataSolution bestSolution = updateSolution(dataSolution);

        DataSolution localSolutionAdd = updateSolution(dataSolution);
        DataSolution localSolutionReplace = updateSolution(dataSolution);

        int n = localSolutionAdd.getRclfeatures().size();

        for (int i = 0; i < n; i++) {
            localSolutionAdd.setIterationLocalSearch(i);
            localSolutionAdd = updateSolution(addMovement(localSolutionAdd));
            localSolutionReplace = updateSolution(replaceMovement(localSolutionAdd));

            if (localSolutionReplace.getF1Score() > bestSolution.getF1Score()) {
                bestSolution = updateSolution(localSolutionReplace);
            }
        }

        log.info("BESTSOLUTION FINAL: {}", bestSolution.getF1Score());
        return bestSolution;
    }

    private DataSolution addMovement(DataSolution solution) throws Exception {
        solution.getSolutionFeatures().add(solution.getRclfeatures().remove(0));

        float f1Score = evaluateWithDataset(solution);
        solution.setF1Score(f1Score);
        solution.setRunnigTime(System.currentTimeMillis());

        logMetrics(solution);
        return solution;
    }

    private DataSolution replaceMovement(DataSolution solution) throws Exception {
        DataSolution bestReplace = updateSolution(solution);

        for (int i = 0; i < solution.getSolutionFeatures().size(); i++) {
            final long time = System.currentTimeMillis();

            DataSolution replaced = updateSolution(solution);
            replaced.getSolutionFeatures().remove(i);

            float f1 = evaluateWithDataset(replaced);
            replaced.setF1Score(f1);
            replaced.setRunnigTime(System.currentTimeMillis() - time);

            logMetrics(replaced);

            if (f1 > bestReplace.getF1Score()) {
                bestReplace = updateSolution(replaced);
                log.info("BESTSOLUTION : {} solution: {}", f1, bestReplace.getSolutionFeatures());
            }
        }

        return bestReplace;
    }

    private float evaluateWithDataset(DataSolution solution) throws Exception {
        Instances training = MachineLearningUtils.lerDataset(
                new FileInputStream(datasetsBasePath + solution.getTrainingFileName())
        );
        Instances testing = MachineLearningUtils.lerDataset(
                new FileInputStream(datasetsBasePath + solution.getTestingFileName())
        );

        AbstractClassifier classifier = getClassifier(solution.getClassfier());

        return MachineLearning.evaluateSolution(
                new ArrayList<>(solution.getSolutionFeatures()),
                new Instances(training),
                new Instances(testing),
                classifier
        );
    }

    public DataSolution resetDataSolution(DataSolution seed, DataSolution data) {
        int k = seed.getRclfeatures().size() + seed.getSolutionFeatures().size();
        ArrayList<Integer> novaRcl = new ArrayList<>();
        for (int i = 1; i <= k; i++) {
            if (!data.getSolutionFeatures().contains(i)) {
                novaRcl.add(i);
            }
        }
        data.setRclfeatures(novaRcl);
        return data;
    }

    private void logMetrics(DataSolution solution) throws Exception {
        br.write(String.join(";",
                solution.getSolutionFeatures().toString(),
                String.valueOf(solution.getF1Score()),
                String.valueOf(solution.getNeighborhood()),
                String.valueOf(solution.getIterationNeighborhood()),
                solution.getLocalSearch().name(),
                String.valueOf(solution.getIterationLocalSearch()),
                String.valueOf(solution.getRunnigTime())
        ));
        br.newLine();
    }

    private static DataSolution updateSolution(DataSolution solution) {
        return DataSolution.builder()
                .seedId(solution.getSeedId())
                .rclfeatures(new ArrayList<>(solution.getRclfeatures()))
                .solutionFeatures(new ArrayList<>(solution.getSolutionFeatures()))
                .iterationNeighborhood(solution.getIterationNeighborhood())
                .classfier(solution.getClassfier())
                .trainingFileName(solution.getTrainingFileName())
                .testingFileName(solution.getTestingFileName())
                .neighborhood(solution.getNeighborhood())
                .f1Score(solution.getF1Score())
                .runnigTime(solution.getRunnigTime())
                .iterationLocalSearch(solution.getIterationLocalSearch())
                .localSearch(solution.getLocalSearch())
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
