package br.com.graspfs.rcl.gr.dto;

public class FeatureAvaliada {
        public double valorFeature;
        public int indiceFeature;

        public FeatureAvaliada(double valorFeature, int indiceFeature) {
            this.valorFeature = valorFeature;
            this.indiceFeature = indiceFeature;
        }

        public double getValorFeature() {
            return valorFeature;
        }

        public int getIndiceFeature() {
            return indiceFeature;
        }
}
