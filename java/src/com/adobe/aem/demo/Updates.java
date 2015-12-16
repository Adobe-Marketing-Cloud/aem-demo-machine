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
package com.adobe.aem.demo;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.adobe.aem.demo.gui.AemDemoConstants;
import com.adobe.aem.demo.gui.AemDemoUtils;

// This file will check if there are updates for the main demo machine components
public class Updates {

	static Logger logger = Logger.getLogger(Updates.class);

	public static void main(String[] args) {

		String rootFolder = null;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("f", true, "Demo Machine root folder");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("f")) {
				rootFolder = cmd.getOptionValue("f");
			}

		} catch(Exception e) {
			System.exit(-1);
		}

		Properties md5properties = new Properties();

		try {
			URL url = new URL("https://raw.githubusercontent.com/Adobe-Marketing-Cloud/aem-demo-machine/master/conf/checksums.properties");
			InputStream in = url.openStream();
			Reader reader = new InputStreamReader(in, "UTF-8");
			md5properties.load(reader);
			reader.close();
		} catch (Exception e) {
			System.out.println("Error: Cannot connect to GitHub.com to check for updates");
			System.exit(-1);
		}

		System.out.println(AemDemoConstants.HR);

		int nbUpdateAvailable = 0;

		List<String[]> listPaths = Arrays.asList(AemDemoConstants.demoPaths);
		for (String[] path:listPaths) {
			if (path.length==4) {
				logger.debug(path[1]);
				File pathFolder = new File(rootFolder + (path[1].length()>0?(File.separator + path[1]):""));
				if (pathFolder.exists()) {
					String newMd5 = AemDemoUtils.calcMD5HashForDir(pathFolder, Boolean.parseBoolean(path[3]), false);
					logger.debug("MD5 is: "+ newMd5);
					String oldMd5 = md5properties.getProperty("demo.md5." + path[0]);
					if (oldMd5==null || oldMd5.length()==0) {
						logger.error("Cannot find MD5 for " + path[0]);
						System.out.println(path[2] + " : Cannot find M5 checksum");
						continue;
					}
					if (newMd5.equals(oldMd5)) {
						continue;						
					} else {
						System.out.println(path[2] + " : New update available" + (path[0].equals("0")?" (use 'git pull' to get the latest changes)":""));
						nbUpdateAvailable++;
					}
				} else {
					System.out.println(path[2] + " : Not installed");
				}
			}
		}

		if (nbUpdateAvailable==0) {
			System.out.println("Your AEM Demo Machine is up to date!");
		}

		System.out.println(AemDemoConstants.HR);

	}
}
