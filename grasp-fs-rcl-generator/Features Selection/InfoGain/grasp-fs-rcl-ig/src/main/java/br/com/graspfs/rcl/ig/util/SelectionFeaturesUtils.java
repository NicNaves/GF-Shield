package br.com.graspfs.rcl.ig.util;

import br.com.graspfs.rcl.ig.dto.DataSolution;
import br.com.graspfs.rcl.ig.dto.FeatureAvaliada;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import weka.attributeSelection.GainRatioAttributeEval;



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

    public static double calcularaInfoGain(Instances instances, int featureIndice) throws Exception {
        InfoGainAttributeEval ase = new InfoGainAttributeEval();
        ase.buildEvaluator(instances);
        return ase.evaluateAttribute(featureIndice);
    }

}

