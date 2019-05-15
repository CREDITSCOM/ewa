package com.credits.service;

import com.credits.general.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class BackwardCompatibilityService {

    public static final List<Class<?>> allVersionsSmartContractClass;
    public static final List<Class<?>> allVersionsBasicStandardClass;

    static {
        allVersionsSmartContractClass = new ArrayList<>();
        allVersionsBasicStandardClass = new ArrayList<>();
        Utils.rethrowUnchecked(() -> {
            allVersionsSmartContractClass.add(com.credits.scapi.v0.SmartContract.class);
            allVersionsSmartContractClass.add(com.credits.scapi.v1.SmartContract.class);

            allVersionsBasicStandardClass.add(com.credits.scapi.v0.BasicStandard.class);
            allVersionsBasicStandardClass.add(com.credits.scapi.v1.BasicTokenStandard.class);
        });
    }

}
