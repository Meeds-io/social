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
package io.meeds.social.space.template.plugin.databind;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPlugin;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPlugin;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.service.injection.SpaceTemplateTranslationImportService;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;
import io.meeds.social.util.JsonUtils;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SpaceTemplateDatabindPlugin implements DatabindPlugin {

  public static final String                      OBJECT_TYPE          = "SpaceTemplate";

  private static final List<String>               ADMINISTRATORS_GROUP = Collections.singletonList("*:/platform/administrators");

  private static final Random                     RANDOM               = new Random();

  private long                                    superUserIdentityId;

  @Autowired
  protected DatabindService                       databindService;

  @Autowired
  protected FileService                           fileService;

  @Autowired
  protected TranslationService                    translationService;

  @Autowired
  protected SpaceTemplateTranslationImportService layoutTranslationService;

  @Autowired
  protected SpaceTemplateService                  spaceTemplateService;

  @Autowired
  protected AttachmentService                     attachmentService;

  @Autowired
  protected UserACL                               userAcl;

  @Autowired
  protected IdentityManager                       identityManager;

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
    databind.setName(spaceTemplate.getName());
    databind.setDescription(spaceTemplate.getDescription());
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
    String jsonData = JsonUtils.toJsonString(databind);
    writeContent(zipOutputStream, objectId, jsonData);
  }

  public CompletableFuture<DatabindReport> deserialize(File zipFile, Map<String, String> params, String username) {
    return CompletableFuture.supplyAsync(() -> importPageTemplates(zipFile))
                            .thenCompose(processedTemplates -> layoutTranslationService.postImport(SpaceTemplateTranslationPlugin.OBJECT_TYPE)
                                                                                       .thenApply(v -> {
                                                                                         DatabindReport report =
                                                                                                               new DatabindReport();
                                                                                         report.setSuccess(!processedTemplates.isEmpty());
                                                                                         report.setProcessedItems(processedTemplates);
                                                                                         return report;
                                                                                       }));

  }

  @ContainerTransactional
  public List<String> importPageTemplates(File zipFile) {
    Map<String, SpaceTemplateDatabind> instances = extractTemplates(zipFile);
    List<String> processedPageTemplates = new ArrayList<>();
    for (Map.Entry<String, SpaceTemplateDatabind> entry : instances.entrySet()) {
      SpaceTemplateDatabind spaceTemplate = entry.getValue();
      processPageTemplate(spaceTemplate);
      processedPageTemplates.add(spaceTemplate.getName());
    }
    return processedPageTemplates;
  }

  private Map<String, SpaceTemplateDatabind> extractTemplates(File zipFile) {
    Map<String, SpaceTemplateDatabind> templateDatabindMap = new HashMap<>();

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile), StandardCharsets.UTF_8)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = zis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
          }
          String jsonContent = baos.toString(StandardCharsets.UTF_8);

          // Deserialize JSON into a Page templates
          SpaceTemplateDatabind databind = JsonUtils.fromJsonString(jsonContent, SpaceTemplateDatabind.class);
          if (databind != null) {
            templateDatabindMap.put(entry.getName(), databind);
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error reading zip file", e);
    }
    return templateDatabindMap;
  }

  private void saveBanner(long pageTemplateId, byte[] bannerBytes) {
    File tempFile = null;
    try {
      tempFile = getBannerFile(bannerBytes);
      String uploadId = "SpaceTemplateBanner" + RANDOM.nextLong();
      UploadResource uploadResource = new UploadResource(uploadId);
      uploadResource.setFileName(tempFile.getName());
      uploadResource.setMimeType("image/png");
      uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
      uploadResource.setStoreLocation(tempFile.getPath());
      attachmentService.deleteAttachments(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE, String.valueOf(pageTemplateId));
      UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail(uploadResource);
      attachmentService.saveAttachment(uploadedAttachmentDetail,
                                       SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE,
                                       String.valueOf(pageTemplateId),
                                       null,
                                       getSuperUserIdentityId());
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error while saving banner file as attachment for space template '%s'",
                                                    pageTemplateId),
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

  private void saveNames(SpaceTemplateDatabind spaceTemplateDatabind, SpaceTemplate spaceTemplate) {
    layoutTranslationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                   spaceTemplate.getId(),
                                                   SpaceTemplateTranslationPlugin.NAME_FIELD_NAME,
                                                   spaceTemplateDatabind.getNames());
  }

  private void saveDescriptions(SpaceTemplateDatabind spaceTemplateDatabind, SpaceTemplate spaceTemplate) {
    layoutTranslationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                   spaceTemplate.getId(),
                                                   SpaceTemplateTranslationPlugin.DESCRIPTION_FIELD_NAME,
                                                   spaceTemplateDatabind.getDescriptions());
  }

  @SneakyThrows
  private void processPageTemplate(SpaceTemplateDatabind spaceTemplateDatabind) {
    SpaceTemplate spaceTemplate = getSpaceTemplate(spaceTemplateDatabind);
    SpaceTemplate createdSpaceTemplate = spaceTemplateService.createSpaceTemplate(spaceTemplate);
    saveNames(spaceTemplateDatabind, createdSpaceTemplate);
    saveDescriptions(spaceTemplateDatabind, createdSpaceTemplate);
    if (spaceTemplateDatabind.getBannerFile() != null) {
      saveBanner(createdSpaceTemplate.getId(), Base64.decodeBase64(spaceTemplateDatabind.getBannerFile()));
    }
  }

  private static SpaceTemplate getSpaceTemplate(SpaceTemplateDatabind spaceTemplateDatabind) {
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setName(spaceTemplateDatabind.getName());
    spaceTemplate.setDescription(spaceTemplateDatabind.getDescription());
    spaceTemplate.setIcon(spaceTemplateDatabind.getIcon());
    spaceTemplate.setSpaceDefaultVisibility(SpaceVisibility.HIDDEN);
    spaceTemplate.setSpaceFields(Arrays.asList("name", "invitation", "properties", "access"));
    spaceTemplate.setSpaceDefaultRegistration(SpaceRegistration.CLOSED);
    spaceTemplate.setSpaceAllowContentCreation(false);
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

  private void writeContent(ZipOutputStream zipOutputStream, String objectId, String content) throws IOException {
    ZipEntry entry = new ZipEntry(String.format("%s_%s.json", OBJECT_TYPE, objectId));
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
}
