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
package com.wl4g.devops.common.kit.access;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * IP access white list control
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月30日
 * @since
 */
public class IPAccessControl {
	final private static String[] LOCAL = { "127.0.0.1", "localhost", "0:0:0:0:0:0:0:1" };
	final public static String IPV6 = "^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){1,7}:)|(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,2})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){1,3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,4})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){1,5})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,6})|(:(:[0-9A-Fa-f]{1,4}){1,7})|(([0-9A-Fa-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[0-9A-Fa-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$";
	final public static String IPV4 = new StringBuffer("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.")
			.append("(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.").append("(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.")
			.append("(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$").toString();

	final public static String IPV4_QUAD = "(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))";

	/**
	 * 10.0.0.0 - 10.255.255.255
	 */
	final public static String OWN_A = "10\\.(?:" + IPV4_QUAD + "\\.){2}" + IPV4_QUAD + "$";

	/**
	 * 172.16.0.0 - 172.31.255.255
	 */
	final public static String OWN_B = "172\\.(?:1[6-9]|2[0-9]|3[0-1])\\." + IPV4_QUAD + "\\." + IPV4_QUAD + "$";

	/**
	 * 192.168.0.0 - 192.168.255.255
	 */
	final public static String OWN_C = "192\\.168\\." + IPV4_QUAD + "\\." + IPV4_QUAD + "$";

	/**
	 * Thread local pattern cache.
	 */
	final private static ThreadLocal<Pattern[]> DEFAULT_PATTERN_CACHE = new InheritableThreadLocal<>();

	private IPAccessProperties config;

	public IPAccessControl(IPAccessProperties config) {
		Assert.notNull(config, "'ipAccessProperties' must not be null");
		this.config = config;
	}

	/**
	 * Access range(extranet and intranet) permitted validate
	 * 
	 * @param remoteIp
	 * @return
	 */
	public boolean isPermitted(String remoteIp) {
		return config.isSecure() ? isAnyNetIPOwnPermitted(remoteIp) : isIPRangePermitted(remoteIp);
	}

