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
package com.wl4g.devops.iam.config;

import static com.wl4g.devops.common.constants.IAMDevOpsConstants.URI_S_EXT_BASE;
import static com.wl4g.devops.common.constants.IAMDevOpsConstants.URI_S_SNS_BASE;

import java.io.Serializable;

import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.wl4g.devops.common.utils.web.WebUtils2;
import com.wl4g.devops.iam.common.config.AbstractIamProperties;
import com.wl4g.devops.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.devops.iam.web.DefaultViewController;
import com.wl4g.devops.iam.config.IamProperties.ServerParamProperties;
import com.wl4g.devops.iam.sns.web.DefaultOauth2SnsController;

/**
 * IAM server properties
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2019年1月4日
 * @since
 */
@ConfigurationProperties(prefix = "spring.cloud.devops.iam")
public class IamProperties extends AbstractIamProperties<ServerParamProperties> {
	private static final long serialVersionUID = -5858422822181237865L;

	/**
	 * Default view loader path
	 */
	private String defaultViewLoaderPath = "classpath:/default-view";

	/**
	 * Default view access base URI
	 */
	private String defaultViewBaseUri = "/view";

	/**
	 * Login page URI
	 */
	private String loginUri = getDefaultViewBaseUri() + "/login.html";

	/**
	 * Login success(index) page URI
	 */
	private String successUri = getDefaultViewBaseUri() + "/index.html";

	/**
	 * Unauthorized(403) page URI
	 */
	private String unauthorizedUri = getDefaultViewBaseUri() + "/403.html";

	/**
	 * Matcher configuration properties.
	 */
	private MatcherProperties matcher = new MatcherProperties();

	/**
	 * Ticket configuration properties.
	 */
	private TicketProperties ticket = new TicketProperties();

	/**
	 * IAM server parameters configuration properties.
	 */
	private ServerParamProperties param = new ServerParamProperties();

	public String getDefaultViewLoaderPath() {
		return defaultViewLoaderPath;
	}

	public void setDefaultViewLoaderPath(String viewLoaderPath) {
		this.defaultViewLoaderPath = viewLoaderPath;
	}

	public String getDefaultViewBaseUri() {
		return defaultViewBaseUri;
	}

	public void setDefaultViewBaseUri(String defaultViewBaseUri) {
		this.defaultViewBaseUri = defaultViewBaseUri;
	}

	@Override
	public String getLoginUri() {
		return loginUri;
	}

	public void setLoginUri(String loginUri) {
		this.loginUri = WebUtils2.cleanURI(loginUri);
	}

	@Override
	public String getSuccessUri() {
		return successUri;
	}

	public void setSuccessUri(String successUri) {
		this.successUri = WebUtils2.cleanURI(successUri);
	}

	@Override
	public String getUnauthorizedUri() {
		return unauthorizedUri;
	}

	public void setUnauthorizedUri(String unauthorizedUri) {
		this.unauthorizedUri = unauthorizedUri;
	}

	public MatcherProperties getMatcher() {
		return matcher;
	}

	public void setMatcher(MatcherProperties matcher) {
		this.matcher = matcher;
	}

	public TicketProperties getTicket() {
		return ticket;
	}

	public void setTicket(TicketProperties ticket) {
		this.ticket = ticket;
	}

	public ServerParamProperties getParam() {
		return this.param;
	}

	public void setParam(ServerParamProperties param) {
		this.param = param;
	}

	public void validation() {
		// Ignore
		//
	}

	/**
	 * Add default filter chain settings.<br/>
	 * {@link DefaultOauth2SnsController#connect}<br/>
	 */
	public void addDefaultFilterChain() {
		// SNS request rules
		super.getFilterChain().put(URI_S_SNS_BASE + "/**", "anon");
		// Extra API request rules
		super.getFilterChain().put(URI_S_EXT_BASE + "/**", "anon");
		// Default view access files request rules
		super.getFilterChain().put(getDefaultViewBaseUri() + DefaultViewController.URI_STATIC + "/**", "anon");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Default filtering set
		this.addDefaultFilterChain();
		// Validation
		this.validation();
	}

	/**
	 * Password matcher configuration properties
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月29日
	 * @since
	 */
	public static class MatcherProperties implements InitializingBean, Serializable {
		private static final long serialVersionUID = -6194767776312196341L;

		/**
		 * Maximum attempt request login count limit
		 */
		private int failFastMatchMaxAttempts = 10;

		/**
		 * Lock request waits for milliseconds after requesting authentication
		 * failure.
		 */
		private long failFastMatchDelay = 60 * 60 * 1000L;

		/**
		 * Continuous match error begins the maximum attempt to enable the
		 * verification code.
		 */
		private int enabledCaptchaMaxAttempts = 3;

		/**
		 * Maximum number of consecutive attempts to request an graph
		 * verification code.
		 */
		private int failFastCaptchaMaxAttempts = 20;

		/**
		 * The millisecond of lock wait after requesting CAPTCHA authentication
		 * fails.
		 */
		private long failFastCaptchaDelay = 10 * 60 * 1000L;

		/**
		 * The graph verification code requesting the application expires in
		 * milliseconds.
		 */
		private long captchaExpireMs = 1 * 60 * 1000L;

		/**
		 * Try to apply for the maximum number of SMS dynamic passwords multiple
		 * times (it will be locked for a while after it is exceeded).
		 */
		private int failFastSmsMaxAttempts = 3;

		/**
		 * The length of time (in milliseconds) that will be locked after trying
		 * to apply for the maximum number of SMS dynamic passwords multiple
		 * times. Reference: failFastSmsMaxAttempts.
		 */
		private long failFastSmsMaxDelay = 30 * 60 * 1000L;

