package xyz.xiezc.ioc.starter.common.asm;

import org.objectweb.asm.*;
import xyz.xiezc.ioc.starter.core.definition.ParamDefinition;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import static org.objectweb.asm.Opcodes.ASM8;

public class AsmUtil {

    /**
     * 获取方法的参数信息， 包含参数名称和注解
     *
     * @return
     */
    public static ParamDefinition[] getMethodParamsAndAnnotaton(Method method) {
        final ParamDefinition[] paramNames = new ParamDefinition[method.getParameterTypes().length];
        final String n = method.getDeclaringClass().getName();

        ClassReader cr;
        try {
            cr = new ClassReader(n);
        } catch (IOException e) {
            return null;
        }

        cr.accept(new ClassVisitor(Opcodes.ASM8) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                final Type[] args = Type.getArgumentTypes(desc);
                // The method paramName is the same and the number of parameters is the same
                if (!name.equals(method.getName()) || !sameType(args, method.getParameterTypes())) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }

                MethodVisitor v = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(ASM8, v) {
                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        int i = index - 1;
                        // if it is a static method, the first is the parameter
                        // if it's not a static method, the first one is "this" and then the parameter of the method
                        if (Modifier.isStatic(method.getModifiers())) {
                            i = index;
                        }
                        Parameter[] parameters = method.getParameters();

                        ParamDefinition paramDefinition = new ParamDefinition();
                        if (i >= 0 && i < paramNames.length) {
                            paramDefinition.setParamName(name);
                            paramDefinition.setParameter(parameters[i]);
                            paramNames[i] = paramDefinition;
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }

                    @Override
                    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
                        return super.visitParameterAnnotation(parameter, descriptor, visible);
                    }
                };
            }
        }, 0);

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            paramNames[i].setParameter(parameters[i]);
        }

        return paramNames;
    }

    private static boolean sameType(Type[] types, Class<?>[] classes) {
        if (types.length != classes.length) return false;
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(classes[i]).equals(types[i])) return false;
        }
        return true;
    }
}
