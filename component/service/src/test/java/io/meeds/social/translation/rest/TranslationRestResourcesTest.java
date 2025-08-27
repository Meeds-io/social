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
package io.meeds.social.translation.rest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.social.service.test.AbstractResourceTest;

import io.meeds.social.translation.model.TranslationConfiguration;
import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.service.TranslationServiceImpl;

public class TranslationRestResourcesTest extends AbstractResourceTest {

  private TranslationRest        translationRest;

  private static final String    OBJECT_TYPE = "activity";

  private TranslationServiceImpl translationService;

  private String                 objectType  = OBJECT_TYPE;

  private String                 objectId    = "322";

  private long                   spaceId     = 322l;

  private long                   audienceId  = 3l;

  private String                 fieldName   = "title";

  private String                 username    = "john";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    translationService = ExoContainerContext.getService(TranslationServiceImpl.class);
    LocaleConfigService localeConfigService = ExoContainerContext.getService(LocaleConfigService.class);
    translationRest = new TranslationRest(translationService, localeConfigService);
    registry(translationRest);

    ExoContainerContext.setCurrentContainer(getContainer());
    restartTransaction();
    begin();
    System.setProperty("gatein.email.domain.url", "localhost:8080");
  }

  @Override
  protected void tearDown() throws Exception {
    translationService.deleteTranslationLabels(objectType, objectId);
    removeTranslationPlugin();
    super.tearDown();
    removeResource(translationRest.getClass());
  }

  public void testSaveTranslationLabels() throws Exception {
    startSessionAs(username);

    setTranslationPlugin(true, false, spaceId, audienceId);

    String input = """
        {
          "en": "Test Label",
          "FR": "Test Label FR"
        }
        """;
    ContainerResponse response = getResponse("POST", getUrl(), input);
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    setTranslationPlugin(true, true, spaceId, audienceId);
    response = getResponse("POST", getUrl(), input);
    assertNotNull(response);
    assertEquals(204, response.getStatus());
  }

  public void testGetTranslationLabels() throws Exception {
    startSessionAs(username);

    setTranslationPlugin(true, true, spaceId, audienceId);

    String input = """
        {
          "en": "Test Label",
          "FR": "Test Label FR"
        }
        """;
    ContainerResponse response = getResponse("POST", getUrl(), input);
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    setTranslationPlugin(false, true, spaceId, audienceId);
    response = getResponse("GET", getUrl(), null);
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    setTranslationPlugin(true, true, spaceId, audienceId);
    response = getResponse("GET", getUrl(), null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    @SuppressWarnings("unchecked")
    Map<String, String> responseEntity = (Map<String, String>) response.getEntity();
    String labelEn = responseEntity.get("en");
    assertEquals("Test Label", labelEn);
    String labelFr = responseEntity.get("fr");
    assertEquals("Test Label FR", labelFr);
  }

  public void testGetTranslationConfiguration() throws Exception {
    startSessionAs(username);
    setTranslationPlugin(false, false, spaceId, audienceId);

    ContainerResponse response = getResponse("GET", getConfigurationUrl(), null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    TranslationConfiguration responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertNotNull(responseEntity.getDefaultLanguage());
    assertNotNull(responseEntity.getSupportedLanguages());
  }

  public void testSaveDefaultLanguageConfiguration() throws Exception {
    startSessionAs(username);
    setTranslationPlugin(false, false, spaceId, audienceId);

    ContainerResponse response = getResponse("GET", getConfigurationUrl(), null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    TranslationConfiguration responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("en", responseEntity.getDefaultLanguage());

    response = saveDefaultLocale("fr");
    assertNotNull(response);
    assertEquals(403, response.getStatus());

    startSessionAs(username, true);
    response = saveDefaultLocale("fr");
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    startSessionAs(username);
    response = getResponse("GET", getConfigurationUrl(), null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("fr", responseEntity.getDefaultLanguage());

    startSessionAs(username, true);
    response = saveDefaultLocale("");
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    startSessionAs(username);
    response = getResponse("GET", getConfigurationUrl(), null);
    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("en", responseEntity.getDefaultLanguage());

    startSessionAs(username, true);
    response = saveDefaultLocale("en");
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    startSessionAs(username);
    response = getResponse("GET", getConfigurationUrl(), null);
    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("en", responseEntity.getDefaultLanguage());
  }

  private ContainerResponse saveDefaultLocale(String locale) throws Exception {
    ContainerResponse response;
    byte[] formDataInput = ("lang=" + locale).getBytes(StandardCharsets.UTF_8);

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", MediaType.APPLICATION_FORM_URLENCODED);
    h.putSingle("content-length", "" + formDataInput.length);

    response = service("PUT", getConfigurationUrl() + "/defaultLanguage", "", h, formDataInput);
    return response;
  }

  private String getConfigurationUrl() {
    return "/social/translations/configuration";
  }

  private String getUrl() {
    return "/social/translations/" + objectType + "/" + objectId + "/" + fieldName;
  }

  private void removeTranslationPlugin() {
    translationService.removePlugin(OBJECT_TYPE);
  }

  private void setTranslationPlugin(boolean hasAccessPermission, boolean hasEditPermission, long spaceId, long audienceId) {
    removeTranslationPlugin();
    TranslationPlugin translationPlugin = new TranslationPlugin() {

      @Override
      public String getObjectType() {
        return OBJECT_TYPE;
      }

      @Override
      public boolean hasEditPermission(String objectId, String username) throws ObjectNotFoundException {
        return hasEditPermission;
      }

      @Override
      public boolean hasAccessPermission(String objectId, String username) throws ObjectNotFoundException {
        return hasAccessPermission;
      }

      @Override
      public long getSpaceId(String objectId) throws ObjectNotFoundException {
        return spaceId;
      }

      @Override
      public long getAudienceId(String objectId) throws ObjectNotFoundException {
        return audienceId;
      }
    };
    translationService.addPlugin(translationPlugin);
  }

  public void testDeleteTranslationLabels() throws Exception {
    startSessionAs(username);

    String url = "/social/translations/" + objectType + "/" + objectId;

    ContainerResponse response = getResponse("DELETE", url, null);
    assertNotNull(response);
    assertEquals(500, response.getStatus());

    setTranslationPlugin(true, false, spaceId, audienceId);

    response = getResponse("DELETE", url, null);
    assertNotNull(response);
    assertEquals(401, response.getStatus());

    setTranslationPlugin(true, true, spaceId, audienceId);

    response = getResponse("DELETE", url, null);
    assertNotNull(response);
    assertEquals(204, response.getStatus());
  }
}
