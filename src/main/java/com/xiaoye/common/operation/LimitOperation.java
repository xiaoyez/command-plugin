package com.xiaoye.common.operation;

import com.xiaoye.command.core.Operation;
import com.xiaoye.util.ArrayUtil;

import java.util.Arrays;

public class LimitOperation<T,S> implements Operation<T[],S,T[]> {
    @Override
    public T[] operate(T[] target, String[] params, S sender) {
        if (!ArrayUtil.hasElem(params))
            return target;
        Integer limit = Integer.valueOf(params[0]);
        if (limit >= target.length)
            return target;
        return Arrays.copyOfRange(target,0,limit);
    }
}
