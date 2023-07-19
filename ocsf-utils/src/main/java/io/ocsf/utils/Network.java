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

/**
 * This class represents an IPV4 network and provides methods for checking if a given IPV4 address
 * belongs to this network.
 *
 * <p> For example, 192.168.1.0/24 is the prefix of the IPv4 network
 * starting at the given address, having 24 bits allocated for the network number, and the rest (8
 * bits) reserved for host addressing. </p>
 */
public final class Network implements Comparable<Network>
{
  private transient String string;
  private final     int    maskedBits;
  private final     int    address;

  public Network(final String mask) throws IllegalArgumentException
  {
    final int x    = mask.indexOf('/');
    final int addr = parse(mask, x);
    final int len  = mask.length();

    int bits = 0;
    for (int i = x + 1, d = 0; i < len; ++i, ++d)
    {
      if (d == 3) throw new IllegalArgumentException("Illegal IP mask");

      final char c = mask.charAt(i);

      if (c < '0' || c > '9') throw new IllegalArgumentException("Illegal number in the mask");

      if (d++ > 0) bits *= 10;

      bits += (c - '0');
    }

    if (bits == 32)
      bits = 0;
    else if (bits <= 0 || bits > 31)
      throw new IllegalArgumentException("Illegal number of bits in the mask");

    maskedBits = bits;
    address    = addr & MASKS[bits];
  }

  private static int parse(final String addr, final int length)
  {
    int dots        = 0;
    int numOfDigits = 0;
    int n           = 0;
    int a           = 0;

    //get a local copy of the IP address string
    final char[] b = new char[length];
    addr.getChars(0, length, b, 0);

    for (final char c : b)
    {
      if (c == '.')
      {
        if (n > 255) throw new IllegalArgumentException(addr);

        a <<= 8;
        a += n;
        numOfDigits = n = 0;

        // IP address has 3 dots
        if (++dots > 3) return a;
      }
      else if (c >= '0' && c <= '9')
      {
        // each number have 3 digits at most
        if (++numOfDigits > 3) break;

        n = n * 10 + (c - '0');
      }
      else
        throw new IllegalArgumentException(addr);
    }

    if (dots < 3) throw new IllegalArgumentException(addr);

    if (n > 255) throw new IllegalArgumentException(addr);

    // add the last number
    return (a << 8) + n;
  }

  @Override
  public String toString()
  {
    if (string == null)
    {
      final StringBuilder sb = new StringBuilder(16);
      sb.append(toString(address));

      if (maskedBits != 0)
      {
        sb.append('/');
        sb.append(maskedBits);
      }
      else
        sb.append("/32");

      string = sb.toString();
    }

    return string;
  }

  @Override
  public boolean equals(final Object other)
  {
    if (this == other)
    {
      return true;
    }

    if (!(other instanceof Network))
    {
      return false;
    }

    final Network that = (Network) other;

    return that.address == address && that.maskedBits == maskedBits;
  }

  @Override
  public int hashCode()
  {
    return Long.valueOf(address | maskedBits).hashCode();
  }

  private static final int[] MASKS = new int[32];

  static
  {
    MASKS[0] = 0xFFFFFFFF;

    int m = 0;
    for (int i = 1; i < MASKS.length; i++)
    {
                 m |= 0x80000000;
      MASKS[i] = m;
                 m >>= 1;
    }
  }

  /**
   * @param ip IP address
   * @return if accepted by mask
   */
  public boolean hasMember(final String ip)
  {
    if (ip != null)
    {
      try
      {
        final int addr = parse(ip, ip.length());
        final int x    = addr & MASKS[maskedBits];

        return x == address;
      }
      catch (final IllegalArgumentException e)
      {
        // not an IP address, therefore not a member of the network
      }
    }
    return false; // null address is not a member of the network
  }

  public boolean hasMember(final int address)
  {
    return (address & MASKS[maskedBits]) == this.address;
  }

  public boolean hasMember(final long address)
  {
    return ((int) address & MASKS[maskedBits]) == this.address;
  }

  /**
   * Convert an integer IP address to dot-separated string form.
   */
  private static final char[] digits    = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
  private static final int[]  sizeTable = {9, 99, 999};

  // Requires positive x
  private static int stringSize(final int x)
  {
    for (int i = 0; ; i++)
    {
      if (x <= sizeTable[i])
      {
        return i + 1;
      }
    }
  }

  private static void getChars(int i, int index, final char[] buf)
  {
    do
    {
      final int q = (i * 52429) >>> (16 + 3);
      final int r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
      buf[--index] = digits[r];
      i            = q;
    }
    while (i != 0);
  }

  private static String toString(int a)
  {
    final char[] buf = new char[16];

    final int d = a & 0xFF;
    a >>= 8;

    final int c = a & 0xFF;
    a >>= 8;

    final int b = a & 0xFF;
    a >>= 8;

    a &= 0xFF;

    int index = stringSize(a);
    getChars(a, index, buf);
    buf[index++] = '.';

    index += stringSize(b);
    getChars(b, index, buf);
    buf[index++] = '.';

    index += stringSize(c);
    getChars(c, index, buf);
    buf[index++] = '.';

    index += stringSize(d);
    getChars(d, index, buf);

    return new String(buf, 0, index);
  }

  @Override
  public int compareTo(final Network other)
  {
    if (maskedBits < other.maskedBits) return -1;

    if (maskedBits > other.maskedBits) return 1;

    final long a = address;
    final long b = other.address;
    if (a != b)
    {
      final long result = (a >>> 16) - (b >>> 16);
      return result == 0 ? (int) ((a & 0xffff) - (b & 0xffff)) : (int) result;
    }

    return 0;
  }
}
