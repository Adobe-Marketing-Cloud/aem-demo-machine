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
import java.io.PrintWriter;
import java.rmi.ServerException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

@SlingServlet(paths="/bin/SetupCommunities", methods = "GET", metatype=false)
public class SetupCommunities extends org.apache.sling.api.servlets.SlingAllMethodsServlet {

	private static final long serialVersionUID = 2891656166255468489L;

	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<link rel=\"stylesheet\" href=\"/etc/clientlibs/granite/coralui3.css\" type=\"text/css\">");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/typekit.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/jquery.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/utils.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/moment.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"/etc/clientlibs/granite/coralui3.js\"></script>");
		out.println("</head><body class=\"coral--light u-coral-clearFix\" style=\"margin:40px\">");
		out.println("<div><h1>AEM Communities - Demo Setup</h1>");
		out.println("<form action=\"/bin/createCommunities\" method=\"GET\" class=\"coral-Form coral-Form--vertical\" style=\"width:700px\">");
		out.println("<section class=\"coral-Form-fieldset\">");
		out.println("<span>All the fun takes place on the Publish instance with AEM - please ensure yours is available at the following coordinates</spanl>");
		out.println("<label class=\"coral-Form-fieldlabel\">Path to configuration files</label>");
		out.println("<input is=\"coral-textfield\" name=\"contentPath\" type=\"text\" value=\"/etc/community/we-retail\" class=\"coral-Form-field coral-Textfield\">");
		out.println("<label class=\"coral-Form-fieldlabel\">Author instance</label>");
		out.println("<div class=\"coral-Form--aligned\">");
		out.println("<input is=\"coral-textfield\" name=\"hostname_author\" type=\"text\" value=\"localhost\" class=\"coral-Textfield\">");
		out.println("<input is=\"coral-textfield\" name=\"port_author\" type=\"text\" value=\"4502\" class=\"coral-Textfield\">");
	    out.println("</div>");
		out.println("<label class=\"coral-Form-fieldlabel\">Publish instance</label>");
		out.println("<div class=\"coral-Form--aligned\">");
		out.println("<input is=\"coral-textfield\" name=\"hostname\" type=\"text\" value=\"localhost\" class=\"coral-Textfield\">");
		out.println("<input is=\"coral-textfield\" name=\"port\" type=\"text\" value=\"4503\" class=\"coral-Textfield\">");
	    out.println("</div>");
		out.println("<label class=\"coral-Form-fieldlabel\">Admin password</label>");
		out.println("<input is=\"coral-textfield\" name=\"password\" type=\"text\" value=\"admin\" class=\"coral-Form-field coral-Textfield\">");
		out.println("<label class=\"coral-Form-fieldlabel\">Please select from the following options</label>");
		printCheckbox(out, "setupSite", "Install Community Site and Templates","This option first creates a Community Site template, then creates the We.Retail Community Site ouf the template, then publishes the We.Retail Community Site");
		printCheckbox(out, "setupContent", "Install Community Content","This option loads Community content on the publish instance for the primary Community functions such as Blogs, Forums, Calendars...");
		printCheckbox(out, "setupGroup", "Install Community Groups","This option creates Community Groups on a publish instance then populates them with content");
		printCheckbox(out, "setupEnablement", "Install Community Enablement","This option creates Community Enablement content (Videos and SCORM) when the Enablement features are available on the instance. The resources are first created on author, then assigned and published to demo users.");
		out.println("<div class=\"coral-Form-fieldwrapper coral-Form-fieldwrapper--alignRight\">");
		out.println("<button class=\"coral-Form-field coral-Button coral-Button--primary\">Submit</button>");
	    out.println("</div>");

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