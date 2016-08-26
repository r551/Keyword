package com.tencent.mig.tmq.fakeapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.tencent.mig.tmq.keyword.Command;

/**
 * keyword使用demo
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test1()
    {
        execute(K.AActionWord1);
        execute(K.AActionWord2);
        execute(K.Keyword1);
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