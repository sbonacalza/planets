package com.mercadolibre.planets;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.mercadolibre.planets.guice.ProductionModule;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Santiago on 21/12/18.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        long startDeploy = System.currentTimeMillis();

        final Injector injector = Guice.createInjector(new ProductionModule());

        Server server = new Server(8080);
        ServletContextHandler sch = new ServletContextHandler(server, "/");

        sch.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return injector;
            }
        });

        // Then add GuiceFilter and configure the server to
        // reroute all requests through this filter.
        sch.addFilter(GuiceFilter.class, "/*", null);

        // Must add DefaultServlet for embedded Jetty.
        // Failing to do this will cause 404 errors.
        // This is not needed if web.xml is used instead.
        sch.addServlet(DefaultServlet.class, "/");

        LOGGER.info("Service started. - Deploy time: " + (System.currentTimeMillis() - startDeploy) + " ms.");

        server.start();

        Worker worker = injector.getInstance(Worker.class);
        worker.start();

        worker.join();
        server.join();
    }
}
