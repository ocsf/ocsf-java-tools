package io.ocsf.schema.cli;

import junit.framework.TestCase;
import org.junit.Assert;

public class MainTest extends TestCase
{
  private static final String RulePath = "src/main/dist/examples";
  private static final String FileName =
    "src/main/dist/examples/crowdstrike/falcon/end_of_process/rule.json";

  public void testResolveFile()
  {
    try
    {
      Assert.assertEquals(FileName, Main.resolveFile(RulePath, FileName));
    }
    catch (final Exception e)
    {
      Assert.fail(e.toString());
    }
  }

  public void testResolveRelativeFile()
  {
    try
    {
      Assert.assertEquals(FileName, Main.resolveFile(RulePath, "crowdstrike/falcon/end_of_process/rule.json"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.toString());
    }
  }

  public void testResolvePartialName()
  {
    try
    {
      Assert.assertEquals(FileName, Main.resolveFile(RulePath, "end_of_process"));
      Assert.assertEquals(FileName, Main.resolveFile(RulePath, "end_of_process/rule"));
      Assert.assertEquals(FileName, Main.resolveFile(RulePath, "end_of_process/rule.json"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.toString());
    }
  }
}