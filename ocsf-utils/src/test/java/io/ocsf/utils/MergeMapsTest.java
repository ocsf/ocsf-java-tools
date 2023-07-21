package io.ocsf.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MergeMapsTest
{
  @Test
  public void test1()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("Foo", 2)
          .p("boo", FMap.<String, Object>b()
                        .p("Greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, true);

    Assert.assertEquals(3, map.size());
    Assert.assertEquals(1, map.get("foo"));
    Assert.assertEquals(2, map.get("Foo"));
    Assert.assertEquals("hello", Maps.getIn(map, "boo.greeting"));
    Assert.assertEquals("bonjour", Maps.getIn(map, "boo.Greeting"));
  }

  @Test
  public void test2()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("foo", 2)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, true);

    Assert.assertEquals(2, map.size());
    Assert.assertEquals(2, map.get("foo"));
    Assert.assertEquals("bonjour", Maps.getIn(map, "boo.greeting"));
  }

  @Test
  public void test3()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("foo", null)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, true);

    Assert.assertEquals(1, map.size());
    Assert.assertNull(map.get("foo"));
    Assert.assertEquals("bonjour", Maps.getIn(map, "boo.greeting"));
  }

  @Test
  public void test4()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("Foo", 2)
          .p("boo", FMap.<String, Object>b()
                        .p("Greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, false);

    Assert.assertEquals(3, map.size());
    Assert.assertEquals(1, map.get("foo"));
    Assert.assertEquals(2, map.get("Foo"));
    Assert.assertEquals("hello", Maps.getIn(map, "boo.greeting"));
    Assert.assertEquals("bonjour", Maps.getIn(map, "boo.Greeting"));
  }

  @Test
  public void test5()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("foo", 2)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, false);

    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("foo"));
    Assert.assertEquals("hello", Maps.getIn(map, "boo.greeting"));
  }

  @Test
  public void test6()
  {
    final Map<String, Object> map1 =
      FMap.<String, Object>b()
          .p("foo", 1)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "hello"));

    final Map<String, Object> map2 =
      FMap.<String, Object>b()
          .p("foo", null)
          .p("boo", FMap.<String, Object>b()
                        .p("greeting", "bonjour"));

    final Map<String, Object> map = Maps.merge(map1, map2, false);

    Assert.assertEquals(2, map.size());
    Assert.assertEquals(1, map.get("foo"));
    Assert.assertEquals("hello", Maps.getIn(map, "boo.greeting"));
  }
}