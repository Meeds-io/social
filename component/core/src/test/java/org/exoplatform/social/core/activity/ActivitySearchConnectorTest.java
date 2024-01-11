package org.exoplatform.social.core.activity;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.*;
import org.exoplatform.social.core.activity.filter.ActivitySearchFilter;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ActivitySearchConnector;
import org.exoplatform.social.core.jpa.search.ActivitySearchProcessor;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.MetadataService;

@RunWith(MockitoJUnitRunner.class)
public class ActivitySearchConnectorTest {

  private static final String ES_INDEX         = "activity_alias";

  public static final String  FAKE_ES_QUERY    =
                                            "{offset: @offset@, limit: @limit@, @term_query@ permissions: @permissions@}";

  @Mock
  ActivitySearchProcessor     activitySearchProcessor;

  @Mock
  IdentityManager             identityManager;

  @Mock
  MetadataService             metadataService;

  @Mock
  ActivityStorage             activityStorage;

  @Mock
  ConfigurationManager        configurationManager;

  @Mock
  ElasticSearchingClient      client;

  String                      searchResult     = null;

  boolean                     developpingValue = false;

  @Before
  public void setUp() throws Exception {// NOSONAR
    searchResult = IOUtil.getStreamContentAsString(getClass().getClassLoader()
                                                             .getResourceAsStream("activities-search-result.json"));

    try {
      Mockito.reset(configurationManager);
      when(configurationManager.getInputStream("FILE_PATH")).thenReturn(new ByteArrayInputStream(FAKE_ES_QUERY.getBytes()));
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving ES Query content", e);
    }
    developpingValue = PropertyManager.isDevelopping();
    PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
    PropertyManager.refresh();
  }

  @After
  public void tearDown() {
    PropertyManager.setProperty(PropertyManager.DEVELOPING, String.valueOf(developpingValue));
    PropertyManager.refresh();
  }

