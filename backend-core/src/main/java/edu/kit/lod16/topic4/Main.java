package edu.kit.lod16.topic4;

import edu.kit.lod16.topic4.services.PropertyRankingService;
import edu.kit.lod16.topic4.util.Configuration;
import edu.kit.lod16.topic4.util.Prefixes;
import org.apache.commons.cli.*;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;

public class Main {
    public static Configuration config;
    private static Options cliOptions;

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("edu.kit.lod16.topic4");
        rc.register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create((String) config.get("baseUri")), rc);
    }

    public static void loadConfig(File filePath) {
        InputStream configInputStream = null;
        try {
            configInputStream = new FileInputStream(filePath);
            Yaml yaml = new Yaml();
            config = new Configuration((Map<String, Object>) yaml.load(configInputStream));
            Prefixes.init((Map<String, String>) config.get("app.sparql.prefixes"));
            configInputStream.close();
        } catch (IOException e) {
            System.out.println("Sorry, unable to find configuration file.");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        cliOptions = new Options();
        cliOptions.addOption("c", "config", true, "Absolute path to the configuration file. Defaults to \"config.yml\" in project root.");

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(cliOptions, args);
            loadConfig(cmd.hasOption("c") ? new File(cmd.getOptionValue("c")) : new File("config.yml"));
        } catch (ParseException e) {
            System.err.println("There was an error parsing command line parameters.");
            System.exit(1);
        }

        PropertyRankingService prs = PropertyRankingService.getInstance();

        Instant start = Instant.now();
        prs.setBlacklist((List) Main.config.get("app.data.blacklist"));
        prs.loadPropertyFrequencies("yago", (String) Main.config.get("app.data.propertyFrequencies.yago"));
        prs.loadClassFrequencies("yago", (String) Main.config.get("app.data.classFrequencies.yago"));
        prs.loadPropertyFrequencies("dbp", (String) Main.config.get("app.data.propertyFrequencies.dbp"));
        prs.loadClassFrequencies("dbp", (String) Main.config.get("app.data.classFrequencies.dbp"));
        Instant end = Instant.now();

        System.out.println("Loaded and parsed property files in " + Duration.between(start, end).toMillis() + " ms");

        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", (String) config.get("baseUri")));

        System.in.read();
        server.stop();
    }
}

