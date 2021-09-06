package com.mou.ppm.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mou.ppm.Config;
import com.mou.ppm.Project;
import com.mou.ppm.exceptions.BadConfigurationException;
import com.mou.ppm.exceptions.UnsupportedChipException;
import com.mou.ppm.util.Console;
import com.mou.ppm.util.Resources;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;
import java.util.UUID;

@CommandLine.Command(name = "init", description = "Initializes PIC project at the current directory")
public class PpmInit extends PpmCommand {
    @CommandLine.Option(names = {"-f", "-force"}, defaultValue = "false", description = "Force the initialize process")
    private boolean force;
    @CommandLine.Option(names = {"-entry"}, defaultValue = "main.c", description = "Sets the entry c file of this project")
    private String entry;
    @CommandLine.Parameters(index = "0", description = "The project name")
    private String projectName;
    @CommandLine.Parameters(index = "1", description = "The chip number")
    private String chip;

    @Override
    public void run() {
        // require configuration
        final var gson = new Gson();
        final var config = gson.fromJson(Objects.requireNonNullElse(getProgramDirectory().read("config.json"), "{}"), Config.class);
        if (config.getMkGeneratorPath() == null || config.getXc8Path() == null) {
            throw new BadConfigurationException("prjMakefilesGenerator.prj and xc8 compiler paths must be configured in ppm before initializing a project!, run (ppm config)");
        }
        // check if the provided chip exists
        if (!new File(config.getXc8Path(), "/pic/include/proc/" + chip.toLowerCase() + ".h").exists()) {
            throw new UnsupportedChipException("couldn't find a chip specific header file for the chip (" + chip + ") at '" + config.getXc8Path() + "/pic/include/proc/'");
        }
        // generate/write project.json
        Project project = new Project();
        project.setChip(chip);
        project.setEntry(entry);
        project.setName(projectName);
        askIfElseRun(!force && getWorkingDirectory().exists("project.json"), "Overwrite 'project.json'?", () -> {
            getWorkingDirectory().write("project.json", gson.toJson(project));
            infoFileGeneration("project.json");
        });

        // copy Makefile & .gitignore
        askIfElseRun(!force && getWorkingDirectory().exists("Makefile"), "Overwrite 'Makefile'?", () -> {
            getWorkingDirectory().write("Makefile", Resources.getString("Makefile"));
            infoFileGeneration("Makefile");
        });

//        askIfElseRun(!force && getWorkingDirectory().exists(".gitignore"), "Overwrite '.gitignore'?", () -> {
//            System.out.println(Resources.getString(".gitignore"));
//            getWorkingDirectory().write(".gitignore", Resources.getString(".gitignore"));
//            infoFileGeneration(".gitignore");
//        });

        // generate c_cpp_properties.json
        JsonObject c_cpp_properties = gson.fromJson(Resources.getString(".vscode/c_cpp_properties.json"), JsonObject.class);
        var c_cpp_properties_config = c_cpp_properties.getAsJsonArray("configurations").get(0).getAsJsonObject();
        // root/configurations[0]/includePath[1] = ....
        c_cpp_properties_config.getAsJsonArray("includePath").set(1, new JsonPrimitive(config.getXc8Path() + "/pic/include/*"));
        // root/configurations[0]/forcedInclude[1] = XC8Path#/pic/include/proc/#chip#.h
        c_cpp_properties_config.getAsJsonArray("forcedInclude").set(0, new JsonPrimitive(config.getXc8Path() + "/pic/include/proc/" + chip + ".h"));
        // root/configurations[0]/browse/path[1] = #xc8Path#/include/*
        c_cpp_properties_config.getAsJsonObject("browse").getAsJsonArray("path").set(1, new JsonPrimitive(config.getXc8Path() + "/pic/include/*"));
        // root/configurations[0]/browse/path[2] = "#xc8Path#/pic/include/legacy/*"
        c_cpp_properties_config.getAsJsonObject("browse").getAsJsonArray("path").set(2, new JsonPrimitive(config.getXc8Path() + "/pic/include/legacy/*"));
        infoFileGeneration(".vscode\\c_cpp_properties.json");
        getWorkingDirectory().write(".vscode/c_cpp_properties.json", gson.toJson(c_cpp_properties));

        try {
            StringWriter wr = new StringWriter();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XPath xPath = XPathFactory.newInstance().newXPath();
            // generate nbproject/project.xml
            Document project_xml = builder.parse(Resources.getStream("nbproject/project.xml"));
            Node projectNameNode = (Node) xPath.evaluate("/project/configuration/data/name", project_xml, XPathConstants.NODE);
            Node creationUidNode = (Node) xPath.evaluate("/project/configuration/data/creation-uuid", project_xml, XPathConstants.NODE);
            projectNameNode.setTextContent(project.getName());
            creationUidNode.setTextContent(UUID.randomUUID().toString());
            infoFileGeneration("nbproject\\project.xml");
            transformer.transform(new DOMSource(project_xml), new StreamResult(wr));
            getWorkingDirectory().write("nbproject/project.xml", wr.toString());
            // generate nbproject/configurations.xml
            Document configurations_xml = builder.parse(Resources.getStream("nbproject/configurations.xml"));
            Node targetDeviceNode = (Node) xPath.evaluate("/configurationDescriptor/confs/conf/toolsSet/targetDevice", configurations_xml, XPathConstants.NODE);
            targetDeviceNode.setTextContent(project.getChip().toUpperCase());
            infoFileGeneration("nbproject\\configurations.xml");
            wr = new StringWriter();
            transformer.transform(new DOMSource(configurations_xml), new StreamResult(wr));
            getWorkingDirectory().write("nbproject/configurations.xml", wr.toString());
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        //

    }

    private void infoFileGeneration(String path) {
        Console.actionSuccess("write", getWorkingDirectory().getPath() + "\\" + path);
    }

    private void askIfElseRun(boolean condition, String question, Runnable runnable) {
        if (condition) {
            Console.prompt(question, runnable);
        } else {
            runnable.run();
        }
    }
}
