package it.unipi.dii.aide.mircv.application.cli;


import org.junit.Test;

import static org.junit.Assert.*;

public class CommandParserTest {

    @Test
    public void testIsHelpCommand() {
        assertTrue(CommandParser.isHelpCommand(new String[]{"help"}));
        assertFalse(CommandParser.isHelpCommand(new String[]{"something"}));
    }

    @Test
    public void testIsBreakCommand() {
        assertTrue(CommandParser.isBreakCommand(new String[]{"break"}));
        assertFalse(CommandParser.isBreakCommand(new String[]{"something"}));
    }

    @Test
    public void testIsValidQuery() {
        assertTrue(CommandParser.isValidQuery("query"));
        assertFalse(CommandParser.isValidQuery(null));
        assertFalse(CommandParser.isValidQuery(""));
    }

    @Test
    public void testParseQuery() {
        assertArrayEquals(new String[]{"query", "c", "s"}, CommandParser.parseQuery("query-c-s"));
    }

    @Test
    public void testIsConjunctiveMode() {
        assertTrue(CommandParser.isConjunctiveMode(new String[]{"query", "c"}));
        assertFalse(CommandParser.isConjunctiveMode(new String[]{"query", "d"}));
    }

    @Test
    public void testIsDisjunctiveMode() {
        assertTrue(CommandParser.isDisjunctiveMode(new String[]{"query", "d"}));
        assertFalse(CommandParser.isDisjunctiveMode(new String[]{"query", "c"}));
    }

    @Test
    public void testIsTfIdfScoring() {
        assertTrue(CommandParser.isTfIdfScoring("tfidf"));
        assertFalse(CommandParser.isTfIdfScoring("bm25"));
    }

    @Test
    public void testIsBM25Scoring() {
        assertTrue(CommandParser.isBM25Scoring("bm25"));
        assertFalse(CommandParser.isBM25Scoring("tfidf"));
    }
}