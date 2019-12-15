package com.xiaoye.command.core;

public interface CommandManager {

     <T,S,R> R execute(String commandStr,S sender,T target);

     void setCommandMap(CommandMap commandMap);

     Command<?,?,?> findCommand(String commandStr);

     CommandMap getCommandMap();
}
