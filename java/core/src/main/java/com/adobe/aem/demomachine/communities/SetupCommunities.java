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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.demomachine.Hostname;

@SlingServlet(paths="/bin/SetupCommunities", methods = "GET", metatype=false)
public class SetupCommunities extends org.apache.sling.api.servlets.SlingAllMethodsServlet {

	private static final long serialVersionUID = 2891656166255468489L;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {

		PrintWriter out = response.getWriter();
		
		// Checking if we have a valid admin user
		ResourceResolver resourceResolver = request.getResourceResolver();
		String userId = resourceResolver.getUserID();
		if (userId==null || !userId.equals("admin")) {
			out.println("Permission denied: admin user requested to access this feature");
			return;
		}

		// Checking if we have valid configuration parameters
		String csvPath = (String) request.getParameter("contentPath");
		if (csvPath==null) {
			csvPath = "";
		}
		
		response.setContentType("text/html");
		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"/etc.clientlibs/clientlibs/granite/coralui3.css\" type=\"text/css\">");
		out.println("<script type=\"text/javascript\" src=\"/etc.clientlibs/clientlibs/granite/typekit.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc.clientlibs/clientlibs/granite/jquery.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc.clientlibs/clientlibs/granite/utils.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc.clientlibs/clientlibs/granite/moment.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc.clientlibs/clientlibs/granite/coralui3.js\"></script>");
		out.println("</head><body class=\"coral--light u-coral-clearFix\" style=\"margin:40px\">");

		// Checking if the page is loaded in a frame (e.g. authoring environment)
		out.println("<script language=\"JavaScript\">if(window.frameElement){window.top.location=window.location.href;}</script>");
		
		out.println("<a name=\"top\"/>");
		out.println("<div><h1>AEM Communities - Demo Setup</h1>");
		out.println("<form action=\"/bin/CreateCommunities\" method=\"GET\" class=\"coral-Form coral-Form--vertical\" style=\"width:700px\">");
		out.println("<section class=\"coral-Form-fieldset\">");
		out.println("<span>All the fun takes place on the Publish instance with AEM - please ensure yours is available at the following coordinates</spanl>");
		out.println("<label class=\"coral-Form-fieldlabel\">Path to configuration files</label>");
		out.println("<input is=\"coral-textfield\" name=\"contentPath\" type=\"text\" value=\"" + csvPath + "\" class=\"coral-Form-field coral-Textfield\">");
		out.println("<label class=\"coral-Form-fieldlabel\">Author instance</label>");
		out.println("<div class=\"coral-Form--aligned\">");
		// Checking if the default host and port are reachable for the author server
		String hostname_author = "localhost";
		String port_author = "4502";
		if (!Hostname.isReachable(hostname_author, port_author)) {
			hostname_author = "";
			port_author = "";
		}
		out.println("<input is=\"coral-textfield\" name=\"hostname_author\" type=\"text\" value=\"" + hostname_author + "\" class=\"coral-Textfield\">");
		out.println("<input is=\"coral-textfield\" name=\"port_author\" type=\"text\" value=\"" + port_author + "\" class=\"coral-Textfield\">");
		out.println("</div>");
		out.println("<label class=\"coral-Form-fieldlabel\">Publish instance</label>");
		// Checking if the default host and port are reachable for the publish server
		String hostname_publish = "localhost";
		String port_publish = "4503";
		if (!Hostname.isReachable(hostname_publish, port_publish)) {
			hostname_publish = "";
			port_publish = "";
			out.println("<coral-alert>");
			out.println("<coral-alert-header>WARNING</coral-alert-header>");
			out.println("<coral-alert-content>Using an AEM Publish instance is strongly recommended. If not using a Publish instance, all UGC will be posted against the Author instance, which might fail if the demo members are not granted appropriate permissions on Author.</coral-alert-content>");
			out.println("</coral-alert>");
		}
		out.println("<div class=\"coral-Form--aligned\">");

		out.println("<input is=\"coral-textfield\" name=\"hostname\" type=\"text\" value=\"" + hostname_publish + "\" class=\"coral-Textfield\">");
		out.println("<input is=\"coral-textfield\" name=\"port\" type=\"text\" value=\"" + port_publish + "\" class=\"coral-Textfield\">");

