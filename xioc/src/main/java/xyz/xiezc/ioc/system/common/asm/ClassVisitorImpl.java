package xyz.xiezc.ioc.system.common.asm;

import cn.hutool.core.util.StrUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.ASM8;

public class ClassVisitorImpl extends ClassVisitor {


    final String[] paramNames;

    Method method;

    public ClassVisitorImpl(int api, final String[] paramNames, Method method) {
        super(api);
        this.paramNames = paramNames;
        this.method = method;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

        String name1 = method.getName();
        if (!StrUtil.equals(name1, name)) {
            return methodVisitor;
        }

        return new MethodVisitorImpl(ASM8, paramNames, methodVisitor);
    }
}

