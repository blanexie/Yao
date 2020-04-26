package xyz.xiezc.ioc.common.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;


class MethodVisitorImpl extends MethodVisitor {


    String[] paramNames;

    public MethodVisitorImpl(int api, String[] paramNames, MethodVisitor mv) {
        super(api, mv);
        this.paramNames = paramNames;
    }


    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        paramNames[index] = name;
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public void visitParameter(String name, int access) {
        paramNames[access] = name;
        super.visitParameter(name, access);
    }


}
