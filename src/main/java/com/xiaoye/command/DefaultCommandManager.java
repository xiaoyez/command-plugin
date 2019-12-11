package com.xiaoye.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
public class DefaultCommandManager implements CommandManager {

    protected CommandMap commandMap = new CommandMap();

    @Override
    public <T,S,R> R execute(String commandStr, S sender,T target) {

        CommandStructure commandStructure = parseCommandStr(commandStr,sender);
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
            for (String paramName : keySet)
            {
                Parameter<R,S> parameter = command.getParameter(paramName);
                if (parameter == null)
                {
                    StringBuilder builder = new StringBuilder();
                    for (String name : commandNames)
                    {
                        builder.append(name + " ");
                    }
                    builder.deleteCharAt(builder.length());
                    throw new ParameterNotFoundException("命令["+builder.toString()+"]不存在参数["+paramName+"]");
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
