package vite.rxbus.compiler;

import com.google.auto.common.SuperficialValidation;
import com.squareup.javapoet.TypeName;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
import javax.tools.Diagnostic;

/**
 * Created by trs on 16-11-25.
 */
final class Printer {

    public static void PrintError(Element element, String format, Object... args) {
        Print(Diagnostic.Kind.ERROR, element, format, args);
    }

    public static void PrintNote(Element element, String format, Object... args) {
        Print(Diagnostic.Kind.NOTE, element, format, args);
    }

    private static void Print(Diagnostic.Kind kind, Element element, String format, Object... args) {
        if (args.length > 0)
            format = String.format(format, args);
        Util.Messager.printMessage(kind, format, element);
    }
}
