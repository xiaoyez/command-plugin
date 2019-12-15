package com.xiaoye.command.core;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class CommandMap {

    private Map<String,Command> commandMap = new LinkedHashMap<>();

    public void registryCommand(Command command)
    {
        commandMap.put(command.getName(),command);
    }

    public Command getCommand(String name)
    {
        return commandMap.get(name);
    }
}
