package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ByteOutputStream;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteStreamTest {
    @Test
    void put_get() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.put((byte) 100);
        ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
        assertEquals(100, in.expect());
        in.expectEmpty();
    }

    @Test
    void put_get_varUInt() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.putVarUInt(Long.MAX_VALUE);
        ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
        assertEquals(Long.MAX_VALUE, in.getVarUIntX(64));
        in.expectEmpty();
    }

    @Test
    void put_get_varSInt() {
        ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
        out.putVarSInt(Long.MAX_VALUE);
        out.putVarSInt(Long.MIN_VALUE);
        ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
        assertEquals(Long.MAX_VALUE, in.getVarSInt64());
        assertEquals(Long.MIN_VALUE, in.getVarSInt64());
        in.expectEmpty();
    }
}
