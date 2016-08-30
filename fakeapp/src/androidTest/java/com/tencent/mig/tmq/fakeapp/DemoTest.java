package com.tencent.mig.tmq.fakeapp;

/**
 * keyword使用demo
 * Created by yoyoqin on 2016/8/30.
 */
public class DemoTest extends AbsApplicationTest {

    public void testXXX()
    {
        execute(K.AActionWord1, "arg1", "arg2");
        execute(K.AActionWord2);
        execute(K.Keyword1);
    }

    public void testYYY()
    {
        execute(K.AActionWord1);
        execute(K.AActionWord2);
        execute(K.Keyword3, 1, 10);
    }
}
