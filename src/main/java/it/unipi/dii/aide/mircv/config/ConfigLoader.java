package it.unipi.dii.aide.mircv.config;

import com.thoughtworks.xstream.XStream;
import it.unipi.dii.aide.mircv.config.model.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigLoader {
    private static final String CONFIG_FILE = "data_resources/config.xml";
    private static final String XSD_CONFIGURAION_PATH = "data_resources/config.xsd";

    public static Config load() {
        XStream xStream = new XStream();
        CustomXStreamSecurity.configureXStreamSecurity(xStream);
        try {
            String inputXML = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)));
            if (!validateXML(inputXML)) {
                System.err.println("Invalid configuration file, fallback to default configuration");
                return new Config();
            }

            xStream.alias("Config", Config.class);
            xStream.alias("vocabularyConfig", VocabularyConfig.class);
            xStream.alias("preprocessConfig", PreprocessConfig.class);
            xStream.alias("invertedIndexConfig", InvertedIndexConfig.class);
            xStream.alias("partialResultsConfig", PartialResultsConfig.class);
            xStream.alias("blockDescriptorConfig", BlockDescriptorConfig.class);

            xStream.alias("scorerConfig", ScorerConfig.class);

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
