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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.apache.tools.ant.ProjectHelper;

public class AemDemoUtils {

	static Logger logger = Logger.getLogger(AemDemoUtils.class);

	public static void main(String[] args) {

	}
	
	public static DefaultListModel<String> listDemoMachines(String demoMachineRootFolder) {

		DefaultListModel<String> demoMachines = new DefaultListModel<String>();
		File folder = new File(demoMachineRootFolder + File.separator + "demos");
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isDirectory()) {
					demoMachines.addElement(listOfFiles[i].getName());
				}
			}
		}

		return demoMachines;

	}

	// Retrieves the list of AEM/CQ .jar files in /dist/bin
	public static AemDemoProperty[] listAEMjars(File buildFile) {

		List<AemDemoProperty> aemJars = new ArrayList<AemDemoProperty>();	
		File folder = new File(buildFile.getParentFile().getAbsolutePath() + File.separator + "dist" + File.separator + "bin");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jar")) {
				// Name value pair:  aem.jar / aem
				aemJars.add(new AemDemoProperty(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().indexOf(".jar")),listOfFiles[i].getName()));
			}
		}

		AemDemoProperty[] aemPropertyArray = new AemDemoProperty[ aemJars.size() ];
		aemJars.toArray( aemPropertyArray );
		return aemPropertyArray;

	}

	// Retrieves the list of Demo Configs from build.properties
	public static AemDemoProperty[] listDemoConfigs(File buildFile) {

		return listOptions(buildFile,AemDemoConstants.OPTIONS_DEMOCONFIGS);

	}

	// Retrieves the list of Topologies from build.properties
	public static AemDemoProperty[] listTopologies(File buildFile) {

		return listOptions(buildFile,AemDemoConstants.OPTIONS_TOPOLOGIES);

	}

	// Retrieves the list of SRPs from build.properties
	public static AemDemoProperty[] listSRPs(File buildFile) {

		return listOptions(buildFile,AemDemoConstants.OPTIONS_SRPS);

	}

	// Retrieves the list of MKs from build.properties
	public static AemDemoProperty[] listMKs(File buildFile) {

		return listOptions(buildFile,AemDemoConstants.OPTIONS_STORES);

	}

	public static AemDemoProperty[] listOptions(File buildFile, String property) {

		List<AemDemoProperty> aemMKs = new ArrayList<AemDemoProperty>();	
		Properties prop = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");
		addPropertyFromString(aemMKs,prop.getProperty(property));
		AemDemoProperty[] aemPropertyArray = new AemDemoProperty[ aemMKs.size() ];
		aemMKs.toArray( aemPropertyArray );
		return aemPropertyArray;

	}

	public static Properties loadProperties(String path) {

		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream(path);
			prop.load(input);
			input.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return prop;

	}

	private static void addPropertyFromString(List<AemDemoProperty> aemProperties, String propertyString) {

		// Pattern is comma separated [ name / value ]
		if (propertyString!=null) {

			Pattern pattern = Pattern.compile("\\[(.*?)\\]");
			Matcher matcher = pattern.matcher(propertyString);
			while (matcher.find()) {
				int sep = matcher.group().indexOf("/");
				if (sep > 0) {
					aemProperties.add(new AemDemoProperty(matcher.group().substring(1,sep),matcher.group().substring(sep+1,matcher.group().length()-1)));
				}
			}

		}

	}

	public static int getSelectedIndex(@SuppressWarnings("rawtypes") JList list, Properties defaultProperties, Properties personalProperties, String propertyString) {

		int index = 0;
		String defaultProperty = defaultProperties.getProperty(propertyString);
		String personalProperty = personalProperties.getProperty(propertyString);
		String actualProperty = (personalProperty!=null)?personalProperty:defaultProperty;
		for (int i=0;i<list.getModel().getSize();i++) {

			if (list.getModel().getElementAt(i) instanceof AemDemoProperty) {

				AemDemoProperty aemProperty = (AemDemoProperty) list.getModel().getElementAt(i);
				if (aemProperty.getValue()!=null && actualProperty!=null && aemProperty.getValue().equals(actualProperty)) {
					return i;
				}

			}

		}

		return index;

	}

	public static String getActualPropertyValue(Properties defaultProperties, Properties personalProperties, String propertyString) {

		String defaultProperty = defaultProperties.getProperty(propertyString);
		String personalProperty = personalProperties.getProperty(propertyString);
		return (personalProperty!=null)?personalProperty:defaultProperty;

	}

	public static void antTarget(AemDemo aemDemo, String targetName) {

		String selectedDemoMachine = (String) aemDemo.getListDemoMachines().getSelectedValue();
		if (Arrays.asList(AemDemoConstants.INSTANCE_ACTIONS).contains(targetName) && (selectedDemoMachine==null || selectedDemoMachine.toString().length()==0)) {

			JOptionPane.showMessageDialog(null, "Please select a demo environment before running this command");

		} else {
			
			// New ANT project
			AemDemoProject p = new AemDemoProject(aemDemo);

			if (selectedDemoMachine!=null && selectedDemoMachine.length()>0) p.setUserProperty("demo.build", selectedDemoMachine.toString());

			p.init();

			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, aemDemo.getBuildFile());

			// Running the target name as a new Thread
			System.out.println("Running ANT target: " + targetName);
			Thread t = new Thread(new AemDemoRunnable(aemDemo, p, targetName));
			t.start();			

		}

	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static void openWebpage(String sUrl) {
		try {
			openWebpage(new URL(sUrl));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final KeyStroke escapeStroke = 
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
	public static final String dispatchWindowClosingActionMapKey = 
			"com.spodding.tackline.dispatch:WINDOW_CLOSING"; 

	public static void installEscapeCloseOperation(final JDialog dialog) { 
		Action dispatchClosing = new AbstractAction() { 

			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) { 
				dialog.dispatchEvent(new WindowEvent( 
						dialog, WindowEvent.WINDOW_CLOSING 
						)); 
			} 
		}; 
		JRootPane root = dialog.getRootPane(); 
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( 
				escapeStroke, dispatchWindowClosingActionMapKey 
				); 
		root.getActionMap().put( dispatchWindowClosingActionMapKey, dispatchClosing 
				); 
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
