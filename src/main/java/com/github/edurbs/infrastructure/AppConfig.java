package com.github.edurbs.infrastructure;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "/app-config.xml";
    private static Boolean earlyTest = null;

    private AppConfig() {
        // Prevent instantiation
    }

    public static boolean withCss() {
        if (earlyTest == null) {
            earlyTest = loadWithCssFlag();
        }
        return earlyTest;
    }

    private static boolean loadWithCssFlag() {
        try (InputStream is = AppConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                logger.warn("Config file {} not found, defaulting withCss to false", CONFIG_FILE);
                return false;
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbFactory.setXIncludeAware(false);
            dbFactory.setExpandEntityReferences(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            String earlyTestValue = root.getElementsByTagName("withCss").item(0).getTextContent();
            return Boolean.parseBoolean(earlyTestValue);
        } catch (Exception e) {
            logger.error("Error reading config file, defaulting withCss to false", e);
            return false;
        }
    }
}
