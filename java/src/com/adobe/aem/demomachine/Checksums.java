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
package com.adobe.aem.demomachine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.adobe.aem.demomachine.gui.AemDemoConstants;
import com.adobe.aem.demomachine.gui.AemDemoUtils;

// This file will generate checksums for 
public class Checksums {

	static Logger logger = Logger.getLogger(Checksums.class);

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
		List<String[]> listPaths = Arrays.asList(AemDemoConstants.demoPaths);
		for (String[] path:listPaths) {
			if (path.length==4) {
				logger.debug(path[1]);
				File pathFolder = new File(rootFolder + (path[1].length()>0?(File.separator + path[1]):""));
				if (pathFolder.exists()) {
					String md5 = AemDemoUtils.calcMD5HashForDir(pathFolder, Boolean.parseBoolean(path[3]), false);
					logger.debug("MD5 is: "+ md5);
					md5properties.setProperty("demo.path." + path[0], path[1]);
					md5properties.setProperty("demo.md5." + path[0], md5);
				} else {
					logger.error("Folder cannot be found");
				}
			}
		}

		File md5 = new File(rootFolder + File.separator + "conf" + File.separator + "checksums.properties");
		try {

			@SuppressWarnings("serial")
			Properties tmpProperties = new Properties() {
				@Override
				public synchronized Enumeration<Object> keys() {
					return Collections.enumeration(new TreeSet<Object>(super.keySet()));
				}
			};
			tmpProperties.putAll(md5properties);			
			tmpProperties.store(new FileOutputStream(md5), null);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		System.out.println("MD5 checkums generated");

	}
}