		out.println("</div>");
		out.println("<label class=\"coral-Form-fieldlabel\">Admin password</label>");
		out.println("<input is=\"coral-textfield\" name=\"password\" type=\"text\" value=\"admin\" class=\"coral-Form-field coral-Textfield\">");
		out.println("<label class=\"coral-Form-fieldlabel\">Please select from the following options</label>");

		// Getting the list of .csv configuration files for this content path
		int intOptions = 0;
		Resource resConfigFiles = resourceResolver.getResource(csvPath);
		if (!csvPath.equals("") && resConfigFiles!=null) {
			ArrayList<String[]> configOptions = new ArrayList<String[]>();
			for (Resource resConfigFile: resConfigFiles.getChildren()) {
				if (resConfigFile!=null && resConfigFile.getName().endsWith(".csv")) {
					String[] resConfigSettings = resConfigFile.getName().split("-");
					configOptions.add(resConfigSettings);
				}

			}
			Collections.sort(configOptions,new Comparator<String[]>() {
				public int compare(String[] strings, String[] otherStrings) {
					return strings[0].compareTo(otherStrings[0]);
				}
			});
			for (String[] configOption : configOptions) {

				// Loading title and description
				String title = configOption[2];
				String description = configOption[0] + "-" + configOption[1] + "-" + configOption[2];
				Resource resConfigFile = resourceResolver.getResource(csvPath + "/" + description + "/jcr:content");
				if (resConfigFile != null) {

					InputStream stream = resConfigFile.adaptTo(InputStream.class);		
					Reader inConfigFile = new InputStreamReader(stream);	
					Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(inConfigFile);
					for (CSVRecord record : records) {
						String rDescription = "# Description: ";
						if (record.get(0).startsWith(rDescription)) description = record.get(0).replace(rDescription, "").trim() + " (" + description + ")";
						String rTitle = "# Title: ";
						if (record.get(0).startsWith(rTitle)) title = record.get(0).replace(rTitle,"").trim();
					}
					intOptions++;

				    try {
				    	inConfigFile.close();
				    	stream.close();
				    } catch (IOException ioex) {
				        //omitted.
				    }
					
				}

				printCheckbox(out, "setup-" + configOption[0], title, description);

			}
		}


		if (intOptions>0) {

			out.println("<div class=\"coral-Form-fieldwrapper coral-Form-fieldwrapper--alignRight\">");
			out.println("<button class=\"coral-Form-field coral-Button coral-Button--primary\">Submit</button>");
			out.println("</div>");

		} else {

			out.println("<p>No configuration file to process</p>");

		}

		String returnURL = (String) request.getParameter("returnURL");
		if (returnURL!=null) {
			out.println("<input type=\"hidden\" name=\"returnURL\" value=\"" + returnURL + "\">");
		}

		out.println("</section></form>");
		out.println("</body></html>");

	}

	private static void printCheckbox(PrintWriter out, String fieldName, String fieldLabel, String fieldTooltip) {

		out.println("<div class=\"coral-Form-fieldwrapper coral-Form-fieldwrapper--singleline\">");
		out.println("<label class=\"coral-Form-field coral-Checkbox\">");
		out.println("<input class=\"coral-Checkbox-input\" type=\"checkbox\" name=\"" + fieldName + "\" checked=\"\">");
		out.println("<span class=\"coral-Checkbox-checkmark\"></span>");
		out.println("<span class=\"coral-Checkbox-description\">" + fieldLabel + "</span>");
		out.println("</label>");
		out.println("<span id=\"coral-Form-Vertical-Checkbox-" + fieldName + "\" class=\"coral-Form-fieldinfo coral-Icon coral-Icon--infoCircle coral-Icon--sizeS\"></span>");
		out.println("<coral-tooltip variant=\"info\" placement=\"right\" target=\"#coral-Form-Vertical-Checkbox-" + fieldName + "\" class=\"coral3-Tooltip coral3-Tooltip--info\" aria-hidden=\"true\" tabindex=\"-1\" role=\"tooltip\" style=\"display: none;\"><coral-tooltip-content>" + fieldTooltip + "</coral-tooltip-content></coral-tooltip>");
		out.println("</div>");

	}

}