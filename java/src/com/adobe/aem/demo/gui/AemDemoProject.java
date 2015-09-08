package com.adobe.aem.demo.gui;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

public class AemDemoProject extends Project {

	private AemDemo aemDemo;
	
    public AemDemoProject( AemDemo aemDemo ) {

    	super();
    	
        this.aemDemo = aemDemo;

		// Configuring the default logger to intercept the system out stream
		DefaultLogger defaultLogger = new DefaultLogger();

		defaultLogger.setErrorPrintStream(System.err);
		defaultLogger.setOutputPrintStream(System.out);
		defaultLogger.setMessageOutputLevel(Project.MSG_WARN);
		this.addBuildListener(defaultLogger);

		// Loading the persisted personal properties
		Properties persistedProperties = AemDemoUtils.loadProperties(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");

		// First setting the personal properties
		@SuppressWarnings("rawtypes")
		Enumeration propEnum = aemDemo.getPersonalProperties().keys();
		while (propEnum.hasMoreElements()) {
			String key = (String) propEnum.nextElement();
			this.setUserProperty(key, aemDemo.getPersonalProperties().getProperty(key));
		}

		// Verifying that some persisted properties have not been disabled in memory, setting to default value if so
		propEnum = aemDemo.getDefaultProperties().keys();
		while (propEnum.hasMoreElements()) {
			String key = (String) propEnum.nextElement();
			if (persistedProperties.containsKey(key) && !aemDemo.getPersonalProperties().containsKey(key)) {
				this.setUserProperty(key, aemDemo.getDefaultProperties().getProperty(key));
			}
		}
		
		// Setting the user properties
		this.setUserProperty("ant.file", aemDemo.getBuildFile().getAbsolutePath());
		this.setUserProperty("demo.compile", "false");

    }
    
    public AemDemo getAemDemo() {
    	return this.aemDemo;
    }
    
	
}
