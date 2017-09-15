package com.adobe.aem.demomachine;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class CypherUtils {

	static Logger logger = Logger.getLogger(CypherUtils.class);

	public static void main(String[] args) throws IOException {

		String key = null;
		String inputFileName = null;
		String outputFileName = null;

		// Command line options for this tool
		Options options = new Options();
		options.addOption("f", true, "Input Filename");
		options.addOption("t", true, "Output Filename");
		options.addOption("k", true, "Key");
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse( options, args);

			if(cmd.hasOption("f")) {
				inputFileName = cmd.getOptionValue("f");
			}

			if(cmd.hasOption("t")) {
				outputFileName = cmd.getOptionValue("t");
			}

			if(cmd.hasOption("k")) {
				key = cmd.getOptionValue("k");
			}

			if (inputFileName==null || outputFileName==null || key==null) {
				System.out.println("Command line parameters: -f inputFileName -t outputFileName -k key");
				System.exit(-1);
			}

		} catch (ParseException ex) {

			logger.error(ex.getMessage());

		}

		// Loading the file content
		String content = readFile(inputFileName, Charset.defaultCharset());
		String cypherContent = encrypt(content, key);

		// Creating the out put file
		PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
		writer.println(cypherContent);
		writer.close();

	}

	public static String encrypt(final String text, final String key) {
		return Base64.encodeBase64String(xor(text.getBytes(), key));
	}

	public static String decrypt(final String hash, final String key) {
		try {
			return new String(xor(Base64.decodeBase64(hash.getBytes()), key), "UTF-8");
		} catch (java.io.UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static byte[] xor(final byte[] input, final String key) {
		final byte[] output = new byte[input.length];
		final byte[] secret = key.getBytes();
		int spos = 0;
		for (int pos = 0; pos < input.length; ++pos) {
			output[pos] = (byte) (input[pos] ^ secret[spos]);
			spos += 1;
			if (spos >= secret.length) {
				spos = 0;
			}
		}
		return output;
	}

	private	static String readFile(String path, Charset encoding) 
			throws IOException 
			{

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);

			}

}
