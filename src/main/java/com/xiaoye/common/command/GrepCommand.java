package com.xiaoye.common.command;

import com.xiaoye.command.core.Command;
import com.xiaoye.common.operation.GrepOperatiom;
import com.xiaoye.common.parameter.LimitParameter;

public class GrepCommand<S> extends Command<String[],S,String[]> {
    {
        name = "grep";
        operation = new GrepOperatiom<>();
        registryParameter(new LimitParameter<String,S>());
    }
}
