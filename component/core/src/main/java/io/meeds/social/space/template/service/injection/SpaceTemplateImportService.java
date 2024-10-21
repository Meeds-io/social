/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.space.template.service.injection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.config.model.UnmarshalledObject;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.attachment.model.UploadedAttachmentDetail;
import org.exoplatform.upload.UploadResource;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.core.space.constant.Registration;
import io.meeds.social.core.space.constant.Visibility;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPlugin;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPlugin;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.service.injection.model.SpaceTemplateDescriptor;
import io.meeds.social.space.template.service.injection.model.SpaceTemplateDescriptorList;
import io.meeds.social.util.JsonUtils;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SpaceTemplateImportService {

  private static final Scope                    SPACE_TEMPLATE_IMPORT_SCOPE = Scope.APPLICATION.id("SPACE_TEMPLATE_IMPORT");

  private static final Context                  SPACE_TEMPLATE_CONTEXT      = Context.GLOBAL.id("SPACE_TEMPLATE");

  private static final String                   SPACE_TEMPLATE_VERSION      = "version";

  private static final Log                      LOG                        =
                                                    ExoLogger.getLogger(SpaceTemplateImportService.class);

  private static final Random                   RANDOM                     = new Random();

  @Autowired
  private SpaceTemplateTranslationImportService layoutTranslationService;

  @Autowired
  private AttachmentService                     attachmentService;

  @Autowired
  private SpaceTemplateService                  spaceTemplateService;

  @Autowired
  private SettingService                        settingService;

  @Autowired
  private ConfigurationManager                  configurationManager;

  @Value("${meeds.space.template.import.override:false}")
  private boolean                               forceReimportTemplates;

  @Value("${meeds.space.template.import.version:1}")
  private long                                  version;

  @PostConstruct
  public void init() {
    CompletableFuture.runAsync(this::importSpaceTemplates);
  }

  @ContainerTransactional
  public void importSpaceTemplates() {
    LOG.info("Importing Space Templates");
    if (!forceReimportTemplates
        && getSettingValue(SPACE_TEMPLATE_VERSION) != version) {
      forceReimportTemplates = true;
    }

    try {
      Enumeration<URL> templateFiles = PortalContainer.getInstance()
                                                      .getPortalClassLoader()
                                                      .getResources("space-templates.json");
      Collections.list(templateFiles)
                 .stream()
                 .map(this::parseDescriptors)
                 .flatMap(List::stream)
                 .forEach(this::importDescriptor);
      LOG.info("Importing Space Templates finished successfully");

      LOG.info("Processing Post Space Templates import");
      layoutTranslationService.postImport(SpaceTemplateTranslationPlugin.OBJECT_TYPE);
      LOG.info("Processing Post Space Templates import finished");

      setSettingValue(SPACE_TEMPLATE_VERSION, version);
    } catch (Exception e) {
      LOG.warn("An error occurred while importing space templates", e);
    }
  }

  protected List<SpaceTemplateDescriptor> parseDescriptors(URL url) {
    try (InputStream inputStream = url.openStream()) {
      String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      SpaceTemplateDescriptorList list = JsonUtils.fromJsonString(content, SpaceTemplateDescriptorList.class);
      return list.getDescriptors();
    } catch (IOException e) {
      LOG.warn("An unkown error happened while parsing space templates from url {}", url, e);
      return Collections.emptyList();
    }
  }

  protected void importDescriptor(SpaceTemplateDescriptor descriptor) {
    String descriptorId = descriptor.getId();
    long existingTemplateId = getSettingValue(descriptorId);
    if (forceReimportTemplates || existingTemplateId == 0) {
      importSpaceTemplate(descriptor, existingTemplateId);
    } else {
      LOG.debug("Ignore re-importing Space Template {}", descriptorId);
    }
  }

  protected void importSpaceTemplate(SpaceTemplateDescriptor d, long oldTemplateId) {
    LOG.info("Importing Space Template {}", d.getId());
    try {
      SpaceTemplate spaceTemplate = createSpaceTemplate(d, oldTemplateId);
      if (forceReimportTemplates || oldTemplateId == 0 || spaceTemplate.getId() != oldTemplateId) {
        LOG.info("Importing Space Template {} title translations", d.getId());
        saveNames(d, spaceTemplate);
        LOG.info("Importing Space Template {} description translations", d.getId());
        saveDescriptions(d, spaceTemplate);
        LOG.info("Importing Space Template {} illustration", d.getId());
        saveBanner(spaceTemplate.getId(), d.getBannerPath());
        // Mark as imported
        setSettingValue(d.getId(), spaceTemplate.getId());
      }
      LOG.info("Importing Space Template {} finished successfully", d.getId());
    } catch (Exception e) {
      LOG.warn("An error occurred while importing Space template {}", d.getId(), e);
    }
  }

  protected void saveNames(SpaceTemplateDescriptor d, SpaceTemplate spaceTemplate) {
    layoutTranslationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                   spaceTemplate.getId(),
                                                   SpaceTemplateTranslationPlugin.NAME_FIELD_NAME,
                                                   d.getNames());
  }

  protected void saveDescriptions(SpaceTemplateDescriptor d, SpaceTemplate spaceTemplate) {
    layoutTranslationService.saveTranslationLabels(SpaceTemplateTranslationPlugin.OBJECT_TYPE,
                                                   spaceTemplate.getId(),
                                                   SpaceTemplateTranslationPlugin.DESCRIPTION_FIELD_NAME,
                                                   d.getDescriptions());
  }

  @SneakyThrows
  protected SpaceTemplate createSpaceTemplate(SpaceTemplateDescriptor d, long oldTemplateId) {
    SpaceTemplate spaceTemplate = null;
    if (oldTemplateId > 0) {
      spaceTemplate = spaceTemplateService.getSpaceTemplate(oldTemplateId);
    }
    boolean isNew = spaceTemplate == null;
    if (isNew) {
      spaceTemplate = new SpaceTemplate();
    }
    spaceTemplate.setSystem(d.isSystem());
    spaceTemplate.setIcon(d.getIcon());
    spaceTemplate.setEnabled(d.isEnabled());
    spaceTemplate.setLayout(d.getLayout());
    spaceTemplate.setSpaceFields(d.getSpaceFields());
    spaceTemplate.setPermissions(d.getPermissions());
    spaceTemplate.setSpaceLayoutPermissions(d.getSpaceLayoutPermissions());
    spaceTemplate.setSpaceDeletePermissions(d.getSpaceDeletePermissions());
    spaceTemplate.setSpaceDefaultRegistration(Registration.valueOf(d.getSpaceDefaultRegistration().toUpperCase()));
    spaceTemplate.setSpaceDefaultVisibility(Visibility.valueOf(d.getSpaceDefaultVisibility().toUpperCase()));
    spaceTemplate.setSpaceAllowContentCreation(d.isSpaceAllowContentCreation());
    if (isNew) {
      return spaceTemplateService.createSpaceTemplate(spaceTemplate);
    } else {
      return spaceTemplateService.updateSpaceTemplate(spaceTemplate);
    }
  }

  protected void saveBanner(long templateId, String imagePath) {
    File tempFile = null;
    try {
      tempFile = getIllustrationFile(imagePath);
      String uploadId = "SpaceTemplateBanner" + RANDOM.nextLong();
      UploadResource uploadResource = new UploadResource(uploadId);
      uploadResource.setFileName(tempFile.getName());
      uploadResource.setMimeType("image/png");
      uploadResource.setStatus(UploadResource.UPLOADED_STATUS);
      uploadResource.setStoreLocation(tempFile.getPath());
      UploadedAttachmentDetail uploadedAttachmentDetail = new UploadedAttachmentDetail(uploadResource);
      attachmentService.deleteAttachments(SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE, String.valueOf(templateId));
      attachmentService.saveAttachment(uploadedAttachmentDetail,
                                       SpaceTemplateBannerAttachmentPlugin.OBJECT_TYPE,
                                       String.valueOf(templateId),
                                       null,
                                       1l);
    } catch (Exception e) {
      throw new IllegalStateException(String.format("Error while saving Image '%s' as attachment for template '%s'",
                                                    imagePath,
                                                    templateId),
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
  protected Container fromXML(String xml) {
    UnmarshalledObject<Container> obj = ModelUnmarshaller.unmarshall(Container.class, xml.getBytes(StandardCharsets.UTF_8));
    return obj.getObject();
  }

  protected void setSettingValue(String name, long value) {
    settingService.set(SPACE_TEMPLATE_CONTEXT,
                       SPACE_TEMPLATE_IMPORT_SCOPE,
                       name,
                       SettingValue.create(String.valueOf(value)));
  }

  protected long getSettingValue(String name) {
    try {
      SettingValue<?> settingValue = settingService.get(SPACE_TEMPLATE_CONTEXT, SPACE_TEMPLATE_IMPORT_SCOPE, name);
      return settingValue == null || settingValue.getValue() == null ? 0l : Long.parseLong(settingValue.getValue().toString());
    } catch (NumberFormatException e) {
      return 0l;
    }
  }

  private File getIllustrationFile(String imagePath) throws Exception {
    try (InputStream inputStream = configurationManager.getInputStream(imagePath)) {
      File tempFile = File.createTempFile("temp", ".png");
      FileUtils.copyInputStreamToFile(inputStream, tempFile);
      return tempFile;
    }
  }

}
