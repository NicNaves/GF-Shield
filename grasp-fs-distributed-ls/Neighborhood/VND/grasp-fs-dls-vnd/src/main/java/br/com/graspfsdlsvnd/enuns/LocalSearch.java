package br.com.graspfsdlsvnd.enuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocalSearch {
    BIT_FLIP(1), IWSS(2), IWSSR(3) ;

    private final int enumIdentifier;
}
