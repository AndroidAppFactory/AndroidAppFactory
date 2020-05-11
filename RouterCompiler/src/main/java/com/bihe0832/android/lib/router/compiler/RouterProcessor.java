package com.bihe0832.android.lib.router.compiler;

import com.bihe0832.android.lib.router.annotation.Module;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {
    private static final String STUB_PACKAGE_NAME = "com.bihe0832.android.lib.router.stub";
    private static final String ROUTER_PACKAGE_NAME = "com.bihe0832.android.lib.router";

    private static final boolean DEBUG = false;
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> ret = new HashSet<>();
        ret.add(Module.class.getCanonicalName());
        return ret;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debug("process apt with " + annotations.toString());
        if (annotations.isEmpty()) {
            return false;
        }

        ArrayList<String> modulesNameList = new ArrayList<>();
        Set<? extends Element> moduleList = roundEnv.getElementsAnnotatedWith(Module.class);
        debug("process moduleList: " + moduleList.size());
        if (moduleList != null && moduleList.size() > 0) {
            for (Element tempModule: moduleList) {
                String moduleName = "RouterMapping";
                Module annotation = tempModule.getAnnotation(Module.class);
                moduleName = moduleName + "_" + annotation.value();
                debug("process moduleName: " + moduleName);
                if(!modulesNameList.contains(moduleName)){
                    modulesNameList.add(moduleName);
                }else{
                    throw new RuntimeException("Module "+ moduleName + "has been add");
                }
            }
        }
        debug("generate default RouterInit");
        return handleRouters(modulesNameList, roundEnv);
    }

    private boolean handleRouters(ArrayList<String> modulesNameList, RoundEnvironment roundEnv) {
        for (String moduleName:modulesNameList ) {
            if(!handleRouter(moduleName,roundEnv)){
                return false;
            }
        }
        return true;
    }

    private boolean handleRouter(String genClassName, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Module.class);

        MethodSpec.Builder mapMethod = MethodSpec.methodBuilder("map")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .addCode("\n");

        for (Element element : elements) {
            Module router = element.getAnnotation(Module.class);
            ClassName className = ClassName.get((TypeElement) element);
            mapMethod.addStatement(ROUTER_PACKAGE_NAME + ".Routers.map($S, $T.class)", router.value(), className);
            mapMethod.addCode("\n");
        }
        TypeSpec routerMapping = TypeSpec.classBuilder(genClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(mapMethod.build())
                .build();
        try {
            JavaFile.builder(STUB_PACKAGE_NAME, routerMapping)
                    .build()
                    .writeTo(filer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    private void debug(String msg) {
        if (DEBUG) {
            messager.printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }
}
