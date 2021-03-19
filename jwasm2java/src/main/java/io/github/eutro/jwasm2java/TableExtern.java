package io.github.eutro.jwasm2java;

import org.objectweb.asm.tree.FieldNode;

public interface TableExtern extends Extern {
    byte type();

    class ModuleTableExtern extends Extern.ModuleFieldExtern implements TableExtern {
        private final byte type;

        public ModuleTableExtern(FieldNode fn, String internalName, byte type) {
            super(fn, internalName);
            this.type = type;
        }

        @Override
        public byte type() {
            return type;
        }
    }
}
