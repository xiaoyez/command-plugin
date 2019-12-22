package com.xiaoye.command.core;

public interface CommandManager {

     <T,S,R> R execute(String commandStr,S sender,T target);

     void setCommandMap(CommandMap commandMap);

     Command<?,?,?> findCommand(String commandStr);

     CommandMap getCommandMap();

     /**
      * 注册顶级拦截器，此类拦截器会拦截一切命令
      * @param interceptor
      */
     void registryTopInterceptor(Interceptor<?,?> interceptor);

     /**
      * 注册命令级拦截器，此类拦截器只会拦截特定命令
      * @param interceptor
      * @param commands
      */
     void registryCommandInterceptor(Interceptor<?,?> interceptor,String[] commands);

     /**
      * 注册参数级拦截器，此类拦截器只会拦截特定的参数
      * @param interceptor
      * @param parameters
      */
     void registryParameterInterceptor(Interceptor<?,?> interceptor, String[] parameters);

     /**
      * 注册命令参数级拦截器，此类拦截器只会拦截特定命令的特定参数
      * @param interceptor
      * @param commandParameters
      */
     void registryCommandParameterInterceptor(Interceptor<?,?> interceptor, String[] commandParameters);
}
