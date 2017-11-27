/*******************************************************************************
 * Copyright 2017 Adobe Systems Incorporated.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class YmlMerge {

	static Logger logger = Logger.getLogger(YmlMerge.class);

	public static void main(String[] args) throws IOException {

		String inputFile1 = null;
		String inputFile2 = null;
		
		// Command line options for this tool
		Options options = new Options();
		options.addOption("s", true, "Source YML");
		options.addOption("m", true, "Merged YML");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("s")) {
				inputFile1 = cmd.getOptionValue("s");
			}

			if(cmd.hasOption("m")) {
				inputFile2 = cmd.getOptionValue("m");
			}

			if (inputFile1==null || inputFile1==null) {
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		// Reading the source YML file
		File file = new File(inputFile1);
		StringBuffer mergedContent = new StringBuffer();
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			mergedContent.append(line + System.lineSeparator());
			if (!line.startsWith(" ")) {
				//We have a new section, let's insert possible matching sections in the merged YML
				File mergedFile = new File(inputFile2);
				FileReader mergedFileReader = new FileReader(mergedFile);
				BufferedReader mergedBufferedReader = new BufferedReader(mergedFileReader);
				String mergedLine;
				while ((mergedLine = mergedBufferedReader.readLine()) != null) {
					if (mergedLine.equals(line)) {
						while ((mergedLine = mergedBufferedReader.readLine()) != null && (mergedLine.startsWith(" ") || mergedLine.startsWith("\t"))) {
							mergedContent.append(mergedLine + System.lineSeparator());
						}
					}
				}
				mergedFileReader.close();
			}
		}
		fileReader.close();

		// Renaming the file
		File fileOld = new File(inputFile1 + ".bak");
		boolean success = file.renameTo(fileOld);

		// Writing the merged file
        BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(inputFile1)));
        bwr.write(mergedContent.toString());
        bwr.flush();
        bwr.close();
		
	}

}