/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.richeditor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.core.richeditor.RichEditorConfiguration;
import io.meeds.social.core.richeditor.RichEditorConfigurationPlugin;
import io.meeds.social.core.richeditor.RichEditorConfigurationService;

public class RichEditorConfigurationServiceImpl implements RichEditorConfigurationService {

  private static final Log                             LOG                      =
                                                           ExoLogger.getLogger(RichEditorConfigurationServiceImpl.class);

  public static final String                           ALL_INSTANCES_KEY        = StringUtils.EMPTY;

  private ConfigurationManager                         configurationManager;

  private Map<String, List<RichEditorConfiguration>>   configurationFilesByType = new HashMap<>();

  private final FutureExoCache<String, String, Object> configurationContentFutureCache;

  private final ExoCache<String, String>               configurationContentCache;

  public RichEditorConfigurationServiceImpl(CacheService cacheService, ConfigurationManager configurationManager) {
    this.configurationManager = configurationManager;
    this.configurationContentCache = cacheService.getCacheInstance("social.richEditorConfiguration");
    this.configurationContentFutureCache = new FutureExoCache<>(new Loader<String, String, Object>() {
      @Override
      public String retrieve(Object context, String instanceType) throws Exception {
        return appendFilesContent(instanceType);
      }
    }, configurationContentCache);
    if (PropertyManager.isDevelopping()) {
      configurationContentCache.setMaxSize(0);
    }
  }

  @Override
  public String getRichEditorConfiguration(String instanceType) {
    if (StringUtils.isBlank(instanceType)) {
      instanceType = ALL_INSTANCES_KEY;
    }
    return configurationContentFutureCache.get(null, instanceType);
  }

  @Override
  public void addPlugin(RichEditorConfigurationPlugin plugin) {
    List<RichEditorConfiguration> richEditorConfigurations = plugin.getRichEditorConfigurations();
    if (richEditorConfigurations != null) {
      richEditorConfigurations.forEach(richEditorConfiguration -> {
        String instanceType = richEditorConfiguration.getInstanceType();
        configurationFilesByType.computeIfAbsent(StringUtils.isBlank(instanceType) ? ALL_INSTANCES_KEY : instanceType,
                                                 key -> new ArrayList<>())
                                .addAll(richEditorConfigurations);
      });
    }
  }

  protected String appendFilesContent(String instanceType) {
    StringBuilder fileContent = new StringBuilder();
    appendFilesContent(fileContent, ALL_INSTANCES_KEY);
    if (StringUtils.isNotBlank(instanceType)) {
      appendFilesContent(fileContent, instanceType);
    }
    return fileContent.toString();
  }

  private void appendFilesContent(StringBuilder fileContent, String instanceType) {
    List<RichEditorConfiguration> richEditorConfigurations = configurationFilesByType.get(instanceType);
    if (CollectionUtils.isNotEmpty(richEditorConfigurations)) {
      appendFilesContent(fileContent, richEditorConfigurations);
    }
  }

  private void appendFilesContent(StringBuilder fileContent, List<RichEditorConfiguration> richEditorConfigurations) {
    richEditorConfigurations.forEach(richEditorConfiguration -> {
      try {
        InputStream is = configurationManager.getInputStream(richEditorConfiguration.getFilePath());
        String content = IOUtils.toString(is, StandardCharsets.UTF_8);
        // Avoid interpreting JS variables using JVM properties
        // To do it, replace $ inside `` by ### before interpreting JVM
        // properties
        content = content.replace("${", "@JSProp{");
        content = content.replace("@JVMProp", "$");
        content = Deserializer.resolveVariables(content);
        content = content.replace("@JSProp{", "${");
        fileContent.append(content).append("\n");
      } catch (Exception e) {
        LOG.warn("Error retrieving Rich Editor file content from path {}", richEditorConfiguration.getFilePath(), e);
      }
    });
  }
}
