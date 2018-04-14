package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Token queue tests.
 */
public class TokenQueueTest {
    @Test public void chompBalanced() {
        TokenQueue tq = new TokenQueue(":contains(one (two) three) four");
        String pre = tq.consumeTo("(");
        String guts = tq.chompBalanced('(', ')');
        String remainder = tq.remainder();

        assertEquals(":contains", pre);
        assertEquals("one (two) three", guts);
        assertEquals(" four", remainder);
    }

    @Test public void chompEscapedBalanced() {
        TokenQueue tq = new TokenQueue(":contains(one (two) \\( \\) \\) three) four");
        String pre = tq.consumeTo("(");
        String guts = tq.chompBalanced('(', ')');
        String remainder = tq.remainder();

        assertEquals(":contains", pre);
        assertEquals("one (two) \\( \\) \\) three", guts);
        assertEquals("one (two) ( ) ) three", TokenQueue.unescape(guts));
        assertEquals(" four", remainder);
    }

    @Test public void chompBalancedMatchesAsMuchAsPossible() {
        TokenQueue tq = new TokenQueue("unbalanced(something(or another)) else");
        tq.consumeTo("(");
        String match = tq.chompBalanced('(', ')');
        assertEquals("something(or another)", match);
    }

    @Test public void unescape() {
        assertEquals("one ( ) \\", TokenQueue.unescape("one \\( \\) \\\\"));
    }

    @Test public void chompToIgnoreCase() {
        String t = "<textarea>one < two </TEXTarea>";
        TokenQueue tq = new TokenQueue(t);
        String data = tq.chompToIgnoreCase("</textarea");
        assertEquals("<textarea>one < two ", data);

        tq = new TokenQueue("<textarea> one two < three </oops>");
        data = tq.chompToIgnoreCase("</textarea");
        assertEquals("<textarea> one two < three </oops>", data);
    }

    @Test public void addFirst() {
        TokenQueue tq = new TokenQueue("One Two");
        tq.consumeWord();
        tq.addFirst("Three");
        assertEquals("Three Two", tq.remainder());
    }


    @Test public void consumeToIgnoreSecondCallTest() {
        String t = "<textarea>one < two </TEXTarea> third </TEXTarea>";
        TokenQueue tq = new TokenQueue(t);
        String data = tq.chompToIgnoreCase("</textarea>");
        assertEquals("<textarea>one < two ", data);

        data = tq.chompToIgnoreCase("</textarea>");
        assertEquals(" third ", data);
    }

    @Test public void testNestedQuotes() {
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=\"func('arg')\" /></body></html>", "a[onclick*=\"('arg\"]");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=func('arg') /></body></html>", "a[onclick*=\"('arg\"]");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick='func(\"arg\")' /></body></html>", "a[onclick*='(\"arg']");
        validateNestedQuotes("<html><body><a id=\"identifier\" onclick=func(\"arg\") /></body></html>", "a[onclick*='(\"arg']");
    }


    @Test public void testPeek() {
        TokenQueue tq1 = new TokenQueue("abc");
        TokenQueue tq2 = new TokenQueue("");

        assertEquals('a', tq1.peek());
        assertEquals(0, tq2.peek());
    }

    @Test public void testMatchesCS() {
        TokenQueue tq = new TokenQueue("Hello");
        assertTrue(tq.matchesCS("Hello"));
        assertFalse(tq.matchesCS("hello"));
    }


    @Test public void testMatchesStartTag() {
        TokenQueue tq1 = new TokenQueue("<p>");
        TokenQueue tq2 = new TokenQueue("abc");
        TokenQueue tq3 = new TokenQueue("<");
        TokenQueue tq4 = new TokenQueue("<<");

        assertTrue(tq1.matchesStartTag());
        assertFalse(tq2.matchesStartTag());
        assertFalse(tq3.matchesStartTag());
        assertFalse(tq4.matchesStartTag());
    }

    @Test public void testConsumeTagName() {
        TokenQueue tq1 = new TokenQueue("p");
        TokenQueue tq2 = new TokenQueue("tag_name");
        TokenQueue tq3 = new TokenQueue("tag-name");
        TokenQueue tq4 = new TokenQueue("ab:cd");
        TokenQueue tq5 = new TokenQueue("%%%");

        assertEquals("p", tq1.consumeTagName());
        assertEquals("tag_name", tq2.consumeTagName());
        assertEquals("tag-name", tq3.consumeTagName());
        assertEquals("ab:cd", tq4.consumeTagName());
        assertEquals("", tq5.consumeTagName());
    }

    @Test public void testConsumeAttribtueKey() {
        TokenQueue tq1 = new TokenQueue("href");
        TokenQueue tq2 = new TokenQueue("_position");
        TokenQueue tq3 = new TokenQueue("http-equiv");
        TokenQueue tq4 = new TokenQueue("xml:lang");
        TokenQueue tq5 = new TokenQueue("%%%");

        assertEquals("href", tq1.consumeAttributeKey());
        assertEquals("_position", tq2.consumeAttributeKey());
        assertEquals("http-equiv", tq3.consumeAttributeKey());
        assertEquals("xml:lang", tq4.consumeAttributeKey());
        assertEquals("", tq5.consumeAttributeKey());
    }

    private static void validateNestedQuotes(String html, String selector) {
        assertEquals("#identifier", Jsoup.parse(html).select(selector).first().cssSelector());
    }
}
