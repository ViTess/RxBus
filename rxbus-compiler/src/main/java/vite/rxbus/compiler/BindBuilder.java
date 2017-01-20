//package vite.rxbus;
//
//import com.squareup.javapoet.ClassName;
//import com.squareup.javapoet.FieldSpec;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.ParameterizedTypeName;
//import com.squareup.javapoet.TypeName;
//import com.squareup.javapoet.TypeSpec;
//import com.squareup.javapoet.TypeVariableName;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//import javax.annotation.processing.Filer;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.element.VariableElement;
//
///**
// * build the java class
// * Created by trs on 16-11-28.
// */
//final class BindBuilder {
//    private static final String CLASS_UNIFORM_MARK = "$$BusBinder";
//
//    private static final ClassName INTERFACE_BUSBINDER = ClassName.get("vite.rxbus", "BusBinder");
//
//    private static final ClassName COLLECTIONS = ClassName.get("java.util", "Collection");
//    private static final ClassName MAP = ClassName.get("java.util", "Map");
//    private static final ClassName CONCURRENT_HASHMAP = ClassName.get("java.util.concurrent", "ConcurrentHashMap");
//    private static final ClassName COPYONWRITE_ARRAYSET = ClassName.get("java.util.concurrent", "CopyOnWriteArraySet");
//    private static final ClassName ANDROID_SCHEDULERS = ClassName.get("rx.android.schedulers", "AndroidSchedulers");
//    private static final ClassName ACTION1 = ClassName.get("rx.functions", "Action1");
//    private static final ClassName SUBJECT_KEEPER = ClassName.get("vite.rxbus", "SubjectKeeper");
//    private static final ClassName RXBUS = ClassName.get("vite.rxbus", "RxBus");
//
//    private static final TypeName TYPE_STRING = TypeName.get(String.class);
//
//    //Map<String, CopyOnWriteArraySet<SubjectKeeper>>
//    private static final ParameterizedTypeName MAP_CACHE = ParameterizedTypeName.get(MAP, TYPE_STRING,
//            ParameterizedTypeName.get(COPYONWRITE_ARRAYSET, SUBJECT_KEEPER));
//    //CopyOnWriteArraySet<SubjectKeeper>
//    private static final ParameterizedTypeName COW_ARRAYSET = ParameterizedTypeName.get(COPYONWRITE_ARRAYSET, SUBJECT_KEEPER);
//    //Collection<CopyOnWriteArraySet<SubjectKeeper>>
//    private static final ParameterizedTypeName COLLECTION_SET = ParameterizedTypeName.get(COLLECTIONS, COW_ARRAYSET);
//
//    private String packagePath;//包名路径，如com.example
//    private ClassName targetClassName;//目标类名
//    private Set<MethodValue> methods;
//
//    private int hashCode;
//
//    public BindBuilder(ClassName targetClassName) {
//        this.targetClassName = targetClassName;
//        this.packagePath = targetClassName.packageName();
//        methods = new LinkedHashSet<>();
//
//        hashCode = packagePath.hashCode() + targetClassName.simpleName().hashCode();
//    }
//
//
//    public MethodValue createMethodValue(String methodName, Class threadType, VariableElement element) {
//        MethodValue value = new MethodValue(methodName, threadType, element);
//        methods.add(value);
//        return value;
//    }
//
//    /**
//     * Build class for javapoet
//     *
//     * @param filer
//     */
//    public void build(Filer filer) {
//        JavaFile javaFile = JavaFile.builder(packagePath, createClass()).build();
//        try {
//            javaFile.writeTo(filer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void build(File file) {
//        JavaFile javaFile = JavaFile.builder(packagePath, createClass()).build();
//        try {
//            javaFile.writeTo(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private TypeSpec createClass() {
//        return TypeSpec.classBuilder(targetClassName.simpleName() + CLASS_UNIFORM_MARK)
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addTypeVariable(TypeVariableName.get(TypeVariableName.get("T").name, targetClassName))//继承泛型接口
//                .addSuperinterface(INTERFACE_BUSBINDER)//implements interface
//                .addFields(createFields())
//                .addMethods(createMethods())
//                .build();
//    }
//
//    private ArrayList<FieldSpec> createFields() {
//        ArrayList<FieldSpec> fields = new ArrayList<>();
//        fields.add(FieldSpec.builder(TypeVariableName.get("T"), "target", Modifier.PRIVATE).build());
//        //create the map
//        fields.add(FieldSpec.builder(ParameterizedTypeName.get(CONCURRENT_HASHMAP, TYPE_STRING
//                , ParameterizedTypeName.get(COPYONWRITE_ARRAYSET, SUBJECT_KEEPER))
//                , "keepers", Modifier.PRIVATE).build());
//        return fields;
//    }
//
//    private ArrayList<MethodSpec> createMethods() {
//        ArrayList<MethodSpec> methods = new ArrayList<>();
//        methods.add(createConstructor());
//        methods.add(createSetBinder());
//        methods.add(createRelease());
//        methods.add(createAdd());
//        return methods;
//    }
//
//    private MethodSpec createConstructor() {
//        return MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(TypeVariableName.get("T"), "target")
//                .addStatement("this.target = target")
//                .addStatement("keepers = new ConcurrentHashMap<>()")
//                .build();
//    }
//
//    private MethodSpec createSetBinder() {
//        MethodSpec.Builder builder = MethodSpec.methodBuilder("setBinders")
//                .addAnnotation(Override.class)
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(MAP_CACHE, "map");
//
//        builder.addStatement("$T.getScheduler(null)", RXBUS);
//        for (MethodValue methodValue : methods) {
//            for (String tag : methodValue.tags) {
//            }
//        }
//        return builder.build();
//    }
//
//    private MethodSpec createRelease() {
//        MethodSpec.Builder builder = MethodSpec.methodBuilder("release")
//                .addAnnotation(Override.class)
//                .addModifiers(Modifier.PUBLIC);
//        builder.addStatement("this.target = null")
//                .beginControlFlow("if (this.keepers != null)")
//                .addStatement("$T values = keepers.values()", COLLECTION_SET)
//                .beginControlFlow("for ($T sets : values)", COW_ARRAYSET)
//                .beginControlFlow("for ($T keeper : sets)", SUBJECT_KEEPER)
//                .addStatement("keeper.release()")
//                .endControlFlow()
//                .addStatement("sets.clear()")
//                .endControlFlow()
//                .addStatement("keepers.clear()")
//                .addStatement("keepers = null")
//                .endControlFlow();
//        return builder.build();
//    }
//
//    private MethodSpec createAdd() {
//        MethodSpec.Builder builder = MethodSpec.methodBuilder("add")
//                .addModifiers(Modifier.PRIVATE)
//                .addParameter(String[].class, "tags")
//                .addParameter(SUBJECT_KEEPER, "keeper")
//                .addParameter(MAP_CACHE, "map");
//        builder.beginControlFlow("for(String tag : tags)")
//                .addStatement("$T action = keepers.get(tag)", COW_ARRAYSET)
//                .beginControlFlow("if(action == null)")
//                .addStatement("action = new CopyOnWriteArraySet<>()")
//                .addStatement("keepers.put(tag, action)")
//                .endControlFlow()
//                .addStatement("action.add(keeper)")
//                .addStatement("action = map.get(tag)")
//                .beginControlFlow("if(action == null)")
//                .addStatement("action = new CopyOnWriteArraySet<>()")
//                .addStatement("keepers.put(tag, action)")
//                .endControlFlow()
//                .addStatement("action.add(keeper)")
//                .endControlFlow();
//        return builder.build();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null)
//            return false;
//        BindBuilder builder = (BindBuilder) obj;
//        if (builder.packagePath == null || builder.targetClassName == null)
//            return false;
//
//        if (builder.packagePath.equals(packagePath)
//                && builder.targetClassName.simpleName().equals(targetClassName.simpleName()))
//            return true;
//        else
//            return false;
//    }
//
//    @Override
//    public int hashCode() {
//        return hashCode;
//    }
//
//    /**
//     * targer class 's method
//     */
//    public final class MethodValue {
//        private String methodName;//方法名
//        private Set<String> tags;//对应的tag
//        private Class threadType;//对应的Rx线程
//        private VariableElement paramType;
//
//        private int hashCode;
//
//        private MethodValue(String methodName, Class threadType, VariableElement element) {
//            this.methodName = methodName;
//            this.threadType = threadType;
//            this.paramType = element;
//            tags = new LinkedHashSet<>();
//            hashCode = methodName.hashCode() + threadType.hashCode() + paramType.hashCode();
//        }
//
//        public void addTag(String tag) {
//            tags.add(tag);
//        }
//
//        public void setTag(Set<String> tags) {
//            this.tags.addAll(tags);
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null)
//                return false;
//            MethodValue value = (MethodValue) obj;
//            if (value.methodName == null || value.threadType == null || paramType == null)
//                return false;
//
//            if (value.methodName.equals(methodName)
//                    && value.threadType.equals(threadType)
//                    && value.paramType.equals(paramType))
//                return true;
//            else
//                return false;
//        }
//
//        @Override
//        public int hashCode() {
//            return hashCode;
//        }
//    }
//}
