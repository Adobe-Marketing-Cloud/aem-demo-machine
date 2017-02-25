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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
	private static String aemDemoMachineVersion = "Unknown";

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

		// Let's grab the version number for the core Maven file
		String mavenFilePath = (demoMachineRootFolder!=null?demoMachineRootFolder:System.getProperty("user.dir")) + File.separator + "java" + File.separator + "core" + File.separator + "pom.xml";
		File mavenFile = new File(mavenFilePath);
		if (mavenFile.exists() && !mavenFile.isDirectory()) {

			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document;
				document = builder.parse(mavenFile);
				NodeList list = document.getElementsByTagName("version");
				if (list != null && list.getLength() > 0) {
					aemDemoMachineVersion = list.item(0).getFirstChild().getNodeValue();
				}

			} catch (Exception e) {
				logger.error("Can't parse Maven pom.xml file");
			}

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

		// Call onExit() when cross is clicked
		frameMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frameMain.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});

		// Main menu bar for the Frame
		JMenuBar menuBar = new JMenuBar();

		JMenu mnAbout = new JMenu("AEM Demo Machine");
		mnAbout.setMnemonic(KeyEvent.VK_A);
		menuBar.add(mnAbout);

		JMenuItem mntmUpdates = new JMenuItem("Check for Updates");
		mntmUpdates.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmUpdates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "demo_update");
			}
		});
		mnAbout.add(mntmUpdates);

		JMenuItem mntmDoc = new JMenuItem("Help and Documentation");
		mntmDoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_DOCUMENTATION));
			}
		});
		mnAbout.add(mntmDoc);

		JMenuItem mntmScripts = new JMenuItem("Demo Scripts (VPN)");
		mntmScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.openWebpage(AemDemoUtils.getActualPropertyValue(defaultProperties, personalProperties, AemDemoConstants.OPTIONS_SCRIPTS));
			}
		});
		mnAbout.add(mntmScripts);

		JMenuItem mntmDiagnostics = new JMenuItem("Diagnostics");
		mntmDiagnostics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Map<String, String> env = System.getenv();
				System.out.println("====== System Environment Variables ======");
				for (String envName : env.keySet()) {
					System.out.format("%s=%s%n", envName, env.get(envName));
				}
				System.out.println("====== JVM Properties ======");
				RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
				List<String> jvmArgs = runtimeMXBean.getInputArguments();
				for (String arg : jvmArgs) {
					System.out.println(arg);
				}
				System.out.println("====== Runtime Properties ======");
				Properties props = System.getProperties();
				props.list(System.out);
			}
		});
		mnAbout.add(mntmDiagnostics);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});
		mnAbout.add(mntmQuit);

		JMenu mnNew = new JMenu("New");
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

		JMenuItem mntmSitesDownloadAddOn = new JMenuItem("Download Demo Add-on");
		mntmSitesDownloadAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_sites");
			}
		});
		mnSites.add(mntmSitesDownloadAddOn);

		JMenuItem mntmSitesDownloadFP = new JMenuItem("Download Packages (PackageShare)");
		mntmSitesDownloadFP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_sites_packages");
			}
		});
		mnSites.add(mntmSitesDownloadFP);

		// Assets Add-on
		JMenu mnAssets = new JMenu("Assets");
		mnUpdate.add(mnAssets);

		JMenuItem mntmAssetsDownloadAddOn = new JMenuItem("Download Demo Add-on");
		mntmAssetsDownloadAddOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_assets");
			}
		});
		mnAssets.add(mntmAssetsDownloadAddOn);

		JMenuItem mntmAssetsDownloadFP = new JMenuItem("Download Packages (PackageShare)");
		mntmAssetsDownloadFP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_assets_packages");
			}
		});
		mnAssets.add(mntmAssetsDownloadFP);

		// Communities Add-on
		JMenu mnCommunities = new JMenu("Communities/Livefyre");
		mnUpdate.add(mnCommunities);

		JMenuItem mntmAemCommunitiesFeaturePacks = new JMenuItem("Download Packages (PackageShare)");
		mntmAemCommunitiesFeaturePacks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_communities_packages");
			}
		});
		mnCommunities.add(mntmAemCommunitiesFeaturePacks);

		// Forms Add-on
		JMenu mnForms = new JMenu("Forms");
		mnUpdate.add(mnForms);

		JMenuItem mntmAemFormsAddon = new JMenuItem("Download Demo Add-on");
		mntmAemFormsAddon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_forms");
			}
		});
		mnForms.add(mntmAemFormsAddon);

		JMenuItem mntmAemFormsFP = new JMenuItem("Download Packages (PackageShare)");
		mntmAemFormsFP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_forms_packages");
			}
		});
		mnForms.add(mntmAemFormsFP);

		// Mobile Add-on
		JMenu mnApps = new JMenu("Mobile");
		mnUpdate.add(mnApps);

		JMenuItem mntmAemAppsAddon = new JMenuItem("Download Demo Add-on");
		mntmAemAppsAddon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_apps");
			}
		});
		mnApps.add(mntmAemAppsAddon);

		JMenuItem mntmAemApps = new JMenuItem("Download Packages (PackageShare)");
		mntmAemApps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_apps_packages");
			}
		});
		mnApps.add(mntmAemApps);

		// Commerce Add-on
		JMenu mnCommerce = new JMenu("Commerce");
		mnUpdate.add(mnCommerce);

		JMenu mnCommerceDownload = new JMenu("Download Packages");
		mnCommerce.add(mnCommerceDownload);

		// Commerce EP
		JMenuItem mnCommerceDownloadEP = new JMenuItem("ElasticPath (PackageShare)");
		mnCommerceDownloadEP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_commerce_ep");
			}
		});
		mnCommerceDownload.add(mnCommerceDownloadEP);

		// Commerce WebSphere
		JMenuItem mnCommerceDownloadWAS = new JMenuItem("WebSphere (PackageShare)");
		mnCommerceDownloadWAS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_commerce_websphere");
			}
		});
		mnCommerceDownload.add(mnCommerceDownloadWAS);

		// WeRetail Add-on
		JMenu mnWeRetail = new JMenu("We-Retail");
		mnUpdate.add(mnWeRetail);

		JMenuItem mnWeRetailAddon = new JMenuItem("Download Demo Add-on");
		mnWeRetailAddon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_weretail");
			}
		});
		mnWeRetail.add(mnWeRetailAddon);

		// Download all section
		mnUpdate.addSeparator();

		JMenuItem mntmAemDownloadAll = new JMenuItem("Download All Add-ons");
		mntmAemDownloadAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_all");
			}
		});
		mnUpdate.add(mntmAemDownloadAll);

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

		// James options
		JMenu mnJames = new JMenu("James SMTP/POP");

		JMenuItem mntmInfraJames = new JMenuItem("Download");
		mntmInfraJames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_james");
			}
		});
		mnJames.add(mntmInfraJames);

		JMenuItem mntmInfraJamesInstall = new JMenuItem("Install");
		mntmInfraJamesInstall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "install_james");
			}
		});
		mnJames.add(mntmInfraJamesInstall);
		mnJames.addSeparator();

		JMenuItem mntmInfraJamesStart = new JMenuItem("Start");
		mntmInfraJamesStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "james_start");
			}
		});
		mnJames.add(mntmInfraJamesStart);

		JMenuItem mntmInfraJamesStop = new JMenuItem("Stop");
		mntmInfraJamesStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "james_stop");
			}
		});
		mnJames.add(mntmInfraJamesStop);

		mnInfrastructure.add(mnJames);

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

		mnInfrastructure.addSeparator();

		// InDesignServer options
		JMenu mnInDesignServer = new JMenu("InDesign Server");

		JMenuItem mntmInfraInDesignServerDownload = new JMenuItem("Download");
		mntmInfraInDesignServerDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_indesignserver");
			}
		});
		mnInDesignServer.add(mntmInfraInDesignServerDownload);

		mnInDesignServer.addSeparator();

		JMenuItem mntmInfraInDesignServerStart = new JMenuItem("Start");
		mntmInfraInDesignServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "start_indesignserver");
			}
		});
		mnInDesignServer.add(mntmInfraInDesignServerStart);

		JMenuItem mntmInfraInDesignServerStop = new JMenuItem("Stop");
		mntmInfraInDesignServerStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "stop_indesignserver");
			}
		});
		mnInDesignServer.add(mntmInfraInDesignServerStop);

		mnInfrastructure.add(mnInDesignServer);

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

		JMenuItem mntmAemLoad = new JMenuItem("Download Latest AEM Load");
		mntmAemLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmAemLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_load");
			}
		});
		mntmAemDownload.add(mntmAemLoad);

		JMenuItem mntmAemSnapshot = new JMenuItem("Download Latest AEM Snapshot");
		mntmAemSnapshot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmAemSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_snapshot");
			}
		});
		mntmAemDownload.add(mntmAemSnapshot);


		JMenuItem mntmAemDownloadAEM62 = new JMenuItem("Download AEM 6.2");
		mntmAemDownloadAEM62.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_aem62");
			}
		});
		mntmAemDownload.add(mntmAemDownloadAEM62);

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

		JMenuItem mntmAemHotfix = new JMenuItem("Download Latest Hotfixes (PackageShare)");
		mntmAemHotfix.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmAemHotfix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_hotfixes_packages");
			}
		});
		mnOther.add(mntmAemHotfix);

		JMenuItem mntmAemAcs = new JMenuItem("Download Latest ACS Commons and Tools");
		mntmAemAcs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mntmAemAcs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "download_acs");
			}
		});
		mnOther.add(mntmAemAcs);

		// Adding the menu bar
		frameMain.setJMenuBar(menuBar);

		// Adding other form elements
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(24, 163, 650, 230);
		frameMain.getContentPane().add(scrollPane);

		final JTextArea textArea = new JTextArea("");
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		// List of demo machines available
		JScrollPane scrollDemoList = new JScrollPane();
		scrollDemoList.setBounds(24, 34, 208, 100);
		frameMain.getContentPane().add(scrollDemoList);
		listModelDemoMachines = AemDemoUtils.listDemoMachines(buildFile.getParentFile().getAbsolutePath());
		listDemoMachines = new JList(listModelDemoMachines);
		listDemoMachines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listDemoMachines.setSelectedIndex(AemDemoUtils.getSelectedIndex(listDemoMachines,this.getDefaultProperties(), this.getPersonalProperties(),AemDemoConstants.OPTIONS_BUILD_DEFAULT));
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

		btnStart.setBounds(250, 29, 117, 29);
		frameMain.getContentPane().add(btnStart);

		// Set Start as the default button
		JRootPane rootPane = SwingUtilities.getRootPane(btnStart);
		rootPane.setDefaultButton(btnStart);

		JButton btnInfo = new JButton("Details");
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				AemDemoUtils.antTarget(AemDemo.this, "details");

			}
		});
		btnInfo.setBounds(250, 59, 117, 29);
		frameMain.getContentPane().add(btnInfo);

		// Rebuild action
		JButton btnRebuild = new JButton("Rebuild");
		btnRebuild.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (AemDemo.this.getBuildInProgress()) {

					JOptionPane.showMessageDialog(null, "A Demo Environment is currently being built. Please wait until it is finished.");

				} else {

					final AemDemoRebuild dialogRebuild = new AemDemoRebuild(AemDemo.this);
					dialogRebuild.setModal(true);
					dialogRebuild.setVisible(true);
					dialogRebuild.getDemoBuildName().requestFocus();;

				}

			}
		});

		btnRebuild.setBounds(250, 89, 117, 29);
		frameMain.getContentPane().add(btnRebuild);

		// Stop action
		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you really want to stop the running instances?","Warning",JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.NO_OPTION) {
					return;
				}
				AemDemoUtils.antTarget(AemDemo.this, "stop");

			}
		});
		btnStop.setBounds(500, 29, 117, 29);
		frameMain.getContentPane().add(btnStop);

		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onExit();
			}
		});
		btnExit.setBounds(550, 408, 117, 29);
		frameMain.getContentPane().add(btnExit);

		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
		btnClear.setBounds(40, 408, 117, 29);
		frameMain.getContentPane().add(btnClear);

		JButton btnBackup = new JButton("Backup");
		btnBackup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "backup");
			}
		});
		btnBackup.setBounds(500, 59, 117, 29);
		frameMain.getContentPane().add(btnBackup);

		JButton btnRestore = new JButton("Restore");
		btnRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AemDemoUtils.antTarget(AemDemo.this, "restore");
			}
		});
		btnRestore.setBounds(500, 89, 117, 29);
		frameMain.getContentPane().add(btnRestore);

		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure you really want to permanently delete the selected demo configuration?","Warning",JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.NO_OPTION) {
					return;
				}
				AemDemoUtils.antTarget(AemDemo.this, "uninstall");
			}
		});
		btnDelete.setBounds(500, 119, 117, 29);
		frameMain.getContentPane().add(btnDelete);

		JLabel lblSelectYourDemo = new JLabel("Select your Demo Environment");
		lblSelectYourDemo.setBounds(24, 10, 219, 16);
		frameMain.getContentPane().add(lblSelectYourDemo);

		JLabel lblCommandOutput = new JLabel("Command Output");
		lblCommandOutput.setBounds(24, 143, 160, 16);
		frameMain.getContentPane().add(lblCommandOutput);

		// Initializing and launching the ticker
		String tickerOn = AemDemoUtils.getPropertyValue(buildFile,"demo.ticker");
		if (tickerOn==null || (tickerOn!=null && tickerOn.equals("true"))) {
			AemDemoMarquee mp = new AemDemoMarquee(AemDemoConstants.Credits, 60);
			mp.setBounds(140, 440, 650, 30);
			frameMain.getContentPane().add(mp);
			mp.start();
		}

		// Launching the download tracker task
		AemDemoDownload aemDownload = new AemDemoDownload(AemDemo.this);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(aemDownload, 0, 5, TimeUnit.SECONDS);

		// Loading up the README.md file
		String line=null;
		try {
			FileReader fileReader = new FileReader(buildFile.getParentFile().getAbsolutePath() + File.separator + "README.md");
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				if (line.indexOf("AEM Demo Machine!")>0) {
					line = line + " (version: " + aemDemoMachineVersion + ")";
				}
				if (!line.startsWith("Double"))
					System.out.println(line);
			}
			bufferedReader.close();
		}
		catch(Exception ex) {
			logger.error(ex.getMessage());
		}

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

	public String getSelectedDemoMachine() {
		return this.listDemoMachines.getSelectedValue();
	}

	/**
	 * Exiting from application without any error
	 */
	public void onExit() {
		System.exit(0);
	}
}
