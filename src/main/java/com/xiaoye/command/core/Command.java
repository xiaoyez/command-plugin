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
@Data
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
        return childCommands.getCommand(name);
    }

    public Collection<Command> getChildCommands()
    {
        return childCommands.getCommandMap().values();
    }

    public void registryParameter(Parameter parameter)
    {
        if (parameterMap == null)
            parameterMap = new ParameterMap();
        parameterMap.registryParameter(parameter);
    }

    public Parameter getParameter(String name)
    {
        return parameterMap.getParameter(name);
    }

    public Collection<Parameter> getParameters()
    {
        return parameterMap.getParameterMap().values();
    }


}

