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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

public class Loader {

	static Logger logger = Logger.getLogger(Loader.class);

	private static final String USERS = "Users";
	private static final String COMMENTS = "Comments";
	private static final String REVIEWS = "Reviews";
	private static final String RATINGS = "Ratings";
	private static final String FORUM = "Forum";
	private static final String JOURNAL = "Journal";
	private static final String TAG = "Tag";
	private static final String BLOG = "Blog";
	private static final String SUMMARY = "Summary";
	private static final String CALENDAR = "Calendar";
	private static final String PREFERENCES = "Preferences";
	private static final String FILES = "Files";
	private static final String IMAGE = "file";
	private static final String AVATAR = "Avatar";
	private static final String QNA = "QnA";
	private static final String ACTIVITIES = "Activities";
	private static final String SLINGPOST = "SlingPost";
	private static final String SLINGDELETE = "SlingDelete";
	private static final String FRAGMENT = "Fragment";
	private static final String FOLDER = "Folder";
	private static final String PASSWORD = "password";
	private static final String SITE = "Site";
	private static final String SITEUPDATE = "SiteUpdate";
	private static final String SITETEMPLATE = "SiteTemplate";
	private static final String GROUPMEMBERS = "GroupMembers";
	private static final String SITEMEMBERS = "SiteMembers";
	private static final String GROUP = "Group";
	private static final String JOIN = "Join";
	private static final String ASSET = "Asset";
	private static final String KILL = "Kill";
	private static final String MESSAGE = "Message";
	private static final String RESOURCE = "Resource";
	private static final String BADGE = "Badge";
	private static final int RESOURCE_INDEX_PATH = 5;
	private static final int RESOURCE_INDEX_THUMBNAIL = 3;
	private static final int CALENDAR_INDEX_THUMBNAIL = 8;
	private static final int ASSET_INDEX_NAME = 4;
	private static final int RESOURCE_INDEX_SITE = 7;
	private static final int RESOURCE_INDEX_FUNCTION = 9;
	private static final int RESOURCE_INDEX_PROPERTIES = 10;
	private static final int GROUP_INDEX_NAME = 1;
	private static final String FOLLOW = "Follow";
	private static final String NOTIFICATION = "Notification";
	private static final String LEARNING = "LearningPath";
	private static final String BANNER = "pagebanner";
	private static final String THUMBNAIL = "pagethumbnail";
	private static final String LANGUAGE = "baseLanguage";
	private static final String ROOT = "siteRoot";
	private static final String CSS = "pagecss";
	private static final int MAXRETRIES=30;
	private static final int REPORTINGDAYS=-21;
	private static final String ENABLEMENT61FP2 = "1.0.135";
	private static final String ENABLEMENT61FP3 = "1.0.148";

	private static String[] comments = {"This course deserves some improvements", "The conclusion is not super clear", "Very crisp, love it", "Interesting, but I need to look at this course again", "Good course, I'll recommend it.", "Really nice done. Sharing with my peers", "Excellent course. Giving it a top rating."};

	public static void main(String[] args) {

		String hostname = null;
		String port = null;
		String altport = null;
		String csvfile = null;
		String analytics = null;
		String adminPassword = "admin";
		boolean reset = false;
		boolean configure = false;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("h", true, "Hostname");
		options.addOption("p", true, "Port");
		options.addOption("a", true, "Alternate Port");
		options.addOption("f", true, "CSV file");
		options.addOption("r", false, "Reset");
		options.addOption("u", true, "Admin Password");
		options.addOption("c", false, "Configure");
		options.addOption("s", true, "Analytics Endpoint");
		options.addOption("t", false, "Analytics Tracking");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("h")) {
				hostname = cmd.getOptionValue("h");
			}

			if(cmd.hasOption("p")) {
				port = cmd.getOptionValue("p");
			}

			if(cmd.hasOption("a")) {
				altport = cmd.getOptionValue("a");
			}

			if(cmd.hasOption("f")) {
				csvfile = cmd.getOptionValue("f");
			}

			if(cmd.hasOption("u")) {
				adminPassword = cmd.getOptionValue("u");
			}

			if(cmd.hasOption("t")) {
				if(cmd.hasOption("s")) {
					analytics = cmd.getOptionValue("s");
				}
			}

			if(cmd.hasOption("r")) {
				reset = true;
			}

			if(cmd.hasOption("c")) {
				configure = true;
			}


			if (csvfile==null || port == null || hostname == null) {
				System.out.println("Request parameters: -h hostname -p port -a alternateport -u adminPassword -f path_to_CSV_file -r (true|false, delete content before import) -c (true|false, post additional properties)");
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		logger.debug("AEM Demo Loader: Processing file " + csvfile);

		try {

			// Reading and processing the CSV file
			Reader in = new FileReader(csvfile);
			processLoading(null, in, hostname, port, altport, adminPassword, analytics, reset, configure, csvfile);

		} catch (IOException e) {

			logger.error(e.getMessage());

		}

	}

	public static void processLoading(ResourceResolver rr, Reader in, String hostname, String port, String altport, String adminPassword, String analytics, boolean reset, boolean configure, String csvfile) {

		String language = "en";
		String location = null;
		String rootPath = "/content/sites";
		String[] url = new String[10];  // Handling 10 levels maximum for nested comments 
		int urlLevel = 0;
		int row = 0;
		HashMap<String,ArrayList<String>> learningpaths=new HashMap<String,ArrayList<String>>();

		try {

			String componentType = null;

			logger.debug("AEM Demo Loader: Loading bundles versions");
			String bundlesList = doGet(hostname, port,
					"/system/console/bundles.json",
					"admin",adminPassword,
					null);

			// Some steps are specific to the version number of the Enablement add-on
			Version vBundleCommunitiesEnablement = getVersion(bundlesList, "com.adobe.cq.social.cq-social-enablement-impl");
			Version vBundleCommunitiesCalendar = getVersion(bundlesList, "com.adobe.cq.social.cq-social-calendar");
			Version vBundleCommunitiesNotifications = getVersion(bundlesList, "com.adobe.cq.social.cq-social-notifications-impl");

			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			for (CSVRecord record : records) {

				row = row + 1;
				logger.info("Row: " + row + ", new record: " + record.get(0));			

				// Let's see if we deal with a comment
				if (record.get(0).startsWith("#")) {

					// We can ignore the comment line and move on
					continue;

				}

				// Let's see if we need to terminate this process
				if (record.get(0).equals(KILL)) {

					System.exit(1);

				}

				// Let's see if we need to create a new Community site
				if (record.get(0).equals(SITE)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:createSite", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					String urlName = null;

					for (int i=2;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							if (value.equals("TRUE")) { value = "true"; }
							if (value.equals("FALSE")) { value = "false"; }	
							if (name.equals("urlName")) { urlName = value; }
							if (name.equals(LANGUAGE)) { language = value; }
							if (name.equals(ROOT)) { rootPath = value; }
							if (name.equals(BANNER)) {
								addBinaryBody(builder, rr, BANNER, csvfile, value);
							} else if (name.equals(THUMBNAIL)) {
								addBinaryBody(builder, rr, THUMBNAIL, csvfile, value);
							} else if (name.equals(CSS)) {
								addBinaryBody(builder, rr, CSS, csvfile, value);
							} else {
								builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));
							}
						}
					}

					// Site creation
					String siteId = doPost(hostname, port,
							"/content.social.json",
							"admin", adminPassword,
							builder.build(),
							"response/siteId");

					// Site publishing, if there's a publish instance to publish to
					if (!port.equals(altport)) {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("id", "nobot"));
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:publishSite"));
						nameValuePairs.add(new BasicNameValuePair("path", rootPath + "/" + urlName + "/" + language));

						doPost(hostname, port,
								"/communities/sites.html",
								"admin", adminPassword,
								new UrlEncodedFormEntity(nameValuePairs),
								null);

						// Wait for site to be available on Publish
						doWait(hostname, altport,
								"admin", adminPassword,
								(siteId!=null?siteId:urlName) + "-groupadministrators");
					}

