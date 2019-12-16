package com.xiaoye.command.inject.definition;

import com.xiaoye.command.core.Command;
import com.xiaoye.command.core.Parameter;
import lombok.Data;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 命令描述类，用来描述命令对象该如何构建
 */
@Data
public class CommandDefinition {

    private Class<? extends Command> commandClass;

    private String name;

    private String[] parentCommands;

    private String operation;

    private String[] parameters;

    private boolean isTopCommand = true;

}
