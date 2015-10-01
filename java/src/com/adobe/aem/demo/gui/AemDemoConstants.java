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
package com.adobe.aem.demo.gui;

public class AemDemoConstants {

	public static final String OPTIONS_STORES = "demo.store.options";
	public static final String OPTIONS_SRPS = "demo.srp.options";
	public static final String OPTIONS_DEMOCONFIGS = "demo.configs.options";
	public static final String OPTIONS_TOPOLOGIES = "demo.type.options";

	public static final String OPTIONS_STORES_DEFAULT = "demo.store";
	public static final String OPTIONS_SRPS_DEFAULT = "demo.srp";
	public static final String OPTIONS_DEMOCONFIGS_DEFAULT = "demo.configs";
	public static final String OPTIONS_TOPOLOGIES_DEFAULT = "demo.type";
	public static final String OPTIONS_JAR_DEFAULT = "demo.jar";
	public static final String OPTIONS_BUILD_DEFAULT = "demo.build";

	public static final String OPTIONS_DOWNLOAD = "demo.download";
	public static final String OPTIONS_WEBDOWNLOAD = "demo.download.demomachine.all";
	public static final String OPTIONS_DEMODOWNLOAD = "demo.download.demomachine";
	public static final String OPTIONS_DOCUMENTATION = "demo.documentation";
	public static final String OPTIONS_SCRIPTS = "demo.scripts";

	public static final String[] INSTANCE_ACTIONS = new String[] {"start","restore","backup","uninstall"};
	public static final String[] BUILD_ACTIONS = new String[] {"install","demo", "demo_communities", "demo_apps", "demo_assets", "demo_commerce", "demo_sites", "demo_forms", "demo_kitchensink"};
	public static final String[] CLEANUP_ACTIONS = new String[] {"uninstall"};
	public static final String[] STOP_ACTIONS = new String[] {"uninstall","stop"};

	public static final String PASSWORD = "******";
	
}
