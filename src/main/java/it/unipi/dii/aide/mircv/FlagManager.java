package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.config.model.VocabularyConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the flags passed as command line arguments
 */
public class FlagManager {

    private static final Map<String, String> flags = new HashMap<>();
    private static final Map<String, Boolean> booleanFlags = new HashMap<>();

    /**
     * Parses the command line arguments and sets the configuration accordingly
     * @param config the configuration to be set
     * @param args the command line arguments
     * @return the configuration with the new settings
     */
    public static Config parseArgs(Config config, String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if ((i + 1) < args.length && !args[i + 1].startsWith("--")) {
                    flags.put(arg, args[i + 1]);
                    i++;
                } else {
                    booleanFlags.put(arg, true);
                }
            }
        }

        if (booleanFlags.containsKey("--debug")) {
            config.debug = booleanFlags.get("--debug");
        }

        if(booleanFlags.containsKey("--compressed-reading")) {
            config.compressedReading = booleanFlags.get("--compressed-reading");
        }

        if (booleanFlags.containsKey("--stem")) {
            config.getPreprocessConfig().setStemStopRemovalEnabled(booleanFlags.get("--stem"));
        }

        if (booleanFlags.containsKey("--compression")) {
            config.getBlockDescriptorConfig().setCompressionEnabled(booleanFlags.get("--compression"));
        }

        if (booleanFlags.containsKey("--maxScore")) {
            config.scorerConfig.setMaxScoreEnabled(booleanFlags.get("--maxScore"));
        }

        return config;
    }
}
