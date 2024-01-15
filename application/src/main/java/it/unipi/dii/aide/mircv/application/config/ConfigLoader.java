package it.unipi.dii.aide.mircv.application.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ConfigLoader {

    private static final String CONFIG_FILE = "it/unipi/dii/aide/mircv/application/config/config.xml";
    private static ConfigLoader INSTANCE;

    static {
        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Unmarshal the XML file into an ExampleModel object
            Config exampleModel = (Config) unmarshaller.unmarshal(new File(CONFIG_FILE));

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private ConfigLoader() {}

    public static ConfigLoader getInstance() {
        if (INSTANCE == null) {
            return new ConfigLoader();
        }

        return INSTANCE;
    }

}