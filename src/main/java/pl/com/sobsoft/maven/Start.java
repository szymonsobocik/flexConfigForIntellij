package pl.com.sobsoft.maven;

import org.apache.log4j.BasicConfigurator;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: ssobocik
 * Date: 1/2/14
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Start {

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();

        FixIntellijConfigMojo fixConfigMojo = new FixIntellijConfigMojo();
        fixConfigMojo.execute();
    }
}
