package graspfs.rcl.rf.util;

import graspfs.rcl.rf.dto.DataSolution;
import weka.attributeSelection.ReliefFAttributeEval;
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


    public static double calcularaRF(Instances instances, int featureIndice) throws Exception {
        ReliefFAttributeEval ase = new ReliefFAttributeEval();
        ase.buildEvaluator(instances);
        return ase.evaluateAttribute(featureIndice);
    }

}
