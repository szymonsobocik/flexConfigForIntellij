package pl.com.sobsoft.maven;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import pl.com.sobsoft.maven.FixIntellijConfigMojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: ssobocik
 * Date: 1/2/14
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Start {

    public static void main(String[] args) throws MojoExecutionException, IOException {
            //load a properties file
        Properties prop = new Properties();
        prop.load(new FileInputStream("config.properties"));

        FixIntellijConfigMojo fixConfigMojo = new FixIntellijConfigMojo();

        File workDir = new File(System.getProperty("user.dir"));

        String projectDirectory = prop.getProperty("projectDirectory");
        if (projectDirectory != null && !projectDirectory.trim().equals("")){
            workDir = new File(projectDirectory);
        }

        fixConfigMojo.setFlexSDK(prop.getProperty("flexSDK"));
        fixConfigMojo.setJdkTypeFlex(prop.getProperty("jdkTypeFlex"));
        fixConfigMojo.setTargetPlayer(prop.getProperty("targetPlayer"));
        fixConfigMojo.setProjectDirectory(workDir);
        fixConfigMojo.setLog(new DefaultLog(new ConsoleLogger()));
        fixConfigMojo.execute();
    }
}
