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
package io.meeds.social.databind.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.meeds.social.databind.model.DatabindReport;
import io.meeds.social.databind.service.DatabindService;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.space.template.service.injection.SpaceTemplateTranslationImportService;
import io.meeds.social.translation.service.TranslationService;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.manager.IdentityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SpringBootTest(classes = { SpaceTemplateDatabindPlugin.class, })
@ExtendWith(MockitoExtension.class)
class SpaceTemplateDatabindPluginTest {

  @Mock
  private Identity                              userIdentity;

  @MockBean
  private SpaceTemplateService                  spaceTemplateService;

  @MockBean
  private DatabindService                       databindService;

  @MockBean
  private FileService                           fileService;

  @MockBean
  private SpaceTemplateTranslationImportService layoutTranslationService;

  @MockBean
  private TranslationService                    translationService;

  @MockBean
  private AttachmentService                     attachmentService;

  @MockBean
  private UserACL                               userAcl;

  @MockBean
  private IdentityManager                       identityManager;

  @Autowired
  private SpaceTemplateDatabindPlugin           spaceTemplateDatabindPlugin;

  @Test
  void getObjectType() {
    assertEquals(SpaceTemplateDatabindPlugin.OBJECT_TYPE, spaceTemplateDatabindPlugin.getObjectType());
  }

  @Test
  void canHandleDatabind() {
    assertTrue(spaceTemplateDatabindPlugin.canHandleDatabind("SpaceTemplate", "1"));
    assertFalse(spaceTemplateDatabindPlugin.canHandleDatabind("ObjectInstance", "1"));
  }

  @Test
  void serialize() throws IllegalAccessException {
    ZipOutputStream zipOutputStream = mock(ZipOutputStream.class);
    SpaceTemplate spaceTemplate = mock(SpaceTemplate.class);
    when(spaceTemplateService.getSpaceTemplate(anyLong(),
                                               anyString(),
                                               any(Locale.class),
                                               anyBoolean())).thenReturn(spaceTemplate);
    when(spaceTemplate.getName()).thenReturn("spaceTemplate1");

    spaceTemplateDatabindPlugin.serialize("1", zipOutputStream, "root");

    verify(spaceTemplateService, times(1)).getSpaceTemplate(1L, "root", Locale.getDefault(), true);

  }

  @Test
  void deserialize() throws Exception {
    File zipFile = createZipFileWithTwoJsonFiles();

    when(spaceTemplateService.createSpaceTemplate(any())).thenReturn(new SpaceTemplate());

    when(layoutTranslationService.postImport(any())).thenReturn(CompletableFuture.completedFuture(null));

    // When
    CompletableFuture<DatabindReport> futureReport = spaceTemplateDatabindPlugin.deserialize(zipFile, null, "admin");

    DatabindReport report = futureReport.join();

    // Then
    assertNotNull(report);
    assertTrue(report.isSuccess());
    assertEquals(2, report.getProcessedItems().size());
    assertTrue(report.getProcessedItems().contains("12345"));
    assertTrue(report.getProcessedItems().contains("67890"));

    verify(spaceTemplateService, times(2)).createSpaceTemplate(any());
  }

  private File createZipFileWithTwoJsonFiles() throws IOException {
    File tempFile = File.createTempFile("test", ".zip");
    try (FileOutputStream fos = new FileOutputStream(tempFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
      addJsonToZip(zos,
                   "SpaceTemplate_1.json",
                   "{\"name\":\"12345\",\"names\":{\"en\":\"Test Page 1\"},\"descriptions\":{\"en\":\"Desc 1\"}}");
      addJsonToZip(zos,
                   "SpaceTemplate_2.json",
                   "{\"name\":\"67890\",\"names\":{\"en\":\"Test Page 2\"},\"descriptions\":{\"en\":\"Desc 2\"}}");
    }
    return tempFile;
  }

  private void addJsonToZip(ZipOutputStream zos, String fileName, String jsonContent) throws IOException {
    ZipEntry entry = new ZipEntry(fileName);
    zos.putNextEntry(entry);
    zos.write(jsonContent.getBytes());
    zos.closeEntry();
  }
}
