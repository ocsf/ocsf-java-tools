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

package io.ocsf.schema.cli;


import io.ocsf.utils.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Brief simulation of GNU's command line options for C/C++
 */
public class CommandLineParser
{
  private static final char MESSAGE_OPT = 1;

  public static class Argument
  {
    public final char   opt;
    public final String long_opt;

    public final String name;
    public final String desc;

    private String value;

    /**
     * Create a command line argument.
     *
     * @param opt      - must be alpha-numeric
     * @param long_opt - can be null, all alpha-numeric
     * @param name     - null if flag has no arg, otherwise this is its name (for help message)
     * @param desc     - used for the help message
     * @throws IllegalArgumentException if arg values are malformed
     */
    Argument(
      final char opt,
      final String long_opt,
      final String name,
      final String desc) throws IllegalArgumentException
    {
      if (!Character.isLetterOrDigit(opt))
      {
        throw new IllegalArgumentException("Expect single character for short opt");
      }

      if (!Strings.isEmpty(long_opt))
      {
        if (long_opt.charAt(0) == '-')
        {
          throw new IllegalArgumentException("long option cannot begin with '-'");
        }

        for (final char x : long_opt.toCharArray())
        {
          if (!Character.isLetterOrDigit(x) && x != '_' && x != '-')
          {
            throw new IllegalArgumentException("Only alphanumeric, '-' and '_' in long opt name");
          }
        }
        this.long_opt = long_opt;
      }
      else
      {
        this.long_opt = null;
      }

      this.opt  = opt;
      this.name = Strings.isEmpty(name) ? null : name;
      this.desc = desc;

      this.value = null;
    }

    /**
     * Not an actual argument, but used for help message only. Informational breaker.
     *
     * @param msg a help message
     */
    Argument(final String msg)
    {
      this.opt      = MESSAGE_OPT;
      this.long_opt = null;
      this.name     = null;
      this.value    = null;
      this.desc     = msg;
    }

    /**
     * Set the value of the arg
     *
     * @param value - the arg's value
     * @throws IllegalArgumentException if arg does not take a value.
     */
    private void setValue(final String value) throws IllegalArgumentException
    {
      if (name == null)
      {
        throw new IllegalArgumentException(String.format(
          "flag -%c does not take an argument",
          opt));
      }
      this.value = value;
    }

    /**
     * For flags that do not take a value - simply toggles that the flag was seen.
     *
     * @throws IllegalArgumentException if this flag expects an argument.
     */
    private void toggled() throws IllegalArgumentException
    {
      if (name != null)
      {
        throw new IllegalArgumentException(String.format("flag -%c expects an argument", opt));
      }
      value = "true";
    }

    private boolean isMessage()
    {
      return opt == MESSAGE_OPT;
    }

    public boolean isSet()
    {
      return value != null;
    }

    public String value()
    {
      return value;
    }
  }

  private final String name;

  private final String help;

  private final List<Argument>    arguments = new ArrayList<>();
  private final ArrayList<String> extraArgs = new ArrayList<>();

  private char helpOption = 0;

  private int longestLongArg = 0; // used in formatting the help message.

  /**
   * Create a command line parser.
   *
   * @param name - name of the program (for usage/help message)
   * @param help - used for usage/help text.
   */
  public CommandLineParser(final String name, final String help)
  {
    this.name = name;
    this.help = help;
  }

  /**
   * Add an arg line for the help message only
   *
   * @param message - help line message
   */
  public void add(final String message)
  {
    arguments.add(new Argument(message));
  }

  public void add(final char opt, final String long_opt, final String name, final String desc)
  {
    final Argument arg = new Argument(opt, long_opt, name, desc);

    int longLen = 0;

    if (getArg(arg.opt) != null)
    {
      throw new IllegalArgumentException(String.format("duplicate short arg %c", arg.opt));
    }

    if (arg.long_opt != null)
    {
      if (getLongArg(arg.long_opt) != null)
      {
        throw new IllegalArgumentException(String.format("duplicate long arg %s", arg.long_opt));
      }
      longLen += arg.long_opt.length();
    }

    longLen += arg.name != null ? arg.name.length() : 0;
    longestLongArg = Math.max(longLen, longestLongArg);

    arguments.add(arg);
  }

  private Argument getLongArg(final String longArg)
  {
    final Optional<Argument> arg =
      arguments.stream().filter(a -> a.long_opt != null && a.long_opt.equals(longArg)).findFirst();
    return arg.orElse(null);
  }

  public Argument getArg(final char o)
  {
    final Optional<Argument> arg =
      arguments.stream().filter(a -> a.opt > 0 && a.opt == o).findFirst();
    return arg.orElse(null);
  }

  /**
   * For convenience. Adds Posix standard -h/--help command line option.
   */
  public void addHelp()
  {
    addHelp('h', "help", "print this help message");
  }

  public void addHelp(final char helpOpt, final String helpLong, final String description)
  {
    add(helpOpt, helpLong, null, description);
    helpOption = helpOpt;
  }

  public void help()
  {
    System.err.println(buildHelp());
  }

  private String buildHelp()
  {
    final StringBuilder help = new StringBuilder();

    help.append("Usage: ")
        .append(this.name)
        .append(" <options> <files>\n")
        .append(this.help);

    for (final Argument arg : arguments)
    {
      if (arg.isMessage())
      {
        help.append('\n').append(arg.desc);
        continue;
      }

      help.append(String.format("    -%c", arg.opt));
      int thisLongLen = 0;
      if (arg.long_opt != null && arg.long_opt.length() > 1)
      {
        final int startLen = help.length();
        help.append(String.format(", --%s", arg.long_opt));
        thisLongLen = help.length() - startLen;
      }

      if (arg.name != null)
      {
        final int startLen = help.length();
        help.append(" <").append(arg.name).append(">");
        thisLongLen += help.length() - startLen;

      }

      // pad description to uniform len. (+7 for the added ", -- <>", +3 for the actual spacing
      final String pad = "%" + ((longestLongArg + 7 + 17) - thisLongLen) + "s%s%n";
      help.append(String.format(pad, " ", arg.desc));
    }
    return help.toString();
  }


  /**
   * parse the command line with argv. Args not processed by the command line flags are copied to
   * extraArgs. Exits program with error message on command line violations (unknown flag, or
   * missing arg on flag).
   *
   * @param argv - the command line args
   */
  public void parseCommandLine(final String[] argv)
  {
    int i;
    for (i = 0; i < argv.length && argv[i].charAt(0) == '-'; ++i)
    {
      final String flag = argv[i];
      final String name = flag.startsWith("--") ? flag.substring(2) : flag.substring(1);

      final Argument arg = name.length() > 1 ? getLongArg(name) : getArg(name.charAt(0));
      if (arg == null)
      {
        System.err.printf("Unknown argument flag %s. Use -%c for usage%n", flag, helpOption);
        System.exit(2);
      }

      if (arg.opt == helpOption)
      {
        help();
      }

      if (arg.name != null)
      {
        i += 1;
        if (i < argv.length)
        {
          arg.setValue(argv[i]);
        }
        else
        {
          System.err.printf("Flag %s - expecting argument <%s>%n", flag, arg.name);
          System.exit(2);
        }
      }
      else
      {
        arg.toggled();
      }
    }

    for (; i < argv.length; ++i)
         extraArgs.add(argv[i]);
  }

  /**
   * Get the remaining args not processed by command line flags.
   *
   * @return String array of remaining args. Size 0 if none.
   */
  public List<String> extraArgs()
  {
    return extraArgs;
  }
}
