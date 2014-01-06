package pl.com.sobsoft.maven;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
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

        FixIntellijConfigMojo fixConfigMojo = new FixIntellijConfigMojo();
        fixConfigMojo.execute();
    }
}
