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
package com.adobe.aem.demomachine.communities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.demomachine.Hostname;

@SlingServlet(paths="/bin/CreateCommunities", methods = "GET", metatype=false)
public class CreateCommunities extends org.apache.sling.api.servlets.SlingAllMethodsServlet {

	private static final long serialVersionUID = 9187308313909516127L;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		ResourceResolver resourceResolver = request.getResourceResolver();

		String userId = resourceResolver.getUserID();
		if (userId==null || !userId.equals("admin")) {
			out.println("Permission denied: admin user requested to access this feature");
			return;
		}

		// Checking the version of GraniteUI to be loaded (2 or 3 depending on AEM version)
		String coralVersion = "3";
		Resource resCoral = resourceResolver.getResource("/etc/clientlibs/granite/coralui3.js");
		if (resCoral==null) coralVersion = "2";

		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"/etc/clientlibs/granite/coralui" + coralVersion + ".css\" type=\"text/css\">");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/typekit.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/jquery.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/utils.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/moment.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/coralui" + coralVersion + ".js\"></script>");
		out.println("</head><body class=\"coral--light u-coral-clearFix\" style=\"margin:40px\">");
		out.println("<div><h1>");
		if (coralVersion.equals("3")) {
			out.println("<coral-wait></coral-wait> ");
		}
		out.println("AEM Communities demo setup in progress, please wait...</h1>");

		// Checking if we have valid configuration parameters
		String csvPath = (String) request.getParameter("contentPath");
		if (csvPath==null) {
			out.println("Aborting: No content path to configuration file provided with csv query string");
			return;
		}
		String hostname = (String) request.getParameter("hostname");
		String port = (String) request.getParameter("port");
		String hostname_author = (String) request.getParameter("hostname_author");
		String port_author = (String) request.getParameter("port_author");
		String password = (String) request.getParameter("password");
		String analytics = (String) request.getParameter("analytics");
		String minimizeParam = (String) request.getParameter("minimize");
		boolean minimize = (minimizeParam!=null && minimizeParam.length()>0)?true:false;

		// Checking if the specified hosts and ports are reachable
		if (hostname_author==null || port_author==null || !Hostname.isReachable(hostname_author,port_author)) {
			out.println("Aborting: Your AEM Author instance is not reachable. Please verify it is properly started.");
			return;
		}

		if (hostname==null) hostname="";
		if (port==null) port="";
		if (!hostname.equals("") && !port.equals("") && !Hostname.isReachable(hostname,port)) {
			out.println("Aborting: Your AEM Publish instance is not reachable. Please verify it is properly started.");
			return;
		}

		// Defaults to all author server if publish server is not specified
		if (hostname.equals("") || port.equals("")) {
			hostname = hostname_author;
			port = port_author;
			out.println("<div><coral-alert>");
			out.println("<coral-alert-header>WARNING</coral-alert-header>");
			out.println("<coral-alert-content>Using an AEM Publish instance is strongly recommended. All UGC is now being posted against this Author instance, which might fail if the demo members are not granted appropriate permissions on Author.</coral-alert-content>");
			out.println("</coral-alert></div>");
		}

		// Checking if we have a return URL
		String returnURL = (String) request.getParameter("returnURL");

		out.println("<p>Path to configuration file: " + csvPath + "</p>" );

		// Config options
		ArrayList<String> configOptions = new ArrayList<String>();
		for (@SuppressWarnings("unchecked")
		Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {

			String paramName = e.nextElement();
			if (paramName.startsWith("setup")) {

				configOptions.add(paramName);

			}
		}

		Collections.sort(configOptions);

		if (configOptions.size()>0) {

			for (String configOption : configOptions) {

				Resource resConfigFiles = resourceResolver.getResource(csvPath);
				for (Resource resConfigFile: resConfigFiles.getChildren()) {
					if (resConfigFile!=null && resConfigFile.getName().startsWith(configOption.replace("setup-", "")) && (resConfigFile.getName().toLowerCase().endsWith(".csv"))) {

						InputStream stream = resConfigFile.adaptTo(InputStream.class);		

						Reader in = new InputStreamReader(stream);
						out.println("<p>Processing: " + resConfigFile.getName() + "</p>");
						response.flushBuffer();;
						if (resConfigFile.getName().contains("author")) {
							Loader.processLoading(resourceResolver, in, hostname_author, port_author, port, password, analytics, false, true, minimize, csvPath);
						} else {
							Loader.processLoading(resourceResolver, in, hostname, port, port, password, analytics, false, true, minimize, csvPath);
						}

						try {
							in.close();
							stream.close();
						} catch (IOException ioex) {
							//omitted.
						}
					}

				}

			}

		}

		// Printing instructions, when available
		Resource resHelp = resourceResolver.getResource(csvPath + "/readme.html");
		if (resHelp!=null) {
			InputStream stream = resHelp.adaptTo(InputStream.class);		
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String line;
			while((line=in.readLine())!= null){
				out.println(line);
			}

			try {
				in.close();
				stream.close();
			} catch (IOException ioex) {
				//omitted.
			}

		}

		out.println("<p>Process completed!</p>");
		if (returnURL!=null) {
			out.println("<p><a href=\"http://" + request.getServerName() + ":" + request.getServerPort() + returnURL +"\">Check your site !</a></p>");
		}
		response.flushBuffer();
		out.println("</body></html>");

	}

}