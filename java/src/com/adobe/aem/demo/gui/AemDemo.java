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

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AemDemo {

	static Logger logger = Logger.getLogger(AemDemo.class);
	private Properties defaultProperties;
	private Properties personalProperties;
	private static File buildFile;
	private JFrame frameMain;
	private DefaultListModel<String> listModelDemoMachines;
	private JList<String> listDemoMachines;
	private boolean buildInProgress = false;
	private boolean downloadInProgress = false;
	
	public static void main(String[] args) {

		String demoMachineRootFolder = null;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("f", true, "Path to Demo Machine root folder");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("f")) {
				demoMachineRootFolder = cmd.getOptionValue("f");
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		// Let's check if we have a valid build.xml file to work with...
		String buildFilePath = (demoMachineRootFolder!=null?demoMachineRootFolder:System.getProperty("user.dir")) + File.separator + "build.xml";
		logger.debug("Trying to load build file from " + buildFilePath);
		buildFile = new File( buildFilePath );
		if(buildFile.exists() && !buildFile.isDirectory()) {
			
			// Launching the main window
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {

						UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Arial", Font.BOLD, 14));	
						AemDemo window = new AemDemo();
						window.frameMain.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} else {

			logger.error("No valid build.xml file to work with");
			System.exit(-1);

		}
	}

	public AemDemo() {
		initialize();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initialize() {

		// Initialize properties
		setDefaultProperties(AemDemoUtils.loadProperties(buildFile.getParentFile().getAbsolutePath() + File.separator + "build.properties"));
		setPersonalProperties(AemDemoUtils.loadProperties(buildFile.getParentFile().getAbsolutePath() + File.separator + "conf" + File.separator + "build-personal.properties"));

		// Constructing the main frame
		frameMain = new JFrame();
		frameMain.setBounds(100, 100, 700, 530);
		frameMain.getContentPane().setLayout(null);

		// Main menu bar for the Frame
		JMenuBar menuBar = new JMenuBar();

		JMenu mnAbout = new JMenu("AEM Demo Machine");
		mnAbout.setMnemonic(KeyEvent.VK_A);
		menuBar.add(mnAbout);

		JMenuItem mntmDoc = new JMenuItem("Help and Documentation");
		mntmDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_DOCUMENTATION));
			}
		});
		mnAbout.add(mntmDoc);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});
		mnAbout.add(mntmQuit);

		JMenu mnNew = new JMenu("New");
		mnNew.setMnemonic(KeyEvent.VK_N);
		menuBar.add(mnNew);

		// New Demo Machine
		JMenuItem mntmNewDemo = new JMenuItem("Demo Environment");
		mntmNewDemo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmNewDemo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (AemDemo.this.getBuildInProgress()) {

					JOptionPane.showMessageDialog(null, "A Demo Environment is currently being built. Please wait until it is finished.");

				} else {

					final AemDemoNew dialogNew = new AemDemoNew(AemDemo.this);
					dialogNew.setModal(true);
					dialogNew.setVisible(true);
					dialogNew.getDemoBuildName().requestFocus();;

				}
			}
		});
		mnNew.add(mntmNewDemo);

		JMenuItem mntmNewOptions = new JMenuItem("Demo Properties");
		mntmNewOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmNewOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				final AemDemoOptions dialogOptions = new AemDemoOptions(AemDemo.this);
				dialogOptions.setModal(true);
				dialogOptions.setVisible(true);


			}
		});
		mnNew.add(mntmNewOptions);

		JMenu mnUpdate = new JMenu("Add-ons");
		menuBar.add(mnUpdate);

		// Sites Add-on
		JMenu mnSites = new JMenu("Sites");
		mnUpdate.add(mnSites);

		JMenuItem mntmSitesDownloadAddOn = new JMenuItem("Download Add-On");
		mntmSitesDownloadAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_sites");
			}
		});
		mnSites.add(mntmSitesDownloadAddOn);

		JMenuItem mntmSitesDownloadFP = new JMenuItem("Download Feature Pack (VPN)");
		mntmSitesDownloadFP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_sites_fp");
			}
		});
		mnSites.add(mntmSitesDownloadFP);

		mnSites.addSeparator();
		
		JMenuItem mntmSitesInstallAddOn = new JMenuItem("Install Add-on");
		mntmSitesInstallAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "sites");
			}
		});
		mnSites.add(mntmSitesInstallAddOn);

		// Assets Add-on
		JMenu mnAssets = new JMenu("Assets");
		mnUpdate.add(mnAssets);

		JMenuItem mntmAssetsDownloadAddOn = new JMenuItem("Download Add-on");
		mntmAssetsDownloadAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_assets");
			}
		});
		mnAssets.add(mntmAssetsDownloadAddOn);
		mnAssets.addSeparator();

		JMenuItem mntmAssetsInstallAddOn = new JMenuItem("Install Add-on");
		mntmAssetsInstallAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "assets");
			}
		});
		mnAssets.add(mntmAssetsInstallAddOn);

		// Communities Add-on
		JMenu mnCommunities = new JMenu("Communities");
		mnUpdate.add(mnCommunities);
				
		JMenuItem mntmAemCommunitiesUber = new JMenuItem("Download Latest Bundles (VPN)");
		mntmAemCommunitiesUber.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmAemCommunitiesUber.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_communities_bundles");
			}
		});
		mnCommunities.add(mntmAemCommunitiesUber);

		JMenuItem mntmAemCommunitiesFeaturePacks = new JMenuItem("Download Latest Feature Packs (PackageShare)");
		mntmAemCommunitiesFeaturePacks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_communities_fp");
			}
		});
		mnCommunities.add(mntmAemCommunitiesFeaturePacks);

		JMenuItem mntmAemCommunitiesEnablement = new JMenuItem("Download Enablement Demo Site Add-on (4.5GB)");
		mntmAemCommunitiesEnablement.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_enablement");
			}
		});
		mnCommunities.add(mntmAemCommunitiesEnablement);
		mnCommunities.addSeparator();

		JMenuItem mntmAemCommunitiesAddOn = new JMenuItem("Install Add-on");
		mntmAemCommunitiesAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "communities");
			}
		});
		mnCommunities.add(mntmAemCommunitiesAddOn);

		// Forms Add-on
		JMenu mnForms = new JMenu("Forms");
		mnUpdate.add(mnForms);

		JMenuItem mntmAemFormsFP = new JMenuItem("Download Demo Add-on (PackageShare)");
		mntmAemFormsFP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_forms_fp");
			}
		});
		mnForms.add(mntmAemFormsFP);

		mnForms.addSeparator();

		JMenuItem mntmAemFormsAddOn = new JMenuItem("Install Add-on");
		mntmAemFormsAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "forms");
			}
		});
		mnForms.add(mntmAemFormsAddOn);
		
		// Apps Add-on
		JMenu mnApps = new JMenu("Apps");
		mnUpdate.add(mnApps);
		
		JMenuItem mntmAemApps = new JMenuItem("Download Add-on");
		mntmAemApps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_apps");
			}
		});
		mnApps.add(mntmAemApps);

		mnApps.addSeparator();

		JMenuItem mntmAemAppsAddOn = new JMenuItem("Install Add-on");
		mntmAemAppsAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "apps");
			}
		});
		mnApps.add(mntmAemAppsAddOn);
		
		// Commerce Add-on
		JMenu mnCommerce = new JMenu("Commerce");
		mnUpdate.add(mnCommerce);

		JMenu mnCommerceDownload = new JMenu("Download Add-on");
		mnCommerce.add(mnCommerceDownload);

		// Commerce EP
		JMenuItem mnCommerceDownloadEP = new JMenuItem("ElasticPath");
		mnCommerceDownloadEP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_commerce_ep");
			}
		});
		mnCommerceDownload.add(mnCommerceDownloadEP);

		// Commerce WebSphere
		JMenuItem mnCommerceDownloadWAS = new JMenuItem("WebSphere");
		mnCommerceDownloadWAS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_commerce_websphere");
			}
		});
		mnCommerceDownload.add(mnCommerceDownloadWAS);

		mnCommerce.addSeparator();

		JMenuItem mntmAemCommerceAddOn = new JMenuItem("Install Add-on");
		mntmAemCommerceAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "commerce");
			}
		});
		mnCommerce.add(mntmAemCommerceAddOn);

		mnUpdate.addSeparator();

		JMenuItem mntmAemDownloadAll = new JMenuItem("Download All Add-ons");
		mntmAemDownloadAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_all");
			}
		});
		mnUpdate.add(mntmAemDownloadAll);

		JMenuItem mntmAemDownloadFromDrive = new JMenuItem("Download Web Page");
		mntmAemDownloadFromDrive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_WEBDOWNLOAD));

			}
		});
		mnUpdate.add(mntmAemDownloadFromDrive);

		JMenu mnInfrastructure = new JMenu("Infrastructure");
		menuBar.add(mnInfrastructure);

		JMenu mnMongo = new JMenu("MongoDB");

		JMenuItem mntmInfraMongoDB = new JMenuItem("Download");
		mntmInfraMongoDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_mongo");
			}
		});
		mnMongo.add(mntmInfraMongoDB);

		JMenuItem mntmInfraMongoDBInstall = new JMenuItem("Install");
		mntmInfraMongoDBInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "install_mongo");
			}
		});
		mnMongo.add(mntmInfraMongoDBInstall);
		mnMongo.addSeparator();

		JMenuItem mntmInfraMongoDBStart = new JMenuItem("Start");
		mntmInfraMongoDBStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "mongo_start");
			}
		});
		mnMongo.add(mntmInfraMongoDBStart);

		JMenuItem mntmInfraMongoDBStop = new JMenuItem("Stop");
		mntmInfraMongoDBStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "mongo_stop");
			}
		});
		mnMongo.add(mntmInfraMongoDBStop);
		mnInfrastructure.add(mnMongo);
	
		// SOLR options
		JMenu mnSOLR = new JMenu("SOLR");
		
		JMenuItem mntmInfraSOLR = new JMenuItem("Download");
		mntmInfraSOLR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_solr");
			}
		});
		mnSOLR.add(mntmInfraSOLR);

		JMenuItem mntmInfraSOLRInstall = new JMenuItem("Install");
		mntmInfraSOLRInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "install_solr");
			}
		});
		mnSOLR.add(mntmInfraSOLRInstall);
		mnSOLR.addSeparator();

		JMenuItem mntmInfraSOLRStart = new JMenuItem("Start");
		mntmInfraSOLRStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "solr_start");
			}
		});
		mnSOLR.add(mntmInfraSOLRStart);

		JMenuItem mntmInfraSOLRStop = new JMenuItem("Stop");
		mntmInfraSOLRStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "solr_stop");
			}
		});
		mnSOLR.add(mntmInfraSOLRStop);
		
		mnInfrastructure.add(mnSOLR);		

		// MySQL options
		JMenu mnMySQL = new JMenu("MySQL");

		JMenuItem mntmInfraMysql = new JMenuItem("Download");
		mntmInfraMysql.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_mysql");
			}
		});
		mnMySQL.add(mntmInfraMysql);

		JMenuItem mntmInfraMysqlInstall = new JMenuItem("Install");
		mntmInfraMysqlInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "install_mysql");
			}
		});
		mnMySQL.add(mntmInfraMysqlInstall);

		mnMySQL.addSeparator();

		JMenuItem mntmInfraMysqlStart = new JMenuItem("Start");
		mntmInfraMysqlStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "mysql_start");
			}
		});
		mnMySQL.add(mntmInfraMysqlStart);

		JMenuItem mntmInfraMysqlStop = new JMenuItem("Stop");
		mntmInfraMysqlStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "mysql_stop");
			}
		});
		mnMySQL.add(mntmInfraMysqlStop);

		mnInfrastructure.add(mnMySQL);		

		// FFMPEPG options
		JMenu mnFFMPEG = new JMenu("FFMPEG");

		JMenuItem mntmInfraFFMPEG = new JMenuItem("Download");
		mntmInfraFFMPEG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_ffmpeg");
			}
		});
		mnFFMPEG.add(mntmInfraFFMPEG);
		
		JMenuItem mntmInfraFFMPEGInstall = new JMenuItem("Install");
		mntmInfraFFMPEGInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "install_ffmpeg");
			}
		});
		mnFFMPEG.add(mntmInfraFFMPEGInstall);
		
		mnInfrastructure.add(mnFFMPEG);		

		// Apache James options
		JMenu mnJames = new JMenu("James SMTP");

		JMenuItem mnJamesStart = new JMenuItem("Start");
		mnJamesStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "james_start");
			}
		});
		mnJames.add(mnJamesStart);
		
		JMenuItem mnJamesStop = new JMenuItem("Stop");
		mnJamesStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "james_stop");
			}
		});
		mnJames.add(mnJamesStop);
		
		mnInfrastructure.add(mnJames);		

		mnInfrastructure.addSeparator();

		JMenuItem mntmInfraInstall = new JMenuItem("All in One Setup");
		mntmInfraInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "infrastructure");
			}
		});
		mnInfrastructure.add(mntmInfraInstall);

		JMenu mnOther = new JMenu("Other");
		menuBar.add(mnOther);

		JMenu mntmAemDownload = new JMenu("AEM & License files (VPN)");

		JMenuItem mntmAemDownloadAEM61 = new JMenuItem("Download AEM 6.1");
		mntmAemDownloadAEM61.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_aem61");
			}
		});
		mntmAemDownload.add(mntmAemDownloadAEM61);

		JMenuItem mntmAemDownloadAEM60 = new JMenuItem("Download AEM 6.0");
		mntmAemDownloadAEM60.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_aem60");
			}
		});
		mntmAemDownload.add(mntmAemDownloadAEM60);

		JMenuItem mntmAemDownloadCQ561 = new JMenuItem("Download CQ 5.6.1");
		mntmAemDownloadCQ561.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_cq561");
			}
		});
		mntmAemDownload.add(mntmAemDownloadCQ561);

		JMenuItem mntmAemDownloadCQ56 = new JMenuItem("Download CQ 5.6");
		mntmAemDownloadCQ56.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_cq56");
			}
		});
		mntmAemDownload.add(mntmAemDownloadCQ56);

		JMenuItem mntmAemDownloadOthers = new JMenuItem("Other Releases & License files");
		mntmAemDownloadOthers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_DOWNLOAD));
			}
		});
		mntmAemDownload.add(mntmAemDownloadOthers);

		mnOther.add(mntmAemDownload);

		JMenuItem mntmAemSnapshot = new JMenuItem("Download Latest AEM Snapshot (VPN)");
		mntmAemSnapshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmAemSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_snapshot");
			}
		});
		mnOther.add(mntmAemSnapshot);

		JMenuItem mntmAemDemoMachine = new JMenuItem("Download Latest AEM Demo Machine");
		mntmAemDemoMachine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		mntmAemDemoMachine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_DEMODOWNLOAD));
			}
		});
		mnOther.add(mntmAemDemoMachine);

		// Adding the menu bar
		frameMain.setJMenuBar(menuBar);

		// Adding other form elements
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(24, 184, 650, 230);
		frameMain.getContentPane().add(scrollPane);

		final JTextArea textArea = new JTextArea("");
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		// List of demo machines available
		JScrollPane scrollDemoList = new JScrollPane();
		scrollDemoList.setBounds(24, 55, 208, 100);
		frameMain.getContentPane().add(scrollDemoList);	
		listModelDemoMachines = AemDemoUtils.listDemoMachines(buildFile.getParentFile().getAbsolutePath());
		listDemoMachines = new JList(listModelDemoMachines);
		scrollDemoList.setViewportView(listDemoMachines);

		// Capturing the output stream of ANT commands
		AemDemoOutputStream out = new AemDemoOutputStream (textArea);
		System.setOut (new PrintStream (out));
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AemDemoUtils.antTarget(AemDemo.this, "start");

			}
		});
		btnStart.setBounds(250, 50, 117, 29);
		frameMain.getContentPane().add(btnStart);

		// Set Start as the default button
		JRootPane rootPane = SwingUtilities.getRootPane(btnStart); 
		rootPane.setDefaultButton(btnStart);

		JButton btnInfo = new JButton("Details");
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AemDemoUtils.antTarget(AemDemo.this, "version");
				AemDemoUtils.antTarget(AemDemo.this, "configuration");

			}
		});
		btnInfo.setBounds(250, 80, 117, 29);
		frameMain.getContentPane().add(btnInfo);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AemDemoUtils.antTarget(AemDemo.this, "stop");

			}
		});
		btnStop.setBounds(500, 50, 117, 29);
		frameMain.getContentPane().add(btnStop);

		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});
		btnExit.setBounds(550, 429, 117, 29);
		frameMain.getContentPane().add(btnExit);

		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
		btnClear.setBounds(40, 429, 117, 29);
		frameMain.getContentPane().add(btnClear);

		JButton btnBackup = new JButton("Backup");
		btnBackup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "backup");
			}
		});
		btnBackup.setBounds(500, 80, 117, 29);
		frameMain.getContentPane().add(btnBackup);

		JButton btnRestore = new JButton("Restore");
		btnRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "restore");
			}
		});
		btnRestore.setBounds(500, 110, 117, 29);
		frameMain.getContentPane().add(btnRestore);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "uninstall");
			}
		});
		btnDelete.setBounds(500, 140, 117, 29);
		frameMain.getContentPane().add(btnDelete);

		JLabel lblSelectYourDemo = new JLabel("Select your Demo Environment");
		lblSelectYourDemo.setBounds(24, 31, 219, 16);
		frameMain.getContentPane().add(lblSelectYourDemo);

		JLabel lblCommandOutput = new JLabel("Command Output");
		lblCommandOutput.setBounds(24, 164, 160, 16);
		frameMain.getContentPane().add(lblCommandOutput);

		// Launching the download tracker task
		AemDemoDownload aemDownload = new AemDemoDownload(AemDemo.this);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(aemDownload, 0, 5, TimeUnit.SECONDS);

	}

	public Properties getDefaultProperties() {
		return this.defaultProperties;
	}

	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	public Properties getPersonalProperties() {
		return this.personalProperties;
	}

	public void setPersonalProperties(Properties personalProperties) {
		this.personalProperties = personalProperties;
	}

	public File getBuildFile() {
		return AemDemo.buildFile;
	}

	public void setBuildFile(File buildFile) {
		AemDemo.buildFile = buildFile;
	}

	public boolean getBuildInProgress() {
		return this.buildInProgress;
	}

	public void setBuildInProgress(boolean buildInProgress) {
		this.buildInProgress = buildInProgress;
	}

	public boolean getDownloadInProgress() {
		return this.downloadInProgress;
	}

	public void setDownloadInProgress(boolean downloadInProgress) {
		this.downloadInProgress = downloadInProgress;
	}

	public DefaultListModel<String> getListModelDemoMachines() {
		return this.listModelDemoMachines;
	}	
	
	public JList<String> getListDemoMachines() {
		return this.listDemoMachines;
	}	

}
