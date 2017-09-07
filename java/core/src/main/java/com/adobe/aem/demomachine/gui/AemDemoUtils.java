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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
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

import org.apache.commons.codec.digest.DigestUtils;
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
		
		// First, loading the .jar files from the /dist/bin folder
		File folder = new File(buildFile.getParentFile().getAbsolutePath() + File.separator + "dist" + File.separator + "bin");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".jar")) {
				// Name value pair:  aem.jar / aem
				aemJars.add(new AemDemoProperty(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().indexOf(".jar")),listOfFiles[i].getName()));
			}
		}
		
		// Second, loading the docker images from the config files
		Properties defaultProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");
		Properties personalProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
		Properties mergedProps = new Properties();
		mergedProps.putAll(defaultProps);
		mergedProps.putAll(personalProps);
		
		Enumeration<?> e = mergedProps.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.startsWith("demo.docker.images.") & !(key.endsWith("help") || key.endsWith("label"))) {
				String keyValue = mergedProps.getProperty(key);
				String keyName = "docker:" + keyValue;
				int lastSlash = keyName.lastIndexOf("/");
				if (lastSlash>0) {
					keyName = keyName.substring(1 + lastSlash);
				}
				aemJars.add(new AemDemoProperty(keyValue, keyName));
			}
		}
		
		AemDemoProperty[] aemPropertyArray = new AemDemoProperty[ aemJars.size() ];
		aemJars.toArray( aemPropertyArray );
		return aemPropertyArray;

	}

	// Retrieves the list of Demo Addons from build.properties
	public static int[] getDemoAddons(File buildFile) {

		List<Integer> listIndices = new ArrayList<Integer>();
		
		Properties defaultProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");
		Properties personalProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");

		@SuppressWarnings("serial")
		Properties sortedProps = new Properties() {
			@Override
			public synchronized Enumeration<Object> keys() {
				return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			}
		};
		sortedProps.putAll(defaultProps);

		// Looping through all possible options
		Enumeration<?> e = sortedProps.keys();
		
		int currentIndice = 0;
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.startsWith("demo.addons.") & !(key.endsWith("help") || key.endsWith("label"))) {
				String value = sortedProps.getProperty(key);
				if (personalProps.containsKey(key)) {
					value = personalProps.getProperty(key);
				}
				if (value.equals("true")) {
					listIndices.add(currentIndice);
				}
				currentIndice = currentIndice + 1;
			}
		}
		int[] array = new int[listIndices.size()];
		for(int i = 0; i < listIndices.size(); i++) array[i] = listIndices.get(i);
		return array;

	}

	
	// Retrieves the list of Demo Addons from build.properties
	public static AemDemoProperty[] listDemoAddons(File buildFile) {

		List<AemDemoProperty> addons = new ArrayList<AemDemoProperty>();	
		Properties defaultProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");

		@SuppressWarnings("serial")
		Properties sortedProps = new Properties() {
			@Override
			public synchronized Enumeration<Object> keys() {
				return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			}
		};
		sortedProps.putAll(defaultProps);

		// Looping through all possible options
		Enumeration<?> e = sortedProps.keys();
		
		// List of paths to demo packages
		List<String[]> listPaths = Arrays.asList(AemDemoConstants.demoPaths);

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.startsWith("demo.addons.") & !(key.endsWith("help") || key.endsWith("label"))) {
				String newKey = key.substring(1 + key.lastIndexOf("."));
				
				// Check if downloads are required
				boolean downloadRequired = false;
				for (String[] path:listPaths) {
					if (path.length==5 && path[4]!=null && path[4].equals(newKey)) {
						File pathFolder = new File(buildFile.getParentFile().getAbsolutePath() + (path[1].length()>0?(File.separator + path[1]):""));
						if (!pathFolder.exists()) {
							downloadRequired=true;
						}
					}
				}
				addons.add(new AemDemoProperty(newKey, sortedProps.getProperty(key + ".label") + (downloadRequired?" (*)":"")));
			}
		}

		AemDemoProperty[] aemPropertyArray = new AemDemoProperty[ addons.size() ];
		addons.toArray( aemPropertyArray );
		return aemPropertyArray;

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
	
	// Retrieves a particular property from build.properties 
	public static String getPropertyValue(File buildFile, String propertyName) {
		
		String propertyValue = null;
		Properties defaultProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");
		Properties personalProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
		if (personalProps.containsKey(propertyName)) {
			propertyValue = personalProps.getProperty(propertyName);
		} else {
			propertyValue = defaultProps.getProperty(propertyName);
		}
		return propertyValue;

	}

	public static AemDemoProperty[] listOptions(File buildFile, String property) {

		List<AemDemoProperty> aemMKs = new ArrayList<AemDemoProperty>();	
		Properties defaultProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties");
		Properties personalProps = loadProperties (buildFile.getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
		if (personalProps.containsKey(property)) {
			addPropertyFromString(aemMKs,personalProps.getProperty(property));
		} else {
			addPropertyFromString(aemMKs,defaultProps.getProperty(property));
		}
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

			// Make sure host name is there
			try {
				p.setUserProperty("demo.hostname", InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException ex) {
				logger.error(ex.getMessage());
			}

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

	public static String calcMD5HashForDir(File dirToHash, boolean includeSubFolders, boolean includeHiddenFiles) {

		assert (dirToHash.isDirectory());
		Vector<FileInputStream> fileStreams = new Vector<FileInputStream>();

		logger.debug("Found files for hashing:");
		collectInputStreams(dirToHash, fileStreams, includeSubFolders, includeHiddenFiles);

		SequenceInputStream seqStream = 
				new SequenceInputStream(fileStreams.elements());

		try {
			String md5Hash = DigestUtils.md5Hex(seqStream);
			seqStream.close();
			return md5Hash;
		}
		catch (IOException e) {
			throw new RuntimeException("Error reading files to hash in "
					+ dirToHash.getAbsolutePath(), e);
		}

	}

	public static void collectInputStreams(File dir,
			List<FileInputStream> foundStreams,
			boolean includeSubFolders,
			boolean includeHiddenFiles) {

		File[] fileList = dir.listFiles();        
		Arrays.sort(fileList,               // Need in reproducible order
				new Comparator<File>() {
			public int compare(File f1, File f2) {                       
				return f1.getName().compareTo(f2.getName());
			}
		});

		for (File f : fileList) {
			if (!includeHiddenFiles && f.getName().startsWith(".")) continue;
			if (f.isDirectory() && !includeSubFolders) continue;
			if (f.isDirectory()) {
				collectInputStreams(f, foundStreams, includeSubFolders, includeHiddenFiles);
			}
			else {
				try {
					logger.debug(f.getAbsolutePath());
					foundStreams.add(new FileInputStream(f));
				}
				catch (FileNotFoundException e) {
					throw new AssertionError(e.getMessage()
							+ ": file should never not be found!");
				}
			}
		}

	}

}
