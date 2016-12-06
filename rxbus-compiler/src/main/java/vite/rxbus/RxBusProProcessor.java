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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import vite.rxbus.BindBuilder.MethodValue;

import vite.rxbus.thread.RxComputation;
import vite.rxbus.thread.RxIO;
import vite.rxbus.thread.RxImmediate;
import vite.rxbus.thread.RxMainThread;
import vite.rxbus.thread.RxNewThread;
import vite.rxbus.thread.RxTrampoline;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;

/**
 * Created by trs on 16-11-24.
 */
@AutoService(Processor.class)
public class RxBusProProcessor extends AbstractProcessor {

    private static final HashMap<TypeElement, BindBuilder> BindBuilderCache = new HashMap<>();

    private HashSet<String> rxThreadTypes = new HashSet<>();

    private Types mTypeUtils;//处理TypeMirror
    private Elements mElementUtils;//处理Element
    private Filer mFiler;//一般用于生成文件、获取文件
    private Messager mMessager;//打印信息用

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
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
                Printer.SamplePrint3(mTypeUtils, mElementUtils, e, mMessager);

                putBindBuilderCache(e);
            } catch (Exception ee) {
                ee.printStackTrace();
                mMessager.printMessage(Diagnostic.Kind.ERROR, ee.getMessage());
            }
        }

        createBinder();
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

        rxThreadTypes = new HashSet<>();
        rxThreadTypes.add(RxMainThread.class.getCanonicalName());
        rxThreadTypes.add(RxIO.class.getCanonicalName());
        rxThreadTypes.add(RxComputation.class.getCanonicalName());
        rxThreadTypes.add(RxNewThread.class.getCanonicalName());
        rxThreadTypes.add(RxTrampoline.class.getCanonicalName());
        rxThreadTypes.add(RxImmediate.class.getCanonicalName());
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

    private void putBindBuilderCache(Element e) {
        TypeElement clazz = (TypeElement) e.getEnclosingElement();
        BindBuilder bindBuilder = BindBuilderCache.get(clazz);
        if (bindBuilder == null) {
            bindBuilder = new BindBuilder(ClassName.get(clazz));
            BindBuilderCache.put(clazz, bindBuilder);
        }

        Set<String> tags = new LinkedHashSet<>();
        String name = e.getSimpleName().toString();
        Class threadType = null;
        VariableElement paramType = ((ExecutableElement) e).getParameters().get(0);
        List<? extends AnnotationMirror> annoList = e.getAnnotationMirrors();
        for (AnnotationMirror mirror : annoList) {
            Element annoElement = mirror.getAnnotationType().asElement();
            if (annoElement.getKind().equals(ANNOTATION_TYPE)) {
                if (rxThreadTypes.contains(annoElement.toString())) {
                    String threadName = annoElement.getSimpleName().toString();
                    if ("RxMainThread".equals(threadName))
                        threadType = RxMainThread.class;
                    else if ("RxIO".equals(threadName))
                        threadType = RxIO.class;
                    else if ("RxComputation".equals(threadName))
                        threadType = RxComputation.class;
                    else if ("RxNewThread".equals(threadName))
                        threadType = RxNewThread.class;
                    else if ("RxTrampoline".equals(threadName))
                        threadType = RxTrampoline.class;
                    else if ("RxImmediate".equals(threadName))
                        threadType = RxImmediate.class;
                    else
                        threadType = RxMainThread.class;
                } else
                    threadType = RxMainThread.class;

                Map<? extends ExecutableElement, ? extends AnnotationValue> map = mElementUtils.getElementValuesWithDefaults(mirror);
                Iterator iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    //TODO : add tag
                    List<String> values = (List<String>) ((AnnotationValue) entry.getValue()).getValue();
                    tags.addAll(values);
                }
            }
        }

        MethodValue methodValue = bindBuilder.createMethodValue(name, threadType, paramType);
        methodValue.setTag(tags);
    }

    private void createBinder() {
        for (BindBuilder builder : BindBuilderCache.values())
            builder.build(mFiler);
    }
}
