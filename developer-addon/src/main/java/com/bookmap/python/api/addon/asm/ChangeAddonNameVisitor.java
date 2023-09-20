package com.bookmap.python.api.addon.asm;

import static org.objectweb.asm.Opcodes.ASM8;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;

public class ChangeAddonNameVisitor extends ClassVisitor {

    private static final String CONFIGURING_ADDON_NAME_ANNOTATION = "Lvelox/api/layer1/annotations/Layer1StrategyName;";
    private final String newAddonName;

    public ChangeAddonNameVisitor(String newAddonName, ClassVisitor classVisitor) {
        super(ASM8, classVisitor);
        this.newAddonName = newAddonName;
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
            if (descriptor.equals(CONFIGURING_ADDON_NAME_ANNOTATION)) {
                return new ReplacingAddonNameAnnotationVisitor(ASM8, cv.visitAnnotation(descriptor, visible));
            }
            return cv.visitAnnotation(descriptor, visible);
        }
        return null;
    }

    private class ReplacingAddonNameAnnotationVisitor extends AnnotationVisitor {

        protected ReplacingAddonNameAnnotationVisitor(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor);
        }

        @Override
        public void visit(String name, Object value) {
            if (name.equals("value")) {
                super.visit(name, newAddonName);
            }
        }
    }
}
