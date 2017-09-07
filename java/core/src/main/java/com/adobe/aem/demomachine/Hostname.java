/*******************************************************************************
 * Copyright 2017 Adobe Systems Incorporated.
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
package com.adobe.aem.demomachine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Properties;

public class Hostname {

	public static void main(String[] args) {

		String hostname = "localhost";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}

	    try {
	        Properties props = new Properties();
	        props.setProperty("demo.hostname", hostname);
	        File f = new File("hostname.properties");
	        OutputStream out = new FileOutputStream( f );
	        props.store(out, "Detected hostname");
	    }
	    catch (Exception e ) {
	        e.printStackTrace();
	    }
		
	}

	public static boolean isReachable(String host, String port) {

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