  @Test
  public void testSearchArguments() {
    ActivitySearchConnector activitySearchConnector = new ActivitySearchConnector(activitySearchProcessor,
                                                                                  identityManager,
                                                                                  activityStorage,
                                                                                  configurationManager,
                                                                                  client,
                                                                                  getParams());

    ActivitySearchFilter filter = new ActivitySearchFilter("term");
    try {
      activitySearchConnector.search(null, filter, 0, 10);
      fail("Should throw IllegalArgumentException: viewer identity is mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    try {
      activitySearchConnector.search(identity, null, 0, 10);
      fail("Should throw IllegalArgumentException: filter is mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      activitySearchConnector.search(identity, filter, -1, 10);
      fail("Should throw IllegalArgumentException: offset should be positive");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      activitySearchConnector.search(identity, filter, 0, -1);
      fail("Should throw IllegalArgumentException: limit should be positive");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testSearchNoResult() {
    ActivitySearchConnector activitySearchConnector = new ActivitySearchConnector(activitySearchProcessor,
                                                                                  identityManager,
                                                                                  activityStorage,
                                                                                  configurationManager,
                                                                                  client,
                                                                                  getParams());

    ActivitySearchFilter filter = new ActivitySearchFilter("term");
    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    lenient().when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
                                                      ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                                                                                                        filter.getTerm())
                                                                                               .replace("@term_query@",
                                                                                                        filter.getTerm()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    when(client.sendRequest(expectedESQuery, ES_INDEX)).thenReturn("{}");

    List<ActivitySearchResult> result = activitySearchConnector.search(identity, filter, 0, 10);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testSearchWithResult() {
    ActivitySearchConnector activitySearchConnector = new ActivitySearchConnector(activitySearchProcessor,
                                                                                  identityManager,
                                                                                  activityStorage,
                                                                                  configurationManager,
                                                                                  client,
                                                                                  getParams());

    ActivitySearchFilter filter = new ActivitySearchFilter("term");
    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    when(identity.getId()).thenReturn("1");
    when(activityStorage.getStreamFeedOwnerIds(identity)).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
                                                      ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                                                                                                        filter.getTerm())
                                                                                               .replace("@term_query@",
                                                                                                        filter.getTerm()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    when(client.sendRequest(expectedESQuery, ES_INDEX)).thenReturn(searchResult);

    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId("3");
    activity.setParentId("2");
    activity.setParentCommentId("5");
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
    Identity poster = new Identity("posterId");
    when(identityManager.getOrCreateIdentity(Type.USER.getProviderId(), "prettyId")).thenReturn(streamOwner);
    when(identityManager.getIdentity("posterId")).thenReturn(poster);

    when(activityStorage.getActivity("3")).thenReturn(activity);

    List<ActivitySearchResult> result = activitySearchConnector.search(identity, filter, 0, 10);
    assertNotNull(result);
    assertEquals(1, result.size());

    ActivitySearchResult activitySearchResult = result.iterator().next();
    assertEquals(3L, activitySearchResult.getId());
    assertEquals("type", activitySearchResult.getType());
    assertEquals(poster, activitySearchResult.getPoster());
    assertEquals(1234L, activitySearchResult.getPostedTime());
    assertEquals(4321L, activitySearchResult.getLastUpdatedTime());
    assertEquals(streamOwner, activitySearchResult.getStreamOwner());
    assertNull(activitySearchResult.getExcerpts());

    ActivitySearchResult commentSearchResult = activitySearchResult.getComment();
    assertNotNull(commentSearchResult);
    assertEquals(6L, commentSearchResult.getId());
    assertEquals("exosocial:spaces", commentSearchResult.getType());
    assertNull(commentSearchResult.getPoster());
    assertEquals(1592227545758L, commentSearchResult.getPostedTime());
    assertEquals(1592227545759L, commentSearchResult.getLastUpdatedTime());
    assertNull(commentSearchResult.getStreamOwner());
  }

  @Test
  public void testSearchWithIdentityResult() throws IOException {// NOSONAR
    ActivitySearchConnector activitySearchConnector = new ActivitySearchConnector(activitySearchProcessor,
                                                                                  identityManager,
                                                                                  activityStorage,
                                                                                  configurationManager,
                                                                                  client,
                                                                                  getParams());

    ActivitySearchFilter filter = new ActivitySearchFilter("John");
    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    when(activityStorage.getStreamFeedOwnerIds(identity)).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
                                                      ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                                                                                                        filter.getTerm())
                                                                                               .replace("@term_query@",
                                                                                                        filter.getTerm()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    searchResult = IOUtil.getStreamContentAsString(getClass().getClassLoader()
                                                             .getResourceAsStream("activities-search-result-by-identity.json"));
    when(client.sendRequest(expectedESQuery, ES_INDEX)).thenReturn(searchResult);

    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setId("7");
    activity.setType("activity-type");
    activity.setPosterId("7");
    activity.setPostedTime(1234L);
    activity.setUpdated(4321L);

    Identity streamOwner = new Identity("streamOwner");
    streamOwner.setId("10");
    Identity poster = new Identity("posterId");
    lenient().when(identityManager.getOrCreateIdentity(Type.USER.getProviderId(), "prettyId")).thenReturn(streamOwner);
    when(identityManager.getIdentity("2")).thenReturn(poster);
    when(identityManager.getIdentity("10")).thenReturn(streamOwner);

    lenient().when(activityStorage.getActivity(eq("7"))).thenReturn(activity);

    List<ActivitySearchResult> result = activitySearchConnector.search(identity, filter, 0, 10);
    assertNotNull(result);
    assertEquals(1, result.size());

    ActivitySearchResult activitySearchResult = result.iterator().next();
    assertEquals(7L, activitySearchResult.getId());
    assertEquals("activity-type", activitySearchResult.getType());
    assertEquals(poster, activitySearchResult.getPoster());
    assertEquals(1234L, activitySearchResult.getPostedTime());
    assertEquals(4321L, activitySearchResult.getLastUpdatedTime());
    assertNotNull(activitySearchResult.getExcerpts());
    assertEquals(0, activitySearchResult.getExcerpts().size());
    assertEquals(streamOwner, activitySearchResult.getStreamOwner());
  }

  private InitParams getParams() {
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    propertiesParam.setProperty("index", ES_INDEX);

    ValueParam valueParam = new ValueParam();
    valueParam.setName("query.file.path");
    valueParam.setValue("FILE_PATH");

    params.addParameter(propertiesParam);
    params.addParameter(valueParam);
    return params;
  }

}
