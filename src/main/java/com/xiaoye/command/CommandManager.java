package com.xiaoye.command;

public interface CommandManager {

     <T,S,R> R execute(String commandStr,S sender,T target);

     void setCommandMap(CommandMap commandMap);
}
