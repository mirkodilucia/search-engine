package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.config.model.VocabularyConfig;

import java.util.HashMap;
import java.util.Map;

public class FlagManager {

    private static final Map<String, String> flags = new HashMap<>();
    private static final Map<String, Boolean> booleanFlags = new HashMap<>();

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

        if (booleanFlags.containsKey("--stem")) {
            config.getPreprocessConfig().setStemStopRemovalEnabled(Boolean.parseBoolean(flags.get("--stem")));
        }

        if (booleanFlags.containsKey("--compression")) {
            config.getBlockDescriptorConfig().setCompressionEnabled(Boolean.parseBoolean(flags.get("--compression")));
        }

        if (booleanFlags.containsKey("--maxScore")) {
            config.scorerConfig.setMaxScoreEnabled(Boolean.parseBoolean(flags.get("--maxScore")));
        }

        return config;
    }
}
