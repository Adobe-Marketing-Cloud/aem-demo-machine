/*******************************************************************************
 * Copyright 2015 Adobe Systems Incorporated.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
