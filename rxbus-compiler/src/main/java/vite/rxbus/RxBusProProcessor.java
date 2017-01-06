package vite.rxbus;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.google.common.collect.HashMultimap;
import com.squareup.javapoet.ClassName;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;

/**
 * Created by trs on 16-11-24.
 */
@AutoService(Processor.class)
public class RxBusProProcessor extends AbstractProcessor {

    private static final Map<TypeElement, ProxyBuilder> PROXYS = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Util.TypeUtils = processingEnv.getTypeUtils();
        Util.ElementUtils = processingEnv.getElementUtils();
        Util.Filer = processingEnv.getFiler();
        Util.Messager = processingEnv.getMessager();
    }

    /**
     * @param annotations 该处理器声明支持并已经在源码中使用了的注解
     * @param roundEnv    注解处理的上下文环境妈的
     * @return true:支持的注解处理完毕；false:支持的注解在该处理器处理完毕后，接着给其他处理器处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取所有被目标注解标记的元素
        Set<Element> targetElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(Subscribe.class);
        for (Element e : targetElements) {
            try {
                if (!SuperficialValidation.validateElement(e))
                    continue;
                if (!Util.isStandardEncloseingClass(e) || !Util.isStandardMethod(e))
                    continue;
//                Printer.SamplePrint2(e);
                Printer.SamplePrint3(e);
                addProxy(e);
            } catch (Exception ee) {
                ee.printStackTrace();
                Printer.PrintError(e, ee.getMessage());
            }
        }
        createProxy();
        return true;
    }

    /**
     * 替代@SupportedAnnotationTypes
     *
     * @return 支持的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add(Subscribe.class.getCanonicalName());
        types.add(RxThread.class.getCanonicalName());
        return types;
    }

    /**
     * 替代@SupportedSourceVersion
     *
     * @return 支持的java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        //默认返回1.6，改为支持最新版本
        return SourceVersion.latestSupported();
    }

    private void addProxy(Element e) {
        TypeElement clazz = (TypeElement) e.getEnclosingElement();
        ProxyBuilder proxyBuilder = PROXYS.get(clazz);
        if (proxyBuilder == null) {
            proxyBuilder = new ProxyBuilder(ClassName.get(clazz));
            PROXYS.put(clazz, proxyBuilder);
        }

        ThreadType threadType = ThreadType.Immediate;
//        List<? extends AnnotationMirror> annoList = e.getAnnotationMirrors();
//        for (AnnotationMirror mirror : annoList) {
//            Element annoElement = mirror.getAnnotationType().asElement();
//            if (annoElement.getKind().equals(ANNOTATION_TYPE)) {
//                if (RxThread.class.getCanonicalName().equals(annoElement.toString())) {
//                    Printer.PrintNote(mMessager, e, "Element is %s", e.getSimpleName().toString());
//                    Printer.PrintNote(mMessager, e, "annoElement is %s", annoElement.getSimpleName().toString());
//                    threadType = annoElement.getAnnotation(RxThread.class).value();
//                }
//            }
//        }
        RxThread rxThread = e.getAnnotation(RxThread.class);
        if (rxThread != null)
            threadType = rxThread.value();

        MethodBinder methodBinder = new MethodBinder();
        methodBinder.setMethodName(e.getSimpleName().toString());
        methodBinder.setThreadType(threadType);

        String[] tags = e.getAnnotation(Subscribe.class).value();
        for (String tag : tags)
            methodBinder.addTag(tag);

        ExecutableElement executableElement = (ExecutableElement) e;
        int size = executableElement.getParameters().size();
        if (size > 1) {
            Printer.PrintError(executableElement, "%s paramters size can't more than 1!", executableElement.getSimpleName().toString());
        } else if (size == 1) {
            VariableElement ve = executableElement.getParameters().get(0);
            TypeKind kind = ve.asType().getKind();
            if (kind.isPrimitive()) {
                methodBinder.addParamType(Util.TypeUtils.getPrimitiveType(ve.asType().getKind()));
            } else {
                if (kind.equals(TypeKind.ARRAY)) {
                    //数组
                    methodBinder.addParamType((ArrayType) ve.asType());
                } else if (kind.equals(TypeKind.DECLARED)) {
                    //类或接口
                    DeclaredType dt = (DeclaredType) ve.asType();
                    methodBinder.addParamType(dt.asElement().asType());
                    for (TypeMirror tm : dt.getTypeArguments()) {
                        methodBinder.addParamType(tm);
                    }
                }
            }
        } else {

        }
        proxyBuilder.addMethod(methodBinder);
    }

    private void createProxy() {
        for (ProxyBuilder pb : PROXYS.values()) {
            pb.build(Util.Filer);
//            Printer.testPrint(pb.getClassName(), pb.toString());
        }
    }
}
