package com.xiaoye.command.core;

import com.xiaoye.util.ArrayUtil;
import com.xiaoye.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class DefaultCommandManager implements CommandManager {

    protected CommandMap commandMap = new CommandMap();


    protected Set<Interceptor<?,?>> topInterceptors = new HashSet<>();

    protected Map<Interceptor<?,?>,String[]> commandInterceptors = new HashMap<>();

    protected Map<Interceptor<?,?>,String[]> parameterInterceptors = new HashMap<>();

    protected Map<Interceptor<?,?>,String[]> commandParameterInterceptors = new HashMap<>();


    public <T,S,R> R executeCommandWithoutPipeSign(String commandStr, S sender,T target)
    {

        CommandStructure commandStructure = parseCommandStr(commandStr,sender);
        List<String> commandNames = new ArrayList<>(commandStructure.commandNames);

        boolean flag = executeCommandInterceptors(commandNames,commandInterceptors,sender,target);
        if (!flag)
            return null;
        String commandName = commandNames.get(0);
        Command<T,S,R> command = commandMap.getCommand(commandName);
        if (command == null)
            throw new CommandNotFoundException("未找到该命令-->" + commandName) ;
        else
        {
            commandNames.remove(0);
            String[] params = null;
            for (int i = 0; i < commandNames.size(); i++)
            {
                String name = commandNames.get(i);
                Command<T,S,R> childCommand = command.getChildCommand(name);
                if (childCommand == null)
                {
                    params = Arrays.copyOfRange(commandNames.toArray(new String[0]),i,commandNames.size());
                }
                else
                    command = childCommand;
            }
            R result = command.getOperation().operate(target, params, sender);

            Map<String, List<String>> parameters = commandStructure.getParameters();
            Set<String> keySet = parameters.keySet();
            StringBuilder builder = new StringBuilder();
            for (String commandName1 : commandStructure.commandNames)
            {
                builder.append(commandName1.trim() + " ");
            }
            builder.deleteCharAt(builder.length()-1);
            String commandFullName = builder.toString();
            for (String paramName : keySet)
            {
                flag = executeParameterInterceptors(parameterInterceptors,paramName,sender,result);
                if (!flag)
                    return result;
                String commandParameter = commandFullName + " -" + paramName;
                flag = executeCommandParameterInterceptors(commandParameterInterceptors,commandParameter,sender,result);
                if (!flag)
                    return result;
                Parameter<R,S> parameter = command.getParameter(paramName);
                if (parameter == null)
                {
                    throw new ParameterNotFoundException("命令["+commandFullName+"]不存在参数["+paramName+"]");
                }
                else
                {
                    List<String> paramValues = parameters.get(paramName);
                    result = parameter.getOperation().operate(result, paramValues.toArray(new String[paramValues.size()]), sender);
                }
            }
            return result;
        }
    }

    private <S, R> boolean executeCommandParameterInterceptors(Map<Interceptor<?, ?>, String[]> commandParameterInterceptors, String commandParameter, S sender, R target)
    {
        Set<Interceptor<?, ?>> interceptors = commandParameterInterceptors.keySet();

        for (Interceptor<?, ?> interceptor : interceptors)
        {
            String[] interceptCommandParameters = commandParameterInterceptors.get(interceptor);
            boolean contains = ArrayUtil.contains(interceptCommandParameters, commandParameter);
            if (contains)
            {
                boolean flag = runInterceptor(interceptor, sender, target);
                if (!flag)
                    return flag;
            }
        }
        return true;
    }

    private <S, R> boolean executeParameterInterceptors(Map<Interceptor<?, ?>, String[]> parameterInterceptors, String paramName, S sender, R target)
    {
        Set<Interceptor<?, ?>> interceptors = parameterInterceptors.keySet();
        for (Interceptor<?, ?> interceptor : interceptors)
        {
            String[] interceptParams = parameterInterceptors.get(interceptor);
            if(ArrayUtil.contains(interceptParams,paramName))
            {
                boolean flag = runInterceptor(interceptor, sender, target);
                if (!flag)
                    return flag;
            }
        }
        return true;
    }

    private <S, T> boolean executeCommandInterceptors(List<String> commandNames, Map<Interceptor<?, ?>, String[]> commandInterceptors, S sender, T target) {
        Set<Interceptor<?, ?>> interceptors = commandInterceptors.keySet();
        boolean flag = true;
        for (Interceptor<?, ?> interceptor : interceptors)
        {
            String[] interceptCommands = commandInterceptors.get(interceptor);
            for (String interceptCommand : interceptCommands)
            {
                String[] interceptCommandSequence = deleteElementWithoutText(interceptCommand.trim().split(" "));
                if (interceptCommandSequence.length > commandNames.size())
                    continue;

                boolean isNeedIntercept= true;
                for (int i = 0; i < interceptCommandSequence.length; i++)
                {
                    if (!interceptCommandSequence[i].equals(commandNames.get(i)))
                    {
                        isNeedIntercept = false;
                        break;
                    }
                }
                if (isNeedIntercept)
                {
                    flag = runInterceptor(interceptor,sender,target);
                    if (!flag)
                        return flag;
                }
            }

        }
        return flag;
    }

    private <S, T> boolean runInterceptor(Interceptor<?, ?> interceptor,  S sender, T target)
    {
        boolean flag = true;

        Method handler = null;
        try {
            handler = interceptor.getClass().getMethod("handler",
                    new Class[]{sender!=null?sender.getClass():Object.class,target!=null?target.getClass():Object.class});
        } catch (NoSuchMethodException e) {
            handler = null;
        }
        if (handler != null)
        {
            try {
                handler.setAccessible(true);
                flag = (Boolean) handler.invoke(interceptor,new Object[]{sender,target});
            } catch (IllegalAccessException e) {
               flag = true;
            } catch (InvocationTargetException e) {
                flag = true;
            }
        }

        return flag;
    }


    @Override
    public <T, S, R> R execute(String commandStr, S sender, T target)
    {
        for (Interceptor<?, ?> interceptor : topInterceptors)
        {
            boolean flag = runInterceptor(interceptor, sender, target);
            if (!flag)
                return null;
        }
        if (StringUtil.hasText(commandStr))
        {
            String[] commands = commandStr.trim().split("\\|");
            Object result = target;
            for (String command : commands)
            {
                result = executeCommandWithoutPipeSign(command,sender,result);
            }
            return (R)result;
        }
        return null;
    }

    @Override
    public  Command<?,?,?> findCommand(String commandStr)
    {
        CommandStructure commandStructure = parseCommandStr(commandStr,null);

        return findCommand(commandStructure);
    }

    @Override
    public void registryTopInterceptor(Interceptor<?, ?> interceptor) {
        topInterceptors.add(interceptor);
    }

    @Override
    public void registryCommandInterceptor(Interceptor<?, ?> interceptor, String[] commands) {
        commandInterceptors.put(interceptor,commands);
    }

    @Override
    public void registryParameterInterceptor(Interceptor<?, ?> interceptor, String[] parameters) {
        parameterInterceptors.put(interceptor,parameters);
    }

    @Override
    public void registryCommandParameterInterceptor(Interceptor<?, ?> interceptor, String[] commandParameters) {
         commandParameterInterceptors.put(interceptor,commandParameters);
    }



    private <T,S,R> Command<T,S,R> findCommand(CommandStructure commandStructure)
    {
        List<String> commandNames = commandStructure.commandNames;
        String commandName = commandNames.get(0);
        Command<T,S,R> command = commandMap.getCommand(commandName);
        if (command == null)
            throw new CommandNotFoundException("未找到该命令-->" + commandName) ;
        else
        {
            commandNames.remove(0);
            String[] params = null;
            for (int i = 0; i < commandNames.size(); i++)
            {
                String name = commandNames.get(i);
                Command<T, S, R> childCommand = command.getChildCommand(name);
                if (childCommand == null)
                {
                    params = Arrays.copyOfRange(commandNames.toArray(new String[0]), i, commandNames.size());
                }
                else
                    command = childCommand;
            }
        }
        return command;
    }


    /**
     * 解析命令字符串
     * @param commandStr 命令字符串
     * @param sender 命令发送者
     * @param <S> 命令发送者类型
     * @return
     */
    private <S> CommandStructure parseCommandStr(String commandStr, S sender) {

        String[] split = commandStr.trim().split(" ");
        split = deleteElementWithoutText(split);

        List<String> list = new ArrayList<>(Arrays.asList(split));

        List<String> commandNames = parseCommandNames(list);
        Map<String,List<String>> parameters = parseParameters(list);

        return new CommandStructure(commandNames,parameters);
    }

    /**
     * 解析参数
     * @param list
     * @return
     */
    private Map<String, List<String>> parseParameters(List<String> list)
    {
        //解析参数
        Map<String,List<String>> parameters = new HashMap<>();
        for (int i = 0; i < list.size(); i++)
        {
            String next = list.get(i);
            if (next.startsWith("-"))
            {
                String paramName = next.substring(1);
                List<String> paramValues = new ArrayList<>();
                while (i < list.size() - 1)
                {
                    i++;
                    String paramValue = list.get(i);
                    if (!paramValue.startsWith("-"))
                    {
                        paramValues.add(paramValue);
                    }
                    else
                    {
                        i--;
                        break;
                    }
                }
                parameters.put(paramName,paramValues);
            }
            else
            {
                throw new CommandStrParseException("命令参数解析失败，请检查命令是否符合规范");
            }
        }
        return parameters;
    }

    /**
     * 解析命令名
     * @param list
     * @return
     */
    private List<String> parseCommandNames(List<String> list)
    {
        List<String> commandNames = new ArrayList<>();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext())
        {
            String next = iterator.next();
            if (!next.startsWith("-"))
            {
                commandNames.add(next);
                iterator.remove();
            }
            else
                break;
        }
        return commandNames;
    }

    /**
     * 删除数组中空文本的元素
     * @param split
     * @return
     */
    private String[] deleteElementWithoutText(String[] split) {
        List<String> list = new ArrayList<>(Arrays.asList(split));
        Iterator<String> iterator = list.iterator();

        while (iterator.hasNext())
        {
            String next = iterator.next();
            if (next.trim().equals(""))
                iterator.remove();
        }

        String[] result = new String[list.size()];
        return list.toArray(result);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CommandStructure{
        private List<String> commandNames;
        private Map<String,List<String>> parameters;
    }
}
