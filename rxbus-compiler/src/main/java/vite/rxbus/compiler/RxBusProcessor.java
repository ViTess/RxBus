package vite.rxbus.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import vite.rxbus.RxThread;
import vite.rxbus.Subscribe;
import vite.rxbus.ThreadType;

/**
 * Created by trs on 16-11-24.
 */
@AutoService(Processor.class)
public class RxBusProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Util.init(processingEnv);
    }

    /**
     * @param annotations 该处理器声明支持并已经在源码中使用了的注解
     * @param roundEnv    注解处理的上下文环境妈的
     * @return true:支持的注解处理完毕；false:支持的注解在该处理器处理完毕后，接着给其他处理器处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<TypeElement, ProxyBuilder> PROXYS = new LinkedHashMap<>();

        //获取所有被目标注解标记的元素
        Set<Element> targetElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(Subscribe.class);
        for (Element e : targetElements) {
            try {
                if (!SuperficialValidation.validateElement(e))
                    continue;
                if (!Util.isStandardEncloseingClass(e) || !Util.isStandardMethod(e))
                    continue;
                if (!Util.checkParamters(e)) {
                    //it will break out build
                    Printer.PrintError(e, "%s Parameters that implement Map, List, Collectin are not supported!", e);
                }
                addProxy(PROXYS, e);
            } catch (Exception exception) {
                exception.printStackTrace();
                Printer.PrintError(e, exception.getMessage());
            }
        }
        createProxy(PROXYS);
        return false;
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

    private void addProxy(Map<TypeElement, ProxyBuilder> PROXYS, Element e) {
        TypeElement clazz = (TypeElement) e.getEnclosingElement();
        ProxyBuilder proxyBuilder = PROXYS.get(clazz);
        if (proxyBuilder == null) {
            proxyBuilder = new ProxyBuilder(ClassName.get(clazz));
            PROXYS.put(clazz, proxyBuilder);
        }

        ThreadType threadType = ThreadType.MainThread;
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
        if (executableElement.getParameters().size() == 1) {
            VariableElement ve = executableElement.getParameters().get(0);
            methodBinder.setParamType(ve.asType());
        }
        proxyBuilder.addMethod(methodBinder);
    }

    private void createProxy(Map<TypeElement, ProxyBuilder> PROXYS) {
        for (Map.Entry<TypeElement, ProxyBuilder> entry : PROXYS.entrySet()) {
            TypeElement e = entry.getKey();
            ProxyBuilder pb = entry.getValue();
            try {
                pb.build(Util.Filer);
            } catch (IOException IOE) {
                IOE.printStackTrace();
                Printer.PrintError(e, "Unable to create RxBus proxy for type %s", e);
            }
        }
    }
}
