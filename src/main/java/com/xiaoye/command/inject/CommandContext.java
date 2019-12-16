package com.xiaoye.command.inject;

import com.xiaoye.command.core.*;
import com.xiaoye.command.inject.annotation.*;
import com.xiaoye.command.inject.definition.CommandDefinition;
import com.xiaoye.command.inject.definition.OperationDefinition;
import com.xiaoye.command.inject.definition.ParameterDefinition;
import com.xiaoye.command.inject.manager.OperationManager;
import com.xiaoye.command.inject.manager.ParameterManager;
import com.xiaoye.util.AnnotationUtil;
import com.xiaoye.util.ClassUtil;
import com.xiaoye.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

/**
 * 命令上下文，负责全局扫描配置命令和参数对象
 */
public class CommandContext {

    private List<Class> classes;

    @Getter
    private CommandManager commandManager = new DefaultCommandManager();

    public void scanClasses(String basePackage)
    {
        try {
            classes = ClassUtil.searchClass(basePackage);
            if (classes == null || classes.isEmpty())
            {
                return;
            }

            List<CommandDefinition> commandDefinitions = new LinkedList<>();
            List<ParameterDefinition> parameterDefinitions = new LinkedList<>();
            List<OperationDefinition> operationDefinitions = new LinkedList<>();

            CommandMap commandMap  = new CommandMap();
            commandManager.setCommandMap(commandMap);

            Iterator<Class> iterator = classes.iterator();
            while (iterator.hasNext())
            {
                Class next = iterator.next();
                if (Command.class.isAssignableFrom(next))
                {
                    commandDefinitions.add(parseCommandDefinition(next));
                }
                if (Parameter.class.isAssignableFrom(next))
                {
                    parameterDefinitions.add(parseParameterDefinition(next));
                }
                if (com.xiaoye.command.core.Operation.class.isAssignableFrom(next))
                {
                    operationDefinitions.add(parseOperationDefinition(next));
                }
            }
            ParameterManager parameterManager = null;
            OperationManager operationManager = null;
            if (!operationDefinitions.isEmpty())
            {
                operationManager = new OperationManager();
                buildOperations(operationDefinitions,operationManager);
            }
            if (!parameterDefinitions.isEmpty())
            {
                parameterManager = new ParameterManager();
                buildParameters(parameterDefinitions,parameterManager,operationManager);
            }
            if (!commandDefinitions.isEmpty())
            {
                buildCommands(commandDefinitions,commandManager,parameterManager,operationManager);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildCommands(List<CommandDefinition> commandDefinitions,
                               CommandManager commandManager,
                               ParameterManager parameterManager,
                               OperationManager operationManager)
    {
        com.xiaoye.command.inject.manager.CommandManager commandManagerForInject = new com.xiaoye.command.inject.manager.CommandManager();
        CommandMap commandMap = commandManager.getCommandMap();

        buildCommandsInCommandManagerForInject(commandDefinitions,commandManagerForInject,commandMap,parameterManager,operationManager);

        List<CommandLevel> commandLevels = buildCommandLevels(commandDefinitions);

        injectCommands(commandLevels,commandManager,commandManagerForInject);


    }

    private void injectCommands(List<CommandLevel> commandLevels,
                                CommandManager commandManager,
                                com.xiaoye.command.inject.manager.CommandManager commandManagerForInject)
    {
        for (CommandLevel commandLevel : commandLevels)
        {
            String name = commandLevel.getName();
            String parentCommandName = commandLevel.getParentCommandName();
            Command<?, ?, ?> parentCommand1 = commandManager.findCommand(parentCommandName);
            if (parentCommand1 == null)
                continue;
            Command command = commandManagerForInject.get(name);
            parentCommand1.registryChildCommand(command.copy());
        }
    }

    private List<CommandLevel> buildCommandLevels(List<CommandDefinition> commandDefinitions)
    {
        List<CommandLevel> commandLevels = new LinkedList<>();
        for (CommandDefinition commandDefinition : commandDefinitions)
        {
            String[] parentCommands = commandDefinition.getParentCommands();
            if (parentCommands != null && parentCommands.length > 0)
            {
                for (String parentCommand : parentCommands)
                {
                    if (StringUtil.hasText(parentCommand))
                    {
                        int level = parentCommand.trim().split(" ").length;
                        CommandLevel commandLevel = new CommandLevel(commandDefinition.getName(), parentCommand,level);
                        commandLevels.add(commandLevel);
                    }
                }
            }
        }
        commandLevels.sort((x,y)->{return x.compareTo(y);});
        return commandLevels;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class CommandLevel implements Comparable<CommandLevel>{
        private String name;
        private String parentCommandName;
        private Integer level;

        @Override
        public int compareTo(CommandLevel o) {
            return this.level.compareTo(o.level);
        }
    }

    private void buildCommandsInCommandManagerForInject(List<CommandDefinition> commandDefinitions,
                                                        com.xiaoye.command.inject.manager.CommandManager commandManagerForInject,
                                                        CommandMap commandMap,ParameterManager parameterManager,OperationManager operationManager)
    {
        for (CommandDefinition commandDefinition : commandDefinitions)
        {
            Class<? extends Command> commandClass = commandDefinition.getCommandClass();
            Command command = null;
            try {
                command = commandClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (command == null)
                continue;

            command.setOperation(operationManager.get(commandDefinition.getOperation()));
            String[] parameters = commandDefinition.getParameters();
            if (parameters != null)
            {
                for (String parameter : parameters)
                {
                    command.registryParameter(parameterManager.get(parameter));
                }
            }
            commandManagerForInject.registry(commandDefinition.getName(),command);
            if (commandDefinition.isTopCommand())
                commandMap.registryCommand(command.copy());
        }
    }

    private void buildParameters(List<ParameterDefinition> parameterDefinitions, ParameterManager parameterManager, OperationManager operationManager)
    {
        for (ParameterDefinition parameterDefinition : parameterDefinitions)
        {
            Class<? extends Parameter> parameterClass = parameterDefinition.getParameterClass();
            Parameter parameter = null;
            try {
                parameter = parameterClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (parameter == null)
                continue;
            parameter.setOperation(operationManager.get(parameterDefinition.getOperation()));
            parameterManager.registry(parameterDefinition.getName(),parameter);
        }
    }

    private void buildOperations(List<OperationDefinition> operationDefinitions, OperationManager operationManager) {
        for (OperationDefinition operationDefinition : operationDefinitions)
        {
            Class<? extends com.xiaoye.command.core.Operation> operationClass = operationDefinition.getOperationClass();
            com.xiaoye.command.core.Operation operation = null;
            try {
                operation = operationClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (operation == null)
                continue;
            operationManager.registry(operationDefinition.getName(),operation);
        }
    }

    private OperationDefinition parseOperationDefinition(Class operationClass)
    {
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setOperationClass(operationClass);

        Name name = AnnotationUtil.getAnnotation(operationClass, Name.class);
        if (name != null)
        {
            operationDefinition.setName(name.value());
        }
        return operationDefinition;
    }

    private ParameterDefinition parseParameterDefinition(Class parameterClass)
    {
        ParameterDefinition parameterDefinition = new ParameterDefinition();
        parameterDefinition.setParameterClass(parameterClass);

        Name name = AnnotationUtil.getAnnotation(parameterClass, Name.class);
        if (name != null)
        {
            parameterDefinition.setName(name.value());
        }
        com.xiaoye.command.inject.annotation.Operation operation = AnnotationUtil.getAnnotation(parameterClass, com.xiaoye.command.inject.annotation.Operation.class);
        if (operation != null)
        {
            parameterDefinition.setOperation(operation.value());
        }
        return parameterDefinition;
    }

    private CommandDefinition parseCommandDefinition(Class commandClass)
    {
        CommandDefinition commandDefinition = new CommandDefinition();
        commandDefinition.setCommandClass(commandClass);

        Name name = AnnotationUtil.getAnnotation(commandClass, Name.class);
        if (name != null)
        {
            commandDefinition.setName(name.value());
        }
        com.xiaoye.command.inject.annotation.Operation operation = AnnotationUtil.getAnnotation(commandClass, com.xiaoye.command.inject.annotation.Operation.class);
        if (operation != null)
        {
            commandDefinition.setOperation(operation.value());
        }

        Params params = AnnotationUtil.getAnnotation(commandClass, Params.class);
        if (params != null)
        {
            commandDefinition.setParameters(params.value());
        }

        ParentCommands parentCommands = AnnotationUtil.getAnnotation(commandClass, ParentCommands.class);
        if (parentCommands != null)
        {
            commandDefinition.setParentCommands(parentCommands.value());
        }

        TopCommand topCommand = AnnotationUtil.getAnnotation(commandClass, TopCommand.class);
        if (topCommand != null)
        {
            commandDefinition.setTopCommand(false);
        }
        return commandDefinition;
    }
}
