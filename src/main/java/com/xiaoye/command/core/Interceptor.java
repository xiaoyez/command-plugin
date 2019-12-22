package com.xiaoye.command.core;

public interface Interceptor<S,T> {

    boolean handler(S sender, T target);
}