		/**
		 * The number of milliseconds to wait after applying for an SMS dynamic
		 * password (you can reapply).
		 */
		private long failFastSmsDelay = (long) (1.5 * 60 * 1000L);

		/**
		 * Apply for SMS dynamic password every time, valid for authentication
		 * (milliseconds).
		 */
		private long smsExpireMs = 5 * 60 * 1000L;

		public int getFailFastMatchMaxAttempts() {
			return failFastMatchMaxAttempts;
		}

		public void setFailFastMatchMaxAttempts(int failureMaxAttempts) {
			Assert.isTrue(failureMaxAttempts > 0, "failureMaxAttempts code expiration time must be greater than 0");
			this.failFastMatchMaxAttempts = failureMaxAttempts;
		}

		public long getFailFastMatchDelay() {
			return failFastMatchDelay;
		}

		public void setFailFastMatchDelay(long failureDelaySecond) {
			this.failFastMatchDelay = failureDelaySecond;
		}

		public int getEnabledCaptchaMaxAttempts() {
			return enabledCaptchaMaxAttempts;
		}

		public void setEnabledCaptchaMaxAttempts(int enabledCaptchaMaxAttempts) {
			this.enabledCaptchaMaxAttempts = enabledCaptchaMaxAttempts;
		}

		public int getFailFastCaptchaMaxAttempts() {
			return failFastCaptchaMaxAttempts;
		}

		public void setFailFastCaptchaMaxAttempts(int failFastCaptchaMaxAttempts) {
			this.failFastCaptchaMaxAttempts = failFastCaptchaMaxAttempts;
		}

		public long getFailFastCaptchaDelay() {
			return failFastCaptchaDelay;
		}

		public void setFailFastCaptchaDelay(long failFastCaptchaDelay) {
			this.failFastCaptchaDelay = failFastCaptchaDelay;
		}

		public long getCaptchaExpireMs() {
			return captchaExpireMs;
		}

		public void setCaptchaExpireMs(long captchaExpireMs) {
			Assert.isTrue(captchaExpireMs > 0, "Verification code expiration time must be greater than 0");
			this.captchaExpireMs = captchaExpireMs;
		}

		public int getFailFastSmsMaxAttempts() {
			return failFastSmsMaxAttempts;
		}

		public void setFailFastSmsMaxAttempts(int captchaRequiredAttempts) {
			// Assert.isTrue((captchaRequiredAttempts > 0 &&
			// captchaRequiredAttempts < this.getFailFastMatchMaxAttempts()),
			// String.format(
			// "'captchaRequiredAttempts':%s should be must be greater than 0 or
			// less than 'failureMaxAttempts':%s",
			// captchaRequiredAttempts, getFailFastMatchMaxAttempts()));
			this.failFastSmsMaxAttempts = captchaRequiredAttempts;
		}

		public long getFailFastSmsMaxDelay() {
			return failFastSmsMaxDelay;
		}

		public void setFailFastSmsMaxDelay(long failFastSmsMaxDelay) {
			this.failFastSmsMaxDelay = failFastSmsMaxDelay;
		}

		public long getFailFastSmsDelay() {
			return failFastSmsDelay;
		}

		public void setFailFastSmsDelay(long failFastSmsDelay) {
			this.failFastSmsDelay = failFastSmsDelay;
		}

		public long getSmsExpireMs() {
			return smsExpireMs;
		}

		public void setSmsExpireMs(long smsExpireMs) {
			this.smsExpireMs = smsExpireMs;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			// Assert.isTrue(getFailFastSmsMaxAttempts() <
			// getFailFastMatchMaxAttempts(),
			// "failVerifyMaxAttempts must be less than failLockMaxAttempts");
		}

	}

	/**
	 * IAM fast-CAS ticket authentication configuration properties
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月29日
	 * @since
	 */
	public static class TicketProperties implements Serializable {
		private static final long serialVersionUID = -2694422471852860689L;

	}

	/**
	 * IAM server parameters configuration properties
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月29日
	 * @since
	 */
	public static class ServerParamProperties extends ParamProperties {
		private static final long serialVersionUID = 3258460473711285504L;

		/**
		 * Account parameter name at login time of account password.
		 */
		private String principalName = "principal";

		/**
		 * Password parameter name at login time of account password.
		 */
		private String passwordName = "password";

		/**
		 * Client type reference parameter name at login time of account
		 * password.
		 */
		private String clientRefName = "client_ref";

		/**
		 * Captcha parameter name at login time of account password
		 */
		private String captchaName = "captcha";

		/**
		 * Dynamic verification pass code key-name.
		 */
		private String verifyCodeName = "passcode";

		/**
		 * Dynamic verification code operation action type parameter key-name.
		 */
		private String smsActionName = "action";

		public String getPrincipalName() {
			return principalName;
		}

		public void setPrincipalName(String loginUsername) {
			this.principalName = loginUsername;
		}

		public String getPasswordName() {
			return passwordName;
		}

		public void setPasswordName(String loginPassword) {
			this.passwordName = loginPassword;
		}

		public String getClientRefName() {
			return clientRefName;
		}

		public void setClientRefName(String clientRefName) {
			this.clientRefName = clientRefName;
		}

		public String getCaptchaName() {
			return captchaName;
		}

		public void setCaptchaName(String loginCaptcha) {
			this.captchaName = loginCaptcha;
		}

		public String getVerifyCodeName() {
			return verifyCodeName;
		}

		public void setVerifyCodeName(String verifyCodeName) {
			this.verifyCodeName = verifyCodeName;
		}

		public String getSmsActionName() {
			return smsActionName;
		}

		public void setSmsActionName(String smsActionName) {
			this.smsActionName = smsActionName;
		}

	}

}