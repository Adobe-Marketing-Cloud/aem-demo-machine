package com.adobe.aem.demo.gui;

import java.util.Arrays;

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

		p.executeTarget(t);
		p.fireBuildFinished(null);
		
		if (Arrays.asList(AemDemoConstants.BUILD_ACTIONS).contains(t)) {
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
