package graspfs.rcl.rf.service;

import graspfs.rcl.rf.dto.DataSolution;
import graspfs.rcl.rf.dto.EvaluationResult;
import graspfs.rcl.rf.dto.FeatureAvaliada;
import graspfs.rcl.rf.machinelearning.MachineLearning;
import graspfs.rcl.rf.util.SelectionFeaturesUtils;
import graspfs.rcl.rf.util.SystemMetricsUtils.MetricsCollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

@Service
public class RelieFService {

    private final Logger logger = LoggerFactory.getLogger(RelieFService.class);

    /**
     * Executa o ranking das features com base no RelieFService e define a RCL (Restricted Candidate List).
     */
    public void rankFeatures(DataSolution solution, Instances trainingDataset, int rclCutoff) throws Exception {
        try {
            ArrayList<FeatureAvaliada> allFeatures = new ArrayList<>();
            for (int i = 0; i < trainingDataset.numAttributes(); i++) {
                double rfRatio = SelectionFeaturesUtils.calcularaRF(trainingDataset, i);
                allFeatures.add(new FeatureAvaliada(rfRatio, i + 1));
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
            logger.error("Erro ao calcular rfRatio: {}", ex.getMessage());
            throw new Exception("Erro ao calcular o ranking das features com rfRatio.");
        }
    }

    /**
     * Gera a solução inicial com base no dataset de treino, no valor de corte (RCL) e no classificador.
     */
    public DataSolution doRelief(Instances trainingDataset, int rclCutoff, AbstractClassifier classifier, String trainingFileName, String testingFileName) throws Exception {
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

        // ⏱️ Inicia coleta de métricas em paralelo
        MetricsCollector collector = new MetricsCollector();
        Thread monitor = new Thread(collector);
        monitor.start();

        // Avalia a solução com os datasets fornecidos e o classificador escolhido
        EvaluationResult result = MachineLearning.evaluateSolution(
                new ArrayList<>(solutionFeatures),            // evita mutações
                new Instances(trainingDataset),               // cópia profunda do dataset
                new Instances(testingDataset),                // idem
                classifier                                     // classificador escolhido dinamicamente
        );


         // 🚫 Para a coleta
         collector.stop();
         monitor.join();

        rcl.setF1Score(result.getF1Score());
        rcl.setAccuracy(result.getAccuracy());
        rcl.setPrecision(result.getPrecision());
        rcl.setRecall(result.getRecall());

        rcl.setRunnigTime(System.currentTimeMillis() - startTime);
       

        logger.info("Solução gerada - RCL: {} | Solução: {} | F1: {}", rcl.getRclfeatures(), solutionFeatures, rcl.getF1Score());
        
        float avgCpu = collector.getAvgCpu();
        float avgMemory = collector.getAvgMemory();
        float avgMemoryPercent = collector.getAvgMemoryPercent();
        String f1Formatted = String.format(Locale.US, "%.4f", rcl.getF1Score());
        String accFormatted = String.format(Locale.US, "%.4f", rcl.getAccuracy());
        String precFormatted = String.format(Locale.US, "%.4f", rcl.getPrecision());
        String recFormatted = String.format(Locale.US, "%.4f", rcl.getRecall());
        String timeFormatted = String.format(Locale.US, "%d", rcl.getRunnigTime());
        String cpuFormatted = String.format(Locale.US, "%.4f", avgCpu);
        String memFormatted = String.format(Locale.US, "%.4f", avgMemory);
        String memPercentFormatted = String.format(Locale.US, "%.4f", avgMemoryPercent);

        // Escreve a métrica no arquivo CSV com precisão aprimorada
        writer.write(String.join(";",
            solutionFeatures.toString(),
            f1Formatted,
            accFormatted,
            precFormatted,
            recFormatted,
            String.valueOf(rcl.getNeighborhood()),
            String.valueOf(rcl.getIterationNeighborhood()),
            String.valueOf(rcl.getLocalSearch()),
            String.valueOf(rcl.getIterationLocalSearch()),
            timeFormatted,
            cpuFormatted,
            memFormatted,
            memPercentFormatted,
            rcl.getClassfier(),
            rcl.getTrainingFileName(),
            rcl.getTestingFileName()
        ));
        writer.newLine();

        return rcl;
    }
}
