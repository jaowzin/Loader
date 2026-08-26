package dev.jaowzin.carromloader.bridge;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import dev.jaowzin.carromloader.bridge.annotation.BClass;
import dev.jaowzin.carromloader.bridge.annotation.BConstructor;
import dev.jaowzin.carromloader.bridge.annotation.BField;
import dev.jaowzin.carromloader.bridge.annotation.BMethod;
import dev.jaowzin.carromloader.bridge.annotation.BStaticField;
import dev.jaowzin.carromloader.bridge.annotation.BStaticMethod;
import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.proxy.RuntimeBridgeInterfaceProxy;
import dev.jaowzin.carromloader.bridge.proxy.RuntimeBridgeProxy;


@AutoService(Processor.class)
public class RuntimeBridgeProcessor extends AbstractProcessor {

    private Map<String, RuntimeBridgeProxy> mRuntimeBridgeProxies;
    private Map<String, RuntimeBridgeInterfaceProxy> mRuntimeBridgeInterfaceProxies;
    private Map<String, String> mRealMaps = new HashMap<>();

    private Messager mMessager;
    private Elements mElementUtils; 
    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mRuntimeBridgeProxies = new Hashtable<>();
        mRuntimeBridgeInterfaceProxies = new Hashtable<>();
        mRealMaps = new Hashtable<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(BClass.class.getCanonicalName());
        supportTypes.add(BClassName.class.getCanonicalName());

        supportTypes.add(BField.class.getCanonicalName());
        supportTypes.add(BStaticField.class.getCanonicalName());

        supportTypes.add(BMethod.class.getCanonicalName());
        supportTypes.add(BStaticMethod.class.getCanonicalName());

        supportTypes.add(BConstructor.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mRuntimeBridgeProxies.clear();
        mRuntimeBridgeInterfaceProxies.clear();
        mRealMaps.clear();

        for (Element element : roundEnv.getElementsAnnotatedWith(BClassName.class)) {
            BClassName annotation = element.getAnnotation(BClassName.class);
            doProcess(element, annotation.value());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(BClass.class)) {
            BClass annotation = element.getAnnotation(BClass.class);
            String aClass = getClass(annotation).toString();
            doProcess(element, aClass);
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(BStaticMethod.class)) {
            doInterfaceProcess(element, true, false);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(BMethod.class)) {
            doInterfaceProcess(element, false, false);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(BStaticField.class)) {
            doInterfaceProcess(element, true, true);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(BField.class)) {
            doInterfaceProcess(element, false, true);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(BConstructor.class)) {
            doInterfaceProcess(element, true, false);
        }

        for (RuntimeBridgeInterfaceProxy value : mRuntimeBridgeInterfaceProxies.values()) {
            try {
                value.setRealMap(mRealMaps);
                value.generateInterfaceCode().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (RuntimeBridgeProxy value : mRuntimeBridgeProxies.values()) {
            try {
                value.generateJavaCode().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void doInterfaceProcess(Element element, boolean isStatic, boolean isField) {
        String className = element.getEnclosingElement().asType().toString();
        ExecutableElement executableElement = (ExecutableElement) element;
        String packageName = mElementUtils.getPackageOf(executableElement).getQualifiedName().toString();

        RuntimeBridgeInterfaceInfo interfaceInfo = new RuntimeBridgeInterfaceInfo();
        interfaceInfo.setExecutableElement(executableElement);
        interfaceInfo.setField(isField);

        RuntimeBridgeInterfaceProxy reflectionInterfaceProxy = getReflectionInterfaceProxy(packageName,
                className + (isStatic ? "Static" : "Context"),
                className);
        reflectionInterfaceProxy.add(interfaceInfo);
    }

    private void doProcess(Element element, String realClassName) {
        String packageName = mElementUtils.getPackageOf(element).getQualifiedName().toString();
        String className = element.asType().toString();
        RuntimeBridgeInfo info = new RuntimeBridgeInfo();
        info.setRealClass(realClassName);
        info.setClassName(className);

        getReflectionProxy(packageName, className, info);

        
        getReflectionInterfaceProxy(packageName, className + "Context",
                className);
        getReflectionInterfaceProxy(packageName, className + "Static",
                className);
        mRealMaps.put(className, realClassName);
    }

    private static TypeMirror getClass(BClass annotation) {
        try {
            annotation.value(); 
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null; 
    }

    public RuntimeBridgeProxy getReflectionProxy(String packageName, String className, RuntimeBridgeInfo info) {
        RuntimeBridgeProxy runtimeBridgeProxy = mRuntimeBridgeProxies.get(className);
        if (runtimeBridgeProxy == null) {
            runtimeBridgeProxy = new RuntimeBridgeProxy(packageName, info);
            mRuntimeBridgeProxies.put(className, runtimeBridgeProxy);
        }
        return runtimeBridgeProxy;
    }

    public RuntimeBridgeInterfaceProxy getReflectionInterfaceProxy(String packageName, String className, String origClassName) {
        RuntimeBridgeInterfaceProxy runtimeBridgeProxy = mRuntimeBridgeInterfaceProxies.get(className);
        if (runtimeBridgeProxy == null) {
            runtimeBridgeProxy = new RuntimeBridgeInterfaceProxy(packageName, className, origClassName);
            mRuntimeBridgeInterfaceProxies.put(className, runtimeBridgeProxy);
        }
        return runtimeBridgeProxy;
    }
}
