package io.github.eutro.jwasm2java.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.test.ModuleTestBase;
import io.github.eutro.jwasm2java.ModuleAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ModuleAdapterTest extends ModuleTestBase {

    public static final Path WASMOUT = Paths.get("build", "wasmout", "jwasm");

    @BeforeAll
    static void beforeAll() throws IOException {
        Files.createDirectories(WASMOUT);
    }

    Object adapt(String name) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ModuleAdapter ma = new ModuleAdapter();
        try (InputStream is = openResource(name + "_bg.wasm")) {
            ModuleReader.fromInputStream(is).accept(ma);
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String className = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1) + "Bg";
        ma.toJava("jwasm/" + className).accept(new CheckClassAdapter(cw));
        byte[] bytes = cw.toByteArray();
        Files.write(WASMOUT.resolve(className + ".class"), bytes);
        Object[] out = new Object[1];
        new ClassLoader() {
            {
                Class<?> clazz = defineClass("jwasm." + className, bytes, 0, bytes.length);
                out[0] = clazz.getConstructor().newInstance();
            }
        };
        return out[0];
    }

    @Test
    void simple_bg() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        adapt("simple");
    }

    @Test
    void unsimple() throws Throwable {
        Object adapted = adapt("unsimple");
        MethodHandle div = MethodHandles.insertArguments(MethodHandles.lookup()
                .unreflect(adapted.getClass().getMethod("div", int.class, int.class)),
                0,
                adapted);
        MethodHandle divU = MethodHandles.insertArguments(MethodHandles.lookup()
                        .unreflect(adapted.getClass().getMethod("div_u", long.class, long.class)),
                0,
                adapted);
        assertEquals(50, (int) div.invokeExact(100, 2));
        assertEquals(Long.MAX_VALUE, (long) divU.invokeExact(-1L, 2L));
        assertThrows(AssertionError.class, () -> {
            @SuppressWarnings("unused")
            int ignored = (int) div.invokeExact(1, 0);
        });
    }
}
