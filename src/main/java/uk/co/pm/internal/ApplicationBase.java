package uk.co.pm.internal;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.slf4j.bridge.SLF4JBridgeHandler;
import uk.co.pm.internal.database.PortfolioManagerDB;
import uk.co.pm.internal.file.FileWatcher;
import uk.co.pm.internal.jetty.JettyServerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Base application class
 */
public abstract class ApplicationBase {

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private Server server;
    private PortfolioManagerDB database = new PortfolioManagerDB();
    private FileWatcher fileWatcher;

    public final ApplicationBase init(String[] args) throws Exception {
        Configuration config = loadConfiguration(args);
        this.fileWatcher = new FileWatcher();
        this.database.init();
        ResourceConfig jerseyConfig = createDefaultJerseyConfig();

        configure(jerseyConfig, config, database.getDataSource(), fileWatcher);
        this.server = JettyServerFactory.createServer(jerseyConfig, config);

        registerShutdownHook();
        return this;
    }

    /**
     * Loads the application configuration.
     * <p/>
     * If args is empty, then the configuration is loaded from the
     * application.properties file on the classpath; otherwise the first entry in args is assumed to be a configuration
     * file on the local file system.
     *
     * @param args
     * @return initialised application configuration.
     */
    protected Configuration loadConfiguration(String[] args) {
        if (args.length > 0) {
            String configFilePath = args[0];
            return Configuration.loadFromFile(configFilePath);
        }
        return Configuration.loadFromClasspath("application.properties");
    }

    private ResourceConfig createDefaultJerseyConfig() {
        ResourceConfig jerseyConfig = new ResourceConfig();
        jerseyConfig.register(LoggingFilter.class);
        jerseyConfig.register(JacksonFeature.class);
        jerseyConfig.register(FreemarkerMvcFeature.class);
        jerseyConfig.property(MvcFeature.TEMPLATE_BASE_PATH, "/templates");
        return jerseyConfig;
    }

    /**
     * Configure the application by attaching any Jersey resources to the jerseyConfig, and registering any
     * {@link uk.co.pm.internal.file.FileEventHandler}s with the fileWatcher.
     *
     * @param jerseyConfig
     * @param config
     * @param dataSource
     * @param fileWatcher
     */
    protected abstract void configure(ResourceConfig jerseyConfig, Configuration config, DataSource dataSource, FileWatcher fileWatcher) throws SQLException;

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ApplicationBase.this.database.shutdown();
                ApplicationBase.this.fileWatcher.shutdown();
            }
        });
    }

    /**
     * Starts the file watcher and server processes.
     *
     * @throws Exception if the process initialisation fails.
     */
    public void run() throws Exception {
        fileWatcher.start();
        server.start();
        server.join();
    }
}
