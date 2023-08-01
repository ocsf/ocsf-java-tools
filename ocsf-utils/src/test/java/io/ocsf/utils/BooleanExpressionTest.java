/*
 * Copyright 2023 Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ocsf.utils;

public final class BooleanExpressionTest
{
  private BooleanExpressionTest() {}

  private static void test(final String exp)
  {
    try
    {
      final Tree root = BooleanExpression.parse(exp);

      System.out.println(exp);
      System.out.printf("   => %s%n", root.toString());
      System.out.printf("   => %s%n%n", root.asString());
    }
    catch (final Exception e)
    {
      System.err.printf("  *** ERROR: %s%n%n", e.getMessage());
    }
  }

  private static void testUnicode()
  {
    System.out.println(BooleanExpression.parse("name =\"我的公司\""));
    System.out.println(BooleanExpression.parse("name = \"默认组\""));
    System.out.println(BooleanExpression.parse("name = '☯☃☠☕♛'"));
    System.out.println(BooleanExpression.parse("name = '你好，世界'"));

    System.out.println(BooleanExpression.parse("name='\\u4F60\\u597D\\uFF0C\\u4E16\\u754C'"));
  }

  public static void main(final String[] args)
  {
    testUnicode();

    System.out.println();

    test(
      "peripheral_device.name = \"USBSTOR\\\\Disk&Ven_General&Prod_UDisk&Rev_5" +
      ".00\\\\1312290111142051606211&0\"");
    test("file.name match \".*\\\\?.*\"");

    test("id in [ 1,2,3,4 ] and type_id = 9000 and id != null");
    test("id < 0");

    test("age = -0.05e-10");
    test("age = .05e-10");


    test("product = \"Internet Security\" and module=5024");
    test("product = \"Internet Security\" or module !=\"5024\"");

    test("product match \"acme\" and module !=\"5024\" and version = \"16.7.0.30\"");
    test("address match '[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}'");

    test("host = 'hello.com'");
    test("host like \"hello.com\"");
    test("host contains 'hello'");
    test("host != null");
    test("port in [10,22,32]");
    test("error in [\"0\",\"1\",\"2\"]");
    test("port >= 1024");
    test("port >= 80 and ip = \"192.168.1.19\"");
    test("((port >= 80) and ((ip = \"192.168.1.19\") or (host = 'test')))");
    test(
      "((port >= 80) and ((ip = \"192.168.1.19\") or (host = \"test\"))) and ((port = -1) and (" +
      "(ip > 123456) or (host == \"test\")))");
    test("(port = -1) and ((ip > 123456) or (host is null))");
    test("((port == 88) and (ip=\"192.168.0.0\"))");
    test("time > `09/21/07 14:18:57`");
    test("@email:user.email match \"(\\\\w+)@(\\\\w+\\\\.)(\\\\w+)(\\\\.\\\\w+)*\"");
    test("(device_ip match '[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}') and module != \"5024\"");
    test("");

    test("(a = \"b'c\")");

    test("array exec 22");
    test("array exec 22 and 'first name' != 'Joe'");
    test(
      "((list exec 'Employee Id' = 20) or (list exec 'Employee Id' = 20)) and (list exec Company != null)");

    test("'file size' = 22");
    test("'file.size' = 22");

    test("'file.name' ends_with '/'");
  }
}
