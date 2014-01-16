package pl.com.sobsoft.maven;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class FixIntellijConfigMojo {
    private File projectDirectory;
    private String flexSDK;
    private String jdkTypeFlex;
    private String targetPlayer;
    private boolean searchRecursively;

    private Logger logger = Logger.getLogger(this.getClass());

    private List<File> imlFiles;

    public Logger getLog() {
        return logger;
    }

    public void setSearchRecursively(boolean searchRecursively) {
        this.searchRecursively = searchRecursively;
    }

    public void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public void setFlexSDK(String flexSDK) {
        this.flexSDK = flexSDK;
    }

    public void setJdkTypeFlex(String jdkTypeFlex) {
        this.jdkTypeFlex = jdkTypeFlex;
    }

    public void setTargetPlayer(String targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public void execute() throws IOException {

            readProperties();

        getLog().info("Project directory: " + this.projectDirectory.toString());
        this.imlFiles = new ArrayList<File>();
        findImlFilesRecursively(this.projectDirectory);
        processImlFiles(this.imlFiles);
    }

    private void readProperties() throws IOException {
        Properties prop = new Properties();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("flexConfigForIntellij.properties");
        prop.load(resourceAsStream);

        File workDir = new File(System.getProperty("user.dir"));

        String projectDirectory = prop.getProperty("projectDirectory");
        if (projectDirectory != null && !projectDirectory.trim().equals("")){
            workDir = new File(projectDirectory);
        }

        setProjectDirectory(workDir);
        setFlexSDK(prop.getProperty("flexSDK"));
        setJdkTypeFlex(prop.getProperty("jdkTypeFlex"));
        setTargetPlayer(prop.getProperty("targetPlayer"));
        setSearchRecursively("true".equals(prop.getProperty("recursively")));
    }

    private void findImlFilesRecursively(File file) {
        if (file == null){
            return;
        }

        if (file.getName().endsWith(".iml")){
            addToImlFiles(file);
        } else if (this.searchRecursively && file.isDirectory() && !file.getName().endsWith(".git")) {
            for (File fileIn : file.listFiles()) {
                findImlFilesRecursively(fileIn);
            }
        }
    }

    private void processImlFiles(List<File> imlFiles) {
        if (imlFiles == null || imlFiles.isEmpty()){
            getLog().info("No IML files found");
            return;
        }

        getLog().info("Found " + imlFiles.size() + " iml files. Processing...");

        for (File imlFile : imlFiles) {
            try {
                editIMLFile(imlFile);
            } catch (JDOMException e) {
                getLog().error(e);  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                getLog().error(e);  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    private void addToImlFiles(File file) {
        this.imlFiles.add(file);
    }

    private void editIMLFile(File imlFile) throws JDOMException, IOException {
        getLog().info("Processing : " + imlFile.getAbsolutePath());

        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(imlFile);
        XPathExpression<Attribute> xpathAttribute = XPathFactory.instance().compile("/module/@type", Filters.attribute());
        Attribute type = xpathAttribute.evaluateFirst(doc);
        if (type != null && !type.getValue().equals("Flex")) {
            getLog().info("Skipping: " + imlFile.toString());
            return;
        }

        XPathExpression<Element> xPathElement = XPathFactory.instance().compile("/module/component[@name='FlexBuildConfigurationManager']/configurations/configuration/dependencies", Filters.element());
        Element dependenciesElement = xPathElement.evaluateFirst(doc);

        dependenciesElement.setAttribute("target-player", targetPlayer);
        dependenciesElement.setAttribute("framework-linkage", "Merged");

        detachElements(doc, "/module/component[@name='FlexBuildConfigurationManager']/configurations/configuration/dependencies/entries/entry[starts-with(@library-name, 'Maven: org.apache.flex.framework')]");
        detachElements(doc, "/module/component[@name='FlexBuildConfigurationManager']/configurations/configuration/dependencies/entries/entry[starts-with(@library-name, 'Maven: com.adobe.flex.framework')]");
        detachElements(doc, "/module/component[@name='FlexBuildConfigurationManager']/configurations/configuration/compiler-options/option[@name='additionalConfigFilePath']");
        detachElements(doc, "/module/component[@name='NewModuleRootManager']/orderEntry[starts-with(@name, 'Maven: com.adobe.flex.framework')]");

        xPathElement = XPathFactory.instance().compile("/module/component[@name='FlexBuildConfigurationManager']/configurations/configuration/dependencies/sdk", Filters.element());

        Element sdkElement = xPathElement.evaluateFirst(doc);
        sdkElement.setAttribute("name", flexSDK);

        xPathElement = XPathFactory.instance().compile("/module/component[@name='NewModuleRootManager']/orderEntry[@type='jdk']", Filters.element());
        Element jdkElement = xPathElement.evaluateFirst(doc);
        jdkElement.setAttribute("jdkName", flexSDK);
        jdkElement.setAttribute("jdkType", jdkTypeFlex);

        saveDoc(doc, imlFile);
    }

    private void detachElements(Document doc, String xPath) {
        XPathExpression<Element> xPathElement = XPathFactory.instance().compile(xPath, Filters.element());
        List<Element> entries = xPathElement.evaluate(doc);
        for (Element entry : entries) {
            entry.detach();
        }

    }

    private void saveDoc(Document doc, File imlFile) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        xmlOutputter.output(doc, new FileWriter(imlFile));
    }


}
