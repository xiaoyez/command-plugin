package com.xiaoye.command.inject.manager;

import com.xiaoye.command.core.Parameter;

public class ParameterManager extends Manager<Parameter> {


    public void registry(Parameter parameter)
    {
        registry(parameter.getName(),parameter);
    }

}
