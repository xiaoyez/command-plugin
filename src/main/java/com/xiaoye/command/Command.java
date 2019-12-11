package com.xiaoye.command;

import lombok.*;

import java.lang.annotation.Target;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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

    protected T target;

    protected parameterMap parameterMap = null;

    protected Operation<T,S,R> operation;

    private static class parameterMap
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


    public Command(String name, T target, Operation<T, S, R> operation) {
        this.name = name;
        this.target = target;
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
            parameterMap = new parameterMap();
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

