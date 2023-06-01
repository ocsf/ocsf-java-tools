/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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

package io.ocsf.schema.config;

import io.ocsf.schema.util.Maps;

import java.util.Map;

/**
 * This class provides read-only access to configuration data.
 */
public class Config<T>
{
  private final Map<String, T> config;

  public Config(final Map<String, T> config)
  {
    this.config = config;
  }

  /**
   * Returns the configuration value associated with a given <code>name</code>.
   *
   * @param name the configuration name
   * @return the config value, or <code>null</code> if no configuration is associated with the <code>name</code>
   */
  public T get(final String name) {return config.get(name);}

  public T get(final String name, final T defaultValue)
  {
    return Maps.typecast(config.getOrDefault(name, defaultValue));
  }

  public T getIn(final String name)
  {
    return Maps.typecast(Maps.getIn(Maps.typecast(config), name));
  }

  public T getIn(final String name, final T defaultValue)
  {
    final T value = Maps.typecast(Maps.getIn(Maps.typecast(config), name));

    return value != null ? value : defaultValue;
  }
}
