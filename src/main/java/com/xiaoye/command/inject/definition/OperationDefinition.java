package com.xiaoye.command.inject.definition;

import com.xiaoye.command.core.Operation;
import lombok.Data;

/**
 * 操作描述类，用来描述操作对象该如何构建
 */
@Data
public class OperationDefinition {

    private Class<? extends Operation> operationClass;

    private String name;
}
