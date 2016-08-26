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

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by yoyoqin on 2016/8/24.
 * 用来保存Keyword信息的类
 */
public class KeywordAnnotatedClass {
    private TypeElement annotatedTypeElement;
    private int level;

    public KeywordAnnotatedClass
            (TypeElement typeElement) throws IllegalArgumentException {
        this.annotatedTypeElement = typeElement;
        Keyword annotation = typeElement.getAnnotation(Keyword.class);
        this.level = annotation.level();
    }

    public KeywordAnnotatedClass
            (PackageElement packageElement, TypeElement typeElement) throws IllegalArgumentException {
        this.annotatedTypeElement = typeElement;
        KeywordPackage annotation = packageElement.getAnnotation(KeywordPackage.class);
        this.level = annotation.level();
    }

    public int getLevel() {
        return level;
    }

    public TypeElement getAnnotatedTypeElement() {
        return annotatedTypeElement;
    }

}
