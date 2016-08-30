# Keyword
#### Headers
Keyword是腾讯TMQ旗下实现关键字驱动测试的辅助组件，其封装了关键字所需要实现的Command接口及注解，通过在包上或关键字类上使用注解，测试者可以方便的以关键字的形式写作测试用例。
#### 当前版本
v1.0.0
#### 模块介绍
##### api
包括关键字接口Command，和三个注解Keyword、KeywordPackage和KeywordGenPackage。
需要在测试工程中引用api模块。可以将api模块编译成jar包直接引用，Android Studio环境下也可以通过配置被测工程的gradle文件对已发布的api模块进行引用。
##### compiler
辅助被测工程在编译前自动生成使用关键字必须的K.java和TestContext.java文件。需要在被测工程中配置apt插件。
##### fakeapp
使用本关键字驱动组件写作用例的demo模块。
#### 使用方法
具体使用方法请参考fakeapp模块的源码。
##### 配置
1.最外层工程的gradle中配置apt插件：
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.2'

        // 配置apt
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```
2.在具体的被测模块的gradle的一开始进行插件配置：
```groovy
apply plugin: 'com.android.application'
apply plugin: 'com.neenbedankt.android-apt'
```
3.配置对Keyword api和Keyword compiler的引用：
```groovy
dependencies {
    // 在Android测试中使用的场景，要像下面这样分别配置Android测试的依赖和apt引用方式
    androidTestCompile 'com.tencent.mig.tmq.keyword:api:1.0.0'
    androidTestApt 'com.tencent.mig.tmq.keyword:compiler:1.0.0'
}
```
4.在Android测试的场景下，别忘了在defaultConfig中配置测试执行器：
```groovy
defaultConfig {
    testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
}
```
##### 用例写作样例
1.首先实现一个关键字，需要实现com.tencent.mig.tmq.keyword.Command接口，并添加Keyword注解：
```java
@Keyword
public class ActionWord1 implements Command {
    @Override
    public Object execute(Object... objects) {
        return null;
    }
}
```
2.编译，如未设置自动编译，请手动执行一下，这样会生成用例中要使用的K.java文件和TestContext.java文件。

3.给用例设置一个父类，放置公共方法：
```java
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
```
4.具体的用例这样写：
```java
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
```
##### KeywordPackage注解的使用方法
如果关键字都放在指定的几个java包下，比如这样的包结构
com.tencent.mig.tmq.fakeapp

    aaw
    
    aw
    
    kw
        Keyword1.java
        
        Keyword2.java
        
        Keyword3.java
        
        Keyword4.java
        
        package-info.java
        
kw包中的所有以Keyword开头的类都是关键字，此时没有必要给每个以Keyword开头的类都加Keyword注解，只要在kw包的包描述文件package-info.java上添加KeywordPackage注解，即代表kw包中声明的实现了Command接口的类都是关键字。
```java
@KeywordPackage
package com.tencent.mig.tmq.fakeapp.kw;

import com.tencent.mig.tmq.keyword.KeywordPackage;
```    
##### KeywordGenPackage注解的使用方法
K.java文件和TestContext.java默认会生成在编译扫描到的第一个关键字所在包的上层包中。如果需要将它们放置于指定的包内，则需要在指定的包描述文件中加上KeywordGenPackage注解。
```java
/**
 * @author yoyoqin
 * keyword生成的K.java和TestContext.java文件会生成在这个包中
 */
@KeywordGenPackage
package com.tencent.mig.tmq.fakeapp;

import com.tencent.mig.tmq.keyword.KeywordGenPackage;
```