package com.xiaoye.common.parameter;

import com.xiaoye.command.core.Parameter;
import com.xiaoye.common.operation.LimitOperation;

public class LimitParameter<T,S> extends Parameter<T[],S> {
    {
        name = "limit";
        operation = new LimitOperation<T,S>();
    }
}
