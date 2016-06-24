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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Position;

import org.apache.log4j.Logger;
import org.apache.tools.ant.ProjectHelper;

public class AemDemoRebuild extends JDialog {

	private static final long serialVersionUID = 2875921849130484734L;
	private final JPanel contentPanel = new JPanel();
	private JTextField demoBuildName;
	private AemDemo aemDemo;
	private String demoMachine;
	static Logger logger = Logger.getLogger(AemDemoRebuild.class);

	public AemDemoRebuild(AemDemo aemDemoInput) {

		// Initialize as a regular modal window
		super();

		aemDemo = aemDemoInput;

		// Let's make sure we have a valid selection
		boolean validation = true;
		List<String> validationErrors = new ArrayList<String>();	
		
		demoMachine = aemDemoInput.getSelectedDemoMachine();
		if (demoMachine==null) {
			validationErrors.add("Please select an existing Demo Environment!");
			validation = false;
		}

		File rebuildProperties = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "demos" + File.separator + demoMachine + File.separator + "demobuild.properties");
		if (!rebuildProperties.exists()) {
			validationErrors.add("This demo environment cannot be rebuilt because of a missing property file:");
			validationErrors.add(rebuildProperties.getAbsolutePath());
			validation = false;
		}

		if (validation) {

			setBounds(150,150,660,200);
			getContentPane().setLayout(null);
			contentPanel.setBounds(0, 0, 660, 200);
			getContentPane().add(contentPanel);
			contentPanel.setLayout(null);

			// Label for demo rebuild
			JLabel lblSelectADemo = new JLabel("You're going to rebuild a demo environment with the exact same properties");
			lblSelectADemo.setBounds(20, 20, 600, 16);
			contentPanel.add(lblSelectADemo);

			// Input field for demo name
			JLabel lblDemoName = new JLabel("Enter the  name for your demo environment:");
			lblDemoName.setBounds(20, 50, 600, 16);
			contentPanel.add(lblDemoName);

			demoBuildName = new JTextField(demoMachine);
			demoBuildName.setBounds(20, 80, 345, 28);
			contentPanel.add(demoBuildName);
			demoBuildName.setColumns(10);

			// Create Button (DEFAULT)
			JButton createButton = new JButton("Rebuild");
			createButton.setBounds(220, 120, 100, 30);
			createButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

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

					// New ANT project
					AemDemoProject p = new AemDemoProject(aemDemo);

					// Overriding the demo add-on options with the current selection
					Properties rebuildProps = AemDemoUtils.loadProperties(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "demos" + File.separator + demoMachine + File.separator + "demobuild.properties");
					Enumeration<?> props = rebuildProps.keys();
					while (props.hasMoreElements()) {
						String key = (String) props.nextElement();
						if (!(key.startsWith("java") || key.startsWith("sun") || key.startsWith("logs") || key.startsWith("os") || key.startsWith("user"))) {
							p.setUserProperty(key,rebuildProps.getProperty(key));
						}
					}

					// Making sure we are using the correct name
					p.setUserProperty("demo.build", demoBuildName.getText());
					
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
					System.out.println("Rebuilding a demo named " + demoBuildName.getText());
					Thread t = new Thread(new AemDemoRunnable(aemDemo, p, AemDemoConstants.BUILD_ACTION));
					t.start();

					// Closing the dialog
					dispose();

				}
			});

			contentPanel.add(createButton);

			// Cancel Button
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBounds(340, 120, 100, 30);
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
