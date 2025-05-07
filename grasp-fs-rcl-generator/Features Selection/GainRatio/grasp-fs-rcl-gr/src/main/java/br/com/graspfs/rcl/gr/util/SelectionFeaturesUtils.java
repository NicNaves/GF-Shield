package br.com.graspfs.rcl.gr.util;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.dto.FeatureAvaliada;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SelectionFeaturesUtils {

    public static DataSolution createData(String classifier, String trainingFileName, String testingFileName) throws IOException {
        return DataSolution.builder()
                .seedId(UUID.randomUUID()) 
                .rclfeatures(new ArrayList<>())
                .solutionFeatures(new ArrayList<>())
                .classfier(classifier)
                .trainingFileName(trainingFileName)
                .testingFileName(testingFileName)
                .f1Score(0.0F)
                .runnigTime(0L)
                .iterationLocalSearch(0)
                .build();
    }

    public static double calcularaGainRatio(Instances instances, int featureIndice) throws Exception {
        GainRatioAttributeEval ase = new GainRatioAttributeEval();
        ase.buildEvaluator(instances);
        return ase.evaluateAttribute(featureIndice);
    }

}
