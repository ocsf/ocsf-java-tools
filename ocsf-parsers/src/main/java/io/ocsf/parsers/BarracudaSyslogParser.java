/* *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/** 
 * Barracuda Web Security Gateway Log Parses
 * See: https://campus.barracuda.com/product/websecuritygateway/doc/168742383/syslog-and-the-barracuda-web-security-gateway/
 * 
*/

package io.ocsf.parsers;

import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.parsers.PatternParser;
import io.ocsf.utils.parsers.Syslog;

import java.util.Map;


public class BarracudaSyslogParser implements Parser
{   
  private static final String SourceType = "barracuda:syslog";
  private static final String Allowed = "ALLOWED";


    private static final String part1 = "#{timestamp: string(syslog-time)} #{year: string(syslog-time)} #{daemon}: #{epoch : integer} #{src-IP} " + 
    "#{dst-IP} #{content-type} #{src-IP2} #{destination-URL} #{data-size} BYF #{action} #{reason}";

    private static final String part2 =  " #{format-version} #{match-flag} #{tq-flag} #{action-type} #{src-type} " + 
    "#{src-detail} #{dst-type} #{dst-detail} #{spy-type} #{spy-id} #{infection-score} " +
    "#{matched-part} #{matched-category} #{user-info} #{referer-url} #{referer-domain} #{referer-category} " + 
    "#{wsa-remote-user-type}";

    private static final String Pattern = part1 + part2;

    /*
        If action is blocked then a details fields is added after reason
     */
    private static final String blockedPattern = part1 + " #{details} FOUND" + part2;


  private final Parser parser;

  public BarracudaSyslogParser()
  {
    this(Pattern);
  }

  public BarracudaSyslogParser(final String pattern)
  {
    this.parser = PatternParser.create(pattern);
  }

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    Map<String, Object> data = parser.parse(text);
    if(!Allowed.equals((String) data.get("action"))) {
        //Use blocked Pattern
        data = PatternParser.create(blockedPattern).parse(text);
    }
    return data;
  }

  @Override
  public String toString() {return SourceType;}
}