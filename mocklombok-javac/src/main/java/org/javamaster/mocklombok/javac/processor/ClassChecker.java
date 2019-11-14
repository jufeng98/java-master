package org.javamaster.mocklombok.javac.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementScanner8;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * 仿照findbugs等实现一个简单的类编写规范检查
 *
 * @author yudong
 * @date 2019/1/23
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClassChecker extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getRootElements();
        ClassScanner scanner8 = new ClassScanner();
        for (Element element : elements) {
            scanner8.scan(element);
        }
        return false;
    }
}

class ClassScanner extends ElementScanner8<Set<? extends Element>, Element> {

    private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private static final List<String> EXCLUDES = Collections.singletonList("serialVersionUID");
    private Pattern pattern1 = Pattern.compile("^([A-Z]+_*)+[A-Z]+$");
    private Pattern pattern2 = Pattern.compile("^[a-z]+[a-zA-Z0-9]*$");

    @Override
    public Set<? extends Element> visitVariable(VariableElement e, Element element) {
        String name = e.getSimpleName().toString();
        if (e.getConstantValue() != null || e.getEnclosingElement().getKind() == ElementKind.ENUM) {
            if (EXCLUDES.contains(name)) {
                return super.visitVariable(e, element);
            }
            if (!pattern1.matcher(name).matches()) {
                logger.warning(e.getEnclosingElement() + "的静态常量或枚举" + name + "不符合命名规范,应为大写字母加下划线组成");
                return super.visitVariable(e, element);
            }
        } else {
            if (!pattern2.matcher(name).matches()) {
                logger.warning(e.getEnclosingElement() + "的实例变量" + name + "不符合驼峰命名法");
            }
        }
        return super.visitVariable(e, element);
    }

    @Override
    public Set<? extends Element> visitPackage(PackageElement e, Element element) {
        return super.visitPackage(e, element);
    }

    @Override
    public Set<? extends Element> visitType(TypeElement e, Element element) {
        final char a = 'A';
        final char z = 'Z';
        if (!(e.getSimpleName().charAt(0) >= a && e.getSimpleName().charAt(0) <= z)) {
            logger.warning(e.getQualifiedName() + "类名不符合规范,需以大写字母开头");
        }
        return super.visitType(e, element);
    }

    @Override
    public Set<? extends Element> visitExecutable(ExecutableElement e, Element element) {
        if (e.getReturnType().getKind() == TypeKind.BOOLEAN) {
            String isPrefix = "is";
            if (!e.getSimpleName().toString().startsWith(isPrefix)) {
                logger.warning(e.getEnclosingElement() + "返回boolean值的方法" + e.getSimpleName() + "应以is开头");
            }
        }
        return super.visitExecutable(e, element);
    }

    @Override
    public Set<? extends Element> visitTypeParameter(TypeParameterElement e, Element element) {
        return super.visitTypeParameter(e, element);
    }
}
