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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.aem.demomachine.Hostname;

public class Loader {

	static Logger logger = Logger.getLogger(Loader.class);

	private static final String USERS = "Users";
	private static final String COMMENTS = "Comments";
	private static final String REVIEWS = "Reviews";
	private static final String RATINGS = "Ratings";
	private static final String FORUM = "Forum";
	private static final String JOURNAL = "Journal";
	private static final String TAG = "Tag";
	private static final String IDEATION = "Ideation";
	private static final String BLOG = "Blog";
	private static final String SUMMARY = "Summary";
	private static final String CALENDAR = "Calendar";
	private static final String PREFERENCES = "Preferences";
	private static final String FILES = "Files";
	private static final String IMAGE = "file";
	private static final String AVATAR = "Avatar";
	private static final String ACTIVATE = "Activate";
	private static final String QNA = "QnA";
	private static final String ACTIVITIES = "Activities";
	private static final String SLINGPOST = "SlingPost";
	private static final String SLINGDELETE = "SlingDelete";
	private static final String FRAGMENT = "Fragment";
	private static final String FOLDER = "Folder";
	private static final String PASSWORD = "password";
	private static final String SITE = "Site";
	private static final String SITEUPDATE = "SiteUpdate";
	private static final String SITEPUBLISH = "SitePublish";
	private static final String SITEDELETE = "SiteDelete";
	private static final String SITETEMPLATE = "SiteTemplate";
	private static final String SITEPATH = "SitePath";
	private static final String GROUPTEMPLATE = "GroupTemplate";
	private static final String GROUPMEMBERS = "GroupMembers";
	private static final String GROUPPUBLISH = "GroupPublish";
	private static final String GROUPDELETE = "GroupDelete";
	private static final String SITEMEMBERS = "SiteMembers";
	private static final String UGCUPVOTE = "Upvote";
	private static final String UGCDOWNVOTE = "Downvote";
	private static final String UGCREPLY = "Reply";
	private static final String UGCFLAG = "Flag";
	private static final String UGCDENY = "Deny";
	private static final String UGCLIKE = "Like";
	private static final String UGCFEATURE = "Feature";
	private static final String UGCPIN = "Pin";
	private static final String UGCANSWER = "Answer";
	private static final String GROUP = "Group";
	private static final String SUBGROUP = "SubGroup";
	private static final String JOIN = "Join";
	private static final String ASSET = "Asset";
	private static final String ASSETINSIGHTS = "AssetInsights";
	private static final String KILL = "Kill";
	private static final String MESSAGE = "Message";
	private static final String RESOURCE = "Resource";
	private static final String BADGE = "Badge";
	private static final String BADGEIMAGE = "BadgeImage";
	private static final String BADGEASSIGN = "BadgeAssign";
	private static final String OPTION_ANALYTICS = "enableAnalytics";
	private static final String OPTION_FACEBOOK = "allowFacebook";
	private static final String OPTION_TWITTER = "allowTwitter";
	private static final String OPTION_TRANSLATION = "allowMachineTranslation";
	private static final String CLOUDSERVICE_ANALYTICS = "analyticsCloudConfigPath";
	private static final String CLOUDSERVICE_FACEBOOK = "fbconnectoauthid";
	private static final String CLOUDSERVICE_TWITTER = "twitterconnectoauthid";
	private static final String CLOUDSERVICE_TRANSLATION = "translationProviderConfig";
	private static final int RESOURCE_INDEX_PATH = 5;
	private static final int RESOURCE_INDEX_THUMBNAIL = 3;
	private static final int CALENDAR_INDEX_THUMBNAIL = 8;
	private static final int CALENDAR_INDEX_TAGS = 9;
	private static final int ASSET_INDEX_NAME = 4;
	private static final int RESOURCE_INDEX_SITE = 7;
	private static final int RESOURCE_INDEX_FUNCTION = 9;
	private static final int RESOURCE_INDEX_PROPERTIES = 10;
	private static final int GROUP_INDEX_NAME = 1;
	private static final String SLEEP = "Sleep";
	private static final String FOLLOW = "Follow";
	private static final String NOTIFICATION = "Notification";
	private static final String NOTIFICATIONPREFERENCE = "NotificationPreference";
	private static final String LEARNING = "LearningPath";
	private static final String BANNER = "pagebanner";
	private static final String THUMBNAIL = "pagethumbnail";
	private static final String LANGUAGE = "baseLanguage";
	private static final String LANGUAGES = "initialLanguages";
	private static final String ROOT = "siteRoot";
	private static final String CSS = "pagecss";
	private static final int MAXRETRIES=20;
	private static final int REPORTINGDAYS=-21;
	private static final String ENABLEMENT61FP2 = "1.0.135";
	private static final String ENABLEMENT61FP3 = "1.0.148";
	private static final String ENABLEMENT61FP4 = "1.0.164";
	private static final String ENABLEMENT62 = "1.1.0";
	private static final String ENABLEMENT62FP1 = "1.1.19";
	private static final String COMMUNITIES61 = "1.0.13";
	private static final String COMMUNITIES61FP5 = "2.0.7";
	private static final String COMMUNITIES61FP6 = "2.0.14";
	private static final String COMMUNITIES61FP7 = "2.0.15";

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
		boolean minimize = false;
		boolean noenablement = true;
		boolean nomultilingual = true;
		int maxretries = MAXRETRIES;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("h", true, "Hostname");
		options.addOption("p", true, "Port");
		options.addOption("a", true, "Alternate Port");
		options.addOption("f", true, "CSV file");
		options.addOption("r", false, "Reset");
		options.addOption("u", true, "Admin Password");
		options.addOption("c", false, "Configure");
		options.addOption("m", false, "Minimize");
		options.addOption("l", false, "No Multilingual");
		options.addOption("e", false, "No Enablement");
		options.addOption("s", true, "Analytics Endpoint");
		options.addOption("t", false, "Analytics");
		options.addOption("w", false, "Retry");
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

			if(cmd.hasOption("w")) {
				maxretries = Integer.parseInt(cmd.getOptionValue("w"));
			}

			if(cmd.hasOption("c")) {
				configure = true;
			}

			if(cmd.hasOption("m")) {
				minimize = true;
			}

			if(cmd.hasOption("l")) {
				nomultilingual = true;
			}

