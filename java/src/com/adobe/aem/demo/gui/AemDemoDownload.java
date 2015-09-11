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
