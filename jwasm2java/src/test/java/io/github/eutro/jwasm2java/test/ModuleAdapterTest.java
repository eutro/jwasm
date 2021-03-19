package io.github.eutro.jwasm2java.test;

import io.github.eutro.jwasm.ModuleReader;
import io.github.eutro.jwasm.test.ModuleTestBase;
import io.github.eutro.jwasm.tree.ModuleNode;
import io.github.eutro.jwasm2java.ModuleAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModuleAdapterTest extends ModuleTestBase {

    public static final Path WASMOUT = Paths.get("build", "wasmout", "jwasm");

    @BeforeAll
    static void beforeAll() throws IOException {
        Files.createDirectories(WASMOUT);
    }

    @Test
    void simple_bg() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ModuleAdapter ma = new ModuleAdapter();
        try (InputStream is = openResource("simple_bg.wasm")) {
            ModuleReader.fromInputStream(is).accept(ma);
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ma.toJava("jwasm/SimpleBg").accept(cw);
        byte[] bytes = cw.toByteArray();
        Files.write(WASMOUT.resolve("SimpleBg.class"), bytes);
        new ClassLoader() {
            {
                Class<?> clazz = defineClass("jwasm.SimpleBg", bytes, 0, bytes.length);
                Object o = clazz.getConstructor().newInstance();
                System.out.println(o);
            }
        };
    }
}
