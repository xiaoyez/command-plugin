package com.xiaoye.command.core;

import lombok.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @param <T> 操作对象类型
 * @param <S> 命令发送者类型
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Command<T,S,R> {

    /**
     * 命令名，命令根据此属性查找对应操作
     */
    protected String name;

    protected CommandMap childCommands = null;

    protected ParameterMap parameterMap = null;

    protected Operation<T,S,R> operation;

    public Command copy() {
        return new Command<T,S,R>(name,null,parameterMap,operation);
    }

    private static class ParameterMap
    {
        @Getter
        @Setter
        private Map<String,Parameter> parameterMap = new LinkedHashMap<>();

        public void registryParameter(Parameter parameter)
        {
            parameterMap.put(parameter.getName(),parameter);
        }

        public Parameter getParameter(String name)
        {
            return parameterMap.get(name);
        }
    }


    public Command(String name,  Operation<T, S, R> operation) {
        this.name = name;
        this.operation = operation;
    }

    public void registryChildCommand(Command command)
    {
        if (childCommands == null)
            childCommands = new CommandMap();
        childCommands.registryCommand(command);
    }

    public Command getChildCommand(String name)
    {
        if(childCommands == null)
            return null;
        return childCommands.getCommand(name);
    }

    public Collection<Command> getChildCommands()
    {
        if (childCommands == null)
            return null;
        Map<String, Command> commandMap = childCommands.getCommandMap();

        return commandMap == null? null: commandMap.values();
    }

    public void registryParameter(Parameter parameter)
    {
        if (parameterMap == null)
            parameterMap = new ParameterMap();
        parameterMap.registryParameter(parameter);
    }

    public Parameter getParameter(String name)
    {
        return parameterMap == null?null:parameterMap.getParameter(name);
    }

    public Collection<Parameter> getParameters()
    {
        if (parameterMap == null)
            return null;
        Map<String, Parameter> parameterMap = this.parameterMap.getParameterMap();
        if (parameterMap == null)
            return null;
        return parameterMap.values();
    }


}

