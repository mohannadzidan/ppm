package com.mou.ppm;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;

public class SourceDirectory {
    private final String path;

    public SourceDirectory(String path) {
        this.path = path;
    }

    public String read(String child, @NonNull Charset charset) {
        try {
            return Files.asCharSource(new File(path, child), charset).read();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readJson(String child, Class<T> type, @NonNull Charset charset) {
        return new Gson().fromJson(read(child, charset), type);
    }

    public <T> T readJson(String child, Class<T> type) {
        return new Gson().fromJson(read(child), type);
    }

    public JsonElement readJson(String child, @NonNull Charset charset) {
        return new Gson().fromJson(read(child, charset), JsonElement.class);
    }

    public JsonElement readJson(String child) {
        return new Gson().fromJson(read(child), JsonElement.class);
    }

    public String read(String child) {
        return read(child, Charsets.UTF_8);
    }

    public void write(String child, String data, Charset charset) {
        try {
            var file = new File(path, child);
            Files.createParentDirs(file);
            Files.asCharSink(file, charset).write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String child, String data) {
        write(child, data, Charsets.UTF_8);
    }

    public void writeJson(String child, Object object, Charset charset) {
        write(child, new Gson().toJson(object), charset);
    }

    public void writeJson(String child, Object object) {
        write(child, new Gson().toJson(object));
    }

    public boolean exists(String relativePath) {
        return new File(path + "/" + relativePath).exists();
    }

    public Document readXml(String child) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            var builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(read(child))));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        return path;
    }

    public void writeXml(String child, Document document) {
       try{
           Transformer transformer = TransformerFactory.newInstance().newTransformer();
           var wr = new StringWriter();
           transformer.transform(new DOMSource(document), new StreamResult(wr));
           write(child, wr.toString());
       } catch (TransformerException e) {
           throw new RuntimeException(e);
       }
    }
}
