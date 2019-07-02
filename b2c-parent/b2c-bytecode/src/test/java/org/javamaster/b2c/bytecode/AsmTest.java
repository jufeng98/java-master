package org.javamaster.b2c.bytecode;

import org.javamaster.b2c.bytecode.utils.ResourceUtils;
import org.junit.Test;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 测试为class文件添加新方法
 *
 * @author yudong
 * @date 2019/6/27
 */
public class AsmTest {
    @Test
    public void ModifyClassTest() throws Exception {
        // 读取的Person类的class文件
        ClassReader classReader = new ClassReader("org.javamaster.b2c.bytecode.model.Person");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassAdapter classAdapter = new ClassAdapter(classWriter);
        classReader.accept(classAdapter, ClassReader.SKIP_DEBUG);

        // 插入新方法
        // Opcodes.ACC_PUBLIC:方法修饰符为public
        // sayHello:方法名为sayHello
        // ()V:没有入参,返回类型为void
        // new String[]{"javax.validation.ValidationException"}:声明抛出ValidationException异常
        MethodVisitor helloVisitor = classWriter
                .visitMethod(Opcodes.ACC_PUBLIC, "sayHello", "()V", null, new String[]{"javax.validation.ValidationException"});
        // 插入方法体内容,以下涉及到了虚拟机字节码指令
        helloVisitor.visitCode();
        // getstatic 指令:取静态字段
        helloVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // ldc 指令:取"hello world!"常量到操作数栈
        helloVisitor.visitLdcInsn("hello world!");
        // invokevirtual 指令: 调用实例方法println
        helloVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
        // return 指令: 从当前方法返回void
        helloVisitor.visitInsn(Opcodes.RETURN);
        helloVisitor.visitMaxs(1, 1);
        helloVisitor.visitEnd();

        byte[] bytes = classWriter.toByteArray();

        String path = ResourceUtils.getFile("classpath:").getAbsolutePath();
        File file = new File(path, "/org/javamaster/b2c/bytecode/PersonModify.class");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }
}
