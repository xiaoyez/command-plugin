package com.xiaoye.command.inject.manager;

import com.xiaoye.command.core.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Manager<T> {

    @Getter
    @Setter
    protected Map<String,T> map = new LinkedHashMap<>();

    public void registry(String name,T bean)
    {
        map.put(name,bean);
    }

    public T get(String name)
    {
        return map.get(name);
    }
}
