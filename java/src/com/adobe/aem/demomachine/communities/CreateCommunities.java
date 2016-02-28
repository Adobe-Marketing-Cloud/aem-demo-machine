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
import java.rmi.ServerException;

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
			out.println("admin user requested to access this feature");
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
			response.getWriter().write("No content path to configuration file provided with csv query string");
			return;
		}
		String hostname = (String) request.getParameter("hostname");
		String port = (String) request.getParameter("port");
		String hostname_author = (String) request.getParameter("hostname_author");
		String port_author = (String) request.getParameter("port_author");
		String password = (String) request.getParameter("password");

		// Checking if we have a return URL
		String returnURL = (String) request.getParameter("returnURL");

		// Config options
		String setupUGC = (String) request.getParameter("setupUGC");
		String setupSite = (String) request.getParameter("setupSite");
		String setupContent = (String) request.getParameter("setupContent");
		String setupGroup = (String) request.getParameter("setupGroup");
		String setupEnablement = (String) request.getParameter("setupEnablement");

		response.getWriter().write("<p>Path to configuration file: " + csvPath + "</p>" );
		
		// UGC configuration
		Resource resUGC = resourceResolver.getResource(csvPath + "/setup/ugc.csv/jcr:content");
		if (resUGC!=null && setupUGC!=null) {
			InputStream stream = resUGC.adaptTo(InputStream.class);		
			Reader inUGC = new InputStreamReader(stream);
			response.getWriter().write("<p>Creating UGC on Publish...</p>");
			response.flushBuffer();;
			Loader.processLoading(resourceResolver, inUGC, hostname_author, port_author, port, password, null, false, false, csvPath);
		}

		// Site configuration
		Resource resSite = resourceResolver.getResource(csvPath + "/setup/site.csv/jcr:content");
		if (resSite!=null && setupSite!=null) {
			InputStream stream = resSite.adaptTo(InputStream.class);		
			Reader inSites = new InputStreamReader(stream);
			response.getWriter().write("<p>Creating Community Site on Author...</p>");
			response.flushBuffer();;
			Loader.processLoading(resourceResolver, inSites, hostname_author, port_author, port, password, null, false, false, csvPath);
		}

		// Content configuration
		Resource resContent = resourceResolver.getResource(csvPath + "/setup/content.csv/jcr:content");
		if (resContent!=null & setupContent!=null) {
			InputStream streamContent = resContent.adaptTo(InputStream.class);		
			Reader inContent = new InputStreamReader(streamContent);
			response.getWriter().write("<p>Creating Community Content on Publish...</p>");
			response.flushBuffer();;
			Loader.processLoading(resourceResolver, inContent, hostname, port, port, password, null, false, false, csvPath);
		}

		// Group configuration
		Resource resGroup = resourceResolver.getResource(csvPath + "/setup/group.csv/jcr:content");
		if (resGroup!=null & setupGroup!=null) {
			InputStream streamContent = resGroup.adaptTo(InputStream.class);		
			Reader inGroup = new InputStreamReader(streamContent);
			response.getWriter().write("<p>Creating Community Groups on Publish...</p>");
			response.flushBuffer();;
			Loader.processLoading(resourceResolver, inGroup, hostname, port, port, password, null, false, false, csvPath);
		}

		// Enablement configuration
		Resource resEnablement = resourceResolver.getResource(csvPath + "/setup/enablement.csv/jcr:content");
		if (resEnablement!=null & setupEnablement!=null) {
			InputStream streamContent = resEnablement.adaptTo(InputStream.class);		
			Reader inEnablement = new InputStreamReader(streamContent);
			response.getWriter().write("<p>Creating Community Enablement content on Author...</p>");
			response.flushBuffer();;
			Loader.processLoading(resourceResolver, inEnablement, hostname_author, port_author, port, password, null, false, false, csvPath);
		}

		response.getWriter().write("<p>Process completed!</p>");
		if (returnURL!=null) {
			response.getWriter().write("<p><a href=\"http://" + request.getServerName() + ":" + request.getServerPort() + returnURL +"\">Check your site !</a></p>");
		}
		response.flushBuffer();
		out.println("</body></html>");

	}

}