package com.mou.ppm.commands;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.mou.ppm.Config;
import com.mou.ppm.Project;
import com.mou.ppm.exceptions.BadConfigurationException;
import com.mou.ppm.exceptions.MissingDependencyException;
import com.mou.ppm.exceptions.ProjectNotFoundException;
import com.mou.ppm.util.Console;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import picocli.CommandLine;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@CommandLine.Command(name = "build", description = "Build the current project using make")
public class PpmBuild extends PpmCommand implements Runnable {

    @Override
    public void run() {
        long startingMillis = System.currentTimeMillis();
        Project project = getWorkingDirectory().readJson("project.json", Project.class);
        Config config = getProgramDirectory().readJson("config.json", Config.class);
        if (project == null) {
            throw new ProjectNotFoundException("Couldn't find a ppm project at current working directory!");
        }
        if (config == null || config.getMkGeneratorPath() == null) {
            throw new BadConfigurationException("prjMakefilesGenerator.bat path must be set!, run (ppm config)");
        }
        var requiredDependencies = getDependencies(new File(getWorkingDirectory().getPath() + "/" + project.getEntry()), new HashSet<>());
        // parse xml file
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            // generate nbproject/project.xml
            Document configurationsDocument = getWorkingDirectory().readXml("nbproject/configurations.xml");
            Node sourceFilesFolderNode = (Node) xPath.evaluate("/configurationDescriptor/logicalFolder/logicalFolder[@name='SourceFiles']", configurationsDocument, XPathConstants.NODE);
            Node headerFilesFolderNode = (Node) xPath.evaluate("/configurationDescriptor/logicalFolder/logicalFolder[@name='HeaderFiles']", configurationsDocument, XPathConstants.NODE);
            Console.info("checking required dependencies...");
            if (!isDependenciesSatisfied(configurationsDocument, requiredDependencies) || !isMakefilesGenerated()) {
                Console.info("dependencies change detected...");
                sourceFilesFolderNode.setTextContent(""); // clear
                headerFilesFolderNode.setTextContent(""); // clear
                sourceFilesFolderNode.appendChild(configurationsDocument.createElement("itemPath")).setTextContent(project.getEntry());
                requiredDependencies.forEach(dependency -> {
                    Element e = configurationsDocument.createElement("itemPath");
                    e.setTextContent(dependency);
                    if (dependency.charAt(dependency.length() - 1) == 'c') {
                        sourceFilesFolderNode.appendChild(e);
                    } else {
                        headerFilesFolderNode.appendChild(e);
                    }

                });
                getWorkingDirectory().writeXml("nbproject/configurations.xml", configurationsDocument);
                Console.actionSuccess("write", "nbproject/configurations.xml");
                // execute prjMakefilesGenerator.bat
                Console.info("running prjMakefilesGenerator.jar...");
                if (execute(config.getMkGeneratorPath() + " -v \"" + getWorkingDirectory().getPath()+"\"") != 0) {
                    Console.error("prjMakefilesGenerator failed!");
                }
            } else {
                Console.actionSuccess("skip", "dependencies already satisfied.");
            }
            // generate directory
            requiredDependencies.forEach(dependency -> {
                try {
                    Files.createParentDirs(new File(getWorkingDirectory().getPath() + "/build/default/production/" + dependency));
                    Files.createParentDirs(new File(getWorkingDirectory().getPath() + "/build/default/debug/" + dependency));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            Console.info("running GNU Make");
            if (execute("make", null, new File(getWorkingDirectory().getPath())) != 0) {
                Console.error("Make failed!");
            } else {
                double elapsedSeconds = Math.round((System.currentTimeMillis() - startingMillis) / 10.0) / 100.0;
                Console.success("Build finished in " + elapsedSeconds + " second(s)");
            }

        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

    private static int execute(String command, String[] envp, File file) {
        try {
            Process pr = Runtime.getRuntime().exec(command, envp, file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
            }
            pr.waitFor(30, TimeUnit.SECONDS);
            return pr.exitValue();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static int execute(String command) {
        return execute(command, null, null);
    }

    private boolean isMakefilesGenerated() {
        String[] generatedFiles = {
                "Makefile-default.mk",
                "Makefile-impl.mk",
                "Makefile-local-default.mk",
                "Makefile-variables.mk",
                "Makefile-genesis.properties",
                "Package-default.bash"
        };
        for (String generatedFile : generatedFiles) {
            if (!getWorkingDirectory().exists("nbproject/" + generatedFile)) {
                return false;
            }
        }
        return true;

    }

    private boolean isDependenciesSatisfied(Document configurationsDocument, HashSet<String> requiredDependencies) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node sourceFilesFolderNode, headerFilesFolderNode;
        try {
            sourceFilesFolderNode = (Node) xPath.evaluate("//logicalFolder[@name='SourceFiles']", configurationsDocument, XPathConstants.NODE);
            headerFilesFolderNode = (Node) xPath.evaluate("//logicalFolder[@name='HeaderFiles']", configurationsDocument, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        var configuredDependencies = new HashSet<String>();
        for (int i = 0; i < sourceFilesFolderNode.getChildNodes().getLength(); i++) {
            var c = sourceFilesFolderNode.getChildNodes().item(i).getTextContent().trim();
            if (!Strings.isNullOrEmpty(c)) configuredDependencies.add(c);
        }
        for (int i = 0; i < headerFilesFolderNode.getChildNodes().getLength(); i++) {
            var c = headerFilesFolderNode.getChildNodes().item(i).getTextContent().trim();
            if (!Strings.isNullOrEmpty(c)) configuredDependencies.add(c);
        }
        boolean isSatisfied = true;
        for (String dependency : requiredDependencies) {
            if (!configuredDependencies.contains(dependency)) {
                isSatisfied = false;
                break;
            }
        }
        return isSatisfied;
    }

    private HashSet<String> getDependencies(File start, HashSet<String> includesSet) {
        try {
            String src = Files.asCharSource(start, Charsets.UTF_8).read();
            var matcher = Pattern.compile("#include\\s+\"(.+\\.h)\"").matcher(src);
            while (matcher.find()) {
                var match = matcher.group(1);
                var includedHeaderPath = start.getAbsolutePath() + "/../" + match;
                var includedHeaderFile = new File(includedHeaderPath);
                var includedCPath = includedHeaderPath.substring(0, includedHeaderPath.length() - 1) + "c";
                if (!includedHeaderFile.exists()) {
                    throw new MissingDependencyException("While resolving dependencies of '" + start.toPath().normalize() + "' couldn't find the file that corresponds to dependency (" + matcher.group() + ")");
                }
                if (new File(includedCPath).exists()) {
                    includesSet.add(Paths.get(getWorkingDirectory().getPath()).relativize(Paths.get(includedCPath)).toString());
                }
                if (includesSet.add(Paths.get(getWorkingDirectory().getPath()).relativize(includedHeaderFile.toPath()).toString()))
                    getDependencies(new File(start.getAbsolutePath() + "/../" + match), includesSet);
            }
            return includesSet;
        } catch (FileNotFoundException e) {

            throw new MissingDependencyException("While resolving dependencies, couldn't find the file " + start.toPath().normalize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
