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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Position;

import org.apache.log4j.Logger;
import org.apache.tools.ant.ProjectHelper;

public class AemDemoNew extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField demoBuildName;
	@SuppressWarnings({ "rawtypes" })
	private JList listConfigs;	
	@SuppressWarnings({ "rawtypes" })
	private JList listTopologies;	
	@SuppressWarnings({ "rawtypes" })
	private JList listSRP;	
	@SuppressWarnings({ "rawtypes" })
	private JList listAemJars;	
	@SuppressWarnings({ "rawtypes" })
	private JList listMK;	
	private AemDemo aemDemo;
	static Logger logger = Logger.getLogger(AemDemoNew.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AemDemoNew(AemDemo aemDemoInput) {

		// Initialize as a regular modal window
		super();

		aemDemo = aemDemoInput;

		// Validation tests
		boolean validConfiguration = true;
		List<String> validationErrors = new ArrayList<String>();	

		// Let's make sure it's possible to create a demo machine with a license file
		String licenseFileName = "license-" + aemDemo.getDefaultProperties().getProperty("demo.license") + ".properties";
		File licenseFile = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "dist" + File.separator + "license" + File.separator + licenseFileName);
		if (!licenseFile.exists()) {
			validationErrors.add("Please add a valid AEM license file named " + licenseFileName + " in the ${demohome}/dist/license folder");
			validConfiguration = false;
		}

		// Let's make sure it's possible to create a demo machine with at least one .jar file
		AemDemoProperty[] aemJars = AemDemoUtils.listAEMjars(aemDemo.getBuildFile());
		if (aemJars.length==0) {
			validationErrors.add("Please add a valid AEM/CQ .jar file in the ${demohome}/dist/bin folder");
			validConfiguration = false;
		}	

		if (validConfiguration) {

			setBounds(150,150,660,340);
			getContentPane().setLayout(null);
			contentPanel.setBounds(0, 0, 660, 340);
			getContentPane().add(contentPanel);
			contentPanel.setLayout(null);

			// Label for demo configs
			JLabel lblSelectADemo = new JLabel("Select demo add-on(s)");
			lblSelectADemo.setBounds(20, 20, 148, 16);
			contentPanel.add(lblSelectADemo);
			JLabel lblSelectMultiple = new JLabel("Use Command/CTRL key for multiple add-ons. (*) indicates that the add-on needs to be downloaded.");
			lblSelectMultiple.setFont(new Font("Serif", Font.ITALIC, 10));
			lblSelectMultiple.setBounds(20, 20, 600, 416);
			contentPanel.add(lblSelectMultiple);

			// List of demo configs
			JScrollPane scrollConfigs = new JScrollPane();
			scrollConfigs.setBounds(20, 45, 140, 175);
			contentPanel.add(scrollConfigs);
			AemDemoProperty[] aemConfigs = AemDemoUtils.listDemoAddons(aemDemo.getBuildFile());
			listConfigs = new JList(aemConfigs);
			listConfigs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listConfigs.setSelectedIndices(AemDemoUtils.getDemoAddons(aemDemo.getBuildFile()));

			scrollConfigs.setViewportView(listConfigs);

			// Label for topologies
			JLabel lblEnterAName = new JLabel("Select a topology");
			lblEnterAName.setBounds(175, 20, 124, 16);
			contentPanel.add(lblEnterAName);

			// List of topologies
			JScrollPane scrollTopologies = new JScrollPane();
			scrollTopologies.setBounds(175, 45, 140, 175);
			contentPanel.add(scrollTopologies);		
			AemDemoProperty[] aemTopologies = AemDemoUtils.listTopologies(aemDemo.getBuildFile());
			listTopologies = new JList(aemTopologies);
			listTopologies.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listTopologies.setSelectedIndex(AemDemoUtils.getSelectedIndex(listTopologies,aemDemo.getDefaultProperties(), aemDemo.getPersonalProperties(),AemDemoConstants.OPTIONS_TOPOLOGIES_DEFAULT));
			scrollTopologies.setViewportView(listTopologies);

			// Label for AEM/CQ jar
			JLabel lblSelectAMk = new JLabel("Select an AEM/CQ .jar");
			lblSelectAMk.setBounds(331, 20, 139, 16);
			contentPanel.add(lblSelectAMk);

			// List of AEM/CQ Jar files
			JScrollPane scrollAemJars = new JScrollPane();
			scrollAemJars.setBounds(330, 45, 140, 175);
			contentPanel.add(scrollAemJars);	
			listAemJars = new JList(aemJars);
			listAemJars.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listAemJars.setSelectedIndex(AemDemoUtils.getSelectedIndex(listAemJars,aemDemo.getDefaultProperties(), aemDemo.getPersonalProperties(),AemDemoConstants.OPTIONS_JAR_DEFAULT));
			scrollAemJars.setViewportView(listAemJars);

			// Label for persistence options
			JLabel lblPersistence = new JLabel("Micro-Kernel");
			lblPersistence.setBounds(500, 20, 118, 16);
			contentPanel.add(lblPersistence);

			// List of MicroKernels
			JScrollPane scrollMK = new JScrollPane();
			scrollMK.setBounds(500, 45, 140, 80);
			contentPanel.add(scrollMK);	
			AemDemoProperty[] aemMKs = AemDemoUtils.listMKs(aemDemo.getBuildFile());
			listMK = new JList(aemMKs);
			listMK.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listMK.setSelectedIndex(AemDemoUtils.getSelectedIndex(listMK,aemDemo.getDefaultProperties(), aemDemo.getPersonalProperties(),AemDemoConstants.OPTIONS_STORES_DEFAULT));
			scrollMK.setViewportView(listMK);

			// Label for Community persistence options
			JLabel lblCommunityPersistence = new JLabel("Communities");
			lblCommunityPersistence.setBounds(500, 135, 118, 16);
			contentPanel.add(lblCommunityPersistence);

			// List of SRPs
			JScrollPane scrollSRP = new JScrollPane();
			scrollSRP.setBounds(500, 160, 140, 60);
			contentPanel.add(scrollSRP);
			AemDemoProperty[] aemSRPs = AemDemoUtils.listSRPs(aemDemo.getBuildFile());
			listSRP = new JList(aemSRPs);
			listSRP.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listSRP.setSelectedIndex(AemDemoUtils.getSelectedIndex(listSRP,aemDemo.getDefaultProperties(), aemDemo.getPersonalProperties(),AemDemoConstants.OPTIONS_SRPS_DEFAULT));
			scrollSRP.setViewportView(listSRP);

			// Input field for demo name
			JLabel lblDemoName = new JLabel("Enter a name for your demo environment");
			lblDemoName.setBounds(20, 240, 279, 16);
			contentPanel.add(lblDemoName);

			demoBuildName = new JTextField(AemDemoUtils.getActualPropertyValue(aemDemo.getDefaultProperties(), aemDemo.getPersonalProperties(), AemDemoConstants.OPTIONS_BUILD_DEFAULT));
			demoBuildName.setBounds(295, 235, 345, 28);
			contentPanel.add(demoBuildName);
			demoBuildName.setColumns(10);

			// Create Button (DEFAULT)
			JButton createButton = new JButton("Create");
			createButton.setBounds(220, 275, 100, 30);
			createButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					AemDemoProperty selectedSRP  = (AemDemoProperty) listSRP.getModel().getElementAt(listSRP.getSelectedIndex());
					AemDemoProperty selectedMK  = (AemDemoProperty) listMK.getModel().getElementAt(listMK.getSelectedIndex());
					AemDemoProperty selectedTopology  = (AemDemoProperty) listTopologies.getModel().getElementAt(listTopologies.getSelectedIndex());
					AemDemoProperty selectedJar  = (AemDemoProperty) listAemJars.getModel().getElementAt(listAemJars.getSelectedIndex());
					List<AemDemoProperty> selectedAddOns  = listConfigs.getSelectedValuesList();

					// Input validation
					if (!demoBuildName.getText().matches("[a-zA-Z0-9]+")) {

						// Alphanumeric characters only for build name
						JOptionPane.showMessageDialog(null, "Please use alphanumeric characters only for your demo configuration");
						return;

					}

					// Confirmation dialog to avoid accidental replacement of demo machine instances
					if (aemDemo.getListDemoMachines().getModel().getSize()>0) {
						int index = aemDemo.getListDemoMachines().getNextMatch(demoBuildName.getText(),0,Position.Bias.Forward);
						if (index != -1)  {
							int dialogResult = JOptionPane.showConfirmDialog (null, "There's already a demo environmnent with the same name, do you really want to replace it?","Warning",JOptionPane.YES_NO_OPTION);
							if(dialogResult == JOptionPane.NO_OPTION) {
								return;
							}
						}	
					}

					// Since it's a new install, not a rebuild, we need to make sure there isn't an existing demobuild.properties file
					File demobuild = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "demos" + File.separator + demoBuildName.getText() + File.separator + "demobuild.properties");
					if (demobuild!=null && demobuild.exists()) {
						if (demobuild.delete()) System.out.println("Existing demobuild.properties deleted since it's a fresh install");
					}
					
					// New ANT project
					AemDemoProject p = new AemDemoProject(aemDemo);

					// Overriding with the New Dialog properties
					p.setUserProperty("demo.jar", selectedJar.getValue());
					p.setUserProperty("demo.srp", selectedSRP.getValue());
					p.setUserProperty("demo.store", selectedMK.getValue());
					p.setUserProperty("demo.type", selectedTopology.getValue());
					p.setUserProperty("demo.build", demoBuildName.getText());

					// Overriding the demo add-on options with the current selection
					AemDemoProperty[] aemConfigs = AemDemoUtils.listDemoAddons(aemDemo.getBuildFile());
					for (int i=0;i<aemConfigs.length;i++) {

						if (selectedAddOns.contains(aemConfigs[i])) {
							p.setUserProperty("demo.addons." + (i+1) + "." + aemConfigs[i].getValue(),"true");
						} else {
							p.setUserProperty("demo.addons." + (i+1) + "." + aemConfigs[i].getValue(),"false");
						}
					
					}
					
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
					System.out.println("Building a new " + selectedTopology.getValue() + " demo named " + demoBuildName.getText());
					Thread t = new Thread(new AemDemoRunnable(aemDemo, p, AemDemoConstants.BUILD_ACTION));
					t.start();

					// Closing the dialog
					dispose();

				}
			});

			contentPanel.add(createButton);

			// Cancel Button
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBounds(340, 275, 100, 30);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					dispose();

				}
			});
			contentPanel.add(cancelButton);

			// Set Create as the default button
			JRootPane rootPane = SwingUtilities.getRootPane(createButton); 
			rootPane.setDefaultButton(createButton);

		} else {

			setBounds(150,150,760,340);
			getContentPane().setLayout(null);
			contentPanel.setBounds(0, 0, 760, 340);
			getContentPane().add(contentPanel);
			contentPanel.setLayout(null);

			JLabel lblSelectADemo = new JLabel("There are validation errors!");
			lblSelectADemo.setBounds(20, 20, 620, 16);
			contentPanel.add(lblSelectADemo);

			int pos=60;
			for(String errorMsg : validationErrors) {
				JLabel lblError = new JLabel(errorMsg);
				lblError.setBounds(20, pos, 720, 16);
				contentPanel.add(lblError);
				pos=pos+40;
			}

			JButton closeButton = new JButton("Close");
			closeButton.setBounds(20, 260, 60, 30);
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					dispose();

				}
			});
			contentPanel.add(closeButton);

		}

		// Closing ESC
		AemDemoUtils.installEscapeCloseOperation(this);

	}

	public JTextField getDemoBuildName() {

		return demoBuildName;

	}

}
