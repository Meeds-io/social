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

import io.meeds.social.databind.model.DatabindReport;
import io.meeds.social.databind.plugin.DatabindPlugin;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class DatabindServiceImpl implements DatabindService {

  private final Map<String, DatabindPlugin> dataPreferencePlugins = new ConcurrentHashMap<>();

  @Autowired
  private UploadService                     uploadService;

  @Override
  public void addPlugin(DatabindPlugin plugin) {
    dataPreferencePlugins.put(plugin.getObjectType(), plugin);
  }

  @SneakyThrows
  @Override
  public File serialize(String objectType, List<String> objectIds, String username) throws ObjectNotFoundException,
                                                                                    IllegalAccessException {
    String safePrefix = (objectType.length() >= 3 ? objectType : "obj") + "_";
    File zipFile = File.createTempFile(safePrefix, ".zip");

    try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zipOutputStream = new ZipOutputStream(fos)) {
      for (String objectId : objectIds) {
        for (DatabindPlugin plugin : dataPreferencePlugins.values()) {
          if (plugin.canHandleDatabind(objectType, objectId)) {
            plugin.serialize(objectId, zipOutputStream, username);
          }
        }
      }
      addMetadataFile(zipOutputStream, objectType);
    } catch (IOException e) {
      throw new IllegalStateException("Error exporting template", e);
    }
    return zipFile;
  }

  @Async
  public CompletableFuture<DatabindReport> deserialize(String objectType,
                                                       String uploadId,
                                                       Map<String, String> params,
                                                       String username) {
    if (StringUtils.isBlank(uploadId)) {
      throw new IllegalArgumentException("uploadId is mandatory");
    }
    UploadResource uploadResource = uploadService.getUploadResource(uploadId);
    if (uploadResource == null) {
      throw new IllegalStateException("Can't find uploaded resource with id : " + uploadId);
    }

    File zipFile = new File(uploadResource.getStoreLocation());
    if (!zipFile.exists()) {
      throw new IllegalArgumentException("ZIP file missing in upload folder.");
    }

    String zipType = getZipType(zipFile);
    if (zipType == null) {
      throw new IllegalStateException("databind.missingMetadata");
    }

    if (!objectType.equals(zipType)) {
      throw new IllegalStateException("databind.notMatchType");
    }
    CompletableFuture<Pair<DatabindReport, File>> databindReportCompletableFuture =
                                                                                  CompletableFuture.completedFuture(Pair.of(null,
                                                                                                                            zipFile));

    for (DatabindPlugin plugin : dataPreferencePlugins.values()) {
      if (plugin.canHandleDatabind(objectType, null)) {
        databindReportCompletableFuture = databindReportCompletableFuture.thenCompose(previousResult -> {
          File updatedZip = previousResult.getRight();
          return plugin.deserialize(updatedZip, params, username);
        });
      }
    }
    return databindReportCompletableFuture.thenApply(Pair::getLeft);
  }

  private static void addMetadataFile(ZipOutputStream zipOut, String type) throws IOException {
    ZipEntry zipEntry = new ZipEntry("metadata.txt");
    zipOut.putNextEntry(zipEntry);
    zipOut.write(("type=" + type).getBytes());
    zipOut.closeEntry();
  }

  @SneakyThrows
  private String getZipType(File zipFile) {
    try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if ("metadata.txt".equals(entry.getName())) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream));
          String line;
          while ((line = reader.readLine()) != null) {
            if (line.startsWith("type=")) {
              return line.split("=")[1].trim();
            }
          }
        }
      }
    }
    return null;
  }
}
