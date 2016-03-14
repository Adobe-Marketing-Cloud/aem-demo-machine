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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

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

		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"/etc/clientlibs/granite/coralui3.css\" type=\"text/css\">");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/typekit.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/jquery.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/utils.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/moment.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/coralui3.js\"></script>");
		out.println("</head><body class=\"coral--light u-coral-clearFix\" style=\"margin:40px\">");
		out.println("<div><h1>AEM Communities - Demo Setup in Progress - Please wait!</h1>");

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

		// Checking if the specified hosts and ports are reachable
		if (!isReachable(hostname,port)) {
			out.println("Aborting: Your AEM Publish instance is not reachable. Please verify it is properly started.");
			return;
		}
		if (!isReachable(hostname_author,port_author)) {
			out.println("Aborting: Your AEM Author instance is not reachable. Please verify it is properly started.");
			return;
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
					if (resConfigFile!=null && resConfigFile.getName().startsWith(configOption.replace("setup-", "")) && resConfigFile.getName().endsWith(".csv")) {
						InputStream stream = resConfigFile.adaptTo(InputStream.class);		
						Reader in = new InputStreamReader(stream);
						out.println("<p>Processing: " + resConfigFile.getName() + "</p>");
						response.flushBuffer();;
						if (resConfigFile.getName().contains("author")) {
							Loader.processLoading(resourceResolver, in, hostname_author, port_author, port, password, null, false, false, csvPath);
						} else {
							Loader.processLoading(resourceResolver, in, hostname, port, port, password, null, false, false, csvPath);
						}
					}

				}

			}

		}

		out.println("<p>Process completed!</p>");
		if (returnURL!=null) {
			out.println("<p><a href=\"http://" + request.getServerName() + ":" + request.getServerPort() + returnURL +"\">Check your site !</a></p>");
		}
		response.flushBuffer();
		out.println("</body></html>");

	}
	
	private boolean isReachable(String host, String port) {
		
		if (port==null || port.equals("")) return false;
		if (host==null || host.equals("")) return false;
		
		SocketAddress sockaddr = new InetSocketAddress(host, Integer.parseInt(port));
		Socket socket = new Socket();
		boolean online = true;
		try {
		    socket.connect(sockaddr, 10000);
		} catch (SocketTimeoutException stex) {
		    // treating timeout errors separately from other io exceptions
		    online = false;
		} catch (IOException iOException) {
		    online = false;    
		} finally {
		    // As the close() operation can also throw an IOException
		    try {
		        socket.close();
		    } catch (IOException ex) {
		    }
		}
		
		return online;
		
	}

}