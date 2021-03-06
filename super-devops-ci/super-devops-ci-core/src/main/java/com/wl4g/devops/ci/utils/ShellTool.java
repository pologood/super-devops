/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.ci.utils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.wl4g.devops.shell.utils.ShellContextHolder;

/**
 * Shell utility tools.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年5月24日
 * @since
 */
public abstract class ShellTool {
	final private static Logger log = Logger.getLogger(ShellTool.class);

	/**
	 * Execute commands in local
	 */
	public static String exec(String cmd) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("Execution native command for '{}'" + cmd);
		}

		StringBuffer sb = new StringBuffer();
		StringBuffer se = new StringBuffer();

		Process ps = Runtime.getRuntime().exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		BufferedReader be = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
			log.info(line);
			ShellContextHolder.printfQuietly(line);
		}
		while ((line = be.readLine()) != null) {
			se.append(line).append("\n");
			log.info(line);
			ShellContextHolder.printfQuietly(line);
		}

		String result = sb.toString();
		String resulterror = se.toString();
		if (isNotBlank(resulterror)) {
			result += resulterror;
			throw new RuntimeException("Exec command fail,command=" + cmd + "\n cause:" + result.toString());
		}

		return result;
	}

}
