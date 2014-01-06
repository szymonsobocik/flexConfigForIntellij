package pl.com.sobsoft.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;


public class FixIntellijConfigMojo extends AbstractMojo {
    private File projectDirectory;
    private String flexSDK;
    private String jdkTypeFlex;
    private String targetPlayer;

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

    public void execute() throws MojoExecutionException {

        getLog().info("Project directory: " + projectDirectory.toString());
        runInDirectory(projectDirectory);



        File[] imlFiles = projectDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.endsWith(".iml");
            }
        });

        if (imlFiles == null || imlFiles.length == 0){
            return;
        }

        getLog().info("Iml files: ");
        for (File imlFile : imlFiles) {
            getLog().info(imlFile.getName());
        }

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

    private void runInDirectory(File projectDirectory) {
        //To change body of created methods use File | Settings | File Templates.
    }

    private void editIMLFile(File imlFile) throws JDOMException, IOException {

        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = saxBuilder.build(imlFile);
        Element rootElement = doc.getRootElement();
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
