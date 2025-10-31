/**
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
package io.meeds.social.space.template.plugin.databind;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.exoplatform.portal.mop.SiteKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.upload.UploadResource;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.databind.model.DatabindReport;
import io.meeds.social.databind.model.SpaceTemplateDatabind;
import io.meeds.social.databind.plugin.DatabindPlugin;
import io.meeds.social.databind.service.DatabindService;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPlugin;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPlugin;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import io.meeds.social.util.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaceTemplateDatabindPlugin implements DatabindPlugin {

  public static final String        OBJECT_TYPE          = "SpaceTemplate";

  public static final String        CONFIG_JSON          = "config.json";

  private static final List<String> ADMINISTRATORS_GROUP = Collections.singletonList("*:/platform/administrators");

  private static final Random       RANDOM               = new Random();

  private long                      superUserIdentityId;

  @Autowired
  protected DatabindService         databindService;

  @Autowired
  protected FileService             fileService;

  @Autowired
  protected TranslationService      translationService;

  @Autowired
  protected SpaceTemplateService    spaceTemplateService;

  @Autowired
  protected AttachmentService       attachmentService;

  @Autowired
  protected UserACL                 userAcl;

  @Autowired
  protected IdentityManager         identityManager;

  @PostConstruct
  public void init() {
    databindService.addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canHandleDatabind(String objectType, String objectId) {
    return StringUtils.equals(OBJECT_TYPE, objectType);
  }

  @SneakyThrows
  @Override
  public void serialize(String objectId, ZipOutputStream zipOutputStream, String username) {
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(Long.parseLong(objectId),
                                                                        username,
                                                                        Locale.getDefault(),
                                                                        true);

    SpaceTemplateDatabind databind = new SpaceTemplateDatabind();
    TranslationField translationNameField = translationService.getTranslationField(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                                                   Long.parseLong(objectId),
                                                                                   SpaceTemplateTranslationPlugin.NAME_FIELD_NAME,
                                                                                   username);
    if (translationNameField != null) {
      Map<String, String> names = translationNameField.getLabels()
                                                      .entrySet()
                                                      .stream()
                                                      .collect(Collectors.toMap(entry -> entry.getKey().toLanguageTag(),
                                                                                Map.Entry::getValue));
      databind.setNames(names);
    }

    TranslationField translationDescriptionField =
                                                 translationService.getTranslationField(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                                                        Long.parseLong(objectId),
                                                                                        SpaceTemplateTranslationPlugin.DESCRIPTION_FIELD_NAME,
                                                                                        username);
    if (translationDescriptionField != null) {
      Map<String, String> descriptions = translationDescriptionField.getLabels()
                                                                    .entrySet()
                                                                    .stream()
                                                                    .collect(Collectors.toMap(entry -> entry.getKey()
                                                                                                            .toLanguageTag(),
                                                                                              Map.Entry::getValue));
      databind.setDescriptions(descriptions);
    }
    FileItem file = fileService.getFile(spaceTemplate.getBannerFileId());
    if (file != null) {
      databind.setBannerFile(Base64.encodeBase64String(file.getAsByte()));
    }
    databind.setIcon(spaceTemplate.getIcon());
    databind.setSpaceFields(spaceTemplate.getSpaceFields());
    databind.setSpaceDefaultVisibility(spaceTemplate.getSpaceDefaultVisibility());
    databind.setSpaceDefaultRegistration(spaceTemplate.getSpaceDefaultRegistration());
    databind.setSpaceAllowContentCreation(spaceTemplate.isSpaceAllowContentCreation());
    String jsonData = JsonUtils.toJsonString(databind);

    SiteKey siteKey = SiteKey.groupTemplate(spaceTemplate.getLayout());
    String folderPath = siteKey.getType() + "-" + siteKey.getName();

    writeToZip(zipOutputStream, folderPath + "/" + CONFIG_JSON, jsonData);
  }

  public CompletableFuture<Pair<DatabindReport, File>> deserialize(File zipFile, Map<String, String> params, String username) {
    return CompletableFuture.supplyAsync(() -> importSpaceTemplates(zipFile)).thenApply(processedTemplates -> {
      DatabindReport report = new DatabindReport();
      report.setSuccess(!processedTemplates.isEmpty());
      report.setProcessedItems(processedTemplates);
      return Pair.of(report, zipFile);
    });
  }

  @ContainerTransactional
  public List<String> importSpaceTemplates(File zipFile) {
    Map<String, SpaceTemplateDatabind> instances = extractTemplates(zipFile);
    Map<String, String> spaceTemplateIds = new HashMap<>();
    List<String> processedSpaceTemplates = new ArrayList<>();

    for (Map.Entry<String, SpaceTemplateDatabind> entry : instances.entrySet()) {
      String folderName = entry.getKey();
      SpaceTemplateDatabind spaceTemplate = entry.getValue();

      String createdId = processSpaceTemplate(spaceTemplate);
      processedSpaceTemplates.add(spaceTemplate.getLayout());
      spaceTemplateIds.put(folderName, createdId);
    }
    createUpdatedZipWithSpaceTemplateIds(zipFile, spaceTemplateIds);
    return processedSpaceTemplates;
  }

  private Map<String, SpaceTemplateDatabind> extractTemplates(File zipFile) {
    Map<String, SpaceTemplateDatabind> templateDatabindMap = new HashMap<>();

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = zis.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }
        String jsonContent = baos.toString(StandardCharsets.UTF_8);
        String entryName = entry.getName();

        if (entryName.endsWith(CONFIG_JSON)) {
          SpaceTemplateDatabind databindFromJson = JsonUtils.fromJsonString(jsonContent, SpaceTemplateDatabind.class);
          if (databindFromJson != null) {
            String key = entryName.substring(0, entryName.lastIndexOf('/'));
            templateDatabindMap.put(key, databindFromJson);
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error reading zip file", e);
    }
    return templateDatabindMap;
  }

  private void saveBanner(long spaceTemplateId, byte[] bannerBytes) {
    File tempFile = null;
    try {
      tempFile = getBannerFile(bannerBytes);
      String uploadId = "SpaceTemplateBanner" + RANDOM.nextLong();
      UploadResource uploadResource = new UploadResource(uploadId);
      uploadResource.setFileName(tempFile.getName());
      uploadResource.setMimeType("image/png");
      uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
      uploadResource.setStoreLocation(tempFile.getPath());
      attachmentService.deleteAttachments(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE, String.valueOf(spaceTemplateId));
      UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail(uploadResource);
      attachmentService.saveAttachment(uploadedAttachmentDetail,
                                       SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE,
                                       String.valueOf(spaceTemplateId),
                                       null,
                                       getSuperUserIdentityId());
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error while saving banner file as attachment for space template '%s'",
                                                    spaceTemplateId),
                                      e);
    } finally {
      if (tempFile != null) {
        try {
          Files.delete(tempFile.toPath());
        } catch (IOException e) {
          tempFile.deleteOnExit();
        }
      }
    }
  }

  @SneakyThrows
  private void saveNames(SpaceTemplateDatabind spaceTemplateDatabind, SpaceTemplate spaceTemplate) {
    translationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                             spaceTemplate.getId(),
                                             SpaceTemplateTranslationPlugin.NAME_FIELD_NAME,
                                             convertToLocaleMap(spaceTemplateDatabind.getNames()),
                                             false);
  }

  @SneakyThrows
  private void saveDescriptions(SpaceTemplateDatabind spaceTemplateDatabind, SpaceTemplate spaceTemplate) {
    translationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                             spaceTemplate.getId(),
                                             SpaceTemplateTranslationPlugin.DESCRIPTION_FIELD_NAME,
                                             convertToLocaleMap(spaceTemplateDatabind.getDescriptions()),
                                             true);
  }

  @SneakyThrows
  private String processSpaceTemplate(SpaceTemplateDatabind spaceTemplateDatabind) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(spaceTemplateDatabind);
    SpaceTemplate createdSpaceTemplate = spaceTemplateService.createSpaceTemplate(spaceTemplate);
    saveNames(spaceTemplateDatabind, createdSpaceTemplate);
    saveDescriptions(spaceTemplateDatabind, createdSpaceTemplate);
    if (spaceTemplateDatabind.getBannerFile() != null) {
      saveBanner(createdSpaceTemplate.getId(), Base64.decodeBase64(spaceTemplateDatabind.getBannerFile()));
    }
    return String.valueOf(createdSpaceTemplate.getId());
  }

  private static SpaceTemplate getSpaceTemplate(SpaceTemplateDatabind spaceTemplateDatabind) {
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setIcon(spaceTemplateDatabind.getIcon());
    spaceTemplate.setSpaceDefaultVisibility(spaceTemplateDatabind.getSpaceDefaultVisibility());
    spaceTemplate.setSpaceFields(spaceTemplateDatabind.getSpaceFields());
    spaceTemplate.setSpaceDefaultRegistration(spaceTemplateDatabind.getSpaceDefaultRegistration());
    spaceTemplate.setSpaceAllowContentCreation(spaceTemplateDatabind.isSpaceAllowContentCreation());
    spaceTemplate.setAdminPermissions(ADMINISTRATORS_GROUP);
    spaceTemplate.setPermissions(ADMINISTRATORS_GROUP);
    spaceTemplate.setSpaceLayoutPermissions(ADMINISTRATORS_GROUP);
    spaceTemplate.setSpacePublicSitePermissions(ADMINISTRATORS_GROUP);
    spaceTemplate.setSpaceDeletePermissions(ADMINISTRATORS_GROUP);
    spaceTemplate.setEnabled(true);
    return spaceTemplate;
  }

  @SneakyThrows
  private File getBannerFile(byte[] data) {
    if (data == null) {
      throw new IllegalArgumentException("Banner data is null");
    }
    File tempFile = File.createTempFile("temp", ".png");
    FileUtils.writeByteArrayToFile(tempFile, data);
    return tempFile;
  }

  private void writeToZip(ZipOutputStream zipOutputStream, String filePath, String content) throws IOException {
    ZipEntry entry = new ZipEntry(filePath);
    zipOutputStream.putNextEntry(entry);
    zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    zipOutputStream.closeEntry();
  }

  private long getSuperUserIdentityId() {
    if (superUserIdentityId == 0) {
      superUserIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(userAcl.getSuperUser()).getId());
    }
    return superUserIdentityId;
  }

  private static Map<Locale, String> convertToLocaleMap(Map<String, String> inputMap) {
    return inputMap.entrySet()
                   .stream()
                   .collect(Collectors.toMap(entry -> Locale.forLanguageTag(entry.getKey()), Map.Entry::getValue));
  }

  @SneakyThrows
  private void createUpdatedZipWithSpaceTemplateIds(File originalZip, Map<String, String> spaceTemplateIds) {
    File tempZipFile = File.createTempFile("updated_", ".zip");
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(originalZip), StandardCharsets.UTF_8);
        FileOutputStream fos = new FileOutputStream(tempZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        zos.putNextEntry(new ZipEntry(entry.getName()));
        if (entry.getName().endsWith(CONFIG_JSON)) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
          }
          String jsonContent = baos.toString(StandardCharsets.UTF_8);
          String folderName = entry.getName().substring(0, entry.getName().lastIndexOf('/'));
          String spaceTemplateId = spaceTemplateIds.get(folderName);
          if (spaceTemplateId != null) {
            jsonContent = updateConfigJsonWithSpaceTemplateId(jsonContent, spaceTemplateId);
          }
          zos.write(jsonContent.getBytes(StandardCharsets.UTF_8));
        } else {
          zos.write(zis.readAllBytes());
        }
        zos.closeEntry();
      }
    }

    Files.move(tempZipFile.toPath(), originalZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
  }

  @SneakyThrows
  private String updateConfigJsonWithSpaceTemplateId(String jsonContent, String spaceTemplateId) {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode configJsonNode = objectMapper.readTree(jsonContent);
    // Add or update the spaceTemplateId field
    ((ObjectNode) configJsonNode).put("spaceTemplateId", spaceTemplateId);
    return objectMapper.writeValueAsString(configJsonNode);
  }
}
