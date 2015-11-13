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
package com.adobe.aem.demo.communities;

// This class is used to track the bundle versions
public class Version implements Comparable<Version> {

	private String version;

	public final String get() {
		return this.version;
	}

	public Version(String version) {
		if(version == null)
			throw new IllegalArgumentException("Version can not be null");
		if(!version.matches("[0-9]+(\\.[0-9]+)*"))
			throw new IllegalArgumentException("Invalid version format");
		this.version = version;
	}

	@Override public int compareTo(Version that) {
		if(that == null)
			return 1;
		String[] thisParts = this.get().split("\\.");
		String[] thatParts = that.get().split("\\.");
		int length = Math.max(thisParts.length, thatParts.length);
		for(int i = 0; i < length; i++) {
			int thisPart = i < thisParts.length ?
					Integer.parseInt(thisParts[i]) : 0;
					int thatPart = i < thatParts.length ?
							Integer.parseInt(thatParts[i]) : 0;
							if(thisPart < thatPart)
								return -1;
							if(thisPart > thatPart)
								return 1;
		}
		return 0;
	}

	@Override public boolean equals(Object that) {
		if(this == that)
			return true;
		if(that == null)
			return false;
		if(this.getClass() != that.getClass())
			return false;
		return this.compareTo((Version) that) == 0;
	}

}

