package com.adobe.aem.demo.gui;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class AemDemoDownload implements Runnable {

	private AemDemo aemDemo;
	private long currentSize;
	
    public AemDemoDownload( AemDemo aemDemo ) {

       this.aemDemo = aemDemo;

    }

	public void run() {
    	
		if (aemDemo.getDownloadInProgress()) {

		    File theNewestFile = null;
		    File dir = new File(aemDemo.getBuildFile().getParentFile().getAbsolutePath() + File.separator + "dist" + File.separator + "downloads");
		    FileFilter fileFilter = new WildcardFileFilter("*.*");
		    File[] files = dir.listFiles(fileFilter);

		    if (files.length > 0) {
		        /** The newest file comes first **/
		        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		        theNewestFile = files[0];
		        long newSize = theNewestFile.length();
		        if (newSize!=currentSize) {
		        	System.out.println("     [echo] " + theNewestFile.getName() + " (" + AemDemoUtils.humanReadableByteCount(theNewestFile.length(),true) +")");
		        	currentSize = newSize;
		        }
		        
		    }

		}    
		
	}

}
