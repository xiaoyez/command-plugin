package com.xiaoye.command.core;

import lombok.Data;

@Data
public class Parameter<T,S> {


    /**
     * 参数名
     */
    private String name;
    
    private Operation<T,S,T> operation;

    public Parameter(String name, Operation<T, S, T> operation) {
        this.name = name;
        this.operation = operation;
    }
}

