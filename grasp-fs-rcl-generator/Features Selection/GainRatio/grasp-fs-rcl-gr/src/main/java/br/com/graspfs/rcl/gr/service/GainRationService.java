package br.com.graspfs.rcl.gr.service;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.dto.FeatureAvaliada;
import br.com.graspfs.rcl.gr.machinelearning.MachineLearning;
import br.com.graspfs.rcl.gr.util.SelectionFeaturesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Random;

@Service
public class GainRationService {

    private final Logger logger = LoggerFactory.getLogger(GainRationService.class);

    /**
     * Executa o ranking das features com base no GainRatio e define a RCL (Restricted Candidate List).
     */
    public void rankFeatures(DataSolution solution, Instances trainingDataset, int rclCutoff) throws Exception {
        try {
            ArrayList<FeatureAvaliada> allFeatures = new ArrayList<>();
            for (int i = 0; i < trainingDataset.numAttributes(); i++) {
                double grRatio = SelectionFeaturesUtils.calcularaGainRatio(trainingDataset, i);
                allFeatures.add(new FeatureAvaliada(grRatio, i + 1));
            }

            // Ordenar do maior para o menor
            allFeatures.sort((f1, f2) -> Double.compare(f2.getValorFeature(), f1.getValorFeature()));

            ArrayList<Integer> rclFeatures = new ArrayList<>();
            for (int i = 0; i < Math.min(rclCutoff, allFeatures.size()); i++) {
                rclFeatures.add(allFeatures.get(i).getIndiceFeature());
            }

            solution.setRclfeatures(rclFeatures);
            logger.info("RCL features definidas: {}", rclFeatures);

        } catch (RuntimeException ex) {
            logger.error("Erro ao calcular GainRatio: {}", ex.getMessage());
            throw new Exception("Erro ao calcular o ranking das features com GainRatio.");
        }
    }

    /**
     * Gera a solução inicial com base no dataset de treino, no valor de corte (RCL) e no classificador.
     */
    public DataSolution doGainRation(Instances trainingDataset, int rclCutoff, AbstractClassifier classifier, String trainingFileName, String testingFileName) throws Exception {
        String classifierName = classifier.getClass().getSimpleName();// pega nome do classificador
        DataSolution initialSolution = SelectionFeaturesUtils.createData(classifierName, trainingFileName, testingFileName); // criação da estrutura da solução
        rankFeatures(initialSolution, trainingDataset, rclCutoff); // calcula a RCL
        return initialSolution; // a avaliação será feita após a geração das soluções
    }

    /**
     * Gera uma nova solução a partir da RCL e avalia com machine learning.
     */
    public DataSolution GenerationSolutions(DataSolution rcl, int cutoff, BufferedWriter writer,
                                            Instances trainingDataset, Instances testingDataset,
                                            AbstractClassifier classifier) throws Exception {
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        ArrayList<Integer> rclFeatures = new ArrayList<>(rcl.getRclfeatures());
        ArrayList<Integer> solutionFeatures = new ArrayList<>();

        // Gera uma solução aleatória de tamanho "cutoff"
        for (int i = 0; i < cutoff && !rclFeatures.isEmpty(); i++) {
            int index = random.nextInt(rclFeatures.size());
            solutionFeatures.add(rclFeatures.remove(index));
        }

        rcl.setSolutionFeatures(solutionFeatures);

        // Avalia a solução com os datasets fornecidos e o classificador escolhido
        float f1Score = MachineLearning.evaluateSolution(
                new ArrayList<>(solutionFeatures),            // evita mutações
                new Instances(trainingDataset),               // cópia profunda do dataset
                new Instances(testingDataset),                // idem
                classifier                                     // classificador escolhido dinamicamente
        );

        rcl.setF1Score(f1Score);
        rcl.setRunnigTime(System.currentTimeMillis() - startTime);

        logger.info("Solução gerada - RCL: {} | Solução: {} | F1: {}", rcl.getRclfeatures(), solutionFeatures, f1Score);

        // Escreve a métrica no arquivo CSV
        writer.write(String.join(";",
            solutionFeatures.toString(),
            String.valueOf(f1Score),
            String.valueOf(rcl.getNeighborhood()),
            String.valueOf(rcl.getIterationNeighborhood()),
            String.valueOf(rcl.getLocalSearch()),
            String.valueOf(rcl.getIterationLocalSearch()),
            String.valueOf(rcl.getRunnigTime()),
            rcl.getClassfier(),
            rcl.getTrainingFileName(),
            rcl.getTestingFileName()
        ));
        writer.newLine();

        return rcl;
    }
}
