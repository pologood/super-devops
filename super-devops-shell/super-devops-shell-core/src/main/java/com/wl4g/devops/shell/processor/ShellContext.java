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
package com.wl4g.devops.shell.processor;

import com.wl4g.devops.shell.bean.RunState;
import com.wl4g.devops.shell.handler.ChannelMessageHandler;
import com.wl4g.devops.shell.bean.ResultMessage;
import com.wl4g.devops.shell.processor.EmbeddedServerProcessor.ShellHandler;
import com.wl4g.devops.shell.registry.InternalInjectable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;

import static com.wl4g.devops.shell.handler.ChannelMessageHandler.*;
import static com.wl4g.devops.shell.bean.RunState.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Shell handler context
 * 
 * @author wangl.sir
 * @version v1.0 2019年5月24日
 * @since
 */
public final class ShellContext implements InternalInjectable, Closeable {

	final protected Logger log = LoggerFactory.getLogger(getClass());

	/** Shell handler client. */
	final private ShellHandler client;

	/** Line result message state. */
	private volatile RunState state;

	/** Event listenter */
	private EventListener eventListener;

	public ShellContext(ShellHandler client) {
		this(client, NONCE);
	}

	public ShellContext(ShellHandler client, RunState state) {
		Assert.notNull(client, "Client must not be null");
		this.client = client;
		this.state = state;
	}

	public RunState getState() {
		return state;
	}

	public ShellHandler getClient() {
		return client;
	}

	/**
	 * Manually open data flow message transaction output.
	 */
	public synchronized void open() {
		this.state = RUNNING_WAIT;

		// Print start mark
		printf(BEGIN_EOF);
	}

	/**
	 * Manually end data flow message transaction output.
	 */
	@Override
	public synchronized void close() {
		this.state = COMPLATED;

		// Print end mark
		printf(EOF);
	}

	/**
	 * Manually output simple string message to the client console.
	 * 
	 * @param message
	 * @throws IllegalStateException
	 */
	public void printf(String message) throws IllegalStateException {
		if (getState() != RUNNING_WAIT && !equalsAny(message, BEGIN_EOF, EOF)) {
			throw new IllegalStateException("The shell is not printable in the afternoon, has it not been opened or closed?");
		}

		ChannelMessageHandler client = getClient();
		if (client != null && client.isActive()) {
			try {
				client.writeAndFlush(new ResultMessage(getState(), message));
			} catch (IOException e) {
				String errmsg = getRootCauseMessage(e);
				errmsg = isBlank(errmsg) ? getMessage(e) : errmsg;
				log.error("=> {}", errmsg);
			}
		} else {
			throw new IllegalStateException("The current console channel may be closed!");
		}

	}

	/**
	 * Output quietly message to client console
	 * 
	 * @param message
	 */
	public void printfQuietly(String message) {
		try {
			printf(message);
		} catch (Exception e) {
			log.warn("Printf error. cause: {}", getRootCauseMessage(e));
		}
	}

	public EventListener getEventListener() {
		return eventListener;
	}

	public void setEventListener(EventListener eventListener) {
		Assert.notNull(eventListener, "eventListener must not be null");
		this.eventListener = eventListener;
	}

	public static interface EventListener {
		void onInterrupt();
	}

}
