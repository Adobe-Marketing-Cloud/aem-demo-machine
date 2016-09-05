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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class RegExp {

	static Logger logger = Logger.getLogger(RegExp.class);

	public static void main(String[] args) throws IOException {

		String fileName = null;
		String regExp = null;
		String position = null;
		String value = "n/a";
		List<String> allMatches = new ArrayList<String>();

		// Command line options for this tool
		Options options = new Options();
		options.addOption("f", true, "Filename");
		options.addOption("r", true, "RegExp");
		options.addOption("p", true, "Position");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("f")) {
				fileName = cmd.getOptionValue("f");
			}

			if(cmd.hasOption("f")) {
				regExp = cmd.getOptionValue("r");
			}

			if(cmd.hasOption("p")) {
				position = cmd.getOptionValue("p");
			}

			if (fileName==null || regExp==null || position == null) {
				System.out.println("Command line parameters: -f fileName -r regExp -p position");
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		String content = readFile(fileName, Charset.defaultCharset());

		if (content!=null) {
			Matcher m = Pattern.compile(regExp)
					.matcher(content);
			while (m.find()) {
				String group = m.group();
				int pos = group.indexOf(".zip");
				if (pos>0) {
					group = group.substring(0, pos);
				}
				logger.debug("RegExp: " + m.group() + " found returning " + group);
				allMatches.add(group);
			}

			if (allMatches.size()>0) {

				if (position.equals("first")) {
					value = allMatches.get(0);
				}

				if (position.equals("last")) {
					value = allMatches.get(allMatches.size()-1);
				}
			}
		}

		System.out.println(value);

	}

	static String readFile(String path, Charset encoding) 
			throws IOException 
			{

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);

			}

}
