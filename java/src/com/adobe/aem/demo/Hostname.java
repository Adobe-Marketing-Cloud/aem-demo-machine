package com.adobe.aem.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class Hostname {

	static Logger logger = Logger.getLogger(Hostname.class);

	public static void main(String[] args) {

		String hostname = "localhost";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
			logger.debug("Hostname resolved to: " + hostname);
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		}
		
		System.out.println(hostname);
		
	}

}
