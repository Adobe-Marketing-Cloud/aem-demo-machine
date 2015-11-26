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

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class AemDemoMarquee extends JLabel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final int RATE = 12;
	private final Timer timer = new Timer(1000 / RATE, this);
	private final String s;
	private final int n;
	private int index;

	public AemDemoMarquee(String s, int n) {
		if (s == null || n < 1) {
			throw new IllegalArgumentException("Null string or n < 1");
		}
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			sb.append(' ');
		}
		this.s = sb + s + sb;
		this.n = n;
		this.setFont(new Font("Serif", Font.ITALIC, 16));
		this.setText(sb.toString());
	}

	public void start() {
		timer.start();
	}

	public void stop() {
		timer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		index++;
		if (index > s.length() - n) {
			index = 0;
		}
		this.setText(s.substring(index, index + n));
	}
}


