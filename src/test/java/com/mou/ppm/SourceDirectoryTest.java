package com.mou.ppm;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mou.ppm.util.PathsUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class SourceDirectoryTest {
    private static final String TESTING_DIR = PathsUtil.getJarDirectory() + "\\test-source-directory";
    private static final String TEMP_FILE_NAME = "temp.txt";
    private static final Gson GSON = new Gson();

    private class TestClass {
        private String property;

        public TestClass() {
        }

        public TestClass(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    @BeforeAll
    public static void beforeAll()  {
        System.out.println(new File(TESTING_DIR).mkdir());
    }


    @AfterEach
    public void afterEach() throws IOException {
        new File(TESTING_DIR, TEMP_FILE_NAME).delete();
    }

    @Test
    public void testReadNonExisting() {
        var srcDir = new SourceDirectory(TESTING_DIR);
        Assertions.assertNull(srcDir.read("file-that-not-exist.txt"));
    }

    @Test
    public void testRead() throws IOException {
        var srcDir = new SourceDirectory(TESTING_DIR);
        Files.asCharSink(new File(TESTING_DIR, TEMP_FILE_NAME), Charsets.UTF_8).write("123");
        Assertions.assertEquals("123", srcDir.read(TEMP_FILE_NAME));
    }

    @Test
    public void testReadJson() throws IOException {
        var srcDir = new SourceDirectory(TESTING_DIR);
        Files.asCharSink(new File(TESTING_DIR, TEMP_FILE_NAME), Charsets.UTF_8).write("{\"prop\":\"OK\"}");
        Assertions.assertEquals("OK", srcDir.readJson(TEMP_FILE_NAME).getAsJsonObject().get("prop").getAsString());
    }

    @Test
    public void writeText() throws IOException {
        var srcDir = new SourceDirectory(TESTING_DIR);
        srcDir.write(TEMP_FILE_NAME, "OK");
        var readData = Files.asCharSource(new File(TESTING_DIR, TEMP_FILE_NAME), Charsets.UTF_8).read();
        Assertions.assertEquals("OK", readData);
    }

    @Test
    public void writeJson() throws IOException {
        var srcDir = new SourceDirectory(TESTING_DIR);
        srcDir.writeJson(TEMP_FILE_NAME, new TestClass("123"));
        var readObject = new Gson().fromJson(Files.asCharSource(new File(TESTING_DIR, TEMP_FILE_NAME), Charsets.UTF_8).read(), TestClass.class);
        Assertions.assertNotNull(readObject);
        Assertions.assertEquals(readObject.getProperty(), "123");
    }
}
