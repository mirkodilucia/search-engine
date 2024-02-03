package it.unipi.dii.aide.mircv.application.config;

import com.thoughtworks.xstream.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.validation.*;
import javax.xml.transform.stream.*;
import java.io.IOException;

import java.io.*;
import java.nio.file.*;

public class ConfigLoader {

    private static final String CONFIG_FILE = "resources/config.xml";
    private static final String XSD_CONFIGURAION_PATH = "resources/config.xsd";

    public static Config load() {
        XStream xStream = new XStream();
        CustomXStreamSecurity.configureXStreamSecurity(xStream);
        try {
            String inputXML = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)));
            if (!validateXML(inputXML)) {
                return null;
            }

            xStream.alias("Config", Config.class);
            Object configuration = xStream.fromXML(inputXML);
            return (Config) configuration;

        } catch (IOException e) {
            System.err.println("Unable to load configuration file: " + e.getLocalizedMessage());
        }

        return null;
    }

    private static boolean validateXML(String inputXML) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document XMLDoc = documentBuilder.parse(new InputSource(new StringReader(inputXML)));

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(new File(XSD_CONFIGURAION_PATH)));

            schema.newValidator().validate(new DOMSource(XMLDoc));
            return true;
        } catch (ParserConfigurationException e) {
            System.out.println("Invalid xml, " + e.getLocalizedMessage());
        } catch (IOException e) {
            System.out.println("IO error:" + e.getLocalizedMessage());
        } catch (SAXException e) {
            System.out.println("SAXError: " + e.getLocalizedMessage());
        }

        return false;
    }
}