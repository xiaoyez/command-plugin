package com.xiaoye.command;

/**
 * 命令对应要执行的操作类
 * @param <T> 操作对象
 * @param <S> 命令发送者的类型
 * @param <R> 操作返回值
 */
public interface Operation<T, S,R> {

    R operate(T target,String[] params, S sender);
}
