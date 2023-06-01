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

package io.ocsf.schema.transformers;

import io.ocsf.schema.util.Json5Parser;
import io.ocsf.schema.util.Maps;
import io.ocsf.schema.util.ParserException;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TransformerTest extends TestCase
{
  private static final String JsonData = "{name: 'foo', port:42, rule: 'test data'}";
  private Map<String, Object> data;

  @Override
  public void setUp() throws ParserException
  {
    data = Json5Parser.to(JsonData);
  }

  // test empty rules: no rules transformations
  public void testEmptyRules() throws IOException
  {
    Assert.assertEquals(data, Transformer.fromString("{}").apply(data));
    Assert.assertEquals(data, Transformer.fromString("{rules:[]}").apply(data));
  }

  public void testSetValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}]}")
        .apply(new HashMap<>());

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(22, translated.get("port"));
  }

  public void testSetValueEx() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}, {port: {@value: {value: 42, overwrite: false}}}]}")
        .apply(new HashMap<>());

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(22, translated.get("port"));
  }

  public void testCondSetValue1() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: {value: 22, overwrite: false, when: 'port = 42'}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(22, translated.get("port"));
  }

  public void testCondSetValue2() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: {value: 22, when: 'port != 42'}}}]}")
        .apply(data);

    Assert.assertEquals(0, translated.size());
    Assert.assertNull(translated.get("port"));
  }

  public void testClone() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}, {port: {@clone: 'dst.port'}}]}")
        .apply(new HashMap<>());

    Assert.assertEquals(2, translated.size());
    Assert.assertEquals(22, translated.get("port"));
    Assert.assertEquals(22, Maps.getIn(translated, "dst.port"));
  }

  public void testCloneEx() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}, {port: {@clone: {name: 'dst.port'}}}]}")
        .apply(new HashMap<>());

    Assert.assertEquals(2, translated.size());
    Assert.assertEquals(22, translated.get("port"));
    Assert.assertEquals(22, Maps.getIn(translated, "dst.port"));
  }

  public void testCondClone1() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}, {port: {@clone: {name: 'dst.port', when: 'port = 42'}}}]}")
        .apply(data);

    Assert.assertEquals(3, data.size());
    Assert.assertEquals(42, data.get("port"));
    Assert.assertEquals(2, translated.size());
    Assert.assertEquals(22, translated.get("port"));
    Assert.assertEquals(22, Maps.getIn(translated, "dst.port"));
  }

  public void testCondClone2() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@value: 22}}, {port: {@clone: {name: 'dst.port', when: 'port = 22'}}}]}")
        .apply(data);

    Assert.assertEquals(3, data.size());
    Assert.assertEquals(42, data.get("port"));
    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(22, translated.get("port"));
    Assert.assertNull(Maps.getIn(translated, "dst.port"));
  }

  public void testMoveValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@move: 'port'}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(42, translated.get("port"));
    Assert.assertNull(data.get("port"));
  }

  public void testCondMoveValue1() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@move: {name: 'port', when: 'port = 42'}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(42, translated.get("port"));
    Assert.assertNull(data.get("port"));
  }

  public void testCondMoveValue2() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@move: {name: 'port', when: 'port != 42'}}}]}")
        .apply(data);

    Assert.assertEquals(0, translated.size());
  }

  public void testMoveArrayValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
      .fromString("{rules: [{port: {@move: {name: 'port', is_array: true}}}]}")
      .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertTrue(translated.get("port") instanceof List<?>);
  }

  public void testCopyValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@copy: 'port'}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(42, translated.get("port"));
    Assert.assertEquals(42, data.get("port"));
  }

  public void testCondCopyValue1() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@copy: {name: 'port', when: 'port = 42'}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(42, translated.get("port"));
    Assert.assertEquals(42, data.get("port"));
  }

  public void testCondCopyValue2() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@copy: {name: 'port', when: 'port != 42'}}}]}")
        .apply(data);

    Assert.assertNull(translated.get("port"));
    Assert.assertEquals(0, translated.size());
    Assert.assertEquals(42, data.get("port"));
  }

  public void testCopyArrayValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
      .fromString("{rules: [{port: {@copy: {name: 'port', is_array: true}}}]}")
      .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertTrue(translated.get("port") instanceof List<?>);
  }

  public void testRemoveValue() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@remove: 'port'}}]}")
        .apply(data);

    Assert.assertEquals(0, translated.size());
    Assert.assertNull(data.get("port"));
  }

  public void testCondRemoveValue1() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@remove: 'port', when: 'port = 42'}}]}")
        .apply(data);

    Assert.assertNull(data.get("port"));
    Assert.assertEquals(2, data.size());
    Assert.assertEquals(0, translated.size());
  }

  public void testCondRemoveValue2() throws IOException
  {
    final Map<String, Object> translated = Transformer
        .fromString("{rules: [{port: {@remove: {when: 'port != 42'}}}]}")
        .apply(data);

    Assert.assertNotNull(data.get("port"));
    Assert.assertEquals(3, data.size());
    Assert.assertEquals(0, translated.size());
  }

  public void testEnumValue() throws IOException
  {
    Map<String, Object> translated = Transformer
        .fromString("{rules:[{Opcode:{@enum: {name: 'id', default: 0, other: 'status', values: {'0':1, '1':2 }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(0, translated.get("id"));

    translated = Transformer
        .fromString("{rules:[{name:{@enum: {name: 'name', default: 'none', values: {'Foo':'boo', 'hello':'there' }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals("boo", translated.get("name"));
    Assert.assertNull(data.get("name"));

    translated = Transformer
        .fromString("{rules:[{port:{@enum: {name: 'src_port', default: 22, values: {42:80, 41:8080 }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(80, translated.get("src_port"));
    Assert.assertNull(data.get("port"));
  }

  public void testCondEnumValue1() throws IOException
  {
    Map<String, Object> translated = Transformer
        .fromString("{rules:[{Opcode:{@enum: {when: 'port = 42', name: 'id', default: 0, other: 'status', values: {'0':1, '1':2 }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(0, translated.get("id"));

    translated = Transformer
        .fromString("{rules:[{name:{@enum: {when: 'name = \"foo\"', name: 'name', default: 'none', values: {'Foo':'boo', 'hello':'there' }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals("boo", translated.get("name"));
    Assert.assertNull(data.get("name"));

    translated = Transformer
        .fromString("{rules:[{port:{@enum: {when: 'port = 42', name: 'src_port', default: 22, values: {42:80, 41:8080 }}}}]}")
        .apply(data);

    Assert.assertEquals(1, translated.size());
    Assert.assertEquals(80, translated.get("src_port"));
    Assert.assertNull(data.get("port"));
  }

  public void testCondEnumValue2() throws IOException
  {
    Map<String, Object> translated = Transformer
        .fromString("{rules:[{Opcode:{@enum: {when: 'port != 42', name: 'id', default: 0, other: 'status', values: {'0':1, '1':2 }}}}]}")
        .apply(data);

    Assert.assertEquals(3, data.size());
    Assert.assertEquals(0, translated.size());

    translated = Transformer
        .fromString("{rules:[{name:{@enum: {when: 'name != \"foo\"', name: 'name', default: 'none', values: {'Foo':'boo', 'hello':'there' }}}}]}")
        .apply(data);

    Assert.assertEquals(3, data.size());
    Assert.assertEquals(0, translated.size());

    translated = Transformer
        .fromString("{rules:[{port:{@enum: {when: 'port != 42', name: 'src_port', default: 22, values: {42:80, 41:8080 }}}}]}")
        .apply(data);

    Assert.assertEquals(3, data.size());
    Assert.assertEquals(0, translated.size());
  }

  public void testPathToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'path': '/tmp/test.txt'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("/tmp/test.txt", file.get("path"));
    Assert.assertEquals("test.txt", file.get("name"));
    Assert.assertEquals("/tmp", file.get("parent_folder"));
  }

  public void testNameToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'path': 'test.txt'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path'}}}]}")
        .apply(data);

    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("test.txt", file.get("path"));
    Assert.assertEquals("test.txt", file.get("name"));
    Assert.assertNull(file.get("parent_folder"));
  }

  public void testNullToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'foo': 'test.txt'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path'}}}]}")
        .apply(data);

    Assert.assertNotNull(translated);
    Assert.assertNull(Maps.typecast(translated.get("file")));
  }

  public void testRootFileToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'path': '/test.txt'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("/test.txt", file.get("path"));
    Assert.assertEquals("test.txt", file.get("name"));
    Assert.assertEquals("/", file.get("parent_folder"));
  }

  public void testRootToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'path': '/'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("/", file.get("path"));
    Assert.assertEquals("/", file.get("name"));
    Assert.assertNull(file.get("parent_folder"));
  }

  public void testEmptyStringToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'path': ''}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{path:{@move: {name: 'file', type: 'path:1'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("", file.get("name"));
    Assert.assertEquals("", file.get("path"));
    Assert.assertNull(file.get("parent_folder"));
  }

  public void testMissingFieldToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{'cmd': 'hello.exe'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{cmd: {@move: 'actor.process.cmd_line'}},{path:{@move: {name: 'actor.process.file', type: 'path'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(Maps.getIn(translated, "actor.process.file"));
    Assert.assertNull(file);
  }

  public void testObjectToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{file: {name: '/tmp/test.txt', size: 42}}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{file: [{name:{@move: {name: 'file', type: 'path'}}},{size:{@move: {name: 'file.size'}}}]}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("/tmp/test.txt", file.get("path"));
    Assert.assertEquals("test.txt", file.get("name"));
    Assert.assertEquals("/tmp", file.get("parent_folder"));
    Assert.assertEquals(42, file.get("size"));
  }

  public void testMoveFieldsToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{path: '/tmp', name: 'test'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{'path,name':{@move: {separator: '/', name: 'file', type: 'path:1'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("test", file.get("name"));
    Assert.assertEquals("/tmp/test", file.get("path"));
    Assert.assertEquals("/tmp", file.get("parent_folder"));
    Assert.assertEquals(0, data.size());
  }

  public void testCopyFieldsToFile() throws IOException
  {
    final Map<String, Object> data = Json5Parser.to("{path: '/tmp', name: 'test'}");
    final Map<String, Object> translated = Transformer
        .fromString("{rules:[{'path, name':{@copy: {separator: '/', name: 'file', type: 'path:1'}}}]}")
        .apply(data);
    Assert.assertNotNull(translated);

    final Map<String, Object> file = Maps.typecast(translated.get("file"));
    Assert.assertNotNull(file);

    Assert.assertEquals("test", file.get("name"));
    Assert.assertEquals("/tmp/test", file.get("path"));
    Assert.assertEquals("/tmp", file.get("parent_folder"));
    Assert.assertEquals(2, data.size());
  }

}
