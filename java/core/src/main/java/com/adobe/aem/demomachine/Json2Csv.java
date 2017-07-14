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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Json2Csv {

	static Logger logger = Logger.getLogger(Json2Csv.class);

	public static void main(String[] args) throws IOException {

		String inputFile1 = null;
		String inputFile2 = null;
		String outputFile = null;

		HashMap<String,String> hmReportSuites =new HashMap<String,String>();
		
		// Command line options for this tool
		Options options = new Options();
		options.addOption("c", true, "Filename 1");
		options.addOption("r", true, "Filename 2");
		options.addOption("o", true, "Filename 3");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("c")) {
				inputFile1 = cmd.getOptionValue("c");
			}

			if(cmd.hasOption("r")) {
				inputFile2 = cmd.getOptionValue("r");
			}

			if(cmd.hasOption("o")) {
				outputFile = cmd.getOptionValue("o");
			}

			if (inputFile1==null || inputFile1==null || outputFile==null) {
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		// List of customers and report suites for these customers
		String sInputFile1 = readFile(inputFile1, Charset.defaultCharset());
		sInputFile1 = sInputFile1.replaceAll("ObjectId\\(\"([0-9a-z]*)\"\\)", "\"$1\"");

		// Processing the list of report suites for each customer
		try {

			JSONArray jCustomers = new JSONArray(sInputFile1.trim());
			for (int i = 0, size = jCustomers.length(); i < size; i++)
			{
				JSONObject jCustomer = jCustomers.getJSONObject(i);
				Iterator<?> keys = jCustomer.keys();
				String companyName=null;
				while( keys.hasNext() ) {
					String key = (String)keys.next();
					if (key.equals("company")) {
						companyName=jCustomer.getString(key);
					}
				}
				 keys = jCustomer.keys();
					while( keys.hasNext() ) {
						String key = (String)keys.next();
	
					if (key.equals("report_suites")) {
						JSONArray jReportSuites = jCustomer.getJSONArray(key);
						for (int j = 0, rSize = jReportSuites.length(); j < rSize; j++)
						{
							hmReportSuites.put(jReportSuites.getString(j),companyName);
							System.out.println(jReportSuites.get(j) + " for company " + companyName);
						}

					}
				}
			}

			// Creating the out put file
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
			writer.println("\"" + "Customer" + "\",\"" + "ReportSuite ID" + "\",\"" + "Number of Documents" + "\",\"" + "Last Updated" + "\"");	
			
			// Processing the list of SOLR collections
			String sInputFile2 = readFile(inputFile2, Charset.defaultCharset());
			sInputFile2 = sInputFile2.replaceAll("NumberLong\\(\"([0-9a-z]*)\"\\)", "\"$1\"");

			JSONObject jResults = new JSONObject(sInputFile2.trim());
			JSONArray jCollections = jResults.getJSONArray("result");
			for (int i = 0, size = jCollections.length(); i < size; i++)
			{
				JSONObject jCollection = jCollections.getJSONObject(i);
				String id = null;
				String number = null;
				String lastupdate = null;

				Iterator<?> keys = jCollection.keys();
				while( keys.hasNext() ) {
					String key = (String)keys.next();
					if (key.equals("_id")) {
						id=jCollection.getString(key);
					}
				}

				keys = jCollection.keys();
				while( keys.hasNext() ) {
					String key = (String)keys.next();
					if (key.equals("noOfDocs")) {
						number=jCollection.getString(key);
					}
				}

				keys = jCollection.keys();
				while( keys.hasNext() ) {
					String key = (String)keys.next();
					if (key.equals("latestUpdateDate")) {
						lastupdate=jCollection.getString(key);
					}
				}

			Date d = new Date(Long.parseLong(lastupdate));
			System.out.println(hmReportSuites.get(id) + "," + id + "," + number + "," + lastupdate + "," + new SimpleDateFormat("MM-dd-yyyy").format(d));	
			writer.println("\"" + hmReportSuites.get(id) + "\",\"" + id + "\",\"" + number + "\",\"" + new SimpleDateFormat("MM-dd-yyyy").format(d) + "\"");	
			
			}

		    writer.close();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	static String readFile(String path, Charset encoding) 
			throws IOException 
			{

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);

			}

}
