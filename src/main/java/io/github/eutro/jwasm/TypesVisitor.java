package io.github.eutro.jwasm;

public class TypesVisitor extends BaseVisitor<TypesVisitor> {
    public TypesVisitor() {
    }

    public TypesVisitor(TypesVisitor dl) {
        super(dl);
    }

    public void visitType(byte[] params, byte[] returns) {
        if (dl != null) dl.visitType(params, returns);
    }
}
