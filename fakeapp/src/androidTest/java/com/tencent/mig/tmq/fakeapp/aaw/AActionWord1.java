package com.tencent.mig.tmq.fakeapp.aaw;

import com.tencent.mig.tmq.fakeapp.AbstractCommand;
import com.tencent.mig.tmq.fakeapp.K;

/**
 * Created by yoyoqin on 2016/8/25.
 */
public class AActionWord1 extends AbstractCommand {
    @Override
    public Object execute(Object... objects) {
        execute(K.Keyword1);
        execute(K.keyword2);
        execute(K.Keyword3);
        return null;
    }
}