	/**
	 * Access range(extranet) permitted validate
	 * 
	 * @param remoteIp
	 *            remoteIp client remote ip.
	 * @return
	 */
	public boolean isIPRangePermitted(String remoteIp) {
		boolean ipV6 = remoteIp != null && remoteIp.indexOf(':') != -1;
		if (ipV6) {
			return "0:0:0:0:0:0:0:1".equals(remoteIp)
					|| (this.config.getDenyList().size() == 0 && this.config.getAllowList().size() == 0);
		}
		IPAddress ipAddress = new IPAddress(remoteIp);
		for (IPRange range : this.config.getDenyList()) {
			if (range.isIPAddressInRange(ipAddress)) {
				return false;
			}
		}
		if (this.config.getAllowList().size() > 0) {
			for (IPRange range : this.config.getAllowList()) {
				if (range.isIPAddressInRange(ipAddress)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Access private(intranet) permitted validate. <br/>
	 * Note: Only IPV4 is supported
	 * 
	 * @param remoteIp
	 *            client remote IP.
	 * @return
	 */
	public boolean isAnyNetIPOwnPermitted(String remoteIp) {
		// Local check
		for (String local : LOCAL) {
			if (local.equals(String.valueOf(remoteIp))) {
				return true;
			}
		}

		// Get patterns from cache
		Pattern[] patterns = DEFAULT_PATTERN_CACHE.get();
		if (patterns == null) {
			patterns = new Pattern[] { Pattern.compile(OWN_A), Pattern.compile(OWN_B), Pattern.compile(OWN_C) };
			DEFAULT_PATTERN_CACHE.set(patterns);
		}
		// Matches validation
		for (Pattern pattern : patterns) {
			if (pattern.matcher(remoteIp).matches()) {
				return true; // Allow as long as one of the matches succeeds
			}
		}
		return false;
	}

	/*
	 * Copyright 1999-2101 Wangl.sir Group Holding Ltd.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License.
	 */
	static class IPAddress implements Cloneable {

		/** IP address */
		protected int ipAddress = 0;

		public IPAddress(String ipAddressStr) {
			ipAddress = parseIPAddress(ipAddressStr);
		}

		public IPAddress(int address) {
			ipAddress = address;
		}

		// -------------------------------------------------------------------------
		/**
		 * Return the integer representation of the IP address.
		 * 
		 * @return The IP address.
		 */
		public final int getIPAddress() {
			return ipAddress;
		}

		// -------------------------------------------------------------------------
		/**
		 * Return the string representation of the IP Address following the
		 * common decimal-dotted notation xxx.xxx.xxx.xxx.
		 * 
		 * @return Return the string representation of the IP address.
		 */
		public String toString() {
			StringBuilder result = new StringBuilder();
			int temp;

			temp = ipAddress & 0x000000FF;
			result.append(temp);
			result.append(".");

			temp = (ipAddress >> 8) & 0x000000FF;
			result.append(temp);
			result.append(".");

			temp = (ipAddress >> 16) & 0x000000FF;
			result.append(temp);
			result.append(".");

			temp = (ipAddress >> 24) & 0x000000FF;
			result.append(temp);

			return result.toString();
		}

		// -------------------------------------------------------------------------
		/**
		 * Check if the IP address is belongs to a Class A IP address.
		 * 
		 * @return Return <code>true</code> if the encapsulated IP address
		 *         belongs to a class A IP address, otherwise returne
		 *         <code>false</code>.
		 */
		public final boolean isClassA() {
			return (ipAddress & 0x00000001) == 0;
		}

		// -------------------------------------------------------------------------
		/**
		 * Check if the IP address is belongs to a Class B IP address.
		 * 
		 * @return Return <code>true</code> if the encapsulated IP address
		 *         belongs to a class B IP address, otherwise returne
		 *         <code>false</code>.
		 */
		public final boolean isClassB() {
			return (ipAddress & 0x00000003) == 1;
		}

		// -------------------------------------------------------------------------
		/**
		 * Check if the IP address is belongs to a Class C IP address.
		 * 
		 * @return Return <code>true</code> if the encapsulated IP address
		 *         belongs to a class C IP address, otherwise returne
		 *         <code>false</code>.
		 */
		public final boolean isClassC() {
			return (ipAddress & 0x00000007) == 3;
		}

		// -------------------------------------------------------------------------
		/**
		 * Convert a decimal-dotted notation representation of an IP address
		 * into an 32 bits interger value.
		 * 
		 * @param ipAddressStr
		 *            Decimal-dotted notation (xxx.xxx.xxx.xxx) of the IP
		 *            address.
		 * @return Return the 32 bits integer representation of the IP address.
		 * @exception InvalidIPAddressException
		 *                Throws this exception if the specified IP address is
		 *                not compliant to the decimal-dotted notation
		 *                xxx.xxx.xxx.xxx.
		 */
		final int parseIPAddress(String ipAddressStr) {
			int result = 0;

			if (ipAddressStr == null) {
				throw new IllegalArgumentException();
			}

			try {
				String tmp = ipAddressStr;

				// get the 3 first numbers
				int offset = 0;
				for (int i = 0; i < 3; i++) {

					// get the position of the first dot
					int index = tmp.indexOf('.');

					// if there is not a dot then the ip string representation
					// is
					// not compliant to the decimal-dotted notation.
					if (index != -1) {

						// get the number before the dot and convert it into
						// an integer.
						String numberStr = tmp.substring(0, index);
						int number = Integer.parseInt(numberStr);
						if ((number < 0) || (number > 255)) {
							throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
						}

						result += number << offset;
						offset += 8;
						tmp = tmp.substring(index + 1);
					} else {
						throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
					}
				}

				// the remaining part of the string should be the last number.
				if (tmp.length() > 0) {
					int number = Integer.parseInt(tmp);
					if ((number < 0) || (number > 255)) {
						throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
					}

					result += number << offset;
					ipAddress = result;
				} else {
					throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
				}
			} catch (NoSuchElementException ex) {
				throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]", ex);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]", ex);
			}

			return result;
		}

		public int hashCode() {
			return this.ipAddress;
		}

		public boolean equals(Object another) {
			return another instanceof IPAddress && ipAddress == ((IPAddress) another).ipAddress;
		}
	}

	/*
	 * Copyright 1999-2101 Wangl.sir Group Holding Ltd.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License"); you may
	 * not use this file except in compliance with the License. You may obtain a
	 * copy of the License at
	 *
	 * http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License. <br/> <br/><br/>This class represents an IP Range,
	 * which are represented by an IP address and and a subnet mask. The
	 * standards describing modern routing protocols often refer to the
	 * extended-network-prefix-length rather than the subnet mask. The prefix
	 * length is equal to the number of contiguous one-bits in the traditional
	 * subnet mask. This means that specifying the network address 130.5.5.25
	 * with a subnet mask of 255.255.255.0 can also be expressed as
	 * 130.5.5.25/24. The prefix-length notation is more compact and easier to
	 * understand than writing out the mask in its traditional dotted-decimal
	 * format.
	 * 
	 * @author Marcel Dullaart
	 * 
	 * @version 1.0
	 * 
	 * @see IPAddress
	 */
	static class IPRange {

		/** IP address */
		private IPAddress ipAddress = null;

		/** IP subnet mask */
		private IPAddress ipSubnetMask = null;

		/** extended network prefix */
		private int extendedNetworkPrefix = 0;

		public IPRange(String range) {
			parseRange(range);
		}

		// -------------------------------------------------------------------------
		/**
		 * Return the encapsulated IP address.
		 * 
		 * @return The IP address.
		 */
		public final IPAddress getIPAddress() {
			return ipAddress;
		}

		// -------------------------------------------------------------------------
		/**
		 * Return the encapsulated subnet mask
		 * 
		 * @return The IP range's subnet mask.
		 */
		public final IPAddress getIPSubnetMask() {
			return ipSubnetMask;
		}

		// -------------------------------------------------------------------------
		/**
		 * Return the extended extended network prefix.
		 * 
		 * @return Return the extended network prefix.
		 */
		public final int getExtendedNetworkPrefix() {
			return extendedNetworkPrefix;
		}

		// -------------------------------------------------------------------------
		/**
		 * Convert the IP Range into a string representation.
		 * 
		 * @return Return the string representation of the IP Address following
		 *         the common format xxx.xxx.xxx.xxx/xx (IP address/extended
		 *         network prefixs).
		 */
		public String toString() {
			return ipAddress.toString() + "/" + extendedNetworkPrefix;
		}

		// -------------------------------------------------------------------------
		/**
		 * Parse the IP range string representation.
		 * 
		 * @param range
		 *            String representation of the IP range.
		 * @exception IllegalArgumentException
		 *                Throws this exception if the specified range is not a
		 *                valid IP network range.
		 */
		final void parseRange(String range) {
			if (range == null) {
				throw new IllegalArgumentException("Invalid IP range");
			}

			int index = range.indexOf('/');
			String subnetStr = null;
			if (index == -1) {
				ipAddress = new IPAddress(range);
			} else {
				ipAddress = new IPAddress(range.substring(0, index));
				subnetStr = range.substring(index + 1);
			}

			// try to convert the remaining part of the range into a decimal
			// value.
			try {
				if (subnetStr != null) {
					extendedNetworkPrefix = Integer.parseInt(subnetStr);
					if ((extendedNetworkPrefix < 0) || (extendedNetworkPrefix > 32)) {
						throw new IllegalArgumentException("Invalid IP range [" + range + "]");
					}
					ipSubnetMask = computeMaskFromNetworkPrefix(extendedNetworkPrefix);
				}
			} catch (NumberFormatException ex) {

				// the remaining part is not a valid decimal value.
				// Check if it's a decimal-dotted notation.
				ipSubnetMask = new IPAddress(subnetStr);

				// create the corresponding subnet decimal
				extendedNetworkPrefix = computeNetworkPrefixFromMask(ipSubnetMask);
				if (extendedNetworkPrefix == -1) {
					throw new IllegalArgumentException("Invalid IP range [" + range + "]", ex);
				}
			}
		}

		// -------------------------------------------------------------------------
		/**
		 * Compute the extended network prefix from the IP subnet mask.
		 * 
		 * @param mask
		 *            Reference to the subnet mask IP number.
		 * @return Return the extended network prefix. Return -1 if the
		 *         specified mask cannot be converted into a extended prefix
		 *         network.
		 */
		private int computeNetworkPrefixFromMask(IPAddress mask) {

			int result = 0;
			int tmp = mask.getIPAddress();

			while ((tmp & 0x00000001) == 0x00000001) {
				result++;
				tmp = tmp >>> 1;
			}

			if (tmp != 0) {
				return -1;
			}

			return result;
		}

		public static String toDecimalString(String inBinaryIpAddress) {
			StringBuilder decimalIp = new StringBuilder();
			String[] binary = new String[4];

			for (int i = 0, c = 0; i < 32; i = i + 8, c++) {
				binary[c] = inBinaryIpAddress.substring(i, i + 8);
				int octet = Integer.parseInt(binary[c], 2);
				decimalIp.append(octet);
				if (c < 3) {

					decimalIp.append('.');
				}
			}
			return decimalIp.toString();
		}

		// -------------------------------------------------------------------------
		/**
		 * Convert a extended network prefix integer into an IP number.
		 * 
		 * @param prefix
		 *            The network prefix number.
		 * @return Return the IP number corresponding to the extended network
		 *         prefix.
		 */
		private IPAddress computeMaskFromNetworkPrefix(int prefix) {

			/*
			 * int subnet = 0; for (int i=0; i<prefix; i++) { subnet = subnet <<
			 * 1; subnet += 1; }
			 */

			StringBuilder str = new StringBuilder();
			for (int i = 0; i < 32; i++) {
				if (i < prefix) {
					str.append("1");
				} else {
					str.append("0");
				}
			}

			String decimalString = toDecimalString(str.toString());
			return new IPAddress(decimalString);

		}

		// -------------------------------------------------------------------------
		/**
		 * Check if the specified IP address is in the encapsulated range.
		 * 
		 * @param address
		 *            The IP address to be tested.
		 * @return Return <code>true</code> if the specified IP address is in
		 *         the encapsulated IP range, otherwise return
		 *         <code>false</code>.
		 */
		public boolean isIPAddressInRange(IPAddress address) {
			if (ipSubnetMask == null) {
				return this.ipAddress.equals(address);
			}

			int result1 = address.getIPAddress() & ipSubnetMask.getIPAddress();
			int result2 = ipAddress.getIPAddress() & ipSubnetMask.getIPAddress();

			return result1 == result2;
		}
	}

	/**
	 * IP white list access control properties
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月30日
	 * @since
	 */
	public static class IPAccessProperties implements InitializingBean {
		final private Logger log = LoggerFactory.getLogger(getClass());

		/**
		 * True means only intranet access is allowed
		 */
		private boolean secure = true;
		private String allowIp = "127.0.0.1"; // Default by local.
		private String denyIp = ""; // Use default EMPTY when not set

		// Temporary members
		private Set<IPRange> allowList = new HashSet<>();
		private Set<IPRange> denyList = new HashSet<>();

		public boolean isSecure() {
			return secure;
		}

		public void setSecure(boolean secure) {
			this.secure = secure;
		}

		public String getAllowIp() {
			return allowIp;
		}

		public void setAllowIp(String allowIp) {
			this.allowIp = allowIp;
		}

		public String getDenyIp() {
			return denyIp;
		}

		public void setDenyIp(String disableIp) {
			this.denyIp = disableIp;
		}

		private Set<IPRange> getAllowList() {
			return allowList;
		}

		private Set<IPRange> getDenyList() {
			return denyList;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			// Allow list.
			try {
				String allows = this.getAllowIp();
				if (allows != null && allows.trim().length() != 0) {
					allows = allows.trim();
					String[] items = allows.split(",");

					for (String item : items) {
						if (item == null || item.length() == 0)
							continue;
						this.getAllowList().add(new IPRange(item));
					}
				}
			} catch (Exception e) {
				String msg = "Init config error, allow : " + this.getDenyIp();
				log.error(msg, e);
			}

			// Deny list.
			try {
				String denys = this.getDenyIp();
				if (denys != null && denys.trim().length() != 0) {
					denys = denys.trim();
					String[] items = denys.split(",");

					for (String item : items) {
						if (item == null || item.length() == 0)
							continue;
						this.getDenyList().add(new IPRange(item));
					}
				}
			} catch (Exception e) {
				String msg = "Init config error, deny : " + this.getDenyIp();
				log.error(msg, e);
			}
		}

	}

}