package io.github.eutro.jwasm2java;

public class FuncType {
    final byte[] params;
    final byte[] returns;

    public FuncType(byte[] params, byte[] returns) {
        this.params = params;
        this.returns = returns;
    }

    public String toDescriptor() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (byte param : params) {
            sb.append(Types.toJava(param));
        }
        sb.append(")");
        sb.append(Types.returnType(returns));
        return sb.toString();
    }
}
