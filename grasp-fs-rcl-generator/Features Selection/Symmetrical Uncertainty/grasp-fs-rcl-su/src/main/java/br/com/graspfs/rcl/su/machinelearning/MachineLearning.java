package br.com.graspfs.rcl.su.machinelearning;
import br.com.graspfs.rcl.su.util.MachineLearningUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

// https://github.com/sequincozes/TES
public class MachineLearning {

    public static double normalClass = 0; // isso aqui representa as classes com o valor N

    public static float evaluateSolution(
            ArrayList<Integer> features,
            Instances trainingDataset,
            Instances testingDataset,
            AbstractClassifier classificador // agora o classificador é recebido como parâmetro
    ) throws Exception {

        // Reduz os datasets com as features selecionadas
        trainingDataset = MachineLearningUtils.selecionaFeatures(trainingDataset, features);
        testingDataset = MachineLearningUtils.selecionaFeatures(testingDataset, features);

        AbstractClassifier classificadorTreinado = MachineLearningUtils.construir(trainingDataset, classificador); // treina o classificador recebido

        // Resultados
        float VP = 0; // quando o IDS diz que está acontecendo um ataque, e realmente está
        float VN = 0; // quando o IDS diz que NÃO está acontecendo um ataque, e realmente NÃO está
        float FP = 0; // quando o IDS diz que está acontecendo um ataque, PORÉM NÃO ESTÁ
        float FN = 0; // quando o IDS diz que NÃO está acontecendo um ataque, PORÉM ESTÁ!
        long beginNano = System.nanoTime();

        for (int i = 0; i < testingDataset.size(); i++) { //percorre cada uma das amostras de teste
            try {
                Instance testando = testingDataset.instance(i);
                double resultado = MachineLearningUtils.testarInstancia(classificadorTreinado, testando);
                double esperado = testando.classValue();
                if (resultado == esperado) { // já sabemos que o resultado é verdadeiro
                    if (resultado == normalClass) {
                        VN = VN + 1; // O IDS diz que NÃO está acontecendo um ataque, e realmente NÃO está
                    } else {
                        VP = VP + 1; // o IDS diz que está acontecendo um ataque, e realmente está
                    }
                } else { // sabemos que é um "falso"
                    if (resultado == normalClass) {
                        FN = FN + 1; // o IDS diz que NÃO está acontecendo um ataque, PORÉM ESTÁ!
                    } else {
                        FP = FP + 1; // o IDS diz que está acontecendo um ataque, PORÉM NÃO ESTÁ
                    }
                }

            } catch (ArrayIndexOutOfBoundsException a) {
                System.err.println("Erro: " + a.getLocalizedMessage());
                System.err.println("DICA: " + "Tem certeza que o número de classes está definido corretamente?");
                throw new RuntimeException("Erro de índice ao testar instância", a);
            } catch (Exception e) {
                System.err.println("Erro: " + e.getLocalizedMessage());
                throw new RuntimeException("Erro inesperado ao testar instância", e);
            }
        }

        long endNano = System.nanoTime();
        float totalNano = (endNano - beginNano) / 1000f; // converte para microssegundos

        float f1score = calculateF1Score(testingDataset, totalNano, VP, VN, FP, FN);

        //MachineLearningUtils.printResults(testingDataset, totalNano, VP, VN, FP, FN);

        return f1score;
    }

    public static float calculateF1Score(Instances datasetTestes, float totalNano, float VP, float VN, float FP, float FN) {
        float acuracia = (VP + VN) * 100 / (VP + VN + FP + FN); // quantos acertos o IDS teve
        float recall = (VP + FN) == 0 ? 0 : (VP * 100) / (VP + FN); // quantas vezes eu acertei dentre as vezes REALMENTE ESTAVA acontecendo um ataque
        float precision = (VP + FP) == 0 ? 0 : (VP * 100) / (VP + FP); // quantas vezes eu acertei dentre as vezes que eu DISSE que estava acontecendo
        float f1score = (recall + precision) == 0 ? 0 : 2 * (recall * precision) / (recall + precision);
        return f1score;
    }

}