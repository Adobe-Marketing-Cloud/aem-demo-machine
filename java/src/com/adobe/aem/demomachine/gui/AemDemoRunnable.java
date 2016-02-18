/*******************************************************************************
 * Copyright 2016 Adobe Systems Incorporated.
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
package com.adobe.aem.demomachine.gui;

import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class AemDemoRunnable implements Runnable {
	private AemDemo aemDemo;
	private Project p;
	private String t;

	public AemDemoRunnable( AemDemo aemDemo, Project p, String t) {
		this.p = p;
		this.t = t;
		this.aemDemo = aemDemo;
	}

	public void run() {

		if (Arrays.asList(AemDemoConstants.BUILD_ACTIONS).contains(t)) {
			aemDemo.setBuildInProgress(true);
		}

		if (t.startsWith("download") || t.equals("infrastructure")) {
			aemDemo.setDownloadInProgress(true);
		}

		try {
			
			p.executeTarget(t);
			p.fireBuildFinished(null);
			
		} catch (BuildException ex) {
			System.out.println("Sorry, this target couldn't be completed properly");
			System.out.println("The error message is:");
			System.out.println(ex.getMessage());
		}

		if (Arrays.asList(AemDemoConstants.BUILD_ACTIONS).contains(t) || Arrays.asList(AemDemoConstants.STOP_ACTIONS).contains(t)) {
			aemDemo.setBuildInProgress(false);
		}

		if (t.startsWith("download")) {
			aemDemo.setDownloadInProgress(false);
		}

		// After the end of each run, refresh the list of available demo environments
		String buildName = p.getProperty("demo.build");
		if (Arrays.asList(AemDemoConstants.CLEANUP_ACTIONS).contains(t)) aemDemo.getListModelDemoMachines().removeElement(buildName);
		if (Arrays.asList(AemDemoConstants.BUILD_ACTIONS).contains(t) && !aemDemo.getListModelDemoMachines().contains(buildName)) aemDemo.getListModelDemoMachines().addElement(buildName);
		
	}
}
