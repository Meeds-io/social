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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.meeds.social.translation.model.TranslationField;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentService;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.databind.model.DatabindReport;
import io.meeds.social.databind.service.DatabindService;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;
import io.meeds.social.translation.service.TranslationService;

@RunWith(MockitoJUnitRunner.class)
public class SpaceTemplateDatabindPluginTest {

  @Mock
  private Identity                    userIdentity;

  @Mock
  private SpaceTemplateService        spaceTemplateService;

  @Mock
  private DatabindService             databindService;

  @Mock
  private FileService                 fileService;

  @Mock
  private TranslationService          translationService;

  @Mock
  private AttachmentService           attachmentService;

  @Mock
  private UserACL                     userAcl;

  @Mock
  private IdentityManager             identityManager;

  private SpaceTemplateDatabindPlugin spaceTemplateDatabindPlugin;

  @Before
  public void init() {
    spaceTemplateDatabindPlugin = new SpaceTemplateDatabindPlugin();
    spaceTemplateDatabindPlugin.spaceTemplateService = spaceTemplateService;
    spaceTemplateDatabindPlugin.translationService = translationService;
    spaceTemplateDatabindPlugin.fileService = fileService;
  }

  @Test
  public void getObjectType() {
    assertEquals(SpaceTemplateDatabindPlugin.OBJECT_TYPE, spaceTemplateDatabindPlugin.getObjectType());
  }

  @Test
  public void canHandleDatabind() {
    assertTrue(spaceTemplateDatabindPlugin.canHandleDatabind("SpaceTemplate", "1"));
    assertFalse(spaceTemplateDatabindPlugin.canHandleDatabind("ObjectInstance", "1"));
  }

  @Test
  public void serialize() throws Exception {
    ZipOutputStream zipOutputStream = mock(ZipOutputStream.class);
    SpaceTemplate spaceTemplate = mock(SpaceTemplate.class);
    TranslationField translationField = mock(TranslationField.class);
    when(spaceTemplate.getLayout()).thenReturn("layout");
    when(spaceTemplateService.getSpaceTemplate(anyLong(),
                                               anyString(),
                                               any(Locale.class),
                                               anyBoolean())).thenReturn(spaceTemplate);
    when(translationService.getTranslationField(anyString(), anyLong(), anyString(), anyString())).thenReturn(translationField);
    Map<Locale, String> labels = new HashMap<>();
    labels.put(Locale.getDefault(), "test");
    when(translationField.getLabels()).thenReturn(labels);

    spaceTemplateDatabindPlugin.serialize("1", zipOutputStream, "root");

    verify(spaceTemplateService, times(1)).getSpaceTemplate(1L, "root", Locale.getDefault(), true);

  }

  @Test
  public void deserialize() throws Exception {
    File zipFile = createZipFileWithTwoJsonFiles();

    when(spaceTemplateService.createSpaceTemplate(any())).thenReturn(new SpaceTemplate());

    // When
    CompletableFuture<Pair<DatabindReport, File>> futureReport = spaceTemplateDatabindPlugin.deserialize(zipFile, null, "admin");

    DatabindReport report = futureReport.thenApply(Pair::getLeft).join();

    // Then
    assertNotNull(report);
    assertTrue(report.isSuccess());
    assertEquals(2, report.getProcessedItems().size());
    assertTrue(report.getProcessedItems().contains("layout1"));
    assertTrue(report.getProcessedItems().contains("layout2"));

    verify(spaceTemplateService, times(2)).createSpaceTemplate(any());
  }

  private File createZipFileWithTwoJsonFiles() throws IOException {
    File tempFile = File.createTempFile("test", ".zip");
    try (FileOutputStream fos = new FileOutputStream(tempFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
      addJsonToZip(zos,
                   "space1/config.json",
                   "{\"name\":\"12345\",\"layout\":\"layout1\",\"names\":{\"en\":\"Test Page 1\"},\"descriptions\":{\"en\":\"Desc 1\"}}");
      addJsonToZip(zos,
                   "space2/config.json",
                   "{\"name\":\"67890\",\"layout\":\"layout2\",\"names\":{\"en\":\"Test Page 2\"},\"descriptions\":{\"en\":\"Desc 2\"}}");
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
