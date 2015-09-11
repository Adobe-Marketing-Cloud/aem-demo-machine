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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

// Posting an XML fragment to Adobe Analytics endpoint as per the Data Insertion API
public class Analytics {

	static Logger logger = Logger.getLogger(Analytics.class);

	public static void main(String[] args) {

		String hostname=null;
		String url=null;
		String eventfile=null;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("h", true, "Hostname");
		options.addOption("u", true, "Url");
		options.addOption("f", true, "Event data file");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("u")) {
				url = cmd.getOptionValue("u");
			}

			if(cmd.hasOption("f")) {
				eventfile = cmd.getOptionValue("f");
			}

			if(cmd.hasOption("h")) {
				hostname = cmd.getOptionValue("h");
			}

			if (eventfile==null || hostname==null || url == null) {
				System.out.println("Command line parameters: -h hostname -u url -f path_to_XML_file");
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		URLConnection urlConn         = null;
		DataOutputStream printout     = null;
		BufferedReader input        = null;
		String u            = "http://" + hostname + "/" + url;
		String tmp             = null;
		try {
			
			URL myurl = new URL( u );
			urlConn = myurl.openConnection();
			urlConn.setDoInput( true );
			urlConn.setDoOutput( true );
			urlConn.setUseCaches( false );
			urlConn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );

			printout = new DataOutputStream(urlConn.getOutputStream());

			String xml = readFile(eventfile,StandardCharsets.UTF_8);
			printout.writeBytes( xml );
			printout.flush();
			printout.close();

			input = new BufferedReader( new InputStreamReader( urlConn.getInputStream( ) ) );

			logger.debug(xml);
			while( null != ( ( tmp = input.readLine() ) ) )
			{
				logger.debug(tmp);
			}
			printout.close();
			input.close();
			
		} catch (Exception ex) {

			logger.error(ex.getMessage());

		}

	}

	static String readFile(String path, Charset encoding) 
			throws IOException 
			{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
			}
}
