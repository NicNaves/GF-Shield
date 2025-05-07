package br.com.graspfs.ls.bf.enuns;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NormalClassEnum {

    NORMAL(0),FLOODING(1),PASSWORD_CRACKIN(2);

    private final int enumIdentifier;

}
