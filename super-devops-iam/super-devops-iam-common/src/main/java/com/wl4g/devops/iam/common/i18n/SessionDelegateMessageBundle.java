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
package com.wl4g.devops.iam.common.i18n;

import static com.wl4g.devops.common.constants.IAMDevOpsConstants.KEY_USE_LOCALE;

import java.util.Locale;

import com.wl4g.devops.common.i18n.AbstractDelegateMessageBundle;
import com.wl4g.devops.iam.common.utils.SessionBindings;

/**
 * Session delegate resource boundle message source.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年3月24日
 * @since
 */
public class SessionDelegateMessageBundle extends AbstractDelegateMessageBundle {

	public SessionDelegateMessageBundle() {
		super();
	}

	public SessionDelegateMessageBundle(Class<?> withClassPath) {
		super(withClassPath);
	}

	public SessionDelegateMessageBundle(String... basenames) {
		super(basenames);
	}

	@Override
	protected Locale getSessionLocale() {
		return (Locale) SessionBindings.getBindValue(KEY_USE_LOCALE);
	}

}