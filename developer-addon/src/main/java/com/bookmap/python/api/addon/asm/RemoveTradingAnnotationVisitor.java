package com.bookmap.python.api.addon.asm;

import static org.objectweb.asm.Opcodes.ASM8;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;

/**
 * We need this visitor to delete an annotation Layer1TradingStrategy
 * from RpcServerAddon if the addon won't perform any trading activity,
 * otherwise, don't delete Layer1TradingStrategy
 * */
public class RemoveTradingAnnotationVisitor extends ClassVisitor {

    private static final String TRADING_STRATEGY_ANNOTATION = "Lvelox/api/layer1/annotations/Layer1TradingStrategy;";
    private final boolean isTradingStrategy;

    public RemoveTradingAnnotationVisitor(ClassVisitor classVisitor, boolean isTradingStrategy) {
        super(ASM8, classVisitor);
        this.isTradingStrategy = isTradingStrategy;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        if (cv != null) {
            if (descriptor.equals(TRADING_STRATEGY_ANNOTATION) && !isTradingStrategy) {
                return null;
            }
            return cv.visitAnnotation(descriptor, visible);
        }
        return null;
    }
}
