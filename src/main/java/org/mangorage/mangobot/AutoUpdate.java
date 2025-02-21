package org.mangorage.mangobot;

import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobotapi.core.plugin.extra.JDAPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;

public final class AutoUpdate implements Runnable {
    private static final String URL = "https://maven.minecraftforge.net/org/mangorage/mangobotplugin/maven-metadata.xml";

    public static String getLastUpdated(String mavenMetadataUrl) {
        try {
            InputStream inputStream = new URL(mavenMetadataUrl).openStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);

            NodeList nodeList = doc.getElementsByTagName("lastUpdated");
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or on error
    }

    private long lastUpdated = -1;
    private boolean running = true;
    private final JDAPlugin plugin;

    AutoUpdate(JDAPlugin plugin) {
        this.plugin = plugin;
        if (MangoBotPlugin.AUTO_UPDATE.get()) {
            try {
                this.lastUpdated = Long.valueOf(getLastUpdated(URL));
            } catch (Exception ignored) {}
            LogHelper.info("Started Auto Updater");
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10_000);
                var last = Long.valueOf(getLastUpdated(URL));
                if (this.lastUpdated == -1) {
                    this.lastUpdated = last;
                } else if (this.lastUpdated != last) {
                    LogHelper.info("Detected New Version! Restarting!");
                    running = false;
                }
            } catch (Exception ignored) {}
        }

        LogHelper.info("Shutting down soon...");
        System.exit(0);
    }
}