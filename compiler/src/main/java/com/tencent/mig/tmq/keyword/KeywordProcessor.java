package com.tencent.mig.tmq.keyword;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class KeywordProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<Integer, Set<KeywordAnnotatedClass>> keyMap; // 每个AW都需要带权重

    /**
     * Initializes the processor with the processing environment by
     * setting the {@code processingEnv} field to the value of the
     * {@code processingEnv} argument.  An {@code
     * IllegalStateException} will be thrown if this method is called
     * more than once on the same object.
     *
     * @param processingEnv environment to access facilities the tool framework
     * provides to the processor
     * @throws IllegalStateException if this method is called more than once.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        keyMap = new LinkedHashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new LinkedHashSet<>();
        result.add(Keyword.class.getCanonicalName());
        result.add(KeywordPackage.class.getCanonicalName());
        result.add(KeywordGenPackage.class.getCanonicalName());
        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
//        return SourceVersion.RELEASE_7;
        return SourceVersion.latestSupported();
    }

    /**
     * Processes a set of annotation types on type elements originating from the prior round
     * and returns whether or not these annotations are claimed by this processor.
     * If true is returned, the annotations are claimed and subsequent processors will
     * not be asked to process them;
     * if false is returned, the annotations are unclaimed and subsequent processors may be
     * asked to process them. A processor may always return the same boolean value
     * or may vary the result based on chosen criteria.
     * The input set will be empty if the processor supports "*" and the root elements
     * have no annotations. A Processor must gracefully handle an empty set of annotations.
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 先统计KeywordPackage注解包中的类并记录，后面Keyword注解如果已被注解包包含，则略过不要重复了
        // 本需求比较简单，收集完整KW类的名字即可，注意处理重复
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(KeywordPackage.class)) {

            // 检查被注解为@KeywordPackage的元素是否是package-info.java
            if (annotatedElement.getKind() != ElementKind.PACKAGE) {
                error(annotatedElement, "Only packages can be annotated with @%s",
                        KeywordPackage.class.getSimpleName());
                return true; // 退出处理
            }

            PackageElement packageElement = (PackageElement) annotatedElement;

            // 本包下所有类都被认为是KW进行记录
            int level = packageElement.getAnnotation(KeywordPackage.class).level();
            Set<KeywordAnnotatedClass> levelKwSet = keyMap.get(level);
            if (null == levelKwSet)
            {
                levelKwSet = new LinkedHashSet<>();
                keyMap.put(level, levelKwSet);
            }
            for (Element e : packageElement.getEnclosedElements())
            {
                if (e.getKind() == ElementKind.CLASS)
                {
                    // 确认是实现的Command接口，则将其加入有效的Keyword集
                    if (isInheritFromCommand((TypeElement)e))
                    {
                        levelKwSet.add(new KeywordAnnotatedClass(packageElement, (TypeElement) e));
                    }
                }
            }
        }

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Keyword.class)) {

            // 检查被注解为@Keyword的元素是否是一个类
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        Keyword.class.getSimpleName());
                return true; // 退出处理
            }

            int level = annotatedElement.getAnnotation(Keyword.class).level();
            Set<KeywordAnnotatedClass> levelKwSet = keyMap.get(level);
            if (null == levelKwSet)
            {
                levelKwSet = new LinkedHashSet<>();
                keyMap.put(level, levelKwSet);
            }

            // 确认是实现的Command接口，则将其加入有效的Keyword集
            if (isInheritFromCommand((TypeElement)annotatedElement))
            {
                levelKwSet.add(new KeywordAnnotatedClass((TypeElement)annotatedElement));
            }
        }

        /*
         * 确定生成文件所在的包名，
         * 如果没有被KeywordGenPackage注解的包，则以取读到的第一个被Keyword注解类所在包的父包为准
         */
        String genPackageName = null;
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(KeywordGenPackage.class))
        {
            // 检查被注解为@KeywordGenPackage的元素是否是package-info.java
            if (annotatedElement.getKind() != ElementKind.PACKAGE) {
                error(annotatedElement, "Only packages can be annotated with @%s",
                        KeywordPackage.class.getSimpleName());
                return true; // 退出处理
            }

            PackageElement packageElement = (PackageElement) annotatedElement;
            genPackageName = packageElement.getQualifiedName().toString();
            // 只取第一个带KeywordGenPackage注解的包为基准
            break;
        }

        // 最后，生成K.java和TestContext.java
        try {
            generateCode(genPackageName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // process可能多轮执行，防止代码文件重复生成，会报错
        keyMap.clear();
        return false;
    }

    private boolean isInheritFromCommand(TypeElement annotatedElement)
    {
        boolean isInheritFromCommand = false;
        TypeElement currentClass = annotatedElement;
        while(true)
        {
            // 以判断当前类实现的接口为目标
            List<? extends TypeMirror> interfaceMirrorList = currentClass.getInterfaces();
            for (TypeMirror mirror : interfaceMirrorList)
            {
                if (mirror.toString().equals(Command.class.getName()))
                {
                    // 符合要求
                    isInheritFromCommand = true;
                    break;
                }
            }
            if (isInheritFromCommand)
            {
                break;
            }
            // 到达了基本类型(java.lang.Object), 所以退出
            TypeMirror superClassType = currentClass.getSuperclass();
            if (superClassType.getKind() == TypeKind.NONE) {

                error(annotatedElement, "The class %s annotated with @%s must implement from %s",
                        currentClass.getQualifiedName().toString(), Keyword.class.getSimpleName(),
                        Command.class.getName());
                break;
            }
            // 在继承树上继续向上搜寻
            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }
        return isInheritFromCommand;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void generateCode(String pkgName) throws IOException {
        System.out.println("================TMQ start generate config files================");
        PackageElement pkg = null;
        String packageName = pkgName;

        if (keyMap.isEmpty())
        {
            return;
        }

        /*
         * 生成K文件
         */
        TypeSpec.Builder kBuilder = TypeSpec.enumBuilder("K")
                .addModifiers(Modifier.PUBLIC);

        for (int level : keyMap.keySet())
        {
            Set<KeywordAnnotatedClass> annotatedClassSet = keyMap.get(level);
            for (KeywordAnnotatedClass annotatedClass : annotatedClassSet)
            {
                // 如果传进来的packageName为null，则先选定生成文件所在的包，默认取第一个录入的keyword的所在包的父包
                if (packageName == null && pkg == null)
                {
                    pkg = elementUtils.getPackageOf(annotatedClass.getAnnotatedTypeElement());
                    if (pkg != null)
                    {
                        if (pkg.isUnnamed())
                        {
                            packageName = "";
                        }
                        else
                        {
                            String subPackageName = pkg.getQualifiedName().toString();
                            // 这种判断方式比较简陋，对内部类就不适用，先这样写着，主要还是依赖于外部指定
                            packageName = subPackageName.substring(0, subPackageName.lastIndexOf("."));
                        }
                        System.out.println("================" + packageName + "==============");
                    }
                }
                kBuilder.addEnumConstant(annotatedClass.getAnnotatedTypeElement().getSimpleName().toString());
            }
        }
        TypeSpec typeSpec = kBuilder.build();
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);

        /*
         * 生成TestContext文件
         * private static final Map<K, Command> awMap = new java.util.HashMap();
         * 注意依赖第一轮生成的K
         */
        CodeBlock.Builder initBlockBuilder = CodeBlock.builder();
        initBlockBuilder.add("new java.util.HashMap()");

        ParameterizedTypeName parameterizedTypeName =
                ParameterizedTypeName.get(Map.class, Object.class, Command.class);

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(parameterizedTypeName, "awMap")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addModifiers(Modifier.FINAL).initializer(initBlockBuilder.build());

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        for (int level : keyMap.keySet())
        {
            Set<KeywordAnnotatedClass> annotatedClassSet = keyMap.get(level);
            for (KeywordAnnotatedClass annotatedClass : annotatedClassSet)
            {
                ClassName kwClassName = ClassName.get(annotatedClass.getAnnotatedTypeElement());
                codeBlockBuilder.addStatement(
                        "awMap.put(K."
                                + annotatedClass.getAnnotatedTypeElement().getSimpleName().toString()
                                + ", new " + kwClassName.toString() + "())");
            }
        }

        TypeSpec.Builder contextBuilder = TypeSpec.classBuilder("TestContext")
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldBuilder.build())
                .addStaticBlock(codeBlockBuilder.build());

        TypeSpec contextSpec = contextBuilder.build();
        JavaFile.builder(packageName, contextSpec).build().writeTo(filer);
        System.out.println("================TMQ end  generate config files================");
    }
}
