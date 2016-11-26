package vite.rxbus;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * Created by trs on 16-11-25.
 */
final class Util {
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

        if (element.getModifiers().contains(PUBLIC) && !element.getModifiers().contains(STATIC))
            return false;

        return true;
    }
}