					continue;
				}

				// Let's see if we need to update an existing Community site
				if (record.get(0).equals(SITEUPDATE) && record.get(1)!=null && record.get(2)!=null) {

					logger.debug("Updating a Community Site " + record.get(1));

					// Let's fetch the theme for this Community Site Url
					String siteConfig = doGet(hostname, port,
							rootPath + "/" + record.get(1) + "/" + record.get(2) + "/configuration.social.json",
							"admin",adminPassword,
							null);

					logger.debug("Community Site configuration " + siteConfig);

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:updateSite", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					// Adding the mandatory values for being able to save a site via the JSON endpoint
					List<String> props = Arrays.asList("urlName", "theme", "moderators", "createGroupPermission", "groupAdmin", "twitterconnectoauthid", "fbconnectoauthid");
					try {
						JSONObject siteprops = new JSONObject(siteConfig).getJSONObject("properties");
						for (String prop : props) {
							if (siteprops.has(prop)) {
								Object propValue = siteprops.get(prop); 
								if (propValue instanceof JSONArray) {
									JSONArray propArray = (JSONArray) propValue;
									for (int i=0;i<propArray.length();i++) {
										logger.debug("Updating + " + prop + " with value " + propArray.get(i).toString() );
										builder.addTextBody(prop, propArray.get(i).toString(), ContentType.create("text/plain", MIME.UTF8_CHARSET));																	
									}
								} else {
									logger.debug("Updating + " + prop + " with value " + propValue.toString() );
									builder.addTextBody(prop, propValue.toString(), ContentType.create("text/plain", MIME.UTF8_CHARSET));								
								}
							}
						}

					} catch (Exception e) {
						logger.error(e.getMessage());
					}

					// Adding the override values from the CSV record
					for (int i=3;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							logger.debug("Updating + " + name + " with value " + value );
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

						}

					}

					doPost(hostname, port,
							rootPath + "/" + record.get(1) + "/" + record.get(2) + ".social.json",
							"admin", adminPassword,
							builder.build(),
							null);

					continue;
				}


				// Let's see if we need to create a new Tag
				if (record.get(0).equals(TAG)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					for (int i=1;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0 && record.get(i+1).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

						}
					}

					// Tag creation
					doPost(hostname, port,
							"/bin/tagcommand",
							"admin", adminPassword,
							builder.build(),
							null);

					continue;
				}

				// Let's see if we need to assign some badges
				if (record.get(0).equals(BADGE)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);

					for (int i=2;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0 && record.get(i+1).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

						}
					}

					// Badge assignment
					doPost(hostname, port,
							record.get(1),
							"admin", adminPassword,
							builder.build(),
							null);

					continue;
				}


				// Let's see if we need to create a new Community site template, and if we can do it (script run against author instance)
				if (record.get(0).equals(SITETEMPLATE)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:createSiteTemplate", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					for (int i=2;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

						}
					}

					// Site template creation
					doPost(hostname, port,
							"/content.social.json",
							"admin", adminPassword,
							builder.build(),
							null);

					continue;
				}

				// Let's see if we need to create a new Community group
				if (record.get(0).equals(GROUP)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:createCommunityGroup", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					for (int i=3;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							if (value.equals("TRUE")) { value = "true"; }
							if (value.equals("FALSE")) { value = "false"; }	
							if (name.equals(IMAGE)) {
								addBinaryBody(builder, rr, IMAGE, csvfile, value);
							} else {
								builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));
							}
						}
					}

					// Group creation
					String memberGroupId = doPost(hostname, port,
							record.get(1),
							getUserName(record.get(2)), getPassword(record.get(2), adminPassword),
							builder.build(),
							"response/memberGroupId");

					// Wait for group to be available on Publish, if available
					logger.debug("Waiting for completion of Community Group creation");
					doWait(hostname, port,
							"admin", adminPassword,
							memberGroupId);

					continue;

				}

				// Let's see if it's simple Sling Delete request
				if (record.get(0).equals(SLINGDELETE)) {

					doDelete(hostname, port,
							record.get(1),
							"admin", adminPassword);

					continue;

				}

				// Let's see if we need to add users to an AEM Group
				if ((record.get(0).equals(GROUPMEMBERS) || record.get(0).equals(SITEMEMBERS)) && record.get(GROUP_INDEX_NAME)!=null) {

					// Checking if we have a member group for this site
					String groupName = record.get(GROUP_INDEX_NAME);
					if (record.get(0).equals(SITEMEMBERS)) {

						// Let's fetch the siteId for this Community Site Url
						String siteConfig = doGet(hostname, port,
								groupName,
								"admin",adminPassword,
								null);

						try {

							String siteId = new JSONObject(siteConfig).getString("siteId");
							if (siteId!=null) groupName = "community-" + siteId + "-members";
							logger.debug("Member group name is " + groupName);

						} catch (Exception e) {

							logger.error(e.getMessage());

						}

					}

					// Pause until the group can found
					doWait(hostname, port,
							"admin", adminPassword,
							groupName
							);

					List<NameValuePair>  nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("filter", "[{\"operation\":\"like\",\"rep:principalName\":\"" + groupName + "\"}]"));
					nameValuePairs.add(new BasicNameValuePair("type", "groups"));
					String groupList = doGet(hostname, port,
							"/libs/social/console/content/content/userlist.social.0.10.json",
							"admin",adminPassword,
							nameValuePairs);

					logger.debug("List of groups" + groupList);

					if (groupList.indexOf(groupName)>0) {

						logger.debug("Group was found on " + port);
						try {
							JSONArray jsonArray = new JSONObject(groupList).getJSONArray("items");
							if (jsonArray.length()==1) {
								JSONObject jsonObject = jsonArray.getJSONObject(0);
								String groupPath= jsonObject.getString("path");

								logger.debug("Group path is " + groupPath);

								// Constructing a multi-part POST for group membership
								MultipartEntityBuilder builder = MultipartEntityBuilder.create();
								builder.setCharset(MIME.UTF8_CHARSET);
								builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

								List<NameValuePair> groupNameValuePairs = buildNVP(record, 2);
								for (NameValuePair nameValuePair : groupNameValuePairs) {
									builder.addTextBody(nameValuePair.getName(), nameValuePair.getValue(), ContentType.create("text/plain", MIME.UTF8_CHARSET));
								}

								// Adding the list of group members
								doPost(hostname, port,
										groupPath + ".rw.userprops.html",
										"admin", adminPassword,
										builder.build(),
										null);

							} else {
								logger.info("We have more than one match for a group with this name!");			
							}
						} catch (Exception e) {
							logger.error(e.getMessage());
						}
					}

					continue;

				}

				// Let's see if it's user related
				if (record.get(0).equals(USERS)) {

					//First we need to get the path to the user node
					String json = doGet(hostname, port,
							"/libs/granite/security/currentuser.json",
							getUserName(record.get(1)), getPassword(record.get(1), adminPassword),
							null);

					if (json!=null) {

						try {

							// Fetching the home property
							String home = new JSONObject(json).getString("home");
							if (record.get(2).equals(PREFERENCES)) {
								home = home + "/preferences";
							} else {
								home = home + "/profile";
							}
							logger.debug(home);

							// Now we can post all the preferences or the profile
							List<NameValuePair> nameValuePairs = buildNVP(record, 3);
							doPost(hostname, port,
									home,
									"admin", adminPassword,
									new UrlEncodedFormEntity(nameValuePairs),
									null);

						} catch (Exception e) {
							logger.error(e.getMessage());
						}

					}

					continue;

				}

				// Let's see if we deal with a new block of content or just a new entry
				if (record.get(0).equals(CALENDAR) 
						|| record.get(0).equals(SLINGPOST)
						|| record.get(0).equals(RATINGS) 
						|| record.get(0).equals(BLOG) 
						|| record.get(0).equals(JOURNAL) 
						|| record.get(0).equals(COMMENTS) 
						|| record.get(0).equals(REVIEWS) 
						|| record.get(0).equals(FILES) 
						|| record.get(0).equals(SUMMARY) 
						|| record.get(0).equals(ACTIVITIES) 
						|| record.get(0).equals(JOIN) 
						|| record.get(0).equals(FOLLOW) 
						|| record.get(0).equals(NOTIFICATION) 
						|| record.get(0).equals(MESSAGE) 
						|| record.get(0).equals(ASSET) 
						|| record.get(0).equals(AVATAR) 
						|| record.get(0).equals(RESOURCE) 
						|| record.get(0).equals(FOLDER) 
						|| record.get(0).equals(FRAGMENT) 
						|| record.get(0).equals(LEARNING) 
						|| record.get(0).equals(QNA) 
						|| record.get(0).equals(FORUM)) {

					// New block of content, we need to reset the processing to first Level
					componentType = record.get(0);
					url[0] = record.get(1);
					urlLevel=0;

					if (!componentType.equals(SLINGPOST) && reset) {

						int pos = record.get(1).indexOf("/jcr:content");
						if (pos>0) 
							doDelete(hostname, port,
									"/content/usergenerated" + record.get(1).substring(0,pos),
									"admin", adminPassword);

					}

					// If the Configure command line flag is set, we try to configure the component with all options enabled
					if (componentType.equals(SLINGPOST) || configure) {

						String configurePath = getConfigurePath(record.get(1));

						List<NameValuePair> nameValuePairs = buildNVP(record, 2);
						if (nameValuePairs.size()>2)    // Only do this when really have configuration settings
							doPost(hostname, port,
									configurePath,
									"admin", adminPassword,
									new UrlEncodedFormEntity(nameValuePairs),
									null);

					}

					// We're done with this line, moving on to the next line in the CSV file
					continue;
				}

				// Let's see if we need to indent the list, if it's a reply or a reply to a reply
				if (record.get(1).length()!=1) continue;  // We need a valid level indicator

				if (Integer.parseInt(record.get(1))>urlLevel) {
					url[++urlLevel] = location;
					logger.debug("Incremented urlLevel to: " + urlLevel + ", with a new location:" + location);
				} else if (Integer.parseInt(record.get(1))<urlLevel) {
					urlLevel = Integer.parseInt(record.get(1));
					logger.debug("Decremented urlLevel to: " + urlLevel);
				}

				// Get the credentials or fall back to password
				String password = getPassword(record.get(0), adminPassword);
				String userName = getUserName(record.get(0));

				// Adding the generic properties for all POST requests
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setCharset(MIME.UTF8_CHARSET);
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				if (!componentType.equals(RESOURCE))
					nameValuePairs.add(new BasicNameValuePair("id", "nobot"));

				nameValuePairs.add(new BasicNameValuePair("_charset_", "UTF-8"));

				// Setting some specific fields depending on the content type
				if (componentType.equals(COMMENTS)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
					nameValuePairs.add(new BasicNameValuePair("message", record.get(2)));

				}

				// Creates a forum post (or reply)
				if (componentType.equals(FORUM)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createForumPost"));
					nameValuePairs.add(new BasicNameValuePair("subject", record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         

				}

				// Follows a user (followedId) for the user posting the request
				if (componentType.equals(FOLLOW)) {

					if (vBundleCommunitiesNotifications!=null && vBundleCommunitiesNotifications.compareTo(new Version("1.0.12"))<0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:follow"));
						nameValuePairs.add(new BasicNameValuePair("userId", "/social/authors/" + userName));
						nameValuePairs.add(new BasicNameValuePair("followedId", "/social/authors/" + record.get(2)));

					} else {

						logger.debug("Ignoring FOLLOW with this version of AEM Communities");
						continue;

					}
				}

				// Notifications
				if (componentType.equals(NOTIFICATION)) {

					if (vBundleCommunitiesNotifications!=null && vBundleCommunitiesNotifications.compareTo(new Version("1.0.11"))>0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:updatesubscriptions"));
						nameValuePairs.add(new BasicNameValuePair("types", "following"));
						nameValuePairs.add(new BasicNameValuePair("types", "notification"));
						nameValuePairs.add(new BasicNameValuePair("states", record.get(2).toLowerCase()));
						nameValuePairs.add(new BasicNameValuePair("states", record.get(3).toLowerCase()));
						nameValuePairs.add(new BasicNameValuePair("subscribedId", record.get(5)));

					} else {

						logger.debug("Ignoring NOTIFICATION with this version of AEM Communities");
						continue;

					}
				}

				// Uploading Avatar picture
				if (componentType.equals(AVATAR)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:changeAvatar"));

				}

				// Joins a user (posting the request) to a Community Group (path)
				if (componentType.equals(JOIN)) {
					nameValuePairs.add(new BasicNameValuePair(":operation", "social:joinCommunityGroup"));
					int pos = url[0].indexOf("/configuration.social.json");
					if (pos>0)
						nameValuePairs.add(new BasicNameValuePair("path", url[0].substring(0,pos) + ".html"));
					else
						continue; // Invalid record
				}

				// Creates a new private message
				if (componentType.equals(MESSAGE)) {
					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createMessage"));
					nameValuePairs.add(new BasicNameValuePair("sendMail", "Sending..."));
					nameValuePairs.add(new BasicNameValuePair("content", record.get(4)));
					nameValuePairs.add(new BasicNameValuePair("subject", record.get(3)));
					nameValuePairs.add(new BasicNameValuePair("serviceSelector", "/bin/community"));
					nameValuePairs.add(new BasicNameValuePair("to", "/social/authors/" + record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("userId", "/social/authors/" + record.get(2)));
					nameValuePairs.add(new BasicNameValuePair(":redirect", "//messaging.html"));
					nameValuePairs.add(new BasicNameValuePair(":formid", "generic_form"));
					nameValuePairs.add(new BasicNameValuePair(":formstart", rootPath + "/communities/messaging/compose/jcr:content/content/primary/start"));
				}

				// Creates a file or a folder
				if (componentType.equals(FILES)) {

					// Top level is always assumed to be a folder, second level files, and third and subsequent levels comments on files
					if (urlLevel==0) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createFileLibraryFolder"));
						nameValuePairs.add(new BasicNameValuePair("name", record.get(2)));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         
					} else if (urlLevel==1) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
					}

				}

				// Creates a question, a reply or mark a reply as the best answer
				if (componentType.equals(QNA)) {
					if (urlLevel == 0) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createQnaPost"));
						nameValuePairs.add(new BasicNameValuePair("subject", record.get(2)));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));
					} else if (urlLevel == 1) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createQnaPost"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));
					} else if (urlLevel == 2) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:selectAnswer"));	            	   
					}
				}

				// Creates an article or a comment
				if (componentType.equals(JOURNAL) || componentType.equals(BLOG)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createJournalComment"));
					nameValuePairs.add(new BasicNameValuePair("subject", record.get(2)));
					StringBuffer message = new StringBuffer("<p>" + record.get(3) + "</p>");

					//We might have more paragraphs to add to the blog or journal article
					for (int i=6; i < record.size();i++) {
						if (record.get(i).length()>0) {
							message.append("<p>" + record.get(i) + "</p>");
						}
					}

					//We might have some tags to add to the blog or journal article
					if (record.get(5).length()>0) {
						nameValuePairs.add(new BasicNameValuePair("tags", record.get(5)));		         				
					}

					nameValuePairs.add(new BasicNameValuePair("message", message.toString()));		         

				}

				// Creates a review or a comment
				if (componentType.equals(REVIEWS)) {

					nameValuePairs.add(new BasicNameValuePair("message", record.get(2)));

					// This might be a top level review, or a comment on a review or another comment
					if (urlLevel==0) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createReview"));
						nameValuePairs.add(new BasicNameValuePair("ratings", record.get(3)));
						if (record.size()>4 &&
								record.get(4).length()>0) {
							// If we are dealing with a non-existent resource, then the design drives the behavior
							nameValuePairs.add(new BasicNameValuePair("scf:resourceType", "social/reviews/components/hbs/reviews"));
							nameValuePairs.add(new BasicNameValuePair("scf:included",record.get(4)));							
						}
					} else {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
					}

				}

				// Creates a rating
				if (componentType.equals(RATINGS)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
					nameValuePairs.add(new BasicNameValuePair("tallyType", "Rating"));
					nameValuePairs.add(new BasicNameValuePair("response", record.get(2)));

				}

				// Creates a DAM asset
				if (componentType.equals(ASSET) && record.get(ASSET_INDEX_NAME).length()>0) {

					nameValuePairs.add(new BasicNameValuePair("fileName", record.get(ASSET_INDEX_NAME)));

				}

				// Creates a simple Folder
				if (componentType.equals(FOLDER)) {

					nameValuePairs.add(new BasicNameValuePair("./jcr:content/jcr:title", record.get(2)));
					nameValuePairs.add(new BasicNameValuePair(":name", record.get(3)));
					nameValuePairs.add(new BasicNameValuePair("./jcr:primaryType", "sling:Folder"));
					nameValuePairs.add(new BasicNameValuePair("./jcr:content/jcr:primaryType", "nt:unstructured"));

				}				

				// Creates a simple Text Fragment
				if (componentType.equals(FRAGMENT)) {

					nameValuePairs.add(new BasicNameValuePair("template", "/libs/settings/dam/cfm/templates/simple/jcr:content"));
					nameValuePairs.add(new BasicNameValuePair("name", record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("parentPath", record.get(3)));
					nameValuePairs.add(new BasicNameValuePair("./jcr:title", record.get(4)));
					nameValuePairs.add(new BasicNameValuePair("description", record.get(5)));
					nameValuePairs.add(new BasicNameValuePair("author", record.get(0)));

					//We might have some tags to add to the content fragment
					if (record.get(5).length()>0) {
						nameValuePairs.add(new BasicNameValuePair("tags", record.get(6)));		         				
						nameValuePairs.add(new BasicNameValuePair("tags@TypeHint", "String[]"));		         				
						nameValuePairs.add(new BasicNameValuePair("tags@Delete", ""));		         				
					}

				}				

				// Creates an Enablement resource
				if (componentType.equals(RESOURCE)) {

					// Making sure it's referencing some existing file
					File attachment = new File(csvfile.substring(0, csvfile.indexOf(".csv")) + File.separator + record.get(2));
					if (attachment.exists()) {

						nameValuePairs.add(new BasicNameValuePair(":operation", vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))>0?"social:createResource":"se:createResource"));

						List<NameValuePair> otherNameValuePairs = buildNVP(record, RESOURCE_INDEX_PROPERTIES);
						nameValuePairs.addAll(otherNameValuePairs);

						// Special processing of lists with multiple users, need to split a String into multiple entries
						if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))>0) {

							nameValuePairs = convertArrays(nameValuePairs,"add-learners");
							nameValuePairs = convertArrays(nameValuePairs,"resource-author");
							nameValuePairs = convertArrays(nameValuePairs,"resource-contact");
							nameValuePairs = convertArrays(nameValuePairs,"resource-expert");

							nameValuePairs.add(new BasicNameValuePair("enablement-type", "social/enablement/components/hbs/resource"));

						}

						// Adding the site
						nameValuePairs.add(new BasicNameValuePair("site", rootPath + "/" + record.get(RESOURCE_INDEX_SITE) + "/resources/en"));

						// Building the cover image fragment
						if (record.get(RESOURCE_INDEX_THUMBNAIL).length()>0) {
							nameValuePairs.add(new BasicNameValuePair("cover-image", doThumbnail(rr, hostname, port, adminPassword, csvfile, record.get(RESOURCE_INDEX_THUMBNAIL))));
						} else {
							nameValuePairs.add(new BasicNameValuePair("cover-image", ""));			
						}

						// Building the asset fragment
						String coverPath = "/content/dam/" + record.get(RESOURCE_INDEX_SITE) + "/resource-assets/" + record.get(2) + "/jcr:content/renditions/cq5dam.thumbnail.319.319.png";
						String coverSource = "dam";
						String assets = "[{\"cover-img-path\":\"" + coverPath + "\",\"thumbnail-source\":\"" + coverSource + "\",\"asset-category\":\"enablementAsset:dam\",\"resource-asset-name\":null,\"state\":\"A\",\"asset-path\":\"/content/dam/" + record.get(RESOURCE_INDEX_SITE) + "/resource-assets/" + record.get(2) + "\"}]";
						nameValuePairs.add(new BasicNameValuePair("assets", assets));

						logger.debug("assets:" + assets);

					} else {
						logger.error("Resource cannot be created at the referenced file is missing");
						continue;
					}

				}

				// Creates a learning path
				if (componentType.equals(LEARNING)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0?"social:editLearningPath":"se:editLearningPath"));

					List<NameValuePair> otherNameValuePairs = buildNVP(record, RESOURCE_INDEX_PROPERTIES);
					nameValuePairs.addAll(otherNameValuePairs);

					// Special processing of lists with multiple users, need to split a String into multiple entries
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0) {

						nameValuePairs = convertArrays(nameValuePairs,"add-learners");
						nameValuePairs = convertArrays(nameValuePairs,"resource-author");
						nameValuePairs = convertArrays(nameValuePairs,"resource-contact");
						nameValuePairs = convertArrays(nameValuePairs,"resource-expert");

						nameValuePairs.add(new BasicNameValuePair("enablement-type", "social/enablement/components/hbs/learningpath"));

					}

					// Adding the site
					nameValuePairs.add(new BasicNameValuePair("site", rootPath + "/" + record.get(RESOURCE_INDEX_SITE) + "/resources/en"));

					// Building the cover image fragment
					if (record.get(RESOURCE_INDEX_THUMBNAIL).length()>0) {
						nameValuePairs.add(new BasicNameValuePair(vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0?"cover-image":"card-image", doThumbnail(rr, hostname, port, adminPassword, csvfile, record.get(RESOURCE_INDEX_THUMBNAIL))));
					}

					// Building the learning path fragment
					StringBuffer assets = new StringBuffer("[\"");
					if (learningpaths.get(record.get(2)) != null) {

						ArrayList<String> paths = learningpaths.get(record.get(2));
						int i=0;
						for (String path : paths) {
							assets.append("{\\\"type\\\":\\\"linked-resource\\\",\\\"path\\\":\\\"");
							assets.append(path);
							assets.append("\\\"}");
							if (i++<paths.size()-1) { assets.append("\",\""); }
						}						

					} else {						
						logger.debug("No asset for this learning path");
					}
					assets.append("\"]");
					nameValuePairs.add(new BasicNameValuePair("learningpath-items", assets.toString()));
					logger.debug("Learning path:" + assets.toString());

				}

				// Creates a calendar event
				if (componentType.equals(CALENDAR)) {

					String startDate = computeDate(record.get(5), record.get(7));
					String endDate = computeDate(record.get(6), record.get(7));

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createEvent"));
					if (vBundleCommunitiesCalendar!=null && vBundleCommunitiesCalendar.compareTo(new Version("1.2.29"))>0) {

						// Post AEM Communities 6.1 FP3
						nameValuePairs.add(new BasicNameValuePair("subject", record.get(2)));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         
						nameValuePairs.add(new BasicNameValuePair("location", record.get(4)));		         
						nameValuePairs.add(new BasicNameValuePair("tags", ""));		         
						nameValuePairs.add(new BasicNameValuePair("address", ""));		         
						nameValuePairs.add(new BasicNameValuePair("isDate", "false"));		         
						nameValuePairs.add(new BasicNameValuePair("start", startDate));		         
						nameValuePairs.add(new BasicNameValuePair("end", endDate));	

						// Let's see if we have a cover image
						if (record.size()>CALENDAR_INDEX_THUMBNAIL && record.get(CALENDAR_INDEX_THUMBNAIL).length()>0) {
							addBinaryBody(builder, rr, "coverimage", csvfile, record.get(CALENDAR_INDEX_THUMBNAIL));
						}

					} else {

						// Pre AEM Communities 6.1 FP3
						try {

							JSONObject event = new JSONObject();

							// Building the JSON fragment for a new calendar event
							event.accumulate("subject", record.get(2));
							event.accumulate("message", record.get(3));
							event.accumulate("location", record.get(4));
							event.accumulate("tags", "");
							event.accumulate("undefined", "update");
							event.accumulate("start", startDate);
							event.accumulate("end",endDate);

							nameValuePairs.add(new BasicNameValuePair("event",event.toString()));

						} catch(Exception ex) {

							logger.error(ex.getMessage());

						}

					}

				}

				for (NameValuePair nameValuePair : nameValuePairs) {
					builder.addTextBody(nameValuePair.getName(), nameValuePair.getValue(), ContentType.create("text/plain", MIME.UTF8_CHARSET));
				}

				// See if we have attachments for this new post - or some other actions require a form nonetheless
				if ((componentType.equals(ASSET) || 
						componentType.equals(AVATAR) ||
						componentType.equals(FORUM) ||
						(componentType.equals(JOURNAL)) || componentType.equals(BLOG)) && record.size()>4 && record.get(ASSET_INDEX_NAME).length()>0) {

					addBinaryBody(builder, rr, "file", csvfile, record.get(ASSET_INDEX_NAME));
				}

				// If it's a resource or a learning path, we need the path to the resource for subsequent publishing
				String jsonElement = "location";
				if (componentType.equals(RESOURCE) && vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))<=0) {
					jsonElement = "changes/argument";
				}
				if (componentType.equals(LEARNING) && vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))<=0) {
					jsonElement = "path";
				}
				if (componentType.equals(ASSET)) {
					jsonElement = null;
				}

				// This call generally returns the path to the content fragment that was just created
				location = Loader.doPost(hostname, port,
						url[urlLevel],
						userName, password,
						builder.build(),
						jsonElement);

				// If we are loading a DAM asset, we are waiting for all renditions to be generated before proceeding
				if (componentType.equals(ASSET)) {
					int pathIndex = url[urlLevel].lastIndexOf(".createasset.html");
					if (pathIndex>0)
						doWaitPath(hostname, port, adminPassword, url[urlLevel].substring(0, pathIndex) + "/" + record.get(ASSET_INDEX_NAME) + "/jcr:content/renditions", "nt:file");
				}

				// If we are loading a content fragment, we need to post the actual content next
				if (componentType.equals(FRAGMENT)) {

					// Publishing the learning path 
					List<NameValuePair> fragmentNameValuePairs = new ArrayList<NameValuePair>();
					fragmentNameValuePairs.add(new BasicNameValuePair("contentType","text/html"));

					StringBuffer message = new StringBuffer("<p>" + record.get(7) + "</p>");

					//We might have more paragraphs to add to the fragment
					if (record.size()>8) {
						for (int i=8; i < record.size();i++) {
							if (record.get(i).length()>0) {
								message.append("<p>" + record.get(i) + "</p>");
							}
						}
					}

					fragmentNameValuePairs.add(new BasicNameValuePair("content", message.toString()));		         

					Loader.doPost(hostname, port,
							record.get(3) + "/" + record.get(2) + ".cfm.content.json",
							userName, password,
							new UrlEncodedFormEntity(fragmentNameValuePairs),
							null);

				}

				// Let's see if it needs to be added to a learning path
				if (componentType.equals(RESOURCE) && record.get(RESOURCE_INDEX_PATH).length()>0 && location!=null) {

					// Adding the location to a list of a resources for this particular Learning Path
					if (learningpaths.get(record.get(RESOURCE_INDEX_PATH)) == null) learningpaths.put(record.get(RESOURCE_INDEX_PATH), new ArrayList<String>());
					logger.debug("Adding resource to Learning path: " + record.get(RESOURCE_INDEX_PATH));
					ArrayList<String> locations = learningpaths.get(record.get(RESOURCE_INDEX_PATH));
					locations.add(location);
					learningpaths.put(record.get(RESOURCE_INDEX_PATH), locations);

				}

				// If it's a Learning Path, we publish it when possible
				if (componentType.equals(LEARNING) && !port.equals(altport) && location!=null) {

					// Publishing the learning path 
					List<NameValuePair> publishNameValuePairs = new ArrayList<NameValuePair>();
					publishNameValuePairs.add(new BasicNameValuePair(":operation","se:publishEnablementContent"));
					publishNameValuePairs.add(new BasicNameValuePair("replication-action","activate"));
					logger.debug("Publishing a learning path from: " + location);					
					Loader.doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishNameValuePairs),
							null);

					// Waiting for the learning path to be published
					Loader.doWait(hostname, altport,
							"admin", adminPassword,
							location.substring(1 + location.lastIndexOf("/"))     // Only search for groups with the learning path in it
							);

					// Decorate the resources within the learning path with comments and ratings, randomly generated
					ArrayList<String> paths = learningpaths.get(record.get(2));
					for (String path : paths) {
						doDecorate(hostname, altport, path, record, analytics, rootPath);
					}						

				}

				// If it's an Enablement Resource, a lot of things need to happen...
				// Step 1. If it's a SCORM resource, we wait for the SCORM metadata workflow to be complete before proceeding
				// Step 2. We publish the resource
				// Step 3. We set a new first published date on the resource (3 weeks earlier) so that reporting data is more meaningful
				// Step 4. We wait for the resource to be available on publish (checking that associated groups are available)
				// Step 5. We retrieve the json for the resource on publish to retrieve the Social endpoints
				// Step 6. We post ratings and comments for each of the enrollees on publish
				if (componentType.equals(RESOURCE) && !port.equals(altport) && location!=null) {

					// Wait for the data to be fully copied
					doWaitPath(hostname, port, adminPassword, location + "/assets/asset", "nt:file");

					// If we are dealing with a SCORM asset, we wait a little bit before publishing the resource to that the SCORM workflow is completed 
					if (record.get(2).indexOf(".zip")>0) {
						doSleep(10000, "SCORM Resource, waiting for workflow to complete");
					}

					// Publishing the resource 
					List<NameValuePair> publishNameValuePairs = new ArrayList<NameValuePair>();
					publishNameValuePairs.add(new BasicNameValuePair(":operation","se:publishEnablementContent"));
					publishNameValuePairs.add(new BasicNameValuePair("replication-action","activate"));
					logger.debug("Publishing a resource from: " + location);					
					Loader.doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishNameValuePairs),
							null);

					// Waiting for the resource to be published
					Loader.doWait(hostname, altport,
							"admin", adminPassword,
							location.substring(1 + location.lastIndexOf("/"))     // Only search for groups with the resource path in it
							);

					// Setting the first published timestamp so that reporting always comes with 3 weeks of data after building a new demo instance
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, REPORTINGDAYS);    
					List<NameValuePair> publishDateNameValuePairs = new ArrayList<NameValuePair>();
					publishDateNameValuePairs.add(new BasicNameValuePair("date-first-published", dateFormat.format(cal.getTime())));
					logger.debug("Setting the publish date for a resource from: " + location);
					doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishDateNameValuePairs),
							null);

					// Adding comments and ratings for this resource
					doDecorate(hostname, altport, location, record, analytics, rootPath);

				}				

			}

		} catch (IOException e) {

			logger.error(e.getMessage());

		}		

	}

	// This method adds a binary file to the future POST
	private static void addBinaryBody(MultipartEntityBuilder builder, ResourceResolver rr, String field, String csvfile, String value) {
		if (rr==null) {
			File attachment = new File(csvfile.substring(0, csvfile.indexOf(".csv")) + File.separator + value);
			// Check for file existence
			if (attachment.exists()) {
				logger.debug("Adding file named " + value + " to POST");
				builder.addBinaryBody(field, attachment, getContentType(value), attachment.getName());
			} else {
				logger.error("A non existent file named " + value + "was referenced");
			}
		} else {
			Resource res = rr.getResource(csvfile + "/" + value + "/jcr:content");
			if (res!=null) {
				logger.debug("Adding resource named " + value + " to POST");
				InputStream stream = res.adaptTo(InputStream.class);
				builder.addBinaryBody(field, stream, getContentType(value), value);
			} else {
				logger.error("A non existent resource named " + value + "was referenced");
			}
		}
	}

	// This method extracts the user name for a record
	private static String getUserName(String record) {

		String userName = record;
		int pass = userName.indexOf("/");
		if (pass>0) {
			userName = userName.substring(0,pass);
		}
		return userName;

	}

	// This method extracts the password for a record
	private static String getPassword(String record, String adminPassword) {

		String defaultPassword = PASSWORD;

		// If this is the AEM admin user, always return the configured admin password
		if (getUserName(record).equals("admin")) {
			return adminPassword;
		}

		// If not and if a password is provided in the CSV record, return this password
		int pass = record.indexOf("/");
		if (pass>0) {
			return record.substring(pass+1);
		}	

		// If not, return the defaut password 
		return defaultPassword;

	}

	// This method gets the configuration path for a record
	private static String getConfigurePath(String record) {

		String configurePath = record;
		int json = configurePath.indexOf(".social.json");
		if (json>0) {
			configurePath = configurePath.substring(0,json);
		}
		return configurePath;

	}

	// This method waits a little bit
	private static void doSleep(long ms, String message) {

		// Wait 2 seconds
		try {
			logger.debug("Waiting " + ms + " milliseconds: " + message);
			Thread.sleep(ms);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

	}

	// This method POSTs a file to be used as a thumbnail later on
	private static String doThumbnail(ResourceResolver rr, String hostname, String port, String adminPassword, String csvfile, String filename) {

		String pathToFile = "/content/dam/communities/resource-thumbnails/" + filename;

		if (rr==null) {

			File attachment = new File(csvfile.substring(0, csvfile.indexOf(".csv")) + File.separator + filename);

			ContentType ct = ContentType.MULTIPART_FORM_DATA;
			if (filename.indexOf(".mp4")>0) {
				ct = ContentType.create("video/mp4", MIME.UTF8_CHARSET);
			} else if (filename.indexOf(".jpg")>0 || filename.indexOf(".jpeg")>0) {
				ct = ContentType.create("image/jpeg", MIME.UTF8_CHARSET);
			} else if (filename.indexOf(".png")>0) {
				ct = ContentType.create("image/png", MIME.UTF8_CHARSET);
			}

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setCharset(MIME.UTF8_CHARSET);
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);				
			builder.addBinaryBody("file", attachment, ct, attachment.getName());
			builder.addTextBody("fileName", filename, ContentType.create("text/plain", MIME.UTF8_CHARSET));

			logger.debug("Adding file for thumbnails with name: " + attachment.getName() + " and type: " + ct.getMimeType());

			Loader.doPost(hostname, port,
					pathToFile,
					"admin", adminPassword,
					builder.build(),
					null);

			logger.debug("Path to thumbnail: " + pathToFile);

		}

		return pathToFile + "/file";

	}

	// This method computes a date with a relative number of padding days
	private static String computeDate(String date, String padding) {

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, Integer.parseInt(padding));

		date = date.replaceAll("YYYY", Integer.toString(calendar.get(Calendar.YEAR)));
		date = date.replaceAll("MM", Integer.toString(1 + calendar.get(Calendar.MONTH)));

		if (date.indexOf("DD")>0 && padding != null ){

			date = date.replaceAll("DD", Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));			

		}

		logger.debug("Date and time is " + date);
		return date;

	}

	// This method returns the right HTTP content type for a file on the file system
	private static ContentType getContentType(String fileName) {
		ContentType ct = ContentType.MULTIPART_FORM_DATA;
		if (fileName.indexOf(".mp4")>0) {
			ct = ContentType.create("video/mp4", MIME.UTF8_CHARSET);
		} else if (fileName.indexOf(".jpg")>0 || fileName.indexOf(".jpeg")>0) {
			ct = ContentType.create("image/jpeg", MIME.UTF8_CHARSET);
		} else if (fileName.indexOf(".png")>0) {
			ct = ContentType.create("image/png", MIME.UTF8_CHARSET);
		} else if (fileName.indexOf(".pdf")>0) {
			ct = ContentType.create("application/pdf", MIME.UTF8_CHARSET);
		} else if (fileName.indexOf(".css")>0) {
			ct = ContentType.create("text/css", MIME.UTF8_CHARSET);
		} else if (fileName.indexOf(".zip")>0) {
			ct = ContentType.create("application/zip", MIME.UTF8_CHARSET);
		}
		return ct;
	}

	// This method POSTs a set of comments and ratings for a resource for a particular location
	private static void doDecorate(String hostname, String altport, String location, CSVRecord record, String analytics, String rootPath) {

		// Getting the JSON view of the resource
		String resourceJson = Loader.doGet(hostname, altport,
				location + ".social.json",
				"admin","admin",
				null);
		logger.debug("JSON view of the resource is: " + resourceJson);					

		// Generating random ratings and comments for the resource for each of the enrolled users
		try {

			JSONObject resourceJsonObject = new JSONObject(resourceJson);

			String resourceRatingsEndpoint = resourceJsonObject.getString("ratingsEndPoint") + ".social.json";
			String resourceCommentsEndpoint = resourceJsonObject.getString("commentsEndPoint")  + ".social.json";
			String resourceID = resourceJsonObject.getString("id");
			String resourceType = resourceJsonObject.getJSONObject("assetProperties").getString("type");
			String referer = "http://localhost:" + altport + rootPath + "/" + record.get(RESOURCE_INDEX_SITE) + "/en" + (record.get(RESOURCE_INDEX_FUNCTION).length()>0?("/" + record.get(RESOURCE_INDEX_FUNCTION)):"") + ".resource.html" + resourceID; 

			logger.debug("Resource Ratings Endpoint: " + resourceRatingsEndpoint);
			logger.debug("Resource Comments Endpoint: " + resourceCommentsEndpoint);
			logger.debug("Resource Type: " + resourceType);
			logger.debug("Resource ID: " + resourceID);
			logger.debug("Referer: " + referer);

			// Looking for the list of enrolled users
			for (int i=0;i<record.size()-1;i=i+1) {

				if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).equals("deltaList")) {

					JSONObject enrolledJsonObject = new JSONObject(record.get(i+1));
					Iterator<?> iter = enrolledJsonObject.keys();
					while (iter.hasNext()) {

						String key = (String) iter.next();
						logger.debug("New Resource Enrollee: " + key);

						// Getting information about this enrollee (user or group?)
						List<NameValuePair>  nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("filter", "[{\"operation\":\"like\",\"rep:principalName\":\"" + key + "\"}]"));
						String list = Loader.doGet(hostname, altport,
								"/libs/social/console/content/content/userlist.social.0.10.json",
								"admin","admin",
								nameValuePairs);

						logger.debug(list);

						JSONArray jsonArray = new JSONObject(list).getJSONArray("items");
						if (jsonArray.length()==1) {

							JSONObject jsonObject = jsonArray.getJSONObject(0);
							String jsonElement = jsonObject.getString("type");

							if (jsonElement!=null && jsonElement.equals("user")) {

								// Always generating a page view event
								if (Math.random() < 0.90) doAnalytics(analytics, "event11", referer, resourceID, resourceType);

								// Sometimes generating a video view event
								if (Math.random() < 0.75 && resourceType.equals("video/mp4")) doAnalytics(analytics, "event2", referer, resourceID, resourceType);

								// Posting ratings and comments
								if (Math.random() < 0.50) doRatings(hostname, altport, key, resourceRatingsEndpoint, referer, resourceID, resourceType, analytics);
								if (Math.random() < 0.35) doComments(hostname, altport, key, resourceCommentsEndpoint, referer, resourceID, resourceType, analytics);

							} else {

								logger.debug("Enrollee is a group :" + key);
								List<NameValuePair>  groupNameValuePairs = new ArrayList<NameValuePair>();
								groupNameValuePairs.add(new BasicNameValuePair("groupId", key));
								groupNameValuePairs.add(new BasicNameValuePair("includeSubGroups", "true"));
								String memberList = Loader.doGet(hostname, altport,
										"/content/community-components/en/communitygroupmemberlist/jcr:content/content/communitygroupmember.social.0.100.json",
										"admin","admin",
										groupNameValuePairs);

								JSONArray memberJsonArray = new JSONObject(memberList).getJSONArray("items");
								for (int j=0; j<memberJsonArray.length();j++) {
									JSONObject memberJsonObject = memberJsonArray.getJSONObject(j);
									String email = memberJsonObject.getString("authorizableId");
									logger.debug("New group member for decoration: " + email);
									if (email!=null) {

										// Always generating a page view event
										if (Math.random() < 0.90) doAnalytics(analytics, "event11", referer, resourceID, "video/mp4");

										// Sometimes generating a video view event
										if (Math.random() < 0.75 && resourceType.equals("video/mp4")) doAnalytics(analytics, "event2", referer, resourceID, resourceType);

										if (Math.random() < 0.50) doRatings(hostname, altport, email, resourceRatingsEndpoint, referer, resourceID, resourceType, analytics);
										if (Math.random() < 0.35) doComments(hostname, altport, email, resourceCommentsEndpoint, referer, resourceID, resourceType, analytics);
									}

								} // For each group member

							} // If user or group

						} // If there's a principal name

					} // For each enrollee

					break; // only one possible deltaList attribute for resource and learning paths

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This methods POSTS an analytics event
	private static void doAnalytics(String analytics, String event, String pageURL, String resourcePath, String resourceType) {

		if (analytics!=null && pageURL!=null && resourcePath!=null && resourceType!=null && event!=null) {

			URLConnection urlConn = null;
			DataOutputStream printout = null;
			BufferedReader input = null;
			String tmp = null;
			try {

				URL pageurl = new URL( pageURL);
				StringBuffer sb = new StringBuffer("<?xml version=1.0 encoding=UTF-8?><request><sc_xml_ver>1.0</sc_xml_ver>");
				sb.append("<events>" + event + "</events>");
				sb.append("<pageURL>" + pageURL + "</pageURL>");
				sb.append("<pageName>" + pageurl.getPath().substring(1,pageurl.getPath().indexOf(".")).replaceAll("/",":") + "</pageName>");
				sb.append("<evar10>" + resourcePath + "</evar10>");
				sb.append("<evar2>" + resourceType + "</evar2>");
				sb.append("<visitorID>demomachine</visitorID>");
				sb.append("<reportSuiteID>" + analytics.substring(0,analytics.indexOf(".")) + "</reportSuiteID>");
				sb.append("</request>");

				logger.debug("New Analytics Event: " + sb.toString());

				URL sitecaturl = new URL( "http://" + analytics );

				urlConn = sitecaturl.openConnection();
				urlConn.setDoInput( true );
				urlConn.setDoOutput( true );
				urlConn.setUseCaches( false );
				urlConn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );

				printout = new DataOutputStream(urlConn.getOutputStream());

				printout.writeBytes( sb.toString() );
				printout.flush();
				printout.close();

				input = new BufferedReader( new InputStreamReader( urlConn.getInputStream( ) ) );

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

	}

	// This methods POSTS a rating and comments
	private static void doRatings(String hostname, String altport, String key, String resourceRatingsEndpoint, String referer, String resourceID, String resourceType, String analytics) {

		try {

			// Posting a Rating for this resource
			List<NameValuePair> ratingNameValuePairs = new ArrayList<NameValuePair>();
			ratingNameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
			ratingNameValuePairs.add(new BasicNameValuePair("tallyType", "Rating"));
			int randomRating = (int) Math.ceil(Math.random()*5);
			logger.debug("Randomly Generated Rating: " + randomRating);
			logger.debug("Referer for Rating: " + referer);
			ratingNameValuePairs.add(new BasicNameValuePair("response", String.valueOf(randomRating)));
			doPost(hostname, altport,
					resourceRatingsEndpoint + ".social.json",
					key, "password",
					new UrlEncodedFormEntity(ratingNameValuePairs),
					null,
					referer);

			doAnalytics(analytics, "event4", referer, resourceID, resourceType);

		} catch(Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This methods POSTS a rating and comments
	private static void doComments(String hostname, String altport, String key, String resourceCommentsEndpoint, String referer, String resourceID, String resourceType, String analytics) {

		try {

			// Posting a Comment for this resource
			int randomComment = (int) Math.ceil(Math.random()*5);
			List<NameValuePair> commentNameValuePairs = new ArrayList<NameValuePair>();
			commentNameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
			commentNameValuePairs.add(new BasicNameValuePair("message", comments[randomComment-1]));
			commentNameValuePairs.add(new BasicNameValuePair("id", "nobot"));
			logger.debug("Referer for Commenting: " + referer);
			doPost(hostname, altport,
					resourceCommentsEndpoint,
					key, "password",
					new UrlEncodedFormEntity(commentNameValuePairs),
					null,
					referer);

			doAnalytics(analytics, "event13", referer, resourceID, resourceType);

		} catch(Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This method POSTs a request to the server, returning the location JSON attribute, when available
	private static String doPost(String hostname, String port, String url, String user, String password, HttpEntity entity, String lookup) {

		return doPost(hostname, port, url, user, password, entity, lookup, null);

	}

	private static String doPost(String hostname, String port, String url, String user, String password, HttpEntity entity, String lookup, String referer) {

		String jsonElement = null;

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				String postUrl = "http://" + hostname + ":" + port + url;
				logger.debug("Posting request as " + user + " with password " + password  + " to " + postUrl);

				// Preparing a standard POST HTTP command
				HttpPost request = new HttpPost(postUrl);
				request.setEntity(entity);
				if (!entity.getContentType().toString().contains("multipart")) {
					request.addHeader("content-type", "application/x-www-form-urlencoded");
				}
				request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
				request.addHeader("Origin", postUrl);
				if (referer!=null) {
					logger.debug("Referer header added to request: " + referer);
					request.addHeader("Referer", referer);
				}

				// Sending the HTTP POST command
				CloseableHttpResponse response = httpClient.execute(target, request, localContext);
				try {            
					String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");   
					logger.debug("Got POST response:" + responseString); 
					if (lookup!=null) {
						logger.debug("JSON lookup value: " + lookup); 
						int separatorIndex = lookup.indexOf("/");
						if (separatorIndex>0) {

							// Grabbing element in a nested element
							Object object = new JSONObject(responseString).get(lookup.substring(0,separatorIndex));
							if (object!=null) {

								if (object instanceof JSONArray) {

									logger.debug("JSON object is a JSONArray");
									JSONArray jsonArray = (JSONArray) object;
									if (jsonArray.length()==1) {
										JSONObject jsonObject = jsonArray.getJSONObject(0);
										jsonElement = jsonObject.getString(lookup.substring(1 + separatorIndex));
										logger.debug("JSON value (jsonArray) returned is " + jsonElement);
									}

								} else if (object instanceof JSONObject) {

									logger.debug("JSON object is a JSONObject");
									JSONObject jsonobject = (JSONObject) object; 
									jsonElement = jsonobject.getString(lookup.substring(1 + separatorIndex));	
									logger.debug("JSON value (jsonObject) returned is " + jsonElement);

								}
							}

						} else {
							// Grabbing element at the top of the JSON response
							jsonElement = new JSONObject(responseString).getString(lookup);
							logger.debug("JSON (top) value returned is " + jsonElement);

						}
					}


				} catch (Exception ex) {
					logger.error(ex.getMessage());
				} finally {
					response.close();
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage());				
			} finally {
				httpClient.close();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());				
		}

		return jsonElement;

	}

	// This method DELETES a request to the server
	private static void doDelete(String hostname, String port, String url, String user, String password) {

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				String postUrl = "http://" + hostname + ":" + port + url;
				logger.debug("Deleting request as " + user + " with password " + password  + " to " + postUrl);
				HttpDelete request = new HttpDelete(postUrl);
				httpClient.execute(target, request, localContext);

			} catch (Exception ex) {
				logger.error(ex.getMessage());				
			} finally {
				httpClient.close();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());				
		}

	}

	// This method WAITs for a group to be present on a server
	private static void doWait(String hostname, String port, String user, String password, String group) {

		if (group==null || group.length()==0) {
			logger.error("Group name was not provided - not waiting for group to be available");
			return;
		}

		if (hostname!=null && port!=null && password!=null && user!=null && (group!=null && group.length()>0)) {

			int retries = 0;

			// Retrieving the list of groups for the newly created site, using alternate port (publish in general)
			List<NameValuePair>  nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("filter", "[{\"operation\":\"like\",\"rep:principalName\":\"" + group + "\"}]"));
			nameValuePairs.add(new BasicNameValuePair("type", "groups"));
			while (retries++ < MAXRETRIES) {

				String groupList = Loader.doGet(hostname, port,
						"/libs/social/console/content/content/userlist.social.0.10.json",
						user, password,
						nameValuePairs);

				logger.debug(groupList);

				if (groupList.indexOf(group)>0) {

					logger.debug("Group was found on " + port);
					break;

				} else {

					doSleep(2000,"Group " + group + " not found yet");

				}

			}

			if (retries==MAXRETRIES) {
				logger.error("Group " + group +" was never found as expected");
			}

		}


	}

	// This method runs a QUERY against an AEM instance
	private static String doQuery(String hostname, String port, String adminPassword, String path, String type) {

		String query = null;

		if (port!=null && hostname!=null && path!=null && type!=null) {

			List<NameValuePair>  nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("path", path));
			nameValuePairs.add(new BasicNameValuePair("type", type));
			String nodeList = Loader.doGet(hostname, port,
					"/bin/querybuilder.json",
					"admin", adminPassword,
					nameValuePairs);

			logger.debug(nodeList);
			query = nodeList;
		}

		return query;
	}

	// This method WAITS for a node to be available
	private static void doWaitPath(String hostname, String port, String adminPassword, String path, String type) {

		int retries = 0;
		while (retries++ < MAXRETRIES) {

			String nodeList = doQuery(hostname, port, adminPassword, path, type);
			try {            
				JSONObject nodeListJson = new JSONObject(nodeList);
				int results = nodeListJson.getInt("results");
				if (results>0) {

					logger.debug("Node was found for: " + path);
					break;

				} else {

					doSleep(2000,"Node not found yet, repeating " + retries);

				}

			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}

		}

	}

	// This method GETs a request to the server, returning the location JSON attribute, when available
	private static String doGet(String hostname, String port, String url, String user, String password, List<NameValuePair> params) {

		String rawResponse = null;

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				URIBuilder uribuilder = new URIBuilder();
				uribuilder.setScheme("http")
				.setHost(hostname)
				.setPort(Integer.parseInt(port))
				.setPath(url);

				// Adding the params
				if (params!=null) for (NameValuePair nvp : params) {
					uribuilder.setParameter(nvp.getName(), nvp.getValue());
				}

				URI uri = uribuilder.build();
				logger.debug("URI built as " + uri.toString());
				HttpGet httpget = new HttpGet(uri);
				CloseableHttpResponse response = httpClient.execute(httpget, localContext);
				try {     
					rawResponse = EntityUtils.toString(response.getEntity(), "UTF-8");   
				} catch (Exception ex) {
					logger.error(ex.getMessage());
				} finally {
					response.close();
				}

			} catch (Exception ex) {
				logger.error(ex.getMessage());
			} finally {
				httpClient.close();
			}

		} catch (IOException e) {

			e.printStackTrace();
		}

		return rawResponse;

	}

	// This method logs the list of value/pairs
	@SuppressWarnings("unused")
	private static void dumpNVP(List<NameValuePair> nameValuePairs) {
		for (NameValuePair nvp : nameValuePairs) logger.debug(nvp.getName() + ":" + nvp.getValue());
	}

	// This method creates a list of name value pairs from an existing one, converting a JSON array into multiple individual entries
	private static List<NameValuePair> convertArrays(List<NameValuePair> nameValuePairs, String key) {

		List<NameValuePair> newNameValuePairs = new ArrayList<NameValuePair>();		
		for (NameValuePair nvp : nameValuePairs) {
			if (nvp.getName().equals(key)) {
				// Let's see if we can split this JSON date
				try {
					JSONArray jsonArray = new JSONArray(nvp.getValue());
					for (int i=0;i<jsonArray.length();i++) {
						String value = (String) jsonArray.get(i);
						value = URLEncoder.encode(value.replaceAll("'", "\""), java.nio.charset.StandardCharsets.UTF_8.toString());
						newNameValuePairs.add(new BasicNameValuePair(key, value)); 
						logger.debug("Setting property " + key + " with value " + value);
					}					
				} catch (Exception e) {
					logger.error("Can't process JSON array for key: " + key);
				}

			} else {
				newNameValuePairs.add(nvp);
			}
		}

		return newNameValuePairs;
	}

	// This method builds a list of NVP for a subsequent Sling post
	private static List<NameValuePair> buildNVP(CSVRecord record, int start) {

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("_charset_", "UTF-8"));

		for (int i=start;i<record.size()-1;i=i+2) {

			if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

				// We have a non String hint to pass to the POST servlet
				String name = record.get(i);
				String value = record.get(i+1);
				if (value.equals("TRUE")) { value = "true"; }
				if (value.equals("FALSE")) { value = "false"; }		

				int hint = name.indexOf("@");
				if (hint>0) {
					logger.debug(name.substring(0,hint) + "@TypeHint:" + name.substring(1+hint));
					nameValuePairs.add(new BasicNameValuePair(name.substring(0,hint) + "@TypeHint", name.substring(1+hint)));					            		
					name = name.substring(0,hint);
				} else {
					nameValuePairs.add(new BasicNameValuePair(name + "@TypeHint", "String"));					            							            		
				}

				// We have multiple values to pass to the POST servlet, e.g. for a String[]
				int multiple = value.indexOf("|");
				if (multiple>0) {
					List<String> values = Arrays.asList(value.split("\\|", -1));
					for (String currentValue : values) {
						nameValuePairs.add(new BasicNameValuePair(name, currentValue));	
						logger.debug(name + " " + currentValue);
					}
				} else {					            		
					nameValuePairs.add(new BasicNameValuePair(name, value));					            							            		
				}

				logger.debug("Setting property "+ name + " with value " + value );

			}

		}	

		return nameValuePairs;

	}

	// This class extracts the version of an AEM bundle from the JSON list of bundles
	public static Version getVersion(String jsonList, String symbolicName) {

		if (jsonList==null || symbolicName==null) return null;

		try {

			JSONObject bundleJson = new JSONObject(jsonList.trim());

			Iterator<?> keys = bundleJson.keys();

			while( keys.hasNext() ) {
				String key = (String)keys.next();
				if (key.equals("data")) {

					JSONArray bundleList = (JSONArray) bundleJson.get(key);
					for (int i=0;i<bundleList.length();i++) {
						JSONObject bundle = (JSONObject) bundleList.get(i);
						if (bundle.get("symbolicName").equals(symbolicName)) {

							String version = (String) bundle.get("version");

							// Making it a "clean version"
							version = version.replace(".SNAPSHOT", "").trim();

							logger.debug(symbolicName + " : " + version);

							return new Version( version );

						}
					}

				}
			}	

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return null;

	}

}
