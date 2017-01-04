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
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;


public class AemDemoOptions extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private final JTable table;
	private AemDemo aemDemo;

	@SuppressWarnings("serial")
	public AemDemoOptions(AemDemo aemDemoInput) {

		super();

		this.aemDemo = aemDemoInput;

		setBounds(150,150,960,600);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 960, 600);
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);

		Enumeration<Object> e = aemDemo.getDefaultProperties().keys();
		int i=0;
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (!key.endsWith(".help") && !key.startsWith("demo.download")) i++;
		}

		Object props[][] = new Object[i][4];
		e = aemDemo.getDefaultProperties().keys();
		i=0;
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (!key.endsWith(".help") && !key.startsWith("demo.download")) {
				String value = (String) aemDemo.getDefaultProperties().getProperty(key);
				Object[] prop = new Object[5];
				if (aemDemo.getPersonalProperties().containsKey(key)) {
					prop[0] = true;
				} else {
					prop[0] = false;
				}
				prop[1] = key;
				prop[2] = key.contains("password")?"******":value;
				if (aemDemo.getPersonalProperties().containsKey(key)) {
					String personalValue = aemDemo.getPersonalProperties().getProperty(key);
					prop[3] = key.contains("password")?AemDemoConstants.PASSWORD:personalValue;
				} else {
					prop[3] = "";
				}
				if (aemDemo.getDefaultProperties().containsKey(key + ".help")) {
					prop[4] = aemDemo.getDefaultProperties().getProperty(key + ".help");
				} else {
					prop[4] = "";
				}
				props[i++]=prop;
			}
		}

		Object[] columnNames = {"Override", "Name", "Default Value", "Custom Value", "Description"};
		DefaultTableModel model = new DefaultTableModel(props, columnNames);
		table = new JTable(model) {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class getColumnClass(int column) {
				switch (column) {
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				case 3:
					return String.class;
				default:
					return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {

				if (column==0 || column==3 || column==4) return true;
				return false;

			}

		};

		// Check if column 3 is modified
		table.putClientProperty("terminateEditOnFocusLost",true);
		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e){
				if (e.getColumn() == 3){
					String value = (String)table.getModel().getValueAt(e.getLastRow(), e.getColumn());
					if (value.equals("")) {
						table.getModel().setValueAt(false, e.getLastRow(), 0);
					} else {
						table.getModel().setValueAt(true, e.getLastRow(), 0);
					}
				}
			}
		});

		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(1);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(240);
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(4).setPreferredWidth(350);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(20, 20, 910, 500);
		contentPanel.add(scrollPane);

		// Create Button (DEFAULT)
		JButton applyButton = new JButton("Apply");
		applyButton.setBounds(700, 535, 100, 30);
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				aemDemo.setPersonalProperties(savePersonalProperties());

				dispose();

			}
		});
		contentPanel.add(applyButton);

		// Cancel Button
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(820, 535, 100, 30);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dispose();

			}
		});
		contentPanel.add(cancelButton);

		// Set Apply as the default button
		JRootPane rootPane = SwingUtilities.getRootPane(applyButton);
		rootPane.setDefaultButton(applyButton);

		// Save Button
		JButton saveButton = new JButton("Save");
		saveButton.setBounds(20, 535, 100, 30);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// backup the old build-personal.conf file
				File personalConf = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
				File personalBackup = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal." + System.currentTimeMillis() + ".properties");
				if (personalConf.exists()) {
					personalConf.renameTo(personalBackup);
				}
				File newPersonalConf = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
				try {
					aemDemo.setPersonalProperties(savePersonalProperties());

					Properties tmpProperties = new Properties() {
						@Override
						public synchronized Enumeration<Object> keys() {
							return Collections.enumeration(new TreeSet<Object>(super.keySet()));
						}
					};
					tmpProperties.putAll(aemDemo.getPersonalProperties());
					tmpProperties.store(new FileOutputStream(newPersonalConf), null);

					JOptionPane.showMessageDialog(null, "Personal properties saved as /conf/build-personal.properties");

				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Error when saving personal properties");
				}

			}
		});
		contentPanel.add(saveButton);

		// Restore Button
		JButton restoreButton = new JButton("Restore");
		restoreButton.setBounds(140, 535, 100, 30);
		restoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// restoring from the personal properties from the build-personal.conf file
				File personalConf = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties");
				if (personalConf.exists()) {
					aemDemo.setPersonalProperties(AemDemoUtils.loadProperties(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties"));
					JOptionPane.showMessageDialog(null, "Personal properties restored from /conf/build-personal.properties");
					dispose();
				} else {
					JOptionPane.showMessageDialog(null, "Problem when restoring personal properties from /conf/build-personal.properties");
				}

			}
		});
		contentPanel.add(restoreButton);

		// Load from File Button
		JButton loadButton = new JButton("Load");
		loadButton.setBounds(260, 535, 100, 30);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser chooser = new JFileChooser(aemDemo.getBuildFile());
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Properties", "props", "properties");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(AemDemoOptions.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {

					Properties mergeProps = AemDemoUtils.loadProperties(chooser.getSelectedFile().getAbsolutePath());
					@SuppressWarnings("rawtypes")
					Enumeration currentProp = mergeProps.propertyNames();
					while (currentProp.hasMoreElements()) {
						String key = (String) currentProp.nextElement();
						String value = (String) mergeProps.getProperty(key);

						// Lookup for the same key in the property table
						for (int i=0; i<table.getRowCount(); i++) {

							if ( table.getModel().getValueAt(i, 1).equals(key) ) {

								table.getModel().setValueAt(value, i, 3);
								table.getModel().setValueAt(true, i, 0);

							}
						}

					}

					aemDemo.setPersonalProperties(savePersonalProperties());

				}
			}
		});
		contentPanel.add(loadButton);

		// Closing ESC
		AemDemoUtils.installEscapeCloseOperation(this);


	}

	private Properties savePersonalProperties() {

		// Saving in memory the new personal properties
		Properties newPersonalProperties = new Properties();
		for (int i=0;i<table.getRowCount();i++) {

			if ( (Boolean) table.getModel().getValueAt(i, 0)) {
				String customValue = (String) table.getModel().getValueAt(i, 3);
				if (customValue !=null && customValue.length()>0) {
					String propertyKey = (String) table.getModel().getValueAt(i, 1);
					if (!customValue.equals(AemDemoConstants.PASSWORD)) {
						// It is a password and its value was edited, hence we used the edited value
						newPersonalProperties.setProperty(propertyKey, customValue);
					} else {
						// It is a password, not displayed in the table and it wasn't changed, hence reuse the previously saved password
						newPersonalProperties.setProperty(propertyKey, aemDemo.getPersonalProperties().getProperty(propertyKey));
					}
				}

			}

		}
		return newPersonalProperties;

	}

}
