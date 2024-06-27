package it.unipi.dii.aide.mircv;

import it.unipi.dii.aide.mircv.cli.CommandParser;
import it.unipi.dii.aide.mircv.cli.QueryParser;
import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.config.Config;

import java.util.Scanner;

public class CommandLine {

    private static Config config;

    private static final int k = 10;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("****** SEARCH ENGINE ******");
        System.out.println("Starting...");

        config = new Config();

        System.out.println("What are you looking for? " + """
                Please insert a query specifying your preferred mode:\s
                -c for conjunctive mode or -d for disjunctive mode. Here's an example:\s
                This is a query example -c \s
                Type "help" to get help or "break" to terminate the service""");

        while (true) {
            String query = scanner.nextLine();
            if (!CommandParser.isValidQuery(query)) {
                System.out.println("The query you entered is empty.");
                continue;
            }

            String[] queryParams = CommandParser.parseQuery(query);
            if (queryParams.length == 1) {
                if (CommandParser.isBreakCommand(queryParams)) {
                    break;
                }

                if (CommandParser.isHelpCommand(queryParams)) {
                    continue;
                }

                System.out.println("The query you entered is in invalid format.");
                continue;
            }

            if (queryParams.length > 1) {
                boolean result = new QueryParser(config, scanner).processQuery(queryParams);
            }
        }

        scanner.close();
    }
}
