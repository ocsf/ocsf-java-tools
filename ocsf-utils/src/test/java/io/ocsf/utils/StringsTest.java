package io.ocsf.utils;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;

public class StringsTest extends TestCase
{
  // Test the default line splitter
  public void testToArray()
  {
    // single value
    Assert.assertEquals(Collections.singletonList(null), Strings.toArray(null));

    Assert.assertEquals(Collections.singletonList(1), Strings.toArray(1));

    Assert.assertEquals(Collections.singletonList("a"), Strings.toArray("a"));

    // array value - all should just pass through
    Assert.assertEquals(Collections.emptyList(), Strings.toArray(Collections.EMPTY_LIST));

    Assert.assertEquals(
      Collections.singletonList(1), Strings.toArray(Collections.singletonList(1)));

    Assert.assertEquals(
      Collections.singletonList(null), Strings.toArray(Collections.singletonList(null)));

    Assert.assertEquals(
      Arrays.asList("a", "b"), Strings.toArray(Arrays.asList("a", "b")));

    // Single string value, split into list
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a \nb"));

    Assert.assertEquals(Collections.singletonList("a\tb"), Strings.toArray("a\tb"));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\nb"));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\n\tb"));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\n\tb\n"));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\n\n\tb\n\n"));
    Assert.assertEquals(Collections.singletonList("a b"), Strings.toArray("a b "));
    Assert.assertEquals(Collections.singletonList("a b"), Strings.toArray(" a b"));

    // Edge cases
    Assert.assertEquals(
      "Empty string results in an empty list",
      Collections.emptyList(), Strings.toArray(""));

    Assert.assertEquals(
      "Split of only whitespace results in an empty list",
      Collections.emptyList(), Strings.toArray("\n"));
  }

  public void testTestToArray()
  {
    // single value
    Assert.assertEquals(Collections.singletonList(null), Strings.toArray(null, Strings.WhiteSpaceSplitter));

    Assert.assertEquals(Collections.singletonList(1), Strings.toArray(1, Strings.WhiteSpaceSplitter));

    Assert.assertEquals(Collections.singletonList("a"), Strings.toArray("a", Strings.WhiteSpaceSplitter));

    // array value - all should just pass through
    Assert.assertEquals(Collections.emptyList(), Strings.toArray(Collections.EMPTY_LIST, Strings.WhiteSpaceSplitter));

    Assert.assertEquals(
      Collections.singletonList(1), Strings.toArray(Collections.singletonList(1), Strings.WhiteSpaceSplitter));

    Assert.assertEquals(
      Collections.singletonList(null), Strings.toArray(Collections.singletonList(null), Strings.WhiteSpaceSplitter));

    Assert.assertEquals(
      Arrays.asList("a", "b"), Strings.toArray(Arrays.asList("a", "b"), Strings.WhiteSpaceSplitter));

    // Single string value, split into list
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a \nb", Strings.WhiteSpaceSplitter));

    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\tb", Strings.WhiteSpaceSplitter));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\nb", Strings.WhiteSpaceSplitter));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\n\tb", Strings.WhiteSpaceSplitter));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a\n\tb\n", Strings.WhiteSpaceSplitter));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray("a b ", Strings.WhiteSpaceSplitter));
    Assert.assertEquals(Arrays.asList("a", "b"), Strings.toArray(" a b", Strings.WhiteSpaceSplitter));

    // Edge cases
    Assert.assertEquals(
      "Empty string results in an empty list",
      Collections.emptyList(), Strings.toArray("", Strings.WhiteSpaceSplitter));

    Assert.assertEquals(
      "Split of only whitespace results in an empty list",
      Collections.emptyList(), Strings.toArray("\n", Strings.WhiteSpaceSplitter));
  }

  public void testIsPathLooped()
  {
    Assert.assertFalse(Strings.isPathLooped(null));
    Assert.assertFalse(Strings.isPathLooped(""));
    Assert.assertFalse(Strings.isPathLooped("foo"));
    Assert.assertFalse(Strings.isPathLooped("foo.bar"));
    Assert.assertTrue(Strings.isPathLooped("foo.foo"));
    Assert.assertTrue(Strings.isPathLooped("foo.bar.foo"));

    // Edge cases that shouldn't happen, but should still work
    Assert.assertFalse(Strings.isPathLooped("."));
    Assert.assertFalse(Strings.isPathLooped(".."));
    Assert.assertFalse(Strings.isPathLooped("foo."));
    Assert.assertFalse(Strings.isPathLooped("foo.."));
    Assert.assertFalse(Strings.isPathLooped(".foo"));
    Assert.assertFalse(Strings.isPathLooped("..foo"));
    Assert.assertFalse(Strings.isPathLooped(".foo."));
    Assert.assertFalse(Strings.isPathLooped(".foo.bar."));
    Assert.assertTrue(Strings.isPathLooped(".foo.bar.foo"));
  }

  public void testQuote() {
    Assert.assertEquals("null", Strings.quote(null));
    Assert.assertEquals("\"Hello world!\"", Strings.quote("Hello world!"));
    Assert.assertEquals("\"Say \\\"Hi!\\\", OK?\"", Strings.quote("Say \"Hi!\", OK?"));
    Assert.assertEquals("\"Hello\\\\world!\"", Strings.quote("Hello\\world!"));
    Assert.assertEquals("\"Say \\\"Hi!\\\",\\\\OK?\"", Strings.quote("Say \"Hi!\",\\OK?"));
    Assert.assertEquals("\"Hello world!\n\"", Strings.quote("Hello world!\n"));

    final Integer i = 42;
    Assert.assertEquals("\"42\"", Strings.quote(i));

    final Object o = null;
    Assert.assertEquals("null", Strings.quote(o));

    Assert.assertEquals("\"foo\"", Strings.quote((Object)"foo"));
  }
}