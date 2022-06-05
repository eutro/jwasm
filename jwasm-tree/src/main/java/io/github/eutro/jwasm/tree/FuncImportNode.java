package io.github.eutro.jwasm.tree;

import io.github.eutro.jwasm.ImportsVisitor;
import io.github.eutro.jwasm.Opcodes;

/**
 * A node that represents a function import.
 *
 * @see ImportsVisitor#visitFuncImport(String, String, int)
 */
public class FuncImportNode extends AbstractImportNode {
    /**
     * The
     * <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     * of the type of the imported function.
     */
    public int type;

    /**
     * Construct a {@link FuncImportNode} with the given module, name and type.
     *
     * @param module The module to import from.
     * @param name   The name to import.
     * @param type   The
     *               <a href="https://webassembly.github.io/spec/core/binary/modules.html#binary-typeidx">index</a>
     *               of the type of the imported function.
     */
    public FuncImportNode(String module, String name, int type) {
        super(module, name);
        this.type = type;
    }

    @Override
    public byte importType() {
        return Opcodes.IMPORTS_FUNC;
    }

    @Override
    public void accept(ImportsVisitor iv) {
        iv.visitFuncImport(module, name, type);
    }
}
