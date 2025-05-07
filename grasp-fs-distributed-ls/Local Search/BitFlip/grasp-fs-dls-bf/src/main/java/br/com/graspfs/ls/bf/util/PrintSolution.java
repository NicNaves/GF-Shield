package br.com.graspfs.ls.bf.util;

import br.com.graspfs.ls.bf.dto.DataSolution;

public class PrintSolution {

    public static void logSolution(DataSolution dataSolution){

        System.out.println("###############################################");
        System.out.println("Logando solução : ");
        System.out.println("          Solution Features:          " +dataSolution.getSolutionFeatures());
        System.out.println("          RCL Features:               " +dataSolution.getRclfeatures());
        System.out.println("          F1 Score :                   " +dataSolution.getF1Score());
        System.out.println("###############################################");

    }

}
