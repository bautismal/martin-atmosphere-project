package org.atmosphere.samples.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.config.service.AtmosphereHandlerService;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;

import java.io.IOException;
import java.util.Date;

@AtmosphereHandlerService(path = "/chat", broadcasterCache = UUIDBroadcasterCache.class, interceptors = { AtmosphereResourceLifecycleInterceptor.class, SuspendTrackerInterceptor.class })
public class ChatService implements AtmosphereHandler {

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void onRequest(AtmosphereResource resource) throws IOException {
		if (resource.getRequest().getMethod().equalsIgnoreCase("GET")) {
			resource.suspend();
		} else {
			String message = resource.getRequest().getReader().readLine();
			System.out.println("onRequest: " + message);
			resource.write((mapper.writeValueAsString(mapper.readValue(message, Data.class))));
		}
	}

	@Override
	public void onStateChange(AtmosphereResourceEvent event) {
		System.out.println("onStateChange: " + event.getMessage());
	}

	@Override
	public void destroy() {

	}

	public final static class Data {

		private String message;
		private String author;
		private long time;

		public Data() {
			this("", "");
		}

		public Data(String author, String message) {
			this.author = author;
			this.message = message;
			this.time = new Date().getTime();
		}

		public String getMessage() {
			return message;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

	}
}
