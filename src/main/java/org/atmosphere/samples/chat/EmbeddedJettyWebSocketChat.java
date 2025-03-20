package org.atmosphere.samples.chat;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EmbeddedJettyWebSocketChat {
	private static final Logger log = LoggerFactory.getLogger(EmbeddedJettyWebSocketChat.class);

	public static void main(String[] args) throws Exception {
		new EmbeddedJettyWebSocketChat().run();
	}

	private void run() throws Exception {
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(8080);
		server.addConnector(connector);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		String currentWorkingDir = System.getProperty("user.dir");
		Path resourceBasePath = Paths.get(currentWorkingDir, "target", "webapp");
		context.setBaseResource(ResourceFactory.root().newResource(resourceBasePath.toUri()));

		// Add DefaultServlet to serve static content
		ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
		defaultServlet.setInitParameter("dirAllowed", "true");
		defaultServlet.setInitParameter("welcomeServlets", "true");
		defaultServlet.setInitParameter("redirectWelcome", "true");
		context.addServlet(defaultServlet, "/*");

		// Add AtmosphereServlet
		ServletHolder atmosphereServlet = new ServletHolder(AtmosphereServlet.class);
		atmosphereServlet.setInitParameter(ApplicationConfig.ANNOTATION_PACKAGE, "org.atmosphere.samples.chat");
		atmosphereServlet.setInitParameter(ApplicationConfig.PROPERTY_COMET_SUPPORT, "org.atmosphere.container.JSR356AsyncSupport");
		atmosphereServlet.setAsyncSupported(true);
		context.addServlet(atmosphereServlet, "/chat/*");

		// Configure WebSocket
		JakartaWebSocketServletContainerInitializer.configure(context, null);

		server.setHandler(context);

		server.start();
		log.info("Server started on port {}", connector.getPort());
		server.join();
	}
}