package com.credits.service;

import java.util.List;

public class BackwardCompatibilityService {

    public static final List<Class<?>> allVersionsSmartContractClass = List.of(com.credits.scapi.v0.SmartContract.class,
                                                                               com.credits.scapi.v1.SmartContract.class);
    public static final List<Class<?>> allVersionsBasicStandardClass = List.of(com.credits.scapi.v0.BasicStandard.class,
                                                                               com.credits.scapi.v1.BasicTokenStandard.class);

}
