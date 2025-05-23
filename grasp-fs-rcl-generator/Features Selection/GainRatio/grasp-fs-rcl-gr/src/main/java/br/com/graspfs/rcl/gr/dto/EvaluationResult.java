package br.com.graspfs.rcl.gr.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvaluationResult {
    private float f1Score;
    private float precision;
    private float recall;
    private float accuracy;
}
