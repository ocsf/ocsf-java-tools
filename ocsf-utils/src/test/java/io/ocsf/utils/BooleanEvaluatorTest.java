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

import junit.framework.TestCase;
import org.junit.Assert;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public final class BooleanEvaluatorTest extends TestCase
{
  public void testEvaluateFlat_BasicOperators()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("k1", "v1")
          .p("k2", 2)
          .p("k3.x", false)
          .p("k4", null);

    Tree node = BooleanExpression.parse("k1 = 'v1'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k2 != 10");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k3.x != null");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k4 is null");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k4 = null");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k2 < 10");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k1 <= 'v1'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k1 > 'v'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("k2 >= 2");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateNested_BasicOperators_WithStringAndNumberAndTimeValues() throws Exception
  {
    // Note DataSource assumes not "nested". With nested, should use Message type
    // hence, MessageEvaluation instead of Evaluator

    final String f1 = String.format("%6.4e", 1934.34343);
    final String f2 = String.format("%f", 12.856);

    final Date t1 = new SimpleDateFormat("MM/dd/yy HH:mm:ss z").parse("01/04/10 01:32:27 PST");
    final long t2 = Times.parse("+6h");

    // Note: the node can be of type Date, long or String to do the comparison. However,
    // to get to the comparison code block, the field has to be type Date
    final Date t3 = new Date();
    final Date t4 =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2016-11-06T15:07:36.665-08:00");

    final FMap<String, Object> map =
      FMap.<String, Object>b()
          .p(
            "top",
            FMap.b()
                .p("k1", "jane_do@server.com")
                .p("k2", f1)
                .p("k3", f2))
          .p(
            "middle",
            FMap.b()
                .p("k1", t1)    // just a value long
                .p("k2", t2)    // +/-[mdh] +3m, -2d, +5h
                .p("k3", t3)    // system's current millis
                .p(
                  "k4",
                  t4)); // negative integer, -30000, 30
    // seconds ago

    final Maps.Supplier<Object> data = name -> Maps.getIn(map, name);

    // Note that string comparison is case-insensitive
    Tree node = BooleanExpression.parse("top.k1 = 'Jane_Do@Server.com'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("top.k1 != 'Jane_Do@Server.com'");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));

    // Numeric values are numbers in the java string format
    node = BooleanExpression.parse("top.k1 != " + f1);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("top.k3 = " + f2);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

//        // Note: Cannot compared zoned time
//        // 01/04/10 01:32:27 UTC vs 01/04/10 01:32:27 PST

    // Absolute time values, regardless of format, converting to long for
    // comparison should be fine
    final long t22 = Times.parse("+6h");
    node = BooleanExpression.parse("middle.k2 <= " + t22);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // Parser only breaks up type token.DATE in case of '`' so the string
    // representing the date time value should be enclosed in `` inside the
    // quotes
    final String t33 = "`-40000`";
    node = BooleanExpression.parse("middle.k3 > " + t33);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    final String t44 = "`2013-11-06T15:07:36.665-08:00`";
    node = BooleanExpression.parse("middle.k4 > " + t44);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // Compare time value in different format
    final String t55 = "`09/20/17 13:18:57`";
    node = BooleanExpression.parse("middle.k4 < " + t55);
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateFlatOperatorInAndNot_in()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("port", 80)
          .p("ip", "192.168.10.33")
          .p("host", "production")
          .p("database", null);

    final String One = "port = 88";
    final String Exp =
      "((port in [80,8080]) and ((ip = \"192.168.1.19\") or (host = 'production')))";

    Assert.assertFalse(BooleanEvaluator.evaluate(BooleanExpression.parse(One), data));
    Assert.assertTrue(BooleanEvaluator.evaluate(BooleanExpression.parse(Exp), data));
    Assert.assertTrue(BooleanEvaluator.evaluate(BooleanExpression.parse("port = 80"), data));

    Tree node = BooleanExpression.parse("port in [80,8080,9093,8901,23]");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse(
      "ip not_in ['192.168.1.19','192.168.1.20','192.168.10.30','192.168.1.11']");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node =
      BooleanExpression.parse("host in [\"production\",\"dev\",\"test\"] and database is null");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // "null in []" will return false
    node = BooleanExpression.parse("database not_in ['couchbase','mongodb','orientdb']");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateFlatOperatorLikeAndNotLike()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("city", "Buenos Aires")
          .p("name", "Jane Do")
          .p("id", 27)
          .p("address", "72 Gustavo Moncada 8585 Piso 20-A");

    Tree node = BooleanExpression.parse("city starts_with 'B'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(city like 'b*')");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(city like 'culver')");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("city like '* Aires'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(name like 'ne  D')");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // "Piso" vs "PISO"
    node = BooleanExpression.parse("address contains '8585 PISO 20-'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(address contains '8585 PISO 20-')");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateFlatOperatorMatchAndNotMatch()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("product", "Internet Security")
          .p("company", "Symantec")
          .p("employeeId", 7086)
          .p("email", "jane_do@example.com")
          .p("version", "17.9.0.10")
          .p("address", "22 Corporate Drive, The City, CA 92022");

    Tree node = BooleanExpression.parse("product match 'Internet.*'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("version match '[0-9]{1,2}.[0-9]{1,2}.[0-9]{1,2}.[0-9]{1,2}'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // make sure the escape sequences are escaped in the string
    node = BooleanExpression.parse(
      "email match '^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse(
      "!(email match '^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$')");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(address like '*city*') and address like '*92022'");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("address like '*city*'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("!(address like '*city*')");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateFlatOperatorsAndOr()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("id", 42)
          .p("name", "Karly Britta")
          .p("contact", "Ruby Carly")
          .p("address", "22 Street, The Big City")
          .p("code", "4100")
          .p("country", "BG");

    Tree node =
      BooleanExpression.parse("id = 32701 or (contact starts_with 'ruby' and country like 'bg')");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse(
      "id in [32700,32702,32789] or (code starts_with '41' and country like 'bg')");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("address like 'Canada' Or (contact LIKE '*carly' AND id != 22)");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse("name != null anD (contact like 'karla*' OR id = 42)");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateNestedOperatorsAndOr() throws Exception
  {
    final Date date = new SimpleDateFormat("MM/dd/yy HH:mm:ss z").parse("08/17/16 01:32:27 PST");
    final FMap<String, Object> map =
      FMap.<String, Object>b()
          .p(
            "top",
            FMap.b()
                .p("Company", "SkyLimited")
                .p("EmployeeId", 32)
                .p("EmployeeName", "Yuri Yu"))
          .p(
            "middle",
            FMap.b()
                .p("OrderId", 32701)
                .p("CustomerId", 10108)
                .p("ShipId", 11))
          .p(
            "bottom",
            FMap.b()
                .p("Date", date));

    final Maps.Supplier<Object> data = name -> Maps.getIn(map, name);

    Tree node = BooleanExpression.parse(
      "top.Company = 'whatLimited' or ( middle.CustomerId = 10108 and middle.ShipId != 11)");
    Assert.assertFalse(BooleanEvaluator.evaluate(node, data));

    node = BooleanExpression.parse(
      "!(top.EmployeeName like 'Lucy') Or (middle.ShipId = 12 and middle.OrderId != 32700)");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));

    // Note: non-existent key error is suppressed
    node =
      BooleanExpression.parse("bottom.Date != null anD (id = 32700 OR top.EmployeeId = 32)");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, data));
  }

  public void testEvaluateArray()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("name", "Joe")
          .p("array", Arrays.asList(1, 2, 3, 22))
          .p("list", Arrays.asList(
            FMap.b()
                .p("Company", "SkyLimited")
                .p("EmployeeId", 20)
                .p("EmployeeName", "Yuri Yu"),
            FMap.b()
                .p("Company", "Acme")
                .p("EmployeeId", 22)
                .p("EmployeeName", "Yuri"),
            FMap.b()
                .p("Company", "SkyLimited")
                .p("EmployeeId", 42)
                .p("EmployeeName", "Yu Vu"),
            FMap.b()
                .p("Company", "Acme")
                .p("EmployeeId", 11)
                .p("EmployeeName", "Li Li")
          ));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("array contains 22"), data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("array contains 22 and name != 'Joe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("list exec Company = 'Acme'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(list exec Company = 'Acme') and name = 'Joe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "list exec Company = 'SkyLimited' and EmployeeId = 20 and EmployeeName != null"),
        data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "((list exec EmployeeId = 20) or (list exec EmployeeId = 20)) and (list exec Company != null)"),
        data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("list exec Company = 'SkyLimited' and EmployeeId = 11"), data));
  }

  public void testEvaluateMap()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("name", "Joe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("EmployeeId", 22)
                .p("EmployeeName", "Joe Dow")
          );

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data exec Company = 'Acme'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(data exec Company = 'Acme') and name = 'Joe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "data exec Company like 'ac*' and (EmployeeId = 20 or EmployeeId = 22)"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "((data exec EmployeeId = 22) or (data exec EmployeeId = 11)) and (data exec Company != null)"),
        data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data exec Company = 'SkyLimited' and EmployeeId = 22"), data));

  }

  public void testEvaluateMapEx()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("first name", "Joe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("Employee Id", 22)
                .p("Employee Name", "Joe Dow")
          );

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data exec Company = 'Acme'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(data exec Company = 'Acme') and 'first name' like 'Joe'"),
        data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "data exec Company like 'ac*' and ('Employee Id' = 20 or 'Employee Id' = 22)"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "((data exec 'Employee Id' = 22) or (data exec 'Employee Id' = 11)) and (data exec Company != null)"),
        data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data exec Company = 'SkyLimited' and 'Employee Id' = 22"),
        data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(data exec Company like 'Ac*') and 'first name' = 'Joe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(data exec Company starts_with 'A') and 'first name' = 'Joe'"),
        data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse(
          "(data exec Company ends_with 'me') and 'first name' starts_with 'Joe'"), data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(data exec Company ends_with 'cm')"), data));

  }

  public void testLike()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("first name", "Joe Doe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("Employee Id", 22)
                .p("Employee Name", "Joe Doe")
          );

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("nothing like null"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("'first name' like '.*doe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("'first name' like 'Joe.*'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("'first name' like 'Joe *'"), data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("'first name' like 'boo'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data exec Company like 'ac.*'"), data));
  }

  public void testLike2()
  {
    final FMap<String, Object> map1 =
      FMap.<String, Object>b()
          .p("name", "Joe Doe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("Employee Id", 22)
                .p("Employee Name", "Joe Doe")
          );

    final FMap<String, Object> map2 =
      FMap.<String, Object>b()
          .p("name", "Jane Doe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Access Inc.")
                .p("Employee Id", 42)
                .p("Employee Name", "Jane Doe")
          );

    Tree node = BooleanExpression.parse("nothing like null");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map1));
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map2));

    node = BooleanExpression.parse("'name' like '.*doe'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map1));
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map2));

    node = BooleanExpression.parse("data exec Company like 'Ac.*'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map1));
    Assert.assertTrue(BooleanEvaluator.evaluate(node, map2));

    node = BooleanExpression.parse("data.Company like 'Ac.*'");
    Assert.assertTrue(BooleanEvaluator.evaluate(node, key -> Maps.getIn(map1, key)));
    Assert.assertTrue(BooleanEvaluator.evaluate(node, key -> Maps.getIn(map2, key)));
  }


  public void testContains()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("name", "Joe Doe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("Employee Id", 22)
                .p("Employee Name", "Joe Doe")
          );

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("data contains 'Joe Doe'"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("'data.Employee Name' contains 'Joe Doe'"), key -> Maps.getIn(data, key)));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("nothing contains null"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("(name contains 'doe')"), data));
  }

  public void testNot()
  {
    final FMap<String, Object> data =
      FMap.<String, Object>b()
          .p("name", "Joe Doe")
          .p(
            "data",
            FMap.b()
                .p("Company", "Acme")
                .p("Employee Id", 22)
                .p("Employee Name", "Joe Doe")
          );

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("not (data contains 'Jane Doe')"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("! (data contains 'Jane Doe')"), data));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("not ('data.Employee Name' contains 'Joe Doe')"), key -> Maps.getIn(data, key)));

    Assert.assertFalse(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("not ((nothing == null) and (name contains 'doe'))"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("name contains 'doe' and !(nothing != null)"), data));

    Assert.assertTrue(
      BooleanEvaluator.evaluate(
        BooleanExpression.parse("name contains 'doe' and not (nothing != null)"), data));
  }

}
