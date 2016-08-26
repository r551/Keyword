/*
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.mig.tmq.keyword;

/**
 * Created by yoyoqin on 2016/7/26.
 * 关键字需要实现的接口
 */
public interface Command {
    /**
     * 命令的执行体方法，客户端代码需要实现
     * @return 返回值，可以为null
     */
    Object execute(Object... params);
}
