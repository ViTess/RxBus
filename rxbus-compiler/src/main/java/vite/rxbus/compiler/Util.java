package vite.rxbus.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.type.TypeKind.ARRAY;

/**
 * Created by trs on 16-11-25.
 */
final class Util {
    public static Types TypeUtils;//处理TypeMirror
    public static Elements ElementUtils;//处理Element
    public static Filer Filer;//一般用于生成文件、获取文件
    public static Messager Messager;//打印信息用

    private static final ClassName TYPE_COLLECTION = ClassName.get(Collection.class);
    private static final ClassName TYPE_MAP = ClassName.get(Map.class);
    private static final ElementVisitor<Boolean, Void> TEST_VISITOR = new AbstractElementVisitor6<Boolean, Void>() {
        @Override
        public Boolean visitPackage(PackageElement packageElement, Void aVoid) {
            return true;
        }

        @Override
        public Boolean visitType(TypeElement typeElement, Void aVoid) {
            return true;
        }

        @Override
        public Boolean visitVariable(VariableElement variableElement, Void aVoid) {
            return true;
        }

        @Override
        public Boolean visitExecutable(ExecutableElement executableElement, Void aVoid) {
            if (executableElement.isVarArgs())
                Printer.PrintError(executableElement, "%s can't be a variable number of arguments", executableElement);
            return checkParamters(executableElement);
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterElement typeParameterElement, Void aVoid) {
            return true;
        }
    };

    public static void init(ProcessingEnvironment processingEnv) {
        TypeUtils = processingEnv.getTypeUtils();
        ElementUtils = processingEnv.getElementUtils();
        Filer = processingEnv.getFiler();
        Messager = processingEnv.getMessager();
    }

    /**
     * 判断该元素的上层元素是否符合目标元素的上层元素
     *
     * @param element
     * @return
     */
    public static final boolean isStandardEncloseingClass(Element element) {
        //判断上层元素是否为类，而且是否为public修饰
        //然后判断包名，android和java开头的不行
        TypeElement encloseingElement = (TypeElement) element.getEnclosingElement();
        if (encloseingElement.getKind() != CLASS)
            return false;

        if (!encloseingElement.getModifiers().contains(PUBLIC))
            return false;

        String qualifiedName = encloseingElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android") || qualifiedName.startsWith("java"))
            return false;
        return true;
    }

    /**
     * 判断是否为目标方法
     *
     * @param element
     * @return
     */
    public static final boolean isStandardMethod(Element element) {
        //元素类型必须为method，必须public修饰，不能为static
        if (element.getKind() != METHOD)
            return false;

        if (!element.getModifiers().contains(PUBLIC) || element.getModifiers().contains(STATIC))
            return false;

        return true;
    }

    public static final boolean checkParamters(Element e) {
        return e.accept(TEST_VISITOR, null);
    }

    private static final boolean checkParamters(ExecutableElement e) {
        List<VariableElement> parameters = (List<VariableElement>) e.getParameters();
        if (parameters.size() > 1) {
            Printer.PrintError(e, "%s The number of parameters can not be greater than 1!", e);
            return false;
        } else if (parameters.size() == 0)
            return true;
        else {
            TypeMirror tm = parameters.get(0).asType();
            if (tm.getKind().isPrimitive() || tm.getKind().equals(ARRAY))//排除基本类型
                return true;
            return checkParamters(parameters.get(0).asType());
        }
    }

    private static final boolean checkParamters(TypeMirror typeMirror) {
        TypeElement typeElement = (TypeElement) Util.TypeUtils.asElement(typeMirror);
        List<TypeMirror> list = (List<TypeMirror>) typeElement.getInterfaces();
        if (list == null || list.size() == 0)//没有接口当作true
            return true;

        ClassName cn;
        for (TypeMirror tm : list) {
            cn = ClassName.get((TypeElement) Util.TypeUtils.asElement(tm));
            if (cn.equals(TYPE_MAP) || cn.equals(TYPE_COLLECTION) || !checkParamters(tm))
                return false;
        }
        return true;
    }
}
