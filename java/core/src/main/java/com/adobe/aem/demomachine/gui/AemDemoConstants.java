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
package com.adobe.aem.demomachine.gui;

public class AemDemoConstants {

	public static final String OPTIONS_STORES = "demo.store.options";
	public static final String OPTIONS_SRPS = "demo.srp.options";
	public static final String OPTIONS_TOPOLOGIES = "demo.type.options";

	public static final String OPTIONS_STORES_DEFAULT = "demo.store";
	public static final String OPTIONS_SRPS_DEFAULT = "demo.srp";
	public static final String OPTIONS_TOPOLOGIES_DEFAULT = "demo.type";
	public static final String OPTIONS_JAR_DEFAULT = "demo.jar";
	public static final String OPTIONS_BUILD_DEFAULT = "demo.build";

	public static final String OPTIONS_DOWNLOAD = "demo.download";
	public static final String OPTIONS_WEBDOWNLOAD = "demo.download.demomachine.all";
	public static final String OPTIONS_DEMODOWNLOAD = "demo.download.demomachine";
	public static final String OPTIONS_DOCUMENTATION = "demo.documentation";
	public static final String OPTIONS_SCRIPTS = "demo.scripts";

	public static final String[] INSTANCE_ACTIONS = new String[] {"start","restore","backup","uninstall", "details"};
	public static final String[] CLEANUP_ACTIONS = new String[] {"uninstall"};
	public static final String[] STOP_ACTIONS = new String[] {"uninstall","stop"};
	public static final String[] BUILD_ACTIONS = new String[] {"demo", "create"};
	public static final String BUILD_ACTION = "create";

	public static final String PASSWORD = "******";
	public static final String HR = "-------------------------------";
	
	public static final String Credits = "Welcome to the AEM Demo Machine! Everyone knows there isn't any good demo without a bouncing scrolltext... The AEM Demo Machine is an OpenSource project. You are welcome to contribute as did the people listed later. Don't forget to \"git pull\" the latest changes often. Also make sure to check the latest updates to the Wiki documentation at https://github.com/Adobe-Marketing-Cloud/aem-demo-machine/wiki . Many thanks to the following people for contributing, one way or the other, to the AEM Demo Machine: Gerd Handke, Cedric Huesler, Greg Klebus, Gabriel Walt, Martin Buergi, Scott Date, Randah McKinnie, Don Walling, Abhinav Chakravarty, Nikhil Vasudeva, Chris Gatihi, Michael Marth, Mark Szulc, Kyle Chau, Marcel Boucher, Michael Point, Brandon Tan, Raul Ugarte, Christophe Loffler, Samuel Blin, Sethu Iyer, Mark Frazer. Designed and built with all the love in the world by @bdecoatpont. Copyright - well, there's actually no copyright. Let's wrap and happy AEM demoing!";

	public static final String[][] demoPaths = new String[][]{
			  { "0", "", "AEM Demo Machine", "false", "packages"},
			  { "1", "dist/apps", "AEM Apps Demo Add-ons", "true", "apps" },
			  { "2", "dist/apps-packages", "AEM Apps Packages", "true", "apps" },
			  { "3", "dist/assets", "AEM Assets Demo Add-ons", "true", "assets" },
			  { "4", "dist/assets-packages", "AEM Assets Packages", "true", "assets" },
			  { "5", "dist/community/featurepacks", "AEM Communities Packages", "true", "communities" },
			  { "6", "dist/sites", "AEM Sites Demo Add-ons", "true", "sites" },
			  { "7", "dist/sites-packages", "AEM Sites Packages", "true", "sites" },
			  { "8", "dist/forms", "AEM Forms Demo Add-ons", "true", "forms" },
			  { "9", "dist/forms-packages", "AEM Forms Packages", "true", "forms" },
			  { "10", "dist/hotfixes", "AEM Hotfixes", "true", "hotfixes" },
			  { "11", "dist/we-retail", "AEM We-Retail", "true", "weretail" },
			  { "12", "dist/livefyre", "AEM Livefyre", "true", "livefyre" },
			};
	
}
