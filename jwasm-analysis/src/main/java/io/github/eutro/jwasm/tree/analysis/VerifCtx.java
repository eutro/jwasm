package io.github.eutro.jwasm.tree.analysis;

import io.github.eutro.jwasm.ValidationException;
import io.github.eutro.jwasm.tree.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class VerifCtx {
    final List<TypeNode> types, funcs;
    final List<TableNode> tables;
    final List<MemoryNode> mems;
    final List<GlobalTypeNode> globals;
    final List<Byte> elems;
    int datas;

    final List<Byte> locals;
    // labels omitted, ExprValidator can track that itself
    final @Nullable List<Byte> returns;

    final Set<Integer> refs;

    private VerifCtx(List<TypeNode> types,
                     List<TypeNode> funcs,
                     List<TableNode> tables,
                     List<MemoryNode> mems,
                     List<GlobalTypeNode> globals,
                     List<Byte> elems,
                     int datas,
                     List<Byte> locals,
                     @Nullable List<Byte> returns,
                     Set<Integer> refs) {
        this.types = types;
        this.funcs = funcs;
        this.tables = tables;
        this.mems = mems;
        this.globals = globals;
        this.elems = elems;
        this.datas = datas;
        this.locals = locals;
        this.returns = returns;
        this.refs = refs;
    }

    public VerifCtx() {
        this(new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                new ArrayList<>(),
                null,
                new HashSet<>());
    }

    TypeNode resolveType(int type) {
        if (types.size() <= type) {
            throw new ValidationException(String.format("Type %d does not exist", type));
        }
        return types.get(type);
    }

    public VerifCtx deriveLocals(
            List<Byte> locals,
            @Nullable List<Byte> returns
    ) {
        return new VerifCtx(
                types,
                funcs,
                tables,
                mems,
                globals,
                elems,
                datas,
                locals,
                returns,
                refs
        );
    }
}
