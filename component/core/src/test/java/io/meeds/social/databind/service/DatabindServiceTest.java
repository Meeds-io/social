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
package io.meeds.social.databind.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.meeds.social.databind.plugin.DatabindPlugin;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class DatabindServiceTest {

  @Mock
  private UploadService       uploadService;

  @Mock
  private DatabindPlugin      databindPlugin;

  @InjectMocks
  private DatabindServiceImpl databindService;

  private final String        objectType = "testObject";

  private final String        uploadId   = "testUploadId";

  private final String        username   = "testUser";

  @Test
  public void testSerialize() throws Exception {
    when(databindPlugin.getObjectType()).thenReturn(objectType);
    when(databindPlugin.canHandleDatabind(any(), any())).thenReturn(true);
    databindService.addPlugin(databindPlugin);
    File file = databindService.serialize(objectType, Collections.singletonList("123"), username);
    assertNotNull(file);
    assertTrue(file.getName().endsWith(".zip"));
  }

  @Test
  public void testDeserialize() {
    when(uploadService.getUploadResource(uploadId)).thenReturn(null);
    Exception exception = assertThrows(IllegalStateException.class,
                                       () -> databindService.deserialize(objectType, uploadId, new HashMap<>(), username));
    assertEquals("Can't find uploaded resource with id : " + uploadId, exception.getMessage());

    UploadResource uploadResource = mock(UploadResource.class);
    when(uploadService.getUploadResource(uploadId)).thenReturn(uploadResource);
    when(uploadResource.getStoreLocation()).thenReturn("invalid/path");

    exception = assertThrows(IllegalArgumentException.class,
                             () -> databindService.deserialize(objectType, uploadId, new HashMap<>(), username));

    assertEquals("ZIP file missing in upload folder.", exception.getMessage());
  }
}
