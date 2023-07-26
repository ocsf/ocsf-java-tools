package io.ocsf.utils;

import junit.framework.TestCase;
import org.junit.Assert;

public class FuzzyHashMapTest extends TestCase
{
  private static final String Name = "FuzzyHashMap";

  private static final Object Foo = new Object()
  {
    @Override
    public String toString()
    {
      return "Foo";
    }
  };

  private static final Object Boo = new Object()
  {
    @Override
    public String toString()
    {
      return "Boo";
    }
  };

  private static final Object WildCard = new Object()
  {
    @Override
    public String toString()
    {
      return "Foo*";
    }
  };

  private static final Object WildCardLowerCase = new Object()
  {
    @Override
    public String toString()
    {
      return "foo*";
    }
  };

  private FuzzyHashMap<Object> map;

  public void setUp()
  {
    map = new FuzzyHashMap<>(Name, 4, 2);
  }

  public void testPut()
  {
    Assert.assertNull(map.put(Foo.toString(), 1));
    Assert.assertEquals(1, map.put("Foo", 2));
  }

  public void testTestPut()
  {
    Assert.assertNull(map.put(Foo));
    Assert.assertEquals(Foo, map.put(Foo.toString(), Boo));
    Assert.assertEquals(Boo, map.get(Foo.toString()));
  }

  public void testGet()
  {
    map.put(Foo);
    map.put(Boo);

    Assert.assertEquals(2, map.size());
    Assert.assertEquals(Foo, map.get(Foo.toString()));
    //noinspection SuspiciousMethodCalls
    Assert.assertEquals(Boo, map.get(Boo));
  }

  public void testWildCardKey()
  {
    map.put(WildCard);
    map.put(WildCardLowerCase);

    Assert.assertEquals(2, map.size());
    Assert.assertEquals(WildCard, map.get(Foo.toString()));
    Assert.assertEquals(WildCard, map.get("Foolish"));
    Assert.assertEquals(WildCardLowerCase, map.get("foolish"));
    Assert.assertNull(map.get("not-foolish"));
  }

  public void testToString()
  {
    Assert.assertEquals(Name, map.toString());
  }
}