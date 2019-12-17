package com.xiaoye.common.operation;

import com.xiaoye.command.core.Operation;
import com.xiaoye.util.ArrayUtil;

import java.util.Arrays;
import java.util.List;

public class GrepOperatiom<S> implements Operation<String[],S,String[]> {
    @Override
    public String[] operate(String[] target, String[] params, S sender)
    {
        if (!ArrayUtil.hasElem(params))
            return target;
        String grep = params[0];
        return Arrays.stream(target).filter((x)->{
            return x.contains(grep);
        }).toArray(String[]::new);
    }
}
