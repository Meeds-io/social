package io.meeds.social.translation.rest;

import java.io.UnsupportedEncodingException;
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

  private LocaleConfigService    localeConfigService;

  private String                 objectType  = OBJECT_TYPE;

  private long                   objectId    = 322l;

  private long                   spaceId     = 322l;

  private long                   audienceId  = 3l;

  private String                 fieldName   = "title";

  private String                 username    = "john";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    translationService = ExoContainerContext.getService(TranslationServiceImpl.class);
    localeConfigService = ExoContainerContext.getService(LocaleConfigService.class);
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
    assertEquals(204, response.getStatus());

    response = getResponse("GET", getConfigurationUrl(), null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("fr", responseEntity.getDefaultLanguage());

    response = saveDefaultLocale("");
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    response = getResponse("GET", getConfigurationUrl(), null);
    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("en", responseEntity.getDefaultLanguage());

    response = saveDefaultLocale("en");
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    response = getResponse("GET", getConfigurationUrl(), null);
    responseEntity = (TranslationConfiguration) response.getEntity();
    assertNotNull(responseEntity);
    assertEquals("en", responseEntity.getDefaultLanguage());
  }

  private ContainerResponse saveDefaultLocale(String locale) throws UnsupportedEncodingException, Exception {
    ContainerResponse response;
    byte[] formDataInput = ("lang=" + locale).getBytes("UTF-8");

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
      public boolean hasEditPermission(long objectId, String username) throws ObjectNotFoundException {
        return hasEditPermission;
      }

      @Override
      public boolean hasAccessPermission(long objectId, String username) throws ObjectNotFoundException {
        return hasAccessPermission;
      }

      @Override
      public long getSpaceId(long objectId) throws ObjectNotFoundException {
        return spaceId;
      }

      @Override
      public long getAudienceId(long objectId) throws ObjectNotFoundException {
        return audienceId;
      }
    };
    translationService.addPlugin(translationPlugin);
  }
}
