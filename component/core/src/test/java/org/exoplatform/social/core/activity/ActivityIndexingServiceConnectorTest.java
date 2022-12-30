package org.exoplatform.social.core.activity;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;
import org.exoplatform.social.core.jpa.search.ActivitySearchProcessor;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.metadata.MetadataService;

@RunWith(MockitoJUnitRunner.class)
public class ActivityIndexingServiceConnectorTest {

  ActivityIndexingServiceConnector activityIndexingServiceConnector = null;

  @Mock
  ActivitySearchProcessor          activitySearchProcessor;

  @Mock
  I18NActivityProcessor            i18nActivityProcessor;

  @Mock
  IdentityManager                  identityManager;

  @Mock
  ActivityManager                  activityManager;

  @Mock
  MetadataService                  metadataService;

  @Test
  public void testGetAllIds() {
    activityIndexingServiceConnector = new ActivityIndexingServiceConnector(activitySearchProcessor,
                                                                            i18nActivityProcessor,
                                                                            identityManager,
                                                                            activityManager,
                                                                            metadataService,
                                                                            getParams());
    try {
      activityIndexingServiceConnector.getAllIds(0, 10);
      fail("getAllIds shouldn't be supported");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testCreate() {
    activityIndexingServiceConnector = new ActivityIndexingServiceConnector(activitySearchProcessor,
                                                                            i18nActivityProcessor,
                                                                            identityManager,
                                                                            activityManager,
                                                                            metadataService,
                                                                            getParams());
    try {
      activityIndexingServiceConnector.create(null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      activityIndexingServiceConnector.create("1");
      fail("IllegalStateException should be thrown");
    } catch (IllegalStateException e) {
      // Expected
    }

    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId("1");
    activity.setParentId("2");
    activity.setParentCommentId("3");
    activity.setType("type");
    activity.setPosterId("posterId");
    activity.setPostedTime(1234L);
    activity.setUpdated(4321L);

    ActivityStreamImpl activityStream = new ActivityStreamImpl();
    activity.setActivityStream(activityStream);
    activityStream.setId("id");
    activityStream.setPrettyId("prettyId");
    activityStream.setType(Type.USER);

    Identity streamOwner = new Identity("streamOwner");
    when(identityManager.getOrCreateIdentity(Type.USER.getProviderId(), "prettyId")).thenReturn(streamOwner);

    Identity posterIdentity = new Identity("posterId");
    Profile posterProfile = new Profile(posterIdentity);
    posterProfile.setProperty("fullName", "Poster FullName");
    posterIdentity.setProfile(posterProfile);
    when(identityManager.getIdentity("posterId")).thenReturn(posterIdentity);

    when(activityManager.getActivity("1")).thenReturn(activity);
    activity.setTitleId("titleId");
    when(i18nActivityProcessor.process(eq(activity), any(Locale.class))).thenAnswer(invocation -> {
      ExoSocialActivity exoSocialActivity = invocation.getArgument(0, ExoSocialActivity.class);
      exoSocialActivity.setTitle("<div><h2>What is Lorem Ipsum?</h2>");
      return exoSocialActivity;
    });

    Document document = activityIndexingServiceConnector.create("1");
    assertNotNull(document);
    assertEquals("1", document.getId());
    assertEquals("2", document.getFields().get("parentId"));
    assertEquals("3", document.getFields().get("parentCommentId"));
    assertEquals("posterId", document.getFields().get("posterId"));
    assertEquals("Poster FullName", document.getFields().get("posterName"));
    assertEquals("1234", document.getFields().get("postedTime"));
    assertNotNull(document.getLastUpdatedDate());
    assertEquals(4321L, document.getLastUpdatedDate().getTime());
    assertNotNull(document.getPermissions());
    assertEquals(2, document.getPermissions().size());
    assertEquals("streamOwner", document.getPermissions().iterator().next());
  }

  @Test
  public void testUpdate() {
    activityIndexingServiceConnector = new ActivityIndexingServiceConnector(activitySearchProcessor,
                                                                            i18nActivityProcessor,
                                                                            identityManager,
                                                                            activityManager,
                                                                            metadataService,
                                                                            getParams());
    try {
      activityIndexingServiceConnector.update(null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      activityIndexingServiceConnector.update("1");
      fail("IllegalStateException should be thrown");
    } catch (IllegalStateException e) {
      // Expected
    }

    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId("1");
    activity.setParentId("2");
    activity.setParentCommentId("3");
    activity.setType("type");
    activity.setPosterId("posterId");
    activity.setPostedTime(1234L);
    activity.setUpdated(4321L);

    ActivityStreamImpl activityStream = new ActivityStreamImpl();
    activity.setActivityStream(activityStream);
    activityStream.setId("id");
    activityStream.setPrettyId("prettyId");
    activityStream.setType(Type.USER);

    Identity streamOwner = new Identity("streamOwner");
    when(identityManager.getOrCreateIdentity(Type.USER.getProviderId(), "prettyId")).thenReturn(streamOwner);

    Identity posterIdentity = new Identity("posterId");
    Profile posterProfile = new Profile(posterIdentity);
    posterProfile.setProperty("fullName", "PosterUpdated FullName");
    posterIdentity.setProfile(posterProfile);
    when(identityManager.getIdentity("posterId")).thenReturn(posterIdentity);

    when(activityManager.getActivity("1")).thenReturn(activity);
    activity.setTitleId("titleId");
    when(i18nActivityProcessor.process(eq(activity), any(Locale.class))).thenAnswer(invocation -> {
      ExoSocialActivity exoSocialActivity = invocation.getArgument(0, ExoSocialActivity.class);
      exoSocialActivity.setTitle("<div><h2>What is Lorem Ipsum?</h2>");
      return exoSocialActivity;
    });

    Document document = activityIndexingServiceConnector.update("1");
    assertNotNull(document);
    assertEquals("1", document.getId());
    assertEquals("2", document.getFields().get("parentId"));
    assertEquals("3", document.getFields().get("parentCommentId"));
    assertEquals("type", document.getFields().get("type"));
    assertEquals("posterId", document.getFields().get("posterId"));
    assertEquals("PosterUpdated FullName", document.getFields().get("posterName"));
    assertEquals("1234", document.getFields().get("postedTime"));
    assertNotNull(document.getLastUpdatedDate());
    assertEquals(4321L, document.getLastUpdatedDate().getTime());
    assertNotNull(document.getPermissions());
    assertEquals(2, document.getPermissions().size());
    assertEquals("streamOwner", document.getPermissions().iterator().next());
  }

  private InitParams getParams() {
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    params.addParameter(propertiesParam);
    propertiesParam.setProperty("index_current", "index_name");
    return params;
  }

}
