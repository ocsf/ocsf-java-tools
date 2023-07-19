
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
 * An immutable class representing a name/value tuple.
 */
public final class Tuple<Name, Value>
{
  public final Name  name;
  public final Value value;

  public Tuple(final Name name, final Value value)
  {
    this.name  = name;
    this.value = value;
  }

  @Override
  public String toString()
  {
    return String.format("(%s, %s)", name, value);
  }

  @Override
  public int hashCode()
  {
    return (name == null ? 0 : 31 * name.hashCode()) + (value == null ? 0 : value.hashCode());
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Tuple<?, ?>)) return false;

    final Tuple<?, ?> other = (Tuple<?, ?>) obj;
    if (name == null)
    {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;

    if (value == null)
      return other.value == null;

    return value.equals(other.value);
  }
}
