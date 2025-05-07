package br.com.graspfsdlsvnd.controller;

import br.com.graspfsdlsvnd.dto.DataSolution;
import br.com.graspfsdlsvnd.producer.KafkaBitFlipProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/bit-flip")
@RequiredArgsConstructor
public class BitFlipControler {

    private final KafkaBitFlipProducer bitFlipProducer;


//    @PostMapping
//    public ResponseEntity<DataSolution> createMessage(){
//        var data = DataSolution.builder()
//                .seedId(1L)
//                .rclfeatures(new ArrayList<>(List.of(6,
//                        7,
//                        8,
//                        9,
//                        10,
//                        11,
//                        12,
//                        13,
//                        14,
//                        15,
//                        16,
//                        17,
//                        18,
//                        19,
//                        20)))
//                .solutionFeatures(new ArrayList<>(List.of(new Integer[]{6,
//                        1,
//                        2,
//                        3,
//                        4,
//                        5})))
//                .neighborhood("VND")
//                .f1Score(0.78F)
//                .runnigTime(84L)
//                .iterationLocalSearch(0)
//                .build();
//        bitFlipProducer.send(data);
//        return ResponseEntity.status(HttpStatus.OK).body(data);
//    }

}
