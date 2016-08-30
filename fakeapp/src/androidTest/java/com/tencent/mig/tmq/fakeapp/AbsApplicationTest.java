package com.tencent.mig.tmq.fakeapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.tencent.mig.tmq.keyword.Command;

/**
 * 放置公共方法的父类
 */
public abstract class AbsApplicationTest extends ApplicationTestCase<Application> {
    public AbsApplicationTest() {
        super(Application.class);
    }

    /**
     * 执行各个配置的AW命令
     * @param sCmd
     * @param params
     */
    public Object execute(K sCmd, Object...params)
    {
        Command command = TestContext.awMap.get(sCmd);
        return command.execute(params);
    }
}