package com.xiaoye.command.inject.definition;

import com.xiaoye.command.core.Parameter;
import lombok.Data;

import java.lang.reflect.Constructor;

/**
 * 参数描述类，用来描述参数对象该如何构建
 */
@Data
public class ParameterDefinition {

    private Class<? extends Parameter> parameterClass;

    private String name;

    private String operation;
}