			if(cmd.hasOption("e")) {
				noenablement = false;
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

			// Reading and processing the CSV file, stand alone or as part of a ZIP file
			if (csvfile!=null && csvfile.toLowerCase().endsWith(".zip")) {

				ZipFile zipFile = new ZipFile(csvfile);
				ZipInputStream stream = new ZipInputStream(new FileInputStream(csvfile));
				ZipEntry zipEntry;
				while((zipEntry = stream.getNextEntry())!=null)
				{
					if (!zipEntry.isDirectory() && zipEntry.getName().toLowerCase().endsWith(".csv")) {

						InputStream is = zipFile.getInputStream(zipEntry);
						BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
						processLoading(null, in, hostname, port, altport, adminPassword, analytics, reset, configure, minimize, noenablement, nomultilingual, csvfile, maxretries);

					}
				}

				try {
					stream.close();
					zipFile.close();
				} catch (IOException ioex) {
					//omitted.
				}

			} else if (csvfile.toLowerCase().endsWith(".csv")) {

				Reader in = new FileReader(csvfile);
				processLoading(null, in, hostname, port, altport, adminPassword, analytics, reset, configure, minimize, noenablement, nomultilingual, csvfile, maxretries);

			}

		} catch (IOException e) {

			logger.error(e.getMessage());

		}

	}

	public static void processLoading(ResourceResolver rr, Reader in, String hostname, String port, String altport, String adminPassword, String analytics, boolean reset, boolean configure, boolean minimize, boolean noenablement, boolean nomultilingual, String csvfile, int maxretries) {

		String location = null;
		String userHome = null;
		String sitePagePath = null;
		String analyticsPagePath = null;
		String resourceType = null;
		String subComponentType = null;
		String rootPath = "/content/sites";
		String[] url = new String[10];  // Handling 10 levels maximum for nested comments 
		int urlLevel = 0;
		int row = 0;
		boolean ignoreUntilNextComponent = false;
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
			if (vBundleCommunitiesCalendar==null) {
				vBundleCommunitiesCalendar = getVersion(bundlesList, "com.adobe.cq.social.cq-social-calendar-impl");				
			}
			Version vBundleCommunitiesNotifications = getVersion(bundlesList, "com.adobe.cq.social.cq-social-notifications-impl");
			Version vBundleCommunitiesSCORM = getVersion(bundlesList, "com.adobe.cq.social.cq-social-scorm-dam");
			Version vBundleCommunitiesSCF = getVersion(bundlesList, "com.adobe.cq.social.cq-social-scf-impl");
			Version vBundleCommunitiesAdvancedScoring = getVersion(bundlesList, "com.adobe.cq.social.cq-social-scoring-advanced-impl");

			// Versions related methods
			boolean isCommunities61 = vBundleCommunitiesSCF!=null && vBundleCommunitiesSCF.compareTo(new Version(COMMUNITIES61))==0;
			boolean isCommunities61FP5orlater = vBundleCommunitiesSCF!=null && vBundleCommunitiesSCF.compareTo(new Version(COMMUNITIES61FP5))>=0;
			boolean isCommunities61FP6orlater = vBundleCommunitiesSCF!=null && vBundleCommunitiesSCF.compareTo(new Version(COMMUNITIES61FP6))>=0;
			boolean isCommunities61FP7orlater = vBundleCommunitiesSCF!=null && vBundleCommunitiesSCF.compareTo(new Version(COMMUNITIES61FP7))>=0;

			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			ignoreUntilNextComponent = false;
			for (CSVRecord record : records) {

				LinkedList<InputStream> lIs = new LinkedList<InputStream>();
				row = row + 1;
				logger.info("Row: " + row + ", new record: " + record.get(0));			
				if (record.size()>2)
					subComponentType = record.get(2);
				else
					logger.info("No subcomponent type to load");

				// Let's see if we deal with a comment
				if (record.get(0).startsWith("#")) {

					// We can ignore the comment line and move on
					continue;

				}

				// Let's see if we need to terminate this process
				if (record.get(0).equals(KILL)) {

					if (rr==null)
						System.exit(1);
					else
						return;

				}

				// Let's see if we need to pause a little bit
				if (record.get(0).equals(SLEEP) && record.get(1).length()>0) {

					doSleep(Long.valueOf(record.get(1)).longValue(), "Pausing " + record.get(1) + " ms");
					continue;

				}

				// Let's see if we need to set the current site path
				if (record.get(0).equals(SITEPATH)) {
					sitePagePath = record.get(1);
				}

				// Let's see if we need to create a new Community site
				if (record.get(0).equals(SITE)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:createSite", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					String urlName = null;
					String[] initialLanguages = null;

					boolean isValid=true;
					for (int i=2;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							if (value.equals("TRUE")) { value = "true"; }
							if (value.equals("FALSE")) { value = "false"; }	
							if (name.equals("urlName")) { urlName = value; }

							// Only create the site when a ROOT path is specified and available
							if (name.equals(ROOT)) {
								rootPath = value;
								logger.debug("Rootpath for subsequent processing is: " + rootPath);
								if (!isResourceAvailable(hostname, port, adminPassword, rootPath)) {
									logger.warn("Rootpath " + rootPath + " is not available, proceeding to next record");
									isValid=false;
								} else {
									logger.info("Rootpath " + rootPath + " is available");
								}
							}

							// Only create the site when a non-english language is specified 
							if (name.equals(LANGUAGE) || name.equals(LANGUAGES)) {
								if (!value.startsWith("en") && nomultilingual) {
									logger.info("Language " + value + " is not desired for this site, proceeding to next record");
									isValid=false;
								}
							}

							if (name.equals(BANNER)) {
								addBinaryBody(builder, lIs, rr, BANNER, csvfile, value);
							} else if (name.equals(THUMBNAIL)) {
								addBinaryBody(builder, lIs, rr, THUMBNAIL, csvfile, value);
							} else if (name.equals(CSS)) {
								addBinaryBody(builder, lIs, rr, CSS, csvfile, value);
							} else if (name.equals(LANGUAGE) || name.equals(LANGUAGES)) {


								// Starting with 6.1 FP5 and 6.2 FP1, we can create multiple languages at once, if expected by the script parameters
								if (isCommunities61FP5orlater && !nomultilingual) {

									initialLanguages = value.split(",");
									for (String initialLanguage : initialLanguages) {
										builder.addTextBody(LANGUAGES, initialLanguage, ContentType.create("text/plain", MIME.UTF8_CHARSET));
									}

								} else {

									// Only keep the first language for pre 6.1 FP5 and 6.2 FP1
									initialLanguages = new String[1];
									initialLanguages[0] = value.split(",")[0];
									builder.addTextBody(LANGUAGE, initialLanguages[0], ContentType.create("text/plain", MIME.UTF8_CHARSET));

								}

							} else {

								// For cloud services, we verify that they are actually available
								if ((name.equals(OPTION_TRANSLATION) || name.equals(OPTION_ANALYTICS) || name.equals(OPTION_FACEBOOK) || name.equals(OPTION_TWITTER)) && value.equals("true")) {

									String cloudName = record.get(i+2).trim();
									String cloudValue = record.get(i+3).trim();

									if ((cloudName.equals(CLOUDSERVICE_TRANSLATION) || cloudName.equals(CLOUDSERVICE_FACEBOOK) || cloudName.equals(CLOUDSERVICE_TWITTER) || cloudName.equals(CLOUDSERVICE_ANALYTICS)) && !isResourceAvailable(hostname, port, adminPassword, cloudValue)) {
										builder.addTextBody(name, "false", ContentType.create("text/plain", MIME.UTF8_CHARSET));
										logger.warn("Cloud service: " + cloudValue + " is not available on this instance");
									} else {	
										// We have a valid cloud service
										builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));
										builder.addTextBody(cloudName, cloudValue, ContentType.create("text/plain", MIME.UTF8_CHARSET));
										i=i+2;
										logger.debug("Cloud service: " + cloudValue + " available on this instance");
									}

								} else {

									// All other values just get added as is
									builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

								}
							}
						}
					}

					// Printing site creation settings
					//ByteArrayOutputStream out = new ByteArrayOutputStream();
					//builder.build().writeTo(out);
					//String string = out.toString();
					//logger.debug(string);

					// Site creation
					if (isValid)
						doPost(hostname, port, "/content.social.json", "admin", adminPassword, builder.build(), null,
								null);
					else
						continue;

					// Waiting for site creation to be complete
					boolean existingSiteWithLocale = rootPath.indexOf("/"+initialLanguages[0])>0;					
					doWaitPath(hostname, port, adminPassword, rootPath + "/" + urlName + (existingSiteWithLocale?"":"/" + initialLanguages[0]), maxretries);

					// Site publishing, if there's a publish instance to publish to
					if (!port.equals(altport)) {

						for (String initialLanguage : initialLanguages) {

							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
							nameValuePairs.add(new BasicNameValuePair("id", "nobot"));
							nameValuePairs.add(new BasicNameValuePair(":operation", "social:publishSite"));
							nameValuePairs.add(new BasicNameValuePair("path", rootPath + "/" + urlName + (existingSiteWithLocale?"":"/" + initialLanguage)));

							logger.debug("Publishing site " + urlName + " for language " + initialLanguage);

							doPost(hostname, port,
									"/communities/sites.html",
									"admin", adminPassword,
									new UrlEncodedFormEntity(nameValuePairs),
									null);

							doWaitPath(hostname, altport, adminPassword, rootPath + "/" + urlName + (existingSiteWithLocale?"": "/" + initialLanguage), maxretries);

						}

					}

					continue;
				}

				// Let's see if we need to update an existing Community site (this doesn't include republishing the site!)
				if (record.get(0).equals(SITEUPDATE) && record.get(1)!=null && record.get(2)!=null) {

					// Let's set if we need to run based on version number
					Version vRecord = null;
					if (record.get(2).startsWith(">") || record.get(2).startsWith("<") || record.get(2).startsWith("=")) {

						try {
							vRecord = new Version(record.get(2).substring(1));
						} catch (Exception e) {
							logger.error("Invalid version number specified" + record.get(2));
						}
					}

					if (vRecord!=null && record.get(2).startsWith(">") && vBundleCommunitiesSCF.compareTo(vRecord)<=0) {
						logger.info("Ignoring the site update command for this version of AEM" + vBundleCommunitiesSCF.get());
						continue;
					}

					if (vRecord!=null && record.get(2).startsWith("<") && vBundleCommunitiesSCF.compareTo(vRecord)>0) {
						logger.info("Ignoring the site update command for this version of AEM" + vBundleCommunitiesSCF.get());
						continue;
					}

					if (isResourceAvailable(hostname, port, adminPassword, record.get(1))) {
						logger.debug("Updating a Community Site " + record.get(1));
					} else {
						logger.error("Can't update a Community Site " + record.get(1));
						continue;
					}

					// Let's fetch the theme for this Community Site Url
					String siteConfig = doGet(hostname, port, record.get(1),
							"admin",adminPassword,
							null);

					if (siteConfig==null) {
						logger.error("Can't update a Community Site " + record.get(1));
						continue;
					}

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:updateSite", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					// Adding the mandatory values for being able to save a site via the JSON endpoint
					List<String> props = Arrays.asList("urlName", "theme", "moderators", "createGroupPermission", "groupAdmin", "twitterconnectoauthid", "fbconnectoauthid", "translationProviderConfig", "translationProvider", "commonStoreLanguage");
					try {
						JSONObject siteprops = new JSONObject(siteConfig).getJSONObject("properties");
						for (String prop : props) {
							if (siteprops.has(prop)) {
								Object propValue = siteprops.get(prop); 
								if (propValue instanceof JSONArray) {
									JSONArray propArray = (JSONArray) propValue;
									for (int i=0;i<propArray.length();i++) {
										builder.addTextBody(prop, propArray.get(i).toString(), ContentType.create("text/plain", MIME.UTF8_CHARSET));																	
									}
								} else {
									builder.addTextBody(prop, propValue.toString(), ContentType.create("text/plain", MIME.UTF8_CHARSET));								
								}
							}
						}

					} catch (Exception e) {
						logger.error(e.getMessage());
					}

					// Adding the override values from the CSV record
					boolean isValid=true;
					for (int i=3;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

							// If the template includes some of the enablement features, then it won't work for 6.1 GA
							if (name.equals("functions") && value.indexOf("assignments")>0 && vBundleCommunitiesEnablement==null) {
								logger.info("Site update is not compatible with this version of AEM");
								isValid=false;
							}

							// If the template includes some of the ideation features, then it won't work until 6.2 FP2
							if (name.equals("functions") && value.indexOf("ideation")>0 && !isCommunities61FP6orlater) {
								logger.info("Site update is not compatible with this version of AEM");
								isValid=false;
							}

						}

					}

					// Convenient for debugging the site update operation
					// printPOST(builder.build());	

					if (isValid)
						doPost(hostname, port,
								record.get(1),
								"admin", adminPassword,
								builder.build(),
								null);

					continue;
				}

				// Let's see if we need to publish a site
				if (record.get(0).equals(SITEPUBLISH) && record.get(1)!=null) {

					if (isResourceAvailable(hostname, port, adminPassword, record.get(1))) {
						logger.debug("Publishing a Community Site " + record.get(1));
					} else {
						logger.warn("Can't publish a Community Site " + record.get(1));
						continue;
					}

					if (!port.equals(altport)) {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("id", "nobot"));
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:publishSite"));
						nameValuePairs.add(new BasicNameValuePair("nestedActivation", "true"));
						nameValuePairs.add(new BasicNameValuePair("path", record.get(1)));

						doPost(hostname, port,
								"/communities/sites.html",
								"admin", adminPassword,
								new UrlEncodedFormEntity(nameValuePairs),
								null);

						doWaitPath(hostname, altport, adminPassword, record.get(1), maxretries);

					}

					continue;

				}

				// Let's see if we need to publish a group
				if (record.get(0).equals(GROUPPUBLISH) && record.get(1)!=null) {

					if (!port.equals(altport)) {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("id", "nobot"));
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:publishCommunityGroup"));
						nameValuePairs.add(new BasicNameValuePair("nestedActivation", "true"));
						nameValuePairs.add(new BasicNameValuePair("path", record.get(1) + "/" + record.get(2)));

						doPost(hostname, port,
								"/communities/communitygroups.html/" + record.get(1),
								"admin", adminPassword,
								new UrlEncodedFormEntity(nameValuePairs),
								null);
					}

					continue;

				}

				// Let's see if we need to activate a tree
				if (record.get(0).equals(ACTIVATE) && record.get(1)!=null) {

					if (!port.equals(altport)) {

						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("cmd", "activate"));
						nameValuePairs.add(new BasicNameValuePair("ignoreactivated", "true"));
						nameValuePairs.add(new BasicNameValuePair("path", record.get(1)));

						doPost(hostname, port,
								"/etc/replication/treeactivation.html",
								"admin", adminPassword,
								new UrlEncodedFormEntity(nameValuePairs),
								null);
					}

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

					if (vBundleCommunitiesEnablement==null || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))<0) {
						logger.info("Badging operations not available with this version of AEM");
						continue;
					}

					List<NameValuePair> nameValuePairs = buildNVP(hostname, port, adminPassword, null, record, 2);

					String badgePath = record.get(1);
					if (badgePath.startsWith("/etc") && (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP4))==0 || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))>0 ) ) {
						badgePath = badgePath.replaceAll("/jcr:content", "");
						nameValuePairs.add(new BasicNameValuePair("sling:resourceType","social/gamification/components/hbs/badging/rulecollection/rule"));
						nameValuePairs.add(new BasicNameValuePair("badgingType","basic"));
					}

					if (nameValuePairs.size()>2) {

						for (int i=0;i<nameValuePairs.size();i=i+1) {

							String name = nameValuePairs.get(i).getName();
							String value = nameValuePairs.get(i).getValue();

							// Special case to accommodate re-factoring of badging images
							if (name.equals("badgeContentPath") && (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP4))==0 || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))>0 ) ) {
								value = value.replaceAll("/jcr:content", "");
								nameValuePairs.set(i, new BasicNameValuePair(name, value));
							}

							// Special case to accommodate re-factoring of badging images
							if (name.startsWith("thresholds") && (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP4))==0 || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))>0 ) ) {
								value = value.replaceAll("/jcr:content(.*)", "");
								nameValuePairs.set(i, new BasicNameValuePair(name, value));
							}

							// Special case to accommodate re-factoring or scoring and badging resource types
							if (name.equals("jcr:primaryType") && (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP4))==0 || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))>0 ) ) {
								if (value.equals("cq:PageContent") || value.equals("cq:Page")) {
									value = "nt:unstructured";
									nameValuePairs.set(i, new BasicNameValuePair(name, value));
								}
							}

							// Special case for accommodate advanced scoring being installed or not
							if (name.endsWith("Rules") && value.contains("adv-") && vBundleCommunitiesAdvancedScoring==null) {
								nameValuePairs.remove(i--);
							}

						}
					}

					// Badge rules operation
					doPost(hostname, port,
							badgePath,
							"admin", adminPassword,
							new UrlEncodedFormEntity(nameValuePairs),
							null);

					continue;
				}

				// Let's see if we need to create a new Community site template, and if we can do it (script run against author instance)
				if (record.get(0).equals(SITETEMPLATE) || record.get(0).equals(GROUPTEMPLATE)) {

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:create" + record.get(0), ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					boolean isValid=true;
					for (int i=2;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));

							// If the template is already there, let's not try to create it
							if (name.equals("templateName") && (isResourceAvailable(hostname, port, adminPassword, "/etc/community/templates/sites/custom/" + title2name(value)) || isResourceAvailable(hostname, port, adminPassword, "/etc/community/templates/groups/custom/" + title2name(value)))) {
								logger.info("Template " + value + " is already there");
								isValid=false;
							}

							// If the template includes some of the enablement features, then it won't work for 6.1 GA
							if (name.equals("functions") && value.indexOf("assignments")>0 && vBundleCommunitiesEnablement==null) {
								logger.info("Template " + record.get(3) + " is not compatible with this version of AEM");
								isValid=false;
							}

							// If the template includes some of the ideation features, then it won't work until 6.2 FP2
							if (name.equals("functions") && value.indexOf("ideation")>0 && !isCommunities61FP6orlater) {
								logger.info("Template " + record.get(3) + " is not compatible with this version of AEM");
								isValid=false;
							}

							// If the group template includes the nested group features, then it won't work until 6.2 FP1
							if (record.get(0).equals(GROUPTEMPLATE) && name.equals("functions") && value.indexOf("groups")>0 && (vBundleCommunitiesEnablement!=null && vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))<=0)) {
								logger.info("Group template " + record.get(3) + " is not compatible with this version of AEM");
								isValid=false;
							}

							// If the group template includes the blogs or calendars, then it won't work with 6.1GA
							if (name.equals("functions") && (value.indexOf("blog")>0 || value.indexOf("calendar")>0) && vBundleCommunitiesEnablement==null) {
								logger.info("Template " + record.get(3) + " is not compatible with this version of AEM");
								isValid=false;
							}

						}
					}

					// Site or Group template creation
					if (isValid) doPost(hostname, port,
							"/content.social.json",
							"admin", adminPassword,
							builder.build(),
							null);

					continue;
				}

				// Let's see if we need to create a new Community group
				if (record.get(0).equals(GROUP) || record.get(0).equals(SUBGROUP)) {

					// SubGroups are only supported with 6.1 FP5 and 6.2 FP1 onwards
					if (record.get(0).equals(SUBGROUP) && !isCommunities61FP5orlater) {
						logger.warn("Subgroups are not supported with this version of AEM Communities");
						continue;
					}

					// Building the form entity to be posted
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setCharset(MIME.UTF8_CHARSET);
					builder.addTextBody(":operation", "social:createCommunityGroup", ContentType.create("text/plain", MIME.UTF8_CHARSET));
					builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));

					String urlName=null;
					String groupType=null;
					for (int i=3;i<record.size()-1;i=i+2) {

						if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

							String name = record.get(i).trim();
							String value = record.get(i+1).trim();
							if (value.equals("TRUE")) { value = "true"; }
							if (value.equals("FALSE")) { value = "false"; }	
							if (name.equals("type")) { groupType = value; }
							if (name.equals(IMAGE)) {
								addBinaryBody(builder, lIs, rr, IMAGE, csvfile, value);
							} else {
								builder.addTextBody(name, value, ContentType.create("text/plain", MIME.UTF8_CHARSET));
							}
							if (name.equals("urlName")) {
								urlName = value;
							}
							if (name.equals("siteRoot")) {
								// Some content root has been provided for the Group. It might result from previous actions and might not be there yet - let's wait for it
								doWaitPath(hostname, port, adminPassword, value, maxretries);
							}
						}
					}

					// Private groups are only support with 6.1 FP1 onwards
					if (groupType!=null && groupType.equals("Secret") && isCommunities61) {
						continue;
					}

					// Group creation
					doPost(hostname, port,
							record.get(1),
							getUserName(record.get(2)), getPassword(record.get(2), adminPassword),
							builder.build(),
							null);

					// Waiting for group to be available either on publish or author
					int i = (record.get(1).indexOf("/jcr:content")>0)?record.get(1).indexOf("/jcr:content"):record.get(1).indexOf(".social.json");
					if (urlName!=null && i>0) {
						doWaitPath(hostname, port, adminPassword, record.get(1).substring(0, i) + "/" + urlName, maxretries);
					} else {
						logger.warn("Not waiting for Group to be fully available");
					}

					continue;

				}

				// Let's see if it's simple Sling Delete request
				if (record.get(0).equals(SLINGDELETE)) {

					doDelete(hostname, port,
							record.get(1),
							"admin", adminPassword);

					continue;

				}

				// Let's see if we need to delete some user groups
				if (record.get(0).equals(GROUPDELETE) && record.get(1)!=null) {

					// Let's query all the Community sites
					String siteList = Loader.doGet(hostname, port,
							"/mnt/overlay/social/console/content-shell3/sites/jcr:content/views/content/items/sitecollection.social.json",
							"admin", adminPassword,
							null);					

					// List of orphan entries
					List<String> orphanKeys = new ArrayList<String>();
					
					// Let's query all the groups
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("type", "rep:Group"));
					nameValuePairs.add(new BasicNameValuePair("p.limit", "-1"));
					nameValuePairs.add(new BasicNameValuePair("p.hits", "full"));

					String groupList = Loader.doGet(hostname, port,
							"/bin/querybuilder.json",
							"admin", adminPassword,
							nameValuePairs);
					JSONArray jsonArray = new JSONObject(groupList).getJSONArray("hits");

					for (int i=0;i<jsonArray.length();i++) {

						JSONObject jsonObject = jsonArray.getJSONObject(i);
						String groupPath=jsonObject.getString("jcr:path");
						String groupName=jsonObject.getString("rep:authorizableId");

						boolean deleteGroup=false;

						// First, an explicit delete is requested
						if (groupName.startsWith(record.get(2)) && groupPath.startsWith(record.get(1))) {
							deleteGroup = true;
						}

						// Second, we might be dealing with an orphan group, let's verify
						String communityGroups = "/home/groups/community/";
						int indexStartCommunity = groupPath.indexOf(communityGroups);
						if (indexStartCommunity>=0) {
							int indexStopCommunity = groupPath.indexOf("/", communityGroups.length() + indexStartCommunity);
							if (indexStopCommunity>0) {
								String keyCommunity = groupPath.substring(communityGroups.length() + indexStartCommunity, indexStopCommunity);
								int indexUrlCommunity = keyCommunity.indexOf("-");
								if (indexUrlCommunity>0) {
									String urlCommunity = keyCommunity.substring(0, indexUrlCommunity);
									if (!siteList.contains(keyCommunity) && !siteList.contains("siteUrlName\":\"" + urlCommunity)) {
										deleteGroup = true;
										orphanKeys.add(keyCommunity);
										orphanKeys.add("community-" + urlCommunity);
									}

								
								}
							}
						}

						if (deleteGroup) {

							logger.debug("Deleting orphan or desired group " + groupName);
							
							List<NameValuePair>  groupDeleteValuePairs = new ArrayList<NameValuePair>();
							groupDeleteValuePairs.add(new BasicNameValuePair("deleteAuthorizable", groupName));

							// Building the form entity to be posted
							MultipartEntityBuilder builder = MultipartEntityBuilder.create();
							builder.setCharset(MIME.UTF8_CHARSET);
							builder.addTextBody("_charset_", "UTF-8", ContentType.create("text/plain", MIME.UTF8_CHARSET));
							builder.addTextBody("deleteAuthorizable", groupName, ContentType.create("text/plain", MIME.UTF8_CHARSET));

							doPost(hostname, port,
									groupPath,
									"admin", adminPassword,
									builder.build(),
									null);

						}
						
					}

					// Let's get rid of all the orphan folders for groups
					for (String key : orphanKeys) {
						logger.debug("Deleting folder /home/groups/community/" + key);
						doDelete(hostname, port,
								"/home/groups/community/" + key,
								"admin", adminPassword);

					}
					
				}

				// Let's see if we need to delete a Community site
				if (record.get(0).equals(SITEDELETE) && record.get(1)!=null) {

					// Let's fetch the siteId for this Community Site Url
					String siteConfig = doGet(hostname, port,
							record.get(1),
							"admin",adminPassword,
							null);

					// No site to Delete
					if (siteConfig==null) continue;

					try {

						String siteRoot = new JSONObject(siteConfig).getString("siteRoot");
						String urlName = new JSONObject(siteConfig).getString("urlName");
						String siteId = new JSONObject(siteConfig).getString("siteId");
						String resourcesRoot = new JSONObject(siteConfig).getString("siteAssetsPath");

						if (siteRoot!=null && urlName!=null && siteId!=null && resourcesRoot!=null) {

							// First, deleting the main JCR path for this site, on author and publish
							doDelete(hostname, port, siteRoot + "/" + urlName, "admin", adminPassword);
							doDelete(hostname, altport, siteRoot + "/" + urlName, "admin", adminPassword);

							// Then, deleting the dam resources for this site, on author and publish
							doDelete(hostname, port, resourcesRoot, "admin", adminPassword);
							doDelete(hostname, altport, resourcesRoot, "admin", adminPassword);

							// Then, deleting the main UGC path for this site, on author and publish
							doDelete(hostname, port, "/content/usergenerated/asi/jcr" + siteRoot + "/" + urlName, "admin", adminPassword);
							doDelete(hostname, altport, "/content/usergenerated/asi/jcr" + siteRoot + "/" + urlName, "admin", adminPassword);

							// Finally, deleting the system groups for this site, on author and publish
							doDelete(hostname, port, "/home/groups/community-" + siteId, "admin", adminPassword);
							doDelete(hostname, altport, "/home/groups/community-" + siteId, "admin", adminPassword);

						}

					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}

				// Let's see if we need to add users to an AEM Group
				if ((record.get(0).equals(GROUPMEMBERS) || record.get(0).equals(SITEMEMBERS)) && record.get(GROUP_INDEX_NAME)!=null) {

					// Checking if we have a member group for this site
					String groupName = null;
					if (record.get(0).equals(SITEMEMBERS)) {

						String configurationPath = record.get(GROUP_INDEX_NAME);

						// Let's make sure the configuration .json is there
						doWaitPath(hostname, port, adminPassword, configurationPath, maxretries);

						// Let's fetch the siteId for this Community Site Url
						String siteConfig = doGet(hostname, port,
								configurationPath,
								"admin",adminPassword,
								null);

						if (siteConfig==null) {
							logger.error("Can't retrieve site configuration");
							continue;
						};

						String siteId = null;
						try {

							siteId = new JSONObject(siteConfig).getString("siteId");

						} catch (Exception e) {

							logger.warn("No site Id available");

						}

						String urlName = null;
						try {

							urlName = new JSONObject(siteConfig).getString("urlName");

						} catch (Exception e) {

							logger.error("No site url available");
							continue;

						}

						if (siteId!=null) 
							groupName = "community-" + siteId + "-members";
						else
							groupName = "community-" + urlName + "-members";

						logger.debug("Site Member group name is " + groupName);

					}

					if (record.get(0).equals(GROUPMEMBERS)) {

						groupName = record.get(GROUP_INDEX_NAME);	

					}

					// We can't proceed if the group name wasn't retrieved from the configuration
					if (groupName==null) continue;

					// Pause until the group can found
					String groupList = doWait(hostname, port,
							"admin", adminPassword,
							groupName, maxretries
							);

					if (groupList!=null && groupList.indexOf("\"results\":1")>0) {

						logger.debug("Group was found on " + port);
						try {
							JSONArray jsonArray = new JSONObject(groupList).getJSONArray("hits");
							if (jsonArray.length()==1) {
								JSONObject jsonObject = jsonArray.getJSONObject(0);
								String groupPath= jsonObject.getString("path");

								logger.debug("Group path is " + groupPath);

								// Constructing a multi-part POST for group membership
								MultipartEntityBuilder builder = MultipartEntityBuilder.create();
								builder.setCharset(MIME.UTF8_CHARSET);
								builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

								List<NameValuePair> groupNameValuePairs = buildNVP(hostname, port, adminPassword, null, record, 2);
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
					} else {
						logger.warn("Group " + groupName + " cannot be updated as expected");
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

							// Now we can post all the preferences or the profile
							List<NameValuePair> nameValuePairs = buildNVP(hostname, port, adminPassword, null, record, 3);
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

				// Let's see if we need to generate analytics events for Assets Insights
				if (record.get(0).equals(ASSETINSIGHTS) && record.size()>1 && analytics!=null) {

					logger.debug("Generating Assets Analytics for reportsuite " + analytics);

					// Generating Impressions
					int impressions = new Random().nextInt(21) + 5;
					for (int i = 0; i < impressions; i++)
						doAssetsAnalytics( analytics, "event1", "list1", record.get(1).replace('|',','), "o", "Asset Impression Event");

					// Generating Clicks for each asset
					List<String> assetIds = Arrays.asList(record.get(1).split("\\|", -1));
					for (String assetId : assetIds) {
						int clicks = new Random().nextInt(5) + 2;
						for (int i = 0; i < clicks; i++)
							doAssetsAnalytics( analytics, "event2", "eVar4", assetId, "e", "Asset Click Event");
					}

					continue;

				}

				// Let's see if we deal with a new block of content or just a new entry
				if (record.get(0).equals(CALENDAR)
						|| record.get(0).equals(SLINGPOST)
						|| record.get(0).equals(RATINGS) 
						|| record.get(0).equals(IDEATION) 
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
						|| record.get(0).equals(NOTIFICATIONPREFERENCE) 
						|| record.get(0).equals(MESSAGE) 
						|| record.get(0).equals(ASSET) 
						|| record.get(0).equals(AVATAR) 
						|| record.get(0).equals(FOLDER) 
						|| record.get(0).equals(BADGEIMAGE) 
						|| record.get(0).equals(BADGEASSIGN) 
						|| record.get(0).equals(FRAGMENT) 
						|| record.get(0).equals(RESOURCE)
						|| record.get(0).equals(LEARNING) 
						|| record.get(0).equals(QNA) 
						|| record.get(0).equals(FORUM)) {

					// New block of content, we need to reset the processing to first Level
					componentType = record.get(0);
					url[0] = record.get(1);
					urlLevel=0;

					// If it's not a SLINGPOST that could result in nodes to be created, let's make sure the end point is really there.
					if (!record.get(0).equals(SLINGPOST) && record.get(1)!=null && !isResourceAvailable(hostname, port, adminPassword, getRootPath(record.get(1)))) {
						ignoreUntilNextComponent = true;
						continue;
					} else {
						ignoreUntilNextComponent = false;
					}

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
						logger.info(configurePath);

						List<NameValuePair> nameValuePairs = buildNVP(hostname, port, adminPassword, configurePath, record, 2);
						if (nameValuePairs.size()>2) {

							// If we're posting against a jcr:content node, let's make sure the parent folder is there
							int pos1 = configurePath.indexOf("/jcr:content");
							if (pos1>0) {

								if (!isResourceAvailable(hostname, port, adminPassword, configurePath.substring(0, pos1)))
									continue;

							}

							// If we're posting against a configuration node, let's make sure the parent folder is there
							int pos2 = configurePath.indexOf("configuration");
							if (pos2>0) {

								if (!isResourceAvailable(hostname, port, adminPassword, configurePath))
									continue;

							}

							// If we're posting to fetch analytics data, let's make sure the analytics host is available
							int pos3 = configurePath.indexOf("analyticsCommunities");
							if (pos3>0) {

								if (!Hostname.isReachable("www.adobe.com", "80")) {
									logger.warn("Analytics cannot be imported since you appear to be offline"); // The things you have to do when coding in airplanes...
									continue;						
								}

							}

							// Only do this when really have configuration settings
							doPost(hostname, port,
									configurePath,
									"admin", adminPassword,
									new UrlEncodedFormEntity(nameValuePairs),
									null);

						}

						// If the Sling POST touches the system console, then we need to make sure the system is open for business again before we proceed
						if (record.get(1).indexOf("system/console")>0) {
							doSleep(10000, "Waiting after a bundle change/restart");
							doWait(hostname, port,
									"admin", adminPassword,
									"administrators", maxretries
									);
						}

					}

					// We're done with this line, moving on to the next line in the CSV file
					continue;
				}

				// Are we processing until the next component because the end point if not available?
				if (ignoreUntilNextComponent) {
					logger.info("Ignoring this record because of unavailable component configuration");
					continue;
				}

				// Let's see if we need to indent the list, if it's a reply or a reply to a reply
				if (record.get(1)==null || record.get(1).length()!=1) continue;  // We need a valid level indicator

				if (Integer.parseInt(record.get(1))>urlLevel) {
					url[++urlLevel] = location;
					logger.debug("Incrementing urlLevel to: " + urlLevel + ", with a new location:" + location);
				} else if (Integer.parseInt(record.get(1))<urlLevel) {
					urlLevel = Integer.parseInt(record.get(1));
					logger.debug("Decrementing urlLevel to: " + urlLevel);
				}

				// Special case for 6.1 GA only with forums and files
				if (vBundleCommunitiesEnablement==null && (!(componentType.equals(FORUM) || componentType.equals(FILES) || componentType.equals(JOIN)))) continue;

				// Get the credentials or fall back to password
				String password = getPassword(record.get(0), adminPassword);
				String userName = getUserName(record.get(0));

				// Adding the generic properties for all POST requests
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setCharset(MIME.UTF8_CHARSET);
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				if (!componentType.equals(RESOURCE) && !componentType.equals(LEARNING))
					nameValuePairs.add(new BasicNameValuePair("id", "nobot"));

				nameValuePairs.add(new BasicNameValuePair("_charset_", "UTF-8"));

				if(urlLevel==0 && (componentType.equals(FORUM) || componentType.equals(FILES) || componentType.equals(QNA) || componentType.equals(IDEATION) || componentType.equals(BLOG) || componentType.equals(CALENDAR)))
				{					
					// Generating a unique hashkey
					nameValuePairs.add(new BasicNameValuePair("ugcUrl", slugify(record.get(2))));
				}

				// Setting some specific fields depending on the content type
				if (componentType.equals(COMMENTS)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
					nameValuePairs.add(new BasicNameValuePair("message", record.get(2)));

				}

				// Follows a user (followedId) for the user posting the request
				if (componentType.equals(FOLLOW)) {

					if (vBundleCommunitiesNotifications!=null && vBundleCommunitiesNotifications.compareTo(new Version("1.0.12"))<0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:follow"));
						nameValuePairs.add(new BasicNameValuePair("userId", "/social/authors/" + userName));
						nameValuePairs.add(new BasicNameValuePair("followedId", "/social/authors/" + record.get(2)));

					} else {

						logger.info("Ignoring FOLLOW with this version of AEM Communities");
						continue;

					}
				}

				// Notifications
				if (componentType.equals(NOTIFICATION)) {

					if (vBundleCommunitiesNotifications!=null && vBundleCommunitiesNotifications.compareTo(new Version("1.0.11"))>0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:updatesubscriptions"));
						nameValuePairs.add(new BasicNameValuePair("types", "following"));
						nameValuePairs.add(new BasicNameValuePair("types", "notification"));
						if (vBundleCommunitiesNotifications.compareTo(new Version("1.1.0"))>0)
							nameValuePairs.add(new BasicNameValuePair("types", "subscription"));
						nameValuePairs.add(new BasicNameValuePair("states", record.get(2).toLowerCase()));
						nameValuePairs.add(new BasicNameValuePair("states", record.get(3).toLowerCase()));
						if (vBundleCommunitiesNotifications.compareTo(new Version("1.1.0"))>0)
							nameValuePairs.add(new BasicNameValuePair("states", record.get(4).toLowerCase()));
						nameValuePairs.add(new BasicNameValuePair("subscribedId", record.get(5)));

					} else {

						logger.info("Ignoring NOTIFICATION with this version of AEM Communities");
						continue;

					}
				}

				// Notification preferences
				if (componentType.equals(NOTIFICATIONPREFERENCE)) {

					if (vBundleCommunitiesNotifications!=null && vBundleCommunitiesNotifications.compareTo(new Version("1.0.11"))>0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:updateUserPreference"));
						List<NameValuePair> otherNameValuePairs = buildNVP(hostname, port, adminPassword, null, record, 2);
						nameValuePairs.addAll(otherNameValuePairs);

					}

				}

				// Uploading Avatar picture
				if (componentType.equals(AVATAR)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:changeAvatar"));

					// Appending the path to the user profile to the target location
					String userJson = doGet(hostname, port,
							"/libs/granite/security/currentuser.json",
							getUserName(record.get(0)), getPassword(record.get(0), adminPassword),
							null);

					userHome = "";
					if (userJson!=null) {

						try {

							// Fetching the home property
							userHome = new JSONObject(userJson).getString("home");

						} catch (Exception e) {

							logger.error("Couldn't figure out home folder for user " + record.get(0));

						}

					}

				}

				// Assigning badge to user
				if (componentType.equals(BADGEASSIGN)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:assignBadge"));

					// Special case to accommodate re-factoring of badging images
					String value = record.get(3);
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP4))==0 || vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62))>0 ) {
						value = value.replaceAll("/jcr:content", "");
					}

					nameValuePairs.add(new BasicNameValuePair("badgeContentPath", value));

					// Appending the path to the user profile to the target location
					String userJson = doGet(hostname, altport,
							"/libs/granite/security/currentuser.json",
							getUserName(record.get(2)), getPassword(record.get(2), adminPassword),
							null);

					userHome = "";
					if (userJson!=null) {

						try {

							// Fetching the home property
							userHome = new JSONObject(userJson).getString("home");

						} catch (Exception e) {

							logger.error("Couldn't figure out home folder for user " + record.get(2));

						}

					}

				}

				// Uploading Badge image
				if (componentType.equals(BADGEIMAGE)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createBadge"));
					nameValuePairs.add(new BasicNameValuePair("jcr:title", record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("badgeDisplayName", record.get(3)));
					nameValuePairs.add(new BasicNameValuePair("badgeDescription", record.get(5)));
					addBinaryBody(builder, lIs, rr, "badgeImage", csvfile, record.get(ASSET_INDEX_NAME));

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

					nameValuePairs.add(new BasicNameValuePair("to", "/social/authors/" + record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("userId", "/social/authors/" + record.get(2)));
					nameValuePairs.add(new BasicNameValuePair("toId", ""));
					nameValuePairs.add(new BasicNameValuePair("serviceSelector", "/bin/community"));
					nameValuePairs.add(new BasicNameValuePair("redirectUrl", "../messaging.html"));
					nameValuePairs.add(new BasicNameValuePair("attachmentPaths", ""));
					nameValuePairs.add(new BasicNameValuePair(":operation", "social:createMessage"));
					nameValuePairs.add(new BasicNameValuePair("subject", record.get(3)));
					nameValuePairs.add(new BasicNameValuePair("content", record.get(4)));
					nameValuePairs.add(new BasicNameValuePair("sendMail", "Sending..."));

				}

				// Creates a forum post (or a reply)
				if (componentType.equals(FORUM)) {

					if (urlLevel == 0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createForumPost"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         
						nameValuePairs.add(new BasicNameValuePair("subject", subComponentType));

					} else if (subComponentType.equals(UGCREPLY)) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createForumPost"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         
						nameValuePairs.add(new BasicNameValuePair("subject", ""));

					}
				}

				// Creates a file or a folder
				if (componentType.equals(FILES)) {

					// Top level is always assumed to be a folder, second level files, and third and subsequent levels comments on files
					if (urlLevel==0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createFileLibraryFolder"));
						nameValuePairs.add(new BasicNameValuePair("name", subComponentType));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));		         

					} else if (subComponentType.equals(UGCREPLY)) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));

					}

				}

				// Creates a question, a reply or mark a reply as the best answer
				if (componentType.equals(QNA)) {

					if(vBundleCommunitiesEnablement==null) {
						logger.info("QnAs are not compatible with this version of AEM");
						continue;
					}

					if (urlLevel==0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createQnaPost"));
						nameValuePairs.add(new BasicNameValuePair("subject", subComponentType));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));

					} else if (subComponentType.equals(UGCREPLY)) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createQnaPost"));
						nameValuePairs.add(new BasicNameValuePair("subject", ""));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));

					}

				}

				// Creates a Blog article or a comment
				if (componentType.equals(JOURNAL) || componentType.equals(BLOG)) {

					if(vBundleCommunitiesEnablement==null) {
						logger.info("Blogs are not compatible with this version of AEM");
						continue;
					}

					if (urlLevel==0) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createJournalComment"));
						nameValuePairs.add(new BasicNameValuePair("subject", subComponentType));
						StringBuffer message = new StringBuffer("<p>" + record.get(3) + "</p>");

						//We might have more paragraphs to add to the blog or journal article
						for (int i=6; i < record.size();i++) {
							if (record.get(i).length()>0) {
								if (record.get(i).startsWith("isDraft")) {
									nameValuePairs.add(new BasicNameValuePair("isDraft", "true"));
								} else {
									message.append("<p>" + record.get(i) + "</p>");
								}
							}
						}

						//We might have some tags to add to the blog or journal article
						if (record.get(5).length()>0) {
							nameValuePairs.add(new BasicNameValuePair("tags", record.get(5)));		         				
						}

						nameValuePairs.add(new BasicNameValuePair("message", message.toString()));		         

					} else if (subComponentType.equals(UGCREPLY)) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createJournalComment"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));	
						nameValuePairs.add(new BasicNameValuePair("subject", ""));

					}

				}

				// Creates an Idea or a comment
				if (componentType.equals(IDEATION)) {

					if(!isCommunities61FP6orlater) {
						logger.info("Ideas are not compatible with this version of AEM");
						continue;
					}

					if (urlLevel==0) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createIdeationComment"));
						nameValuePairs.add(new BasicNameValuePair("subject", subComponentType));
						StringBuffer message = new StringBuffer("");

						//We might have more paragraphs to add to the idea
						for (int i=6; i < record.size();i++) {
							if (record.get(i).length()>0) {
								message.append("<p>" + record.get(i) + "</p>");
							}
						}

						if (record.get(5).equals("TRUE")) {
							nameValuePairs.add(new BasicNameValuePair("isDraft", "true"));
						} else {
							nameValuePairs.add(new BasicNameValuePair("isDraft", "false"));						
						}

						//We might have some tags to add to the blog or journal article
						if (record.get(3).length()>0) {
							nameValuePairs.add(new BasicNameValuePair("tags", record.get(5)));		         				
						}

						nameValuePairs.add(new BasicNameValuePair("message", message.toString()));	

					} else if (subComponentType.equals(UGCREPLY)) {

						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createIdeationComment"));
						nameValuePairs.add(new BasicNameValuePair("message", record.get(3)));	
						nameValuePairs.add(new BasicNameValuePair("subject", ""));

					}

				}

				// Taking care of moderation actions for all types
				if (urlLevel>=1 && !subComponentType.equals(UGCREPLY)) {

					if (subComponentType.equals(UGCPIN)  && !isCommunities61FP5orlater) {
						logger.warn("This feature is not supported by this version of AEM");
						continue;
					}

					if ((subComponentType.equals(UGCFEATURE) || subComponentType.equals(UGCLIKE)) && !isCommunities61FP6orlater) {
						logger.warn("This feature is not supported by this version of AEM");
						continue;
					}

					if (subComponentType.equals(UGCANSWER)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:selectAnswer"));
					}
					if (subComponentType.equals(UGCDENY)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:deny"));
					}
					if (subComponentType.equals(UGCFLAG)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:flag"));
						nameValuePairs.add(new BasicNameValuePair("social:flagformtext", "Marked as spam"));
						nameValuePairs.add(new BasicNameValuePair("social:doFlag", "true"));
					}
					if (subComponentType.equals(UGCFEATURE)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:featured"));
						nameValuePairs.add(new BasicNameValuePair("social:markFeatured", "true"));
					}
					if (subComponentType.equals(UGCPIN)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:pin"));
						nameValuePairs.add(new BasicNameValuePair("social:doPin", "true"));
					}
					if (subComponentType.equals(UGCUPVOTE)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
						nameValuePairs.add(new BasicNameValuePair("response", "1"));
						nameValuePairs.add(new BasicNameValuePair("tallyType", "Voting"));					
					}
					if (subComponentType.equals(UGCDOWNVOTE)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
						nameValuePairs.add(new BasicNameValuePair("response", "-1"));
						nameValuePairs.add(new BasicNameValuePair("tallyType", "Voting"));					
					}
					if (subComponentType.equals(UGCLIKE)) {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
						nameValuePairs.add(new BasicNameValuePair("response", "1"));
						nameValuePairs.add(new BasicNameValuePair("tallyType", "Liking"));							
					}

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
							nameValuePairs.add(new BasicNameValuePair("scf:included",record.get(4)));							
							if (record.size()>5 &&
									record.get(5).length()>0) {
								nameValuePairs.add(new BasicNameValuePair("scf:resourceType",record.get(5)));							
							} else {
								nameValuePairs.add(new BasicNameValuePair("scf:resourceType", "social/reviews/components/hbs/reviews"));
							}
						}
					} else {
						nameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
					}

				}

				// Creates a rating
				if (componentType.equals(RATINGS)) {

					nameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
					nameValuePairs.add(new BasicNameValuePair("tallyType", "Rating"));
					nameValuePairs.add(new BasicNameValuePair("response", subComponentType));

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
					if (rr==null) {
						File attachment = new File(csvfile.substring(0, csvfile.indexOf(".csv")) + File.separator + record.get(2));
						if (!attachment.exists()) {
							logger.error("Resource cannot be created as the referenced file is missing on the file system");
							continue;
						}
					} else {
						Resource res = rr.getResource(csvfile + "/attachments/" + record.get(2) + "/jcr:content");
						if (res==null) {
							logger.error("A non existent resource named " + record.get(2) + "was referenced");
							continue;
						}

					}

					String createResourceOpName = "se:createResource";
					String enablementType = "social/enablement/components/hbs/resource";

					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))>0) createResourceOpName="social:createResource";
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) createResourceOpName="social:createEnablementResourceModel";

					nameValuePairs.add(new BasicNameValuePair(":operation", createResourceOpName));

					List<NameValuePair> otherNameValuePairs = buildNVP(hostname, port, adminPassword, null, record, RESOURCE_INDEX_PROPERTIES);
					nameValuePairs.addAll(otherNameValuePairs);

					// Assignments only make sense when SCORM is configured
					if (vBundleCommunitiesSCORM==null) {
						nameValuePairs.remove("add-learners");
						nameValuePairs.remove("deltaList");
						logger.warn("SCORM not configured on this instance, not assigning a resource");
					}					

					// Special processing of lists with multiple users, need to split a String into multiple entries
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))>0) {
						// Author, contact and experts always make sense
						nameValuePairs = convertArrays(nameValuePairs,"add-learners");
						nameValuePairs = convertArrays(nameValuePairs,"resource-author");
						nameValuePairs = convertArrays(nameValuePairs,"resource-contact");
						nameValuePairs = convertArrays(nameValuePairs,"resource-expert");

					}

					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) {
						nameValuePairs.add(new BasicNameValuePair("sling:resourceType", "social/enablement/components/hbs/resource/model"));
						nameValuePairs = convertKeyName(nameValuePairs, "add-learners", "resource-assignees");
						nameValuePairs = convertKeyName(nameValuePairs, "jcr:title", "resource-name");
						nameValuePairs = convertKeyName(nameValuePairs, "resourceTags", "resource-tags");
						nameValuePairs = convertKeyName(nameValuePairs, "id", "resource-uid");
						enablementType = "resource";
					}

					nameValuePairs.add(new BasicNameValuePair("enablement-type", enablementType));

					// Adding the site
					nameValuePairs.add(new BasicNameValuePair("site", url[0]));

					// Building the cover image fragment
					if (record.get(RESOURCE_INDEX_THUMBNAIL).length()>0) {
						nameValuePairs.add(new BasicNameValuePair("cover-image", doThumbnail(rr, lIs, hostname, port, adminPassword, csvfile, record.get(RESOURCE_INDEX_THUMBNAIL), record.get(RESOURCE_INDEX_SITE), maxretries)));
					} else {
						nameValuePairs.add(new BasicNameValuePair("cover-image", ""));			
					}

					// Building the asset fragment
					String assetFileName = record.get(2);

					// Replacing videos with images in case it's a minimized installation
					int assetFileNamePos = assetFileName.indexOf(".mp4");
					if (assetFileNamePos>0 && minimize) {
						assetFileName = assetFileName.substring(0,assetFileNamePos) + ".jpg";
					}

					// Not processing SCORM files if the ignore option is there
					if (assetFileName.endsWith(".zip") && noenablement) {
						logger.info("Not processing a SCORM resource for this scenario");
						continue;
					}

					String coverPath = "/content/dam/resources/" + record.get(RESOURCE_INDEX_SITE) + "/" + record.get(2) + "/jcr:content/renditions/cq5dam.thumbnail.319.319.png";
					String coverSource = "dam";
					String assets = "[{\"cover-img-path\":\"" + coverPath + "\",\"thumbnail-source\":\"" + coverSource + "\",\"asset-category\":\"enablementAsset:dam\",\"resource-asset-name\":null,\"state\":\"A\",\"asset-path\":\"/content/dam/resources/" + record.get(RESOURCE_INDEX_SITE) + "/" + assetFileName + "\"}]";
					nameValuePairs.add(new BasicNameValuePair("assets", assets));

					// If it's a SCORM asset, making sure the output is available before processing
					if (assetFileName.endsWith(".zip")) {
						doWaitPath(hostname, port, adminPassword, "/content/dam/resources/" + record.get(RESOURCE_INDEX_SITE) + "/" + record.get(2) + "/output", maxretries);
					}

				}

				// Creates a learning path
				if (componentType.equals(LEARNING)) {

					if (vBundleCommunitiesSCORM==null || noenablement) {
						logger.info("Ignoring a learning path");
						continue;
					}

					String createResourceOpName = "se:editLearningPath";
					String enablementType = "social/enablement/components/hbs/learningpath";
					String resourceList = "learningpath-items";

					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0) createResourceOpName="social:editLearningPath";
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) createResourceOpName="social:createEnablementLearningPathModel";

					nameValuePairs.add(new BasicNameValuePair(":operation", createResourceOpName));

					List<NameValuePair> otherNameValuePairs = buildNVP(hostname, port, adminPassword, null, record, RESOURCE_INDEX_PROPERTIES);
					nameValuePairs.addAll(otherNameValuePairs);

					// Special processing of lists with multiple users, need to split a String into multiple entries
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0) {

						nameValuePairs = convertArrays(nameValuePairs,"add-learners");
						nameValuePairs = convertArrays(nameValuePairs,"resource-author");
						nameValuePairs = convertArrays(nameValuePairs,"resource-contact");
						nameValuePairs = convertArrays(nameValuePairs,"resource-expert");

					}

					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) {
						nameValuePairs.add(new BasicNameValuePair("sling:resourceType", "social/enablement/components/hbs/model/learningpath"));
						nameValuePairs = convertKeyName(nameValuePairs, "add-learners", "resource-assignees");
						nameValuePairs = convertKeyName(nameValuePairs, "jcr:title", "resource-name");
						nameValuePairs = convertKeyName(nameValuePairs, "resourceTags", "resource-tags");
						nameValuePairs = convertKeyName(nameValuePairs, "id", "resource-uid");
						enablementType = "learningpath";
						resourceList = "resourcelist";
					}

					nameValuePairs.add(new BasicNameValuePair("enablement-type", enablementType));

					// Adding the site
					nameValuePairs.add(new BasicNameValuePair("site", url[0]));

					// Building the cover image fragment
					if (record.get(RESOURCE_INDEX_THUMBNAIL).length()>0) {
						nameValuePairs.add(new BasicNameValuePair(vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))>0?"cover-image":"card-image", doThumbnail(rr, lIs, hostname, port, adminPassword, csvfile, record.get(RESOURCE_INDEX_THUMBNAIL), record.get(RESOURCE_INDEX_SITE), maxretries)));
					}

					// Building the learning path fragment
					StringBuffer assets = new StringBuffer("[");
					if (learningpaths.get(record.get(2)) != null) {

						assets.append("\"");
						ArrayList<String> paths = learningpaths.get(record.get(2));
						int i=0;
						for (String path : paths) {
							assets.append("{\\\"type\\\":\\\"linked-resource\\\",\\\"path\\\":\\\"");
							assets.append(path);
							assets.append("\\\"}");
							if (i++<paths.size()-1) { assets.append("\",\""); }
						}						
						assets.append("\"");

					} else {						
						logger.warn("No asset for this learning path");
					}

					assets.append("]");
					nameValuePairs.add(new BasicNameValuePair(resourceList , assets.toString()));

				}

				// Creates a calendar event
				if (componentType.equals(CALENDAR)) {

					if(vBundleCommunitiesEnablement==null) {
						logger.info("Calendars are not compatible with this version of AEM");
						continue;
					}

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

						// Let's see if we have tags
						if (record.size()>CALENDAR_INDEX_TAGS && record.get(CALENDAR_INDEX_TAGS).length()>0) {
							nameValuePairs.add(new BasicNameValuePair("tags", record.get(CALENDAR_INDEX_TAGS)));	
						}

						// Let's see if we have a cover image
						if (record.size()>CALENDAR_INDEX_THUMBNAIL && record.get(CALENDAR_INDEX_THUMBNAIL).length()>0) {
							addBinaryBody(builder, lIs, rr, "coverimage", csvfile, record.get(CALENDAR_INDEX_THUMBNAIL));
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
						componentType.equals(IDEATION) ||
						componentType.equals(QNA) ||
						(componentType.equals(JOURNAL)) || componentType.equals(BLOG)) && record.size()>4 && record.get(ASSET_INDEX_NAME).length()>0) {

					addBinaryBody(builder, lIs, rr, "file", csvfile, record.get(ASSET_INDEX_NAME));
				}

				// If it's a resource or a learning path, we need the path to the resource for subsequent publishing
				Map<String, String> elements = new HashMap<String, String>();
				String jsonElement = "location";
				String referrer = null;
				if (componentType.equals(RESOURCE) && vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP2))<=0) {
					jsonElement = "changes/argument";

				}
				if (componentType.equals(LEARNING) && vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT61FP3))<=0) {
					jsonElement = "path";
				}

				if (componentType.equals(RESOURCE) || componentType.equals(LEARNING)) {
					// Useful for debugging complex POST requests
					//printPOST(builder.build());	
				}

				if (!(componentType.equals(ASSET) || componentType.equals(BADGEASSIGN) || componentType.equals(MESSAGE) || componentType.equals(AVATAR))) {
					// Creating an asset doesn't return a JSON string
					elements.put(jsonElement, "");
					elements.put("response/resourceType", "");
					elements.put("response/id", "");
				}

				// This call generally returns the path to the content fragment that was just created
				int returnCode = Loader.doPost(hostname, port, getPostURL(componentType, subComponentType, url[urlLevel], userHome), userName, password, builder.build(), elements, null);

				// Again, Assets being a particular case
				if (!(componentType.equals(ASSET) || componentType.equals(AVATAR))) {
					location = elements.get(jsonElement);
					referrer = elements.get("response/id");
					if (Integer.parseInt(record.get(1)) == 0) {
						analyticsPagePath = location;
						resourceType = elements.get("response/resourceType");
					}
				}

				// In case of Assets or Resources, we are waiting for all workflows to be completed
				if (componentType.equals(ASSET) && returnCode<400) {
					doSleep(1000, "Pausing 1s after submitting asset");
					doWaitWorkflows(hostname, port, adminPassword, "asset", maxretries);
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
				if (componentType.equals(LEARNING) && !port.equals(altport) && location!=null && vBundleCommunitiesSCORM!=null) {

					// Publishing the learning path 
					List<NameValuePair> publishNameValuePairs = new ArrayList<NameValuePair>();

					String publishOpName = "se:publishEnablementContent";
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) publishOpName="social:publishEnablementLearningPathModel";
					publishNameValuePairs.add(new BasicNameValuePair(":operation", publishOpName));				

					publishNameValuePairs.add(new BasicNameValuePair("replication-action","activate"));
					logger.debug("Publishing a learning path from: " + location);					
					Loader.doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishNameValuePairs),
							null);

					// Waiting for the learning path to be published
					doWaitPath(hostname, altport, adminPassword, location, maxretries);

					// Decorate the resources within the learning path with comments and ratings, randomly generated
					ArrayList<String> paths = learningpaths.get(record.get(2));
					for (String path : paths) {
						doDecorate(hostname, altport, adminPassword, path, record, analytics, sitePagePath, vBundleCommunitiesEnablement);
					}						

				}

				// If it's an Enablement Resource that is not part of a learning path, a lot of things need to happen...
				// Step 1. If it's a SCORM resource, we wait for the SCORM metadata workflow to be complete before proceeding
				// Step 2. We publish the resource
				// Step 3. We set a new first published date on the resource (3 weeks earlier) so that reporting data is more meaningful
				// Step 4. We wait for the resource to be available on publish (checking that associated groups are available)
				// Step 5. We retrieve the json for the resource on publish to retrieve the Social endpoints
				// Step 6. We post ratings and comments for each of the enrollees on publish
				if (componentType.equals(RESOURCE) && !port.equals(altport) && location!=null && !location.equals("")) {

					// Wait for the workflows to be completed
					doWaitWorkflows(hostname, port, adminPassword, "resource", maxretries);

					String resourcePath = "/assets/asset";

					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) {
						resourcePath = "/se_assets/se_primary";
					}

					// Wait for the data to be fully copied
					doWaitPath(hostname, port, adminPassword, location + resourcePath, maxretries);

					// If we are dealing with a SCORM asset, we wait for the SCORM workflow to be completed before publishing the resource
					if (record.get(2).indexOf(".zip")>0) {

						// Wait for the output to be available
						doWaitPath(hostname, port, adminPassword, location + resourcePath + "/" + record.get(2) + "/output", maxretries);

						// Wait for 10 seconds
						doSleep(10000, "Processing a SCORM resource");

					}					

					// Wait for the workflows to be completed before publishing the resource
					doWaitWorkflows(hostname, port, adminPassword, "resource", maxretries);

					List<NameValuePair> publishNameValuePairs = new ArrayList<NameValuePair>();

					String publishOpName = "se:publishEnablementContent";
					if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))>0) publishOpName="social:publishEnablementResourceModel";
					publishNameValuePairs.add(new BasicNameValuePair(":operation",publishOpName));				

					publishNameValuePairs.add(new BasicNameValuePair("replication-action","activate"));
					logger.debug("Publishing a Resource from: " + location);					
					Loader.doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishNameValuePairs),
							null);

					// Waiting for the resource to be published
					doWaitPath(hostname, altport, adminPassword, location, maxretries);

					// Adding comments and ratings for this resource
					logger.debug("Decorating the resource with comments and ratings");
					doDecorate(hostname, altport, adminPassword, location, record, analytics, sitePagePath, vBundleCommunitiesEnablement);

					// Setting the first published timestamp so that reporting always comes with 3 weeks of data after building a new demo instance
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, REPORTINGDAYS);    
					List<NameValuePair> publishDateNameValuePairs = new ArrayList<NameValuePair>();
					publishDateNameValuePairs.add(new BasicNameValuePair("se_date-published", dateFormat.format(cal.getTime())));
					logger.debug("Setting the publish date for a resource at: " + location);
					doPost(hostname, port,
							location,
							userName, password,
							new UrlEncodedFormEntity(publishDateNameValuePairs),
							null);

				}

				// Generating Analytics when needed for the new fragment of UGC content
				if (analytics!=null && referrer!=null) {

					logger.debug("Component type: " + componentType + ", Analytics page path: " + analyticsPagePath + ", referrer: " + referrer);
					logger.debug("Analytics: " + analytics + ", resourceType: " + resourceType + ", sitePagePath: " + sitePagePath + ", userName: " + userName);
					if (analyticsPagePath != null && (componentType.equals(FORUM) || componentType.equals(FILES) || componentType.equals(QNA) || componentType.equals(BLOG) || componentType.equals(IDEATION) || componentType.equals(CALENDAR))) {
						logger.debug("level: " + Integer.parseInt(record.get(1)));
						if (Integer.parseInt(record.get(1)) == 0) {
							// We just created a UGC page that gets viewed. simulate view events.
							int views = new Random().nextInt(21) + 10;
							for (int i = 0; i < views; i++) {
								doUGCAnalytics(analytics, "event11", analyticsPagePath, resourceType, sitePagePath,
										userName, referrer);
							}
						} else {
							// We just posted to a UGC page (comment, reply, etc.). simulate post event.
							doUGCAnalytics(analytics, "event13", analyticsPagePath, resourceType, sitePagePath,
									userName, referrer);
						}
					}

				}

				// Closing all the input streams where applicable
				for (InputStream is : lIs) {

					try {
						is.close();
					} catch (IOException e) {
						//Omitted
					}

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage());

		}		

	}

	// This method adds a binary file to the future POST
	private static void addBinaryBody(MultipartEntityBuilder builder, LinkedList<InputStream> lIs, ResourceResolver rr, String field, String csvfile, String value) {
		if (rr==null) {
			File attachment = new File(csvfile.substring(0, csvfile.indexOf(".csv")) + File.separator + value);
			// Check for file existence
			if (attachment.exists()) {
				logger.debug("Adding file named " + value + " to POST");
				builder.addBinaryBody(field, attachment, getContentType(value), attachment.getName());
			} else {
				attachment = new File(csvfile.substring(0, csvfile.lastIndexOf("/")) + File.separator + "attachments" + File.separator + value);
				if (attachment.exists()) {
					builder.addBinaryBody(field, attachment, getContentType(value), attachment.getName());
				} else {
					logger.error("A non existent resource named " + value + " was referenced");
				}
			}
		} else {
			Resource res = rr.getResource(csvfile + "/attachments/" + value + "/jcr:content");
			if (res!=null) {
				logger.debug("Adding resource named " + value + " to POST");
				InputStream is = res.adaptTo(InputStream.class);
				lIs.add(is);
				builder.addBinaryBody(field, is, getContentType(value), value);			
			} else {
				logger.error("A non existent resource named " + value + " was referenced");
			}
		}
	}

	// This method evaluates where to make the POST
	private static String getPostURL(String componentType, String subComponentType, String urlLevel, String userHome) {

		String postURL = urlLevel;

		if (componentType.equals(AVATAR)) {
			postURL = urlLevel + userHome + "/profile";
		}

		if (componentType.equals(BADGEASSIGN)) {
			postURL = userHome + "/profile.social.json";
		}

		if (subComponentType.equals(UGCLIKE) || subComponentType.equals(UGCUPVOTE) || subComponentType.equals(UGCDOWNVOTE)) {
			int pos = postURL.indexOf(".social.json");
			if (pos>0) {
				postURL = postURL.substring(0,pos) + "/voting.social.json";
				logger.debug("VOTING URL: " + postURL);
			} else {
				postURL = postURL + "/voting.social.json";
			}

		}

		return postURL;

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

		// If not, return the default password 
		return defaultPassword;

	}

	// This method gets the root path for a record
	private static String getRootPath(String record) {

		String rootPath = getConfigurePath(record);
		int jcr = rootPath.indexOf("/jcr:content");
		if (jcr>0) {
			rootPath = rootPath.substring(0,jcr);
		}
		jcr = rootPath.indexOf("_jcr_content");
		if (jcr>0) {
			rootPath = rootPath.substring(0,jcr);
		}
		return rootPath;

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
			logger.info("Waiting " + ms + " milliseconds: " + message);
			Thread.sleep(ms);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

	}

	// This method POSTs a DAM file as an asset to be used as a thumbnail later on
	private static String doThumbnail(ResourceResolver rr, LinkedList<InputStream> lIs, String hostname, String port, String adminPassword, String csvfile, String filename, String sitename, int maxretries) {

		if (filename==null || filename.equals("")) return null;

		String pathToFile = "/content/dam/resources/resource-thumbnails/" + sitename + "/" + filename;

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setCharset(MIME.UTF8_CHARSET);
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);				
		addBinaryBody(builder, lIs, rr, "file", csvfile, filename);
		builder.addTextBody("fileName", filename, ContentType.create("text/plain", MIME.UTF8_CHARSET));

		logger.debug("Posting file for thumbnails with name: " + filename);

		Loader.doPost(hostname, port,
				pathToFile,
				"admin", adminPassword,
				builder.build(),
				null);

		doWaitWorkflows(hostname, port, adminPassword, "thumbnail", maxretries);

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
	private static void doDecorate(String hostname, String altport, String adminPassword, String location, CSVRecord record, String analytics, String rootPath, Version vBundleCommunitiesEnablement) {

		// Getting the JSON view of the resource
		String resourceJson = Loader.doGet(hostname, altport,
				location + ".social.json",
				"admin", adminPassword,
				null);

		// Generating random ratings and comments for the resource for each of the enrolled users
		try {

			JSONObject resourceJsonObject = new JSONObject(resourceJson);
			String resourceRatingsEndpoint = location + "/se_social/se_ratings.social.json";
			String resourceCommentsEndpoint = location + "/se_social/se_discussion.social.json";
			String assetPath = "assetProperties";

			if (vBundleCommunitiesEnablement.compareTo(new Version(ENABLEMENT62FP1))<=0) {

				resourceRatingsEndpoint = resourceJsonObject.getString("ratingsEndPoint") + ".social.json";
				resourceCommentsEndpoint = resourceJsonObject.getString("commentsEndPoint")  + ".social.json";

			} else {

				assetPath = "primaryAsset";

			}

			String resourceID = resourceJsonObject.getString("id");
			String resourceType = resourceJsonObject.getJSONObject(assetPath).getString("type");
			String referer = "http://" + hostname + ":" + altport + rootPath + "/" + record.get(RESOURCE_INDEX_SITE) + "/en" + (record.get(RESOURCE_INDEX_FUNCTION).length()>0?("/" + record.get(RESOURCE_INDEX_FUNCTION)):"") + ".resource.html" + resourceID; 

			// Looking for the list of enrolled users
			for (int i=0;i<record.size()-1;i=i+1) {

				if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).equals("deltaList")) {

					JSONObject enrolledJsonObject = new JSONObject(record.get(i+1));
					Iterator<?> iter = enrolledJsonObject.keys();
					while (iter.hasNext()) {

						String key = (String) iter.next();
						logger.debug("New Resource Enrollee: " + key);

						// Getting information about this assignment (user or group?)
						String isGroup = doWait(hostname, altport,"admin", adminPassword, key, 1);

						if (isGroup==null) {

							// Always generating a page view event
							if (Math.random() < 0.90) doAnalytics(analytics, "event11", referer, resourceID, resourceType);

							// Sometimes generating a video view event
							if (Math.random() < 0.75 && resourceType.equals("video/mp4")) doAnalytics(analytics, "event2", referer, resourceID, resourceType);

							// Posting ratings and comments
							if (Math.random() < 0.50) doRatings(hostname, altport, adminPassword, key, resourceRatingsEndpoint, referer, resourceID, resourceType, analytics);
							if (Math.random() < 0.35) doComments(hostname, altport, adminPassword, key, resourceCommentsEndpoint, referer, resourceID, resourceType, analytics);

						} else {

							List<NameValuePair>  groupNameValuePairs = new ArrayList<NameValuePair>();
							groupNameValuePairs.add(new BasicNameValuePair("groupId", key));
							groupNameValuePairs.add(new BasicNameValuePair("includeSubGroups", "true"));
							String memberList = Loader.doGet(hostname, altport,
									"/content/community-components/en/communitygroupmemberlist/jcr:content/content/communitygroupmember.social.0.100.json",
									"admin", adminPassword,
									groupNameValuePairs);

							JSONArray memberJsonArray = new JSONObject(memberList).getJSONArray("items");
							for (int j=0; j<memberJsonArray.length();j++) {
								JSONObject memberJsonObject = memberJsonArray.getJSONObject(j);
								String email = memberJsonObject.getString("authorizableId");
								if (email!=null) {

									// Always generating a page view event
									if (Math.random() < 0.90) doAnalytics(analytics, "event11", referer, resourceID, "video/mp4");

									// Sometimes generating a video view event
									if (Math.random() < 0.75 && resourceType.equals("video/mp4")) doAnalytics(analytics, "event2", referer, resourceID, resourceType);

									if (Math.random() < 0.50) doRatings(hostname, altport, adminPassword, email, resourceRatingsEndpoint, referer, resourceID, resourceType, analytics);
									if (Math.random() < 0.35) doComments(hostname, altport, adminPassword, email, resourceCommentsEndpoint, referer, resourceID, resourceType, analytics);
								}

							} // For each group member

						} // If there's a principal name

					} // For each enrollee

					break; // only one possible deltaList attribute for resource and learning paths

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This methods POSTS analytics events for AEM Assets Insights
	private static void doAssetsAnalytics(String analytics, String event, String evar, String assetID, String linkType, String linkName) {

		if (analytics!=null && event!=null && evar!=null) {

			StringBuffer sb =
					new StringBuffer("<?xml version=1.0 encoding=UTF-8?><request><sc_xml_ver>1.0</sc_xml_ver>");
			sb.append("<events>" + event + "</events>");
			sb.append("<pageURL>http://communities.geometrixx.com</pageURL>");
			sb.append("<" + evar + ">" + assetID + "</" + evar + ">");
			sb.append("<linkType>" + linkType + "</linkType>");
			sb.append("<linkName>" + linkName + "</linkName>");
			sb.append("<visitorID>demomachine</visitorID>");
			sb.append("<reportSuiteID>" + analytics.substring(0, analytics.indexOf(".")) + "</reportSuiteID>");
			sb.append("</request>");

			postAnalytics(analytics, sb.toString());

		}
	}

	// This methods creates an Analytics event for UGC
	private static void doUGCAnalytics(String analytics, String event, String path, String type, String sitePath,
			String user, String referrer) {

		if (analytics != null && path != null && type != null && sitePath != null && user != null && event != null && referrer != null) {

			StringBuffer sb =
					new StringBuffer("<?xml version=1.0 encoding=UTF-8?><request><sc_xml_ver>1.0</sc_xml_ver>");
			sb.append("<events>" + event + "</events>");
			sb.append("<pageURL>" + referrer + "</pageURL>");
			sb.append("<pageName>" + referrer.replaceAll("/",":") + "</pageName>");
			sb.append("<evar10>" + path + "</evar10>");
			sb.append("<evar7>" + type + "</evar7>");
			sb.append("<evar13>" + sitePath + "</evar13>");
			sb.append("<evar9>" + user + "</evar9>");
			sb.append("<visitorID>demomachine</visitorID>");
			sb.append("<reportSuiteID>" + analytics.substring(0, analytics.indexOf(".")) + "</reportSuiteID>");
			sb.append("</request>");

			postAnalytics(analytics, sb.toString());

		}

	}

	// This methods creates an Analytics event for Resources
	private static void doAnalytics(String analytics, String event, String pageURL, String resourcePath, String resourceType) {

		if (analytics!=null && pageURL!=null && resourcePath!=null && resourceType!=null && event!=null) {

			try {

				//
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

				postAnalytics(analytics, sb.toString());				

			} catch (Exception ex) {

				logger.error(ex.getMessage());

			}		

		}

	}

	// This method POSTS an event body to Adobe Analytics
	private static void postAnalytics(String analytics, String body) {

		if (analytics!=null && body!=null) {

			URLConnection urlConn = null;
			DataOutputStream printout = null;
			BufferedReader input = null;
			String tmp = null;
			try {

				logger.debug("New Analytics Event: " + body);

				URL sitecaturl = new URL( "http://" + analytics );

				urlConn = sitecaturl.openConnection();
				urlConn.setDoInput( true );
				urlConn.setDoOutput( true );
				urlConn.setUseCaches( false );
				urlConn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );

				printout = new DataOutputStream(urlConn.getOutputStream());

				printout.writeBytes( body );
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

				logger.warn("Connectivity error: " + ex.getMessage());

			} finally {

				try {
					input.close();
					printout.close();					
				} catch (Exception e) {
					// Omitted
				}

			}

		}

	}

	// This method POSTS a rating and comments
	private static void doRatings(String hostname, String altport, String adminPassword, String key, String resourceRatingsEndpoint, String referer, String resourceID, String resourceType, String analytics) {

		try {

			// Posting a Rating for this resource
			List<NameValuePair> ratingNameValuePairs = new ArrayList<NameValuePair>();
			ratingNameValuePairs.add(new BasicNameValuePair(":operation", "social:postTallyResponse"));
			ratingNameValuePairs.add(new BasicNameValuePair("tallyType", "Rating"));
			int randomRating = (int) Math.ceil(Math.random()*5);
			logger.debug("Randomly Generated Rating: " + randomRating);
			ratingNameValuePairs.add(new BasicNameValuePair("referer", referer));
			ratingNameValuePairs.add(new BasicNameValuePair("response", String.valueOf(randomRating)));
			doPost(hostname, altport,
					resourceRatingsEndpoint + (resourceRatingsEndpoint.indexOf(".social.json")>0?"":".social.json"),
					key, getPassword(key, adminPassword),
					new UrlEncodedFormEntity(ratingNameValuePairs),
					null,
					referer);

			doAnalytics(analytics, "event4", referer, resourceID, resourceType);

		} catch(Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This methods POSTS a rating and comments
	private static void doComments(String hostname, String altport, String adminPassword, String key, String resourceCommentsEndpoint, String referer, String resourceID, String resourceType, String analytics) {

		try {

			// Posting a Comment for this resource
			int randomComment = (int) Math.ceil(Math.random()*5);
			logger.debug("Randomly Posting a Comment " + resourceCommentsEndpoint);
			List<NameValuePair> commentNameValuePairs = new ArrayList<NameValuePair>();
			commentNameValuePairs.add(new BasicNameValuePair(":operation", "social:createComment"));
			commentNameValuePairs.add(new BasicNameValuePair("message", comments[randomComment-1]));
			commentNameValuePairs.add(new BasicNameValuePair("id", "nobot"));
			doPost(hostname, altport,
					resourceCommentsEndpoint,
					key, getPassword(key, adminPassword),
					new UrlEncodedFormEntity(commentNameValuePairs),
					null,
					referer);

			doAnalytics(analytics, "event13", referer, resourceID, resourceType);

		} catch(Exception e) {

			logger.error(e.getMessage());

		}

	}

	// This method POSTs a request to the server, returning the location JSON attribute, when available
	private static String doPost(String hostname, String port, String url, String user, String password,
			HttpEntity entity, String lookup) {

		String returnedString = null;
		Map<String, String> elements = new HashMap<String, String>();
		if (lookup!=null) elements.put(lookup, "");
		doPost(hostname, port, url, user, password, entity, elements, null);
		if (lookup!=null) return elements.get(lookup);
		return returnedString;

	}

	private static int doPost(String hostname, String port, String url, String user, String password,
			HttpEntity entity, Map<String, String> elements, String referer) {
		String jsonElement = null;

		if (hostname==null || port==null || url==null || user==null || password==null) {
			logger.error("Can't POST with requested parameters, one is null");
			return 500;
		}

		int returnCode = 404;

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = 
					HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				String postUrl = "http://" + hostname + ":" + port + url;
				logger.debug("Posting request as " + user + " to " + postUrl);

				// Preparing a standard POST HTTP command
				HttpPost request = new HttpPost(postUrl);
				request.setEntity(entity);
				if (!entity.getContentType().toString().contains("multipart")) {
					request.addHeader("content-type", "application/x-www-form-urlencoded");
				}
				request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
				request.addHeader("Origin", postUrl);
				if (referer != null) {
					request.addHeader("Referer", referer);
				}

				// Sending the HTTP POST command
				CloseableHttpResponse response = httpClient.execute(target, request, localContext);
				try {
					returnCode = response.getStatusLine().getStatusCode();
					String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
					if (returnCode>=500) {
						logger.error("POST return code: " + returnCode);
						logger.debug(responseString);
						return returnCode;
					}
					if (returnCode>=400) {
						logger.warn("POST return code: " + returnCode);
						logger.debug(responseString);
						return returnCode;
					}
					if (elements==null)
						return returnCode;
					Set<String> keys = elements.keySet();
					if (!isJSONValid(responseString) && keys.size()>0) {
						logger.warn("POST operation didn't return a JSON string, hence cannot extract requested value");
						return returnCode;
					}
					for (String lookup : keys) {
						if (lookup != null) {
							int separatorIndex = lookup.indexOf("/");
							if (separatorIndex > 0) {

								// Grabbing element in a nested element
								Object object =
										new JSONObject(responseString).get(lookup.substring(0, separatorIndex));
								if (object != null) {

									if (object instanceof JSONArray) {

										JSONArray jsonArray = (JSONArray) object;
										if (jsonArray.length() == 1) {
											JSONObject jsonObject = jsonArray.getJSONObject(0);
											jsonElement = jsonObject.getString(lookup.substring(1 + separatorIndex));
											//logger.debug("JSON value (jsonArray) returned is " + jsonElement);
										}

									} else if (object instanceof JSONObject) {

										JSONObject jsonobject = (JSONObject) object;
										jsonElement = jsonobject.getString(lookup.substring(1 + separatorIndex));
										//logger.debug("JSON value (jsonObject) returned is " + jsonElement);

									}
								}

							} else {
								// Grabbing element at the top of the JSON response
								jsonElement = new JSONObject(responseString).getString(lookup);
								//logger.debug("JSON (top) value returned is " + jsonElement);
							}
						}
						elements.put(lookup, jsonElement);
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
		return returnCode;
	}

	// Simple POST returning the response as a string
	private static String doPost(String hostname, String port, String url, String user, String password) {

		String responseString = null;
		if (hostname==null || port==null || url==null || user==null || password==null) {
			logger.error("Can't POST with requested parameters, one is null");
			return responseString;
		}

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = 
					HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				String postUrl = "http://" + hostname + ":" + port + url;

				// Preparing a standard POST HTTP command
				HttpPost request = new HttpPost(postUrl);

				// Sending the HTTP POST command
				CloseableHttpResponse response = httpClient.execute(target, request, localContext);
				try {
					int returnCode = response.getStatusLine().getStatusCode();
					responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
					if (returnCode>=500) {
						logger.fatal("Server error" + responseString);
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
		return responseString;
	}


	// This method DELETES a request to the server
	private static void doDelete(String hostname, String port, String url, String user, String password) {

		try {

			HttpHost target = new HttpHost(hostname, Integer.parseInt(port), "http");
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(user, password));
			CloseableHttpClient httpClient = 
					HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();

			try {

				// Adding the Basic Authentication data to the context for this command
				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(target, basicAuth);
				HttpClientContext localContext = HttpClientContext.create();
				localContext.setAuthCache(authCache);

				// Composing the root URL for all subsequent requests
				String postUrl = "http://" + hostname + ":" + port + url;
				logger.debug("Deleting request as " + user + " to " + postUrl);
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

	// This method WAITs of CHECKs for a group on a server
	private static String doWait(String hostname, String port, String user, String password, String group, int max) {

		String groupList = null;

		if (group==null || group.length()==0) {
			logger.warn("Group name was not provided");
			return null;
		}

		if (hostname!=null && port!=null && password!=null && user!=null && (group!=null && group.length()>0)) {

			int retries = 0;

			// Retrieving the list of groups for the newly created site, using alternate port (publish in general)
			List<NameValuePair>  nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("type", "rep:Group"));
			nameValuePairs.add(new BasicNameValuePair("property", "rep:authorizableId"));
			nameValuePairs.add(new BasicNameValuePair("property.value", group));

			while (retries < max) {

				groupList = Loader.doGet(hostname, port,
						"/bin/querybuilder.json",
						user, password,
						nameValuePairs);

				if (groupList.indexOf("\"results\":1")>0) {

					logger.debug("Group " + group + " was found on " + port);
					return groupList;

				} else {

					retries++;
					if (retries < max)
						doSleep(1000,"Group " + group + " not found yet, pausing");

				}

			}

			if (retries==max && max>1) {
				logger.warn("Group " + group +" was not found as expected");
			}

		}

		return null;

	}

	// This method runs a QUERY against an AEM instance
	@SuppressWarnings("unused")
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

			query = nodeList;
		}

		return query;
	}

	// This method waits for all running workflows to be completed
	private static void doWaitWorkflows(String hostname, String port, String adminPassword, String context, int maxretries) {

		int retries = 0;
		while (retries++ < maxretries) {

			String runningWorkflows = doPost(hostname, port, "/system/console/jmx/com.adobe.granite.workflow%3Atype%3DMaintenance/op/listRunningWorkflowsPerModel/", "admin", adminPassword);
			if (runningWorkflows!=null) {

				Matcher m = Pattern.compile("<td>([0-9]+)</td>").matcher(runningWorkflows);
				if (m.find()) {
					doSleep(2000, m.group(1) + " running workflows for " + context + " were found, waiting for completion, attempt " + retries);
					logger.debug(runningWorkflows);
				} else {
					break; // no more running workflows
				}

			} else {

				doSleep(2000, "Cannot get the list of running workflows for " + context + ", attempt " + retries);

			}

		}

	}

	// This method WAITS for a node to be available
	private static void doWaitPath(String hostname, String port, String adminPassword, String path, int maxretries) {

		int retries = 0;
		while (retries++ < maxretries) {

			if (isResourceAvailable(hostname, port, adminPassword, path)) {

				logger.debug("Node is found for: " + path + " on port: " + port);
				return;

			} else {

				doSleep(1000, "Node not found for: " + path + " on port: " + port + " attempt " + retries);

			}

		}

		logger.error("Node was never found - something is wrong!");

	}

	// This method verifies if a resource is available or not on the server by fetching the JSON selector for a given path
	private static boolean isResourceAvailable(String hostname, String port, String password, String path) {

		boolean isAvailable = false;

		if (path==null || hostname==null || port==null || password==null) return false;

		int pos = path.lastIndexOf("/");
		int pos2 = path.indexOf(".",pos);

		if (pos2>0) path = path.substring(0,pos2);

		if (path.endsWith(".json")) path = path.replaceAll("\\.json", "");		
		path = path + ".json";

		String json = doGet(hostname, port, path, "admin", password, null);

		if (json!=null) isAvailable = true;

		return isAvailable;

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
			CloseableHttpClient httpClient = 
					HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();

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
				logger.debug("Getting request at " + uri.toString());
				HttpGet httpget = new HttpGet(uri);
				CloseableHttpResponse response = httpClient.execute(httpget, localContext);
				if (response.getStatusLine().getStatusCode()==200) {
					try {     
						rawResponse = EntityUtils.toString(response.getEntity(), "UTF-8");   
					} catch (Exception ex) {
						logger.error(ex.getMessage());
					} finally {
						response.close();
					}
				} else {
					logger.debug("GET return code: " + response.getStatusLine().getStatusCode());
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

	// This method creates a list of name value pairs from an existing one, converting a JSON array into multiple individual entries
	private static List<NameValuePair> convertKeyName(List<NameValuePair> nameValuePairs, String oldkey, String newkey) {

		List<NameValuePair> newNameValuePairs = new ArrayList<NameValuePair>();		
		for (NameValuePair nvp : nameValuePairs) {
			if (nvp.getName().equals(oldkey)) {
				newNameValuePairs.add(new BasicNameValuePair(newkey, nvp.getValue())); 
			} else {
				newNameValuePairs.add(nvp);
			}
		}

		return newNameValuePairs;
	}


	// This method builds a list of NVP for a subsequent Sling post
	private static List<NameValuePair> buildNVP(String hostname, String port, String adminPassword, String path, CSVRecord record, int start) {

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		List<String> alreadyLoaded = new ArrayList<String>();

		nameValuePairs.add(new BasicNameValuePair("_charset_", "UTF-8"));

		for (int i=start;i<record.size()-1;i=i+2) {

			if (record.get(i)!=null && record.get(i+1)!=null && record.get(i).length()>0) {

				// We have a non String hint to pass to the POST Servlet
				String name = record.get(i);
				String value = record.get(i+1);
				if (value.equals("TRUE")) { value = "true"; }
				if (value.equals("FALSE")) { value = "false"; }		

				// If it's a reference to a resource, let's make sure it's available first
				if (name.startsWith("cq:cloudserviceconfigs") && !isResourceAvailable(hostname, port, adminPassword, value) ) {
					logger.warn("Resource " + value + " is not available");
					continue;
				}

				// We are adding to an existing property supporting multiple values (might not exist yet)
				int addition = name.indexOf("+");
				if (addition>0 && path!=null) {

					name = name.substring(0, addition);

					if (!alreadyLoaded.contains(name)) {
						alreadyLoaded.add(name);
						// Getting the existing values
						String existingValues = doGet(hostname, port, path + ".json", "admin", adminPassword, null);
						try {
							JSONObject propertyJson = new JSONObject(existingValues.trim());
							Iterator<?> keys = propertyJson.keys();
							while( keys.hasNext() ) {

								String key = (String)keys.next();
								if (name.startsWith(key)) {
									JSONArray propertyList = (JSONArray) propertyJson.get(key);
									for (int j=0;j<propertyList.length();j++) {
										String propertyValue = (String) propertyList.get(j);
										nameValuePairs.add(new BasicNameValuePair(name, propertyValue));
									}
								}

							}

						} catch (Exception e) {
							logger.error(e.getMessage());
						}
					}

					// Indicating it's a property with multiple values
					BasicNameValuePair bvHint = new BasicNameValuePair(name + "@TypeHint", "String[]");
					if (!nameValuePairs.contains(bvHint)) {
						nameValuePairs.add(bvHint);					            							            		
					}

					BasicNameValuePair bvOption = new BasicNameValuePair(name, value);
					if (!nameValuePairs.contains(bvOption)) {
						nameValuePairs.add(bvOption);					            							            		
					}

					continue;

				}

				// We default to String unless specified otherwise
				int hint = name.indexOf("@");
				if (hint>0) {
					nameValuePairs.add(new BasicNameValuePair(name.substring(0,hint) + "@TypeHint", name.substring(1+hint)));					            		
					name = name.substring(0,hint);
				} else {
					nameValuePairs.add(new BasicNameValuePair(name + "@TypeHint", "String"));					            							            		
				}

				// We have multiple values to pass to the POST servlet, e.g. for a String[]
				int multiple = value.indexOf("|");
				if (multiple>0) {
					List<String> values = null;
					if (value.indexOf("~")>0) { 
						values = Arrays.asList(value.split("~", -1));
					} else {
						values = Arrays.asList(value.split("\\|", -1));
					}
					for (String currentValue : values) {
						nameValuePairs.add(new BasicNameValuePair(name, currentValue));	
					}
				} else {	
					nameValuePairs.add(new BasicNameValuePair(name, value));					            							            		
				}

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

	// Creating normalized and fixed URLs for the UGC posts
	public static String slugify(String input) {
		return Normalizer.normalize(input, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "")
				.replaceAll("[^ \\w]", "").trim()
				.replaceAll("\\s+", "-").toLowerCase(Locale.ENGLISH);
	}

	// Checking if a string is valid JSON
	public static boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	// Converting a Title into a Name
	public static String title2name(String title) {
		return title.replaceAll(" ","_").replaceAll("\\.","_").toLowerCase();
	}

	// Printing the details of a POST request
	public static void printPOST(HttpEntity entity) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			entity.writeTo(out);
			String string = out.toString();
			logger.debug(string);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
