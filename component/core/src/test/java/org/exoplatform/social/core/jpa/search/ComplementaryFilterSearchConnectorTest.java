package org.exoplatform.social.core.jpa.search;

import org.apache.commons.lang3.tuple.Pair;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplementaryFilterSearchConnectorTest {

  private ComplementaryFilterSearchConnector complementaryFilterSearchConnector;

  @Mock
  private ElasticSearchingClient             client;

  @Before
  public void setUp() throws Exception {
    this.complementaryFilterSearchConnector = new ComplementaryFilterSearchConnector(client);
  }

  @Test
  public void testSearch() {

    List<String> attributes = new ArrayList<>();
    List<String> objectIds = new ArrayList<>();

    attributes.add("profession");
    attributes.add("city");

    objectIds.add("1");
    objectIds.add("2");
    when(client.sendRequest(anyString(), anyString())).thenReturn("""
        {"took":1,"timed_out":false,"_shards":{"total":5,"successful":5,"skipped":0,"failed":0},
        "hits":{"total":{"value":5,"relation":"eq"},"max_score":null,"hits":[]},"aggregations":
        {"common_city":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,"buckets":[{"key":"Ariana","doc_count":4}]},
        "common_profession":{"doc_count_error_upper_bound":0,"sum_other_doc_count":0,
        "buckets":[{"key":"writer","doc_count":3},{"key":"Developer","doc_count":2}]}}}
          """);
    List<Map<String, String>> result = this.complementaryFilterSearchConnector.search(attributes, objectIds, 2, "index_alias");
    assertNotNull(result);
    assertEquals(3, result.size());
  }
}
