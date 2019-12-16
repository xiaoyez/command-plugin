package com.xiaoye.command.core;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Parameter<T,S> {


    /**
     * 参数名
     */
    protected String name;
    
    protected Operation<T,S,T> operation;

    public Parameter(String name, Operation<T, S, T> operation) {
        this.name = name;
        this.operation = operation;
    }
}

