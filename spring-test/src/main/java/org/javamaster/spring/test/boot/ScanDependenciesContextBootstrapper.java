package org.javamaster.spring.test.boot;

import lombok.SneakyThrows;
import org.javamaster.spring.test.annos.ScanTestedDependencies;
import org.javamaster.spring.test.utils.TestUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.WebTestContextBootstrapper;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2021/5/15
 */
public class ScanDependenciesContextBootstrapper extends WebTestContextBootstrapper {

    private final Set<Class<?>> alreadyHandle = new HashSet<>();

    private Vector<?> allTargetClasses;

    @Override
    @SuppressWarnings("all")
    protected MergedContextConfiguration processMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
        MergedContextConfiguration mergedContextConfiguration = super.processMergedContextConfiguration(mergedConfig);
        Class<?> testClass = mergedConfig.getTestClass();
        ScanTestedDependencies annotation = testClass.getAnnotation(ScanTestedDependencies.class);

        Class<?> targetTestedClass = annotation.value();

        initTargetAllClasses(targetTestedClass.getClassLoader());

        List<Class<?>> list = getDependencyClasses(targetTestedClass);

        Class<?>[] interfaces = annotation.additionalInterfaces();
        for (Class<?> additionalInterface : interfaces) {
            List<Class<?>> implClasses = getInterfaceImplClasses(additionalInterface);
            list.addAll(implClasses);
            for (Class<?> clazz : implClasses) {
                list.addAll(getDependencyClasses(clazz));
            }
        }

        list = list.stream()
                .filter(clz -> !clz.getName().startsWith("org.springframework") && !Modifier.isAbstract(clz.getModifiers()))
                .collect(Collectors.toList());

        Class<?>[] classes = mergedConfig.getClasses();
        Class<?>[] targetClasses = list.toArray(new Class<?>[]{});

        Class<?>[] allClasses = new Class[classes.length + targetClasses.length];
        System.arraycopy(classes, 0, allClasses, 0, classes.length);
        System.arraycopy(targetClasses, 0, allClasses, classes.length, targetClasses.length);

        TestUtils.reflectSet(mergedContextConfiguration, "classes", allClasses);
        return mergedContextConfiguration;
    }

    private List<Class<?>> getDependencyClasses(Class<?> clz) {
        List<Field> fields = getFields(clz);
        return getFieldRelateClasses(fields);
    }

    private List<Field> getFields(Class<?> clz) {
        Field[] declaredFields = clz.getDeclaredFields();
        List<Field> list = new ArrayList<>(Arrays.asList(declaredFields));

        Class<?> superclass = clz.getSuperclass();
        if (superclass != Object.class) {
            List<Field> fields = getFields(superclass);
            list.addAll(fields);
        }
        return list;
    }

    private List<Class<?>> getFieldRelateClasses(List<Field> fields) {
        List<Class<?>> list = new ArrayList<>();
        for (Field declaredField : fields) {
            if (declaredField.getAnnotation(Autowired.class) == null) {
                continue;
            }
            Class<?> fieldTypeClass = declaredField.getType();
            if (alreadyHandle.contains(fieldTypeClass)) {
                continue;
            }
            alreadyHandle.add(fieldTypeClass);
            if (fieldTypeClass.isInterface()) {
                List<Class<?>> allSubClass = getInterfaceImplClasses(fieldTypeClass);
                list.addAll(allSubClass);
                for (Class<?> subClass : allSubClass) {
                    List<Class<?>> relateClasses = getFieldRelateClasses(Arrays.asList(subClass.getDeclaredFields()));
                    list.addAll(relateClasses);
                }
            } else {
                list.add(fieldTypeClass);
                List<Class<?>> relateClasses = getFieldRelateClasses(Arrays.asList(fieldTypeClass.getDeclaredFields()));
                list.addAll(relateClasses);
            }
        }
        return list;
    }


    /**
     * 找到接口的所有实现类
     */
    @SneakyThrows
    private List<Class<?>> getInterfaceImplClasses(Class<?> interfaceClass) {
        if (interfaceClass == Logger.class) {
            return Collections.emptyList();
        }
        List<Class<?>> allSubclass = new ArrayList<>();
        for (Object o : allTargetClasses) {
            Class<?> c = (Class<?>) o;
            if (interfaceClass.isAssignableFrom(c) && !c.isInterface()) {
                allSubclass.add(c);
            }
        }
        return allSubclass;
    }

    @SneakyThrows
    private void initTargetAllClasses(ClassLoader classLoader) {
        String testClassPath = Objects.requireNonNull(classLoader.getResource("")).getFile();
        File targetPath = new File(testClassPath).getParentFile();
        File classPath = new File(targetPath, "classes");
        String classPathStr = classPath.toString();
        Files.walkFileTree(classPath.toPath(), new SimpleFileVisitor<Path>() {
            @SneakyThrows
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".class")) {
                    String str = file.toString()
                            .replace(classPathStr, "")
                            .replace(".class", "")
                            .replace("\\", ".");
                    Class.forName(str.substring(1));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Class<?> clazz = classLoader.getClass();
        while (clazz != ClassLoader.class) {
            clazz = clazz.getSuperclass();
        }
        Field field = clazz.getDeclaredField("classes");
        field.setAccessible(true);
        allTargetClasses = (Vector<?>) field.get(classLoader);
    }

}
