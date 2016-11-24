package vite.rxbus;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by trs on 16-11-24.
 */
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
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    /**
     * 替代@SupportedAnnotationTypes
     *
     * @return 支持的注解类型
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
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
}
