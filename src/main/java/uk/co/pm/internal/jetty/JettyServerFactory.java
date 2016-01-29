package uk.co.pm.internal.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import uk.co.pm.internal.Configuration;

public class JettyServerFactory {

    public static final int DEFAULT_SERVER_PORT = 8080;

    public static Server createServer(ResourceConfig jerseyConfig, Configuration config) {
        int port = config.getInteger("server.http.port", DEFAULT_SERVER_PORT);

        HandlerList handlers = new HandlerList();
        handlers.addHandler(staticResourceContextHandler("/static", "/static"));
        handlers.addHandler(jerseyContextHandler("/*", jerseyConfig));

        Server server = new Server(port);
        server.setHandler(handlers);
        return server;
    }

    private static ContextHandler jerseyContextHandler(String contextPath, ResourceConfig resourceConfig) {
        ServletContextHandler contextHandler = new ServletContextHandler();
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        contextHandler.addServlet(servletHolder, contextPath);
        return contextHandler;
    }

    private static ContextHandler staticResourceContextHandler(String contextPath, String resourceRoot) {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource(resourceRoot));

        ContextHandler handlerWrapper = new ContextHandler(contextPath);
        handlerWrapper.setHandler(resourceHandler);

        return handlerWrapper;
    }
}
