package com.credits.service.contract;

import com.credits.general.thrift.generated.APIResponse;
import com.credits.general.thrift.generated.Variant;

public class SmartContractMethodResult {
    public final APIResponse status;
    public final Variant result;

    public SmartContractMethodResult(APIResponse status, Variant result) {
        this.status = status;
        this.result = result;
    }
}
