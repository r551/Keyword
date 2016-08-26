package com.tencent.mig.tmq.keyword;

/**
 * Created by yoyoqin on 2016/7/26.
 */
public interface Command {
    /**
     * 命令的执行体方法，客户端代码需要实现
     * @return 返回值，可以为null
     */
    Object execute(Object... params);
}
