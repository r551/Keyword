package com.tencent.mig.tmq.fakeapp;

import com.tencent.mig.tmq.keyword.Command;

/**
 * Created by yoyoqin on 2016/8/1.
 */
public abstract class AbstractCommand implements Command {
    protected Object execute(K cmd, Object... params)
    {
        return TestContext.awMap.get(cmd).execute(params);
    }

}
