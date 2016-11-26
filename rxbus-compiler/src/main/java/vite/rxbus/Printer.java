package vite.rxbus;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by trs on 16-11-25.
 */
final class Printer {
    private static Messager sMessager;

    public static void setMessager(Messager messager) {
        sMessager = messager;
    }

    public static void SamplePrint(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final String fileLocation = "/home/trs/AndroidStudioProjects/RxBus/testprint" + System.currentTimeMillis() + ".txt";
        File file = new File(fileLocation);
        try {
            if (!file.exists())
                file.createNewFile();

            FileWriter writer = new FileWriter(file, false);
            writer.write("Annotations: " + annotations.size() + "\n");
            for (TypeElement e : annotations) {
                writer.write("SimpleName: " + e.getSimpleName() + "\n");
                writer.write("QualifiedName: " + e.getQualifiedName() + "\n");

                TypeMirror mirror = e.getSuperclass();
                NestingKind kind = e.getNestingKind();
                Element element = e.getEnclosingElement();
                List<Element> elementList = (List<Element>) e.getEnclosedElements();
                List<TypeMirror> interfaces = (List<TypeMirror>) e.getInterfaces();
                List<TypeParameterElement> parameterElements = (List<TypeParameterElement>) e.getTypeParameters();
                writer.write("TypeMirror: " + mirror + "\n");
                writer.write("NestingKind: " + kind + "\n");
                writer.write("EnclosingElement: " + element + "\n");
                writer.write("EnclosedElements: " + elementList + "\n");
                writer.write("Interfaces: " + interfaces + "\n");
                writer.write("TypeParameterElement: " + parameterElements + "\n\n");
            }

            writer.write("=================================================\n\n");

            Set<Element> elements = (Set<Element>) roundEnv.getRootElements();
            writer.write("Elements: " + elements.size() + "\n");
            for (Element e : elements) {
                writer.write("SimpleName: " + e.getSimpleName() + "\n");

                TypeMirror mirror = e.asType();
                ElementKind kind = e.getKind();
                Set<Modifier> modifiers = e.getModifiers();
                Element element = e.getEnclosingElement();
                List<Element> elementList = (List<Element>) e.getEnclosedElements();
                List<AnnotationMirror> annotationMirrors = (List<AnnotationMirror>) e.getAnnotationMirrors();
                writer.write("TypeMirror: " + mirror + "\n");
                writer.write("ElementKind: " + kind + "\n");
                writer.write("Modifiers: " + modifiers + "\n");
                writer.write("EnclosingElement: " + element + "\n");
                writer.write("EnclosedElements: " + elementList + "\n");
                writer.write("AnnotationMirrors: " + annotationMirrors + "\n\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            sMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    public static void SamplePrint2(Element targetElement) {
        final String fileLocation = "/home/trs/AndroidStudioProjects/RxBus/SamplePrint2.txt";
        File file = new File(fileLocation);
        try {
            if (!file.exists())
                file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file, true);
            writer.write("Elements: \n");
            writer.write("SimpleName: " + targetElement.getSimpleName() + "\n");

            TypeMirror mirror = targetElement.asType();
            ElementKind kind = targetElement.getKind();
            Set<Modifier> modifiers = targetElement.getModifiers();
            Element element = targetElement.getEnclosingElement();
            List<Element> elementList = (List<Element>) targetElement.getEnclosedElements();
            List<AnnotationMirror> annotationMirrors = (List<AnnotationMirror>) targetElement.getAnnotationMirrors();
            writer.write("TypeMirror: " + mirror + "\n");
            writer.write("ElementKind: " + kind + "\n");
            writer.write("Modifiers: " + modifiers + "\n");
            writer.write("EnclosingElement: " + element + "\n");
            writer.write("EnclosedElements: " + elementList + "\n");
            writer.write("AnnotationMirrors: " + annotationMirrors + "\n\n");

            writeElement(writer, element);

            writer.flush();
            writer.close();
        } catch (Exception e) {
            sMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private static void writeElement(FileWriter writer, Element targetElement) {
        TypeMirror mirror = targetElement.asType();
        ElementKind kind = targetElement.getKind();
        Set<Modifier> modifiers = targetElement.getModifiers();
        Element element = targetElement.getEnclosingElement();
        List<Element> elementList = (List<Element>) targetElement.getEnclosedElements();
        List<AnnotationMirror> annotationMirrors = (List<AnnotationMirror>) targetElement.getAnnotationMirrors();
        try {
            writer.write("TypeMirror: " + mirror + "\n");
            writer.write("ElementKind: " + kind + "\n");
            writer.write("Modifiers: " + modifiers + "\n");
            writer.write("EnclosingElement: " + element + "\n");
            writer.write("EnclosedElements: " + elementList + "\n");
            writer.write("AnnotationMirrors: " + annotationMirrors + "\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
