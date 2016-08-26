package com.tencent.mig.tmq.keyword;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by yoyoqin on 2016/8/24.
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
