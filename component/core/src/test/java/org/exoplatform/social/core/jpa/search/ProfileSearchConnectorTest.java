package org.exoplatform.social.core.jpa.search;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.search.Sorting;
import org.junit.Assert;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.mockito.Mockito.mockStatic;

public class ProfileSearchConnectorTest {
    private ProfileSearchConnector profileSearchConnector;
    private static final MockedStatic<CommonsUtils> COMMONS_UTILS = mockStatic(CommonsUtils.class);

    @Test
    public void testSearch() {
        ElasticSearchingClient elasticSearchClient = Mockito.mock(ElasticSearchingClient.class);
        IdentityManagerImpl identityManager = Mockito.mock(IdentityManagerImpl.class);
        profileSearchConnector = new ProfileSearchConnector(getInitParams(), elasticSearchClient);
        ProfileFilter filter = new ProfileFilter();
        Identity identity1 = new Identity("test","usernameee");
        String index = "profile_alias";
        String query = "{\n" +
                "   \"from\" : 0, \"size\" : 10,\n" +
                "   \"sort\": {\"name.raw\": {\"order\": \"ASC\"}}\n" +
                "       ,\n" +
                "\"query\" : {\n" +
                "      \"constant_score\" : {\n" +
                "        \"filter\" : {\n" +
                "          \"bool\" :{\n" +
                "      \"must\" : {\n" +
                "        \"query_string\" : {\n" +
                "          \"query\" : \"null\",\n" +
                "          \"fields\" : [\"connections\"]\n" +
                "        }\n" +
                "      }\n" +
                "     } \n" +
                "   } \n" +
                "  }\n" +
                " }\n" +
                ",\"_source\": false\n" +
                ",\"fields\": [\"_id\"]\n" +
                "}\n";
        long offset = 0;
        long limit = 10;
        Mockito.when(elasticSearchClient.sendRequest(query, index)).thenReturn("{\"took\":39,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"profile_v2\",\"_type\":\"_doc\",\"_id\":\"6\",\"_score\":null,\"fields\":{\"userName\":[\"test\"]},\"sort\":[\"test\"]}]}}");
        Mockito.when(identityManager.getIdentity(Mockito.anyString())).thenReturn(identity1);
        COMMONS_UTILS.when(() -> CommonsUtils.getService(Mockito.any())).thenReturn(identityManager);
        Identity identity = new Identity("username","test");
        Relationship.Type type = Relationship.Type.CONFIRMED;
        List<String> result = profileSearchConnector.search(identity, filter, type, offset, limit);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testSearchWithEmail() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ElasticSearchingClient elasticSearchClient = Mockito.mock(ElasticSearchingClient.class);
        IdentityManagerImpl identityManager = Mockito.mock(IdentityManagerImpl.class);
        Identity identity1 = new Identity("test","test");
        Identity identity2 = new Identity("test2","test2");
        profileSearchConnector = new ProfileSearchConnector(getInitParams(), elasticSearchClient);
        ProfileFilter filter = new ProfileFilter();
        Sorting sorting = new Sorting(Sorting.SortBy.FIRSTNAME, Sorting.OrderBy.DESC);
        Map <String,String> profileSettings = new HashMap<>();
        List<Identity> excludedIdentityList = new ArrayList<>();

        excludedIdentityList.add(identity1);
        excludedIdentityList.add(identity2);
        profileSettings.put("test","test");
        filter.setSearchEmail(true);
        filter.setSearchUserName(true);
        filter.setName("te-s t");
        filter.setEnabled(true);
        filter.setConnected(true);
        filter.setSorting(sorting);
        filter.setUserType("internal");
        filter.setEnrollmentStatus("notEnrolled");
        filter.setProfileSettings(profileSettings);
        filter.setExcludedIdentityList(excludedIdentityList);
        String index = "profile_alias";
        String query = "{\n" +
                "   \"from\" : 0, \"size\" : 10,\n" +
                "   \"sort\": {\"firstName.raw\": {\"order\": \"DESC\"}}\n" +
                "       ,\n" +
                "\"query\" : {\n" +
                "      \"constant_score\" : {\n" +
                "        \"filter\" : {\n" +
                "          \"bool\" :{\n" +
                buildAdvancedFilterQuery(filter) +
                ",\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"term\": {\n" +
                "                      \"external\": false\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must_not\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"external\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must_not\": [{\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"enrollmentDate\"\n" +
                "                          }\n" +
                "                        },\n" +
                "                      {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }],\n" +
                "                      \"must\": {\n" +
                "                       \"term\": {\n" +
                "                         \"external\": false\n" +
                "                         }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "      ,\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"ids\" : {\n" +
                "             \"values\" : [\"null\",\"null\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "      ,\n" +
                "    \"filter\": [\n" +
                "      {          \"query_string\": {\n" +
                "            \"query\": \"( name.whitespace:*te-s* OR email:*te-s* OR userName:*te-s*) AND ( name.whitespace:*t* OR email:*t* OR userName:*t*)\"\n" +
                "          }\n" +
                "      }\n" +
                "    ]\n" +
                "     } \n" +
                "   } \n" +
                "  }\n" +
                " }\n" +
                ",\"_source\": false\n" +
                ",\"fields\": [\"_id\"]\n" +
                "}\n";
        long offset = 0;
        long limit = 10;
        Mockito.when(elasticSearchClient.sendRequest(query, index)).thenReturn("{\"took\":39,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"profile_v2\",\"_type\":\"_doc\",\"_id\":\"6\",\"_score\":null,\"fields\":{\"userName\":[\"test\"]},\"sort\":[\"test\"]}]}}");
        Mockito.when(identityManager.getIdentity(Mockito.anyString())).thenReturn(identity1);
        COMMONS_UTILS.when(() -> CommonsUtils.getService(Mockito.any())).thenReturn(identityManager);

        List<String> result = profileSearchConnector.search(null, filter, null, offset, limit);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testSearchWithNameOrUsername() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ElasticSearchingClient elasticSearchClient = Mockito.mock(ElasticSearchingClient.class);
        profileSearchConnector = new ProfileSearchConnector(getInitParams(), elasticSearchClient);
        IdentityManagerImpl identityManager = Mockito.mock(IdentityManagerImpl.class);
        ProfileFilter filter = new ProfileFilter();
        Sorting sorting = new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC);
        Map <String,String> profileSettings = new HashMap<>();
        List<Identity> excludedIdentityList = new ArrayList<>();
        Identity identity1 = new Identity("test","test");
        Identity identity2 = new Identity("test2","test2");
        excludedIdentityList.add(identity1);
        excludedIdentityList.add(identity2);
        profileSettings.put("test","test");
        filter.setSearchEmail(false);
        filter.setSearchUserName(false);
        filter.setName("\\\"te-s t\\\"");
        filter.setEnabled(true);
        filter.setConnected(true);
        filter.setSorting(sorting);
        filter.setUserType("external");
        filter.setEnrollmentStatus("noEnrollmentPossible");
        filter.setProfileSettings(profileSettings);
        filter.setExcludedIdentityList(excludedIdentityList);
        filter.setRemoteIds(Collections.singletonList("test"));
        String index = "profile_alias";
        String query = "{\n" +
                "   \"from\" : 0, \"size\" : 10,\n" +
                "   \"sort\": {\"lastUpdatedDate\": {\"order\": \"DESC\"}}\n" +
                "       ,\n" +
                "\"query\" : {\n" +
                "      \"constant_score\" : {\n" +
                "        \"filter\" : {\n" +
                "          \"bool\" :{\n" +
                buildAdvancedFilterQuery(filter) +
                ",\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"term\": {\n" +
                "                      \"external\": true\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must_not\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"enrollmentDate\"\n" +
                "                          }\n" +
                "                        },\n" +
                "                      \"must\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"term\": {\n" +
                "                      \"external\": true\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "      \"must\" : {\n" +
                "        \"terms\" :{\n" +
                "          \"userName\" : [\"test\"]\n" +
                "        } \n" +
                "      },\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"ids\" : {\n" +
                "             \"values\" : [\"null\",\"null\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "      ,\n" +
                "    \"filter\": [\n" +
                "      {          \"query_string\": {\n" +
                "            \"query\": \"( name.whitespace:*\\\"te-s*) AND ( name.whitespace:*t\\\"*)\"\n" +
                "          }\n" +
                "      }\n" +
                "    ]\n" +
                "     } \n" +
                "   } \n" +
                "  }\n" +
                " }\n" +
                ",\"_source\": false\n" +
                ",\"fields\": [\"_id\"]\n" +
                "}\n";
        Mockito.when(elasticSearchClient.sendRequest(query, index)).thenReturn("{\"took\":39,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"profile_v2\",\"_type\":\"_doc\",\"_id\":\"6\",\"_score\":null,\"fields\":{\"userName\":[\"test\"]},\"sort\":[\"test\"]}]}}");
        Mockito.when(identityManager.getIdentity(Mockito.anyString())).thenReturn(identity1);
        COMMONS_UTILS.when(() -> CommonsUtils.getService(Mockito.any())).thenReturn(identityManager);
        long offset = 0;
        long limit = 10;
        List<String>  result = profileSearchConnector.search(null, filter, null, offset, limit);
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testCount() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ElasticSearchingClient elasticSearchClient = Mockito.mock(ElasticSearchingClient.class);
        profileSearchConnector = new ProfileSearchConnector(getInitParams(), elasticSearchClient);
        ProfileFilter filter = new ProfileFilter();
        Sorting sorting = new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC);
        Map <String,String> profileSettings = new HashMap<>();
        List<Identity> excludedIdentityList = new ArrayList<>();
        Identity identity1 = new Identity("test1","test1");
        Identity identity2 = new Identity("test2","test2");
        excludedIdentityList.add(identity1);
        excludedIdentityList.add(identity2);
        profileSettings.put("test","test");
        filter.setSearchEmail(false);
        filter.setSearchUserName(false);
        filter.setName("\\\"te-s t\\\"");
        filter.setEnabled(true);
        filter.setConnected(true);
        filter.setSorting(sorting);
        filter.setUserType("external");
        filter.setEnrollmentStatus("noEnrollmentPossible");
        filter.setProfileSettings(profileSettings);
        filter.setExcludedIdentityList(excludedIdentityList);
        filter.setRemoteIds(Collections.singletonList("test"));
        String index = "profile_alias";
        String query = "{\n" +
                "   \"from\" : 0, \"size\" : 1,\n" +
                "   \"sort\": {\"lastUpdatedDate\": {\"order\": \"DESC\"}}\n" +
                "       ,\n" +
                "\"query\" : {\n" +
                "      \"constant_score\" : {\n" +
                "        \"filter\" : {\n" +
                "          \"bool\" :{\n" +
                buildAdvancedFilterQuery(filter) +
                ",\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"term\": {\n" +
                "                      \"external\": true\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "    \"should\": [\n" +
                "                  {\n" +
                "                    \"bool\": {\n" +
                "                      \"must_not\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"enrollmentDate\"\n" +
                "                          }\n" +
                "                        },\n" +
                "                      \"must\": {\n" +
                "                        \"exists\": {\n" +
                "                          \"field\": \"lastLoginTime\"\n" +
                "                        }\n" +
                "                      }\n" +
                "                    }\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"term\": {\n" +
                "                      \"external\": true\n" +
                "                    }\n" +
                "                  }\n" +
                "                  ]\n" +
                "                  ,\"minimum_should_match\" : 1\n" +
                "      \"must\" : {\n" +
                "        \"terms\" :{\n" +
                "          \"userName\" : [\"test\"]\n" +
                "        } \n" +
                "      },\n" +
                "      \"must_not\": [\n" +
                "        {\n" +
                "          \"ids\" : {\n" +
                "             \"values\" : [\"null\",\"null\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "      ,\n" +
                "    \"filter\": [\n" +
                "      {          \"query_string\": {\n" +
                "            \"query\": \"( name.whitespace:*\\\"te-s*) AND ( name.whitespace:*t\\\"*)\"\n" +
                "          }\n" +
                "      }\n" +
                "    ]\n" +
                "     } \n" +
                "   } \n" +
                "  }\n" +
                " }\n" +
                ",\"_source\": false\n" +
                ",\"fields\": [\"_id\"]\n" +
                "}\n";
        Mockito.when(elasticSearchClient.sendRequest(query, index)).thenReturn("{\"took\":39,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"max_score\":null,\"hits\":[{\"_index\":\"profile_v2\",\"_type\":\"_doc\",\"_id\":\"6\",\"_score\":null,\"fields\":{\"userName\":[\"test\"]},\"sort\":[\"test\"]}]}}");


        int result = profileSearchConnector.count(null, filter, null);
        Assert.assertEquals(1, result);
    }

    private String buildAdvancedFilterQuery(ProfileFilter filter) throws NoSuchMethodException,
                                                                  InvocationTargetException,
                                                                  IllegalAccessException {
      Method method = profileSearchConnector.getClass().getDeclaredMethod("buildAdvancedFilterExpression", ProfileFilter.class);
      method.setAccessible(true);
      return (String) method.invoke(profileSearchConnector, filter);
    }
    
    private InitParams getInitParams() {
        InitParams params = new InitParams();
        PropertiesParam constructorParams = new PropertiesParam();
        constructorParams.setName("constructor.params");
        constructorParams.setProperty("searchType", "profile_alias");
        constructorParams.setProperty("displayName", "profile_alias");
        constructorParams.setProperty("index", "profile_alias");
        constructorParams.setProperty("type", "profile_alias");
        constructorParams.setProperty("searchFields", "name");
        params.addParam(constructorParams);
        return params;
    }
}
