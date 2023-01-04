package io.github.eutro.jwasm.test;

import io.github.eutro.jwasm.ByteInputStream;
import io.github.eutro.jwasm.ByteOutputStream;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Random;

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
        out.putVarUInt(-1);
        ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
        assertEquals(-1, in.getVarUIntX(10));
        in.expectEmpty();
    }

    @Test
    void put_get_varUIntMany() {
        new Random().longs(10000).forEach(l -> {
            ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
            out.putVarUInt(l);
            ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
            assertEquals(l, in.getVarUIntX(10));
        });
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

    @Test
    void put_get_varSIntMany() {
        new Random().longs(10000).forEach(l -> {
            ByteOutputStream.BaosByteOutputStream out = new ByteOutputStream.BaosByteOutputStream();
            out.putVarSInt(l);
            ByteInputStream.ByteBufferByteInputStream in = new ByteInputStream.ByteBufferByteInputStream(ByteBuffer.wrap(out.toByteArray()));
            assertEquals(l, in.getVarSInt64());
        });
    }
}
