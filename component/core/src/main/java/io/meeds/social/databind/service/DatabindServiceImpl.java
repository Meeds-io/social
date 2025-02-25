/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.databind.service;

import io.meeds.social.databind.plugin.DatabindPreferencePlugin;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DatabindServiceImpl implements DatabindService {

  private final Map<String, DatabindPreferencePlugin> dataPreferencePlugins = new ConcurrentHashMap<>();

  @Override
  public void addDataPreferencePlugin(DatabindPreferencePlugin plugin) {
    dataPreferencePlugins.put(plugin.getDataType(), plugin);
  }

  @Override
  public File serialize(String objectType, List<String> objectIds, String username) throws ObjectNotFoundException,
                                                                                    IllegalAccessException {

    DatabindPreferencePlugin plugin = dataPreferencePlugins.get(objectType);
    if (plugin == null) {
      throw new IllegalArgumentException("No plugin found for objectType: " + objectType);
    }
    File zipFile;
    try {
      zipFile = File.createTempFile(objectType + "_", ".zip");
      try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zipOutputStream = new ZipOutputStream(fos)) {
        for (String objectId : objectIds) {
          File tempFile = File.createTempFile(objectType + "_" + objectId, ".json");
          plugin.serialize(objectId, tempFile, username);
          try (FileInputStream fis = new FileInputStream(tempFile)) {
            ZipEntry zipEntry = new ZipEntry(objectId + ".json");
            zipOutputStream.putNextEntry(zipEntry);
            fis.transferTo(zipOutputStream);
            zipOutputStream.closeEntry();
          }
          Files.delete(tempFile.toPath());
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error exporting template", e);
    }
    return zipFile;
  }
}
