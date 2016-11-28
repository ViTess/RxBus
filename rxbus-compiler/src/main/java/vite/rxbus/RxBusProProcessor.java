package vite.rxbus;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.google.common.collect.HashMultimap;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import vite.rxbus.thread.RxComputation;
import vite.rxbus.thread.RxIO;
import vite.rxbus.thread.RxImmediate;
import vite.rxbus.thread.RxMainThread;
import vite.rxbus.thread.RxNewThread;
import vite.rxbus.thread.RxTrampoline;

/**
 * Created by trs on 16-11-24.
 */
@AutoService(Processor.class)
public class RxBusProProcessor extends AbstractProcessor {

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

        Printer.setMessager(mMessager);
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
            if (!SuperficialValidation.validateElement(e))
                continue;

            Printer.SamplePrint2(e);
            if (!Util.isStandardEncloseingClass(e) || !Util.isStandardMethod(e))
                continue;


        }
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
        types.add(RxMainThread.class.getCanonicalName());
        types.add(RxIO.class.getCanonicalName());
        types.add(RxComputation.class.getCanonicalName());
        types.add(RxNewThread.class.getCanonicalName());
        types.add(RxTrampoline.class.getCanonicalName());
        types.add(RxImmediate.class.getCanonicalName());
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

    /**
     * 获取一个class中含有目标注解的、符合要求的所有元素
     *
     * @param element
     */
    private void findAnnotationTargets(Element element) {
        //获取类中的所有元素，包括方法、变量、静态方法、静态变量、内部类、内部接口等
    }
}
