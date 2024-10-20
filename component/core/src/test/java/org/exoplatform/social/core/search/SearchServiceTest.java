package org.exoplatform.social.core.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.core.search.impl.SearchServiceImpl;

/**
 * Test {@link SearchServiceImpl}
 */
public class SearchServiceTest {

  private static final String TEST_USER = "testUser";

  private SearchServiceImpl   searchService;

  @Before
  public void setup() {
    Map<String, String> settingValues = new HashMap<>();
    SettingService settingService = mock(SettingService.class);
    doAnswer(invocation -> {
      Context context = invocation.getArgument(0, Context.class);
      Scope scope = invocation.getArgument(1, Scope.class);
      String key = invocation.getArgument(2, String.class);
      SettingValue<?> value = invocation.getArgument(3, SettingValue.class);
      String settingKey = context.getId() + context.getName() + scope.getId() + scope.getName() + key;
      settingValues.put(settingKey, value.toString());
      return null;
    }).when(settingService).set(any(Context.class), any(Scope.class), anyString(), any(SettingValue.class));

    when(settingService.get(any(Context.class), any(Scope.class), anyString())).thenAnswer(invocation -> {
      Context context = invocation.getArgument(0, Context.class);
      Scope scope = invocation.getArgument(1, Scope.class);
      String key = invocation.getArgument(2, String.class);
      String settingKey = context.getId() + context.getName() + scope.getId() + scope.getName() + key;
      String value = settingValues.get(settingKey);
      return value == null ? null : SettingValue.create(value);
    });

    searchService = new SearchServiceImpl(settingService);
  }

  @Test
  public void testAddConnector() {
    InitParams params = new InitParams();
    ObjectParameter objectParameter = new ObjectParameter();
    SearchConnector searchConnector = new SearchConnector();
    searchConnector.setEnabled(false);
    searchConnector.setName("testConnector");
    searchConnector.setUri("uri");
    objectParameter.setObject(searchConnector);
    params.addParam(objectParameter);

    SearchConnectorPlugin connectorPlugin = new SearchConnectorPlugin(params);
    searchService.addConnector(connectorPlugin);

    assertNotNull(searchService.getConnectors());
    assertEquals(1, searchService.getConnectors().size());
  }

  @Test
  public void testGetConnectors() {
    InitParams params = new InitParams();
    ObjectParameter objectParameter = new ObjectParameter();
    SearchConnector searchConnector = new SearchConnector();
    searchConnector.setEnabled(false);
    searchConnector.setName("testConnector");
    searchConnector.setUri("uri");
    objectParameter.setObject(searchConnector);
    params.addParam(objectParameter);

    SearchConnectorPlugin connectorPlugin = new SearchConnectorPlugin(params);
    searchService.addConnector(connectorPlugin);

    assertNotNull(searchService.getConnectors());
    assertEquals(1, searchService.getConnectors().size());

    try {
      searchService.getConnectors().iterator().remove();
      fail("Returned list must be unmodifiable");
    } catch (Exception e) {
      // Expected
    }

    SearchConnector searchConnectorResult = searchService.getConnectors().iterator().next();
    assertNotNull(searchConnectorResult);

    assertFalse(searchConnectorResult.isEnabled());
    assertEquals("testConnector", searchConnectorResult.getName());
    assertEquals("uri", searchConnectorResult.getUri());

    searchConnectorResult.setEnabled(true);
    assertTrue(searchConnectorResult.isEnabled());

    searchConnectorResult = searchService.getConnectors().iterator().next();
    assertNotNull(searchConnectorResult);
    assertFalse("modification made on returned object, should not be applied on original objects of service",
                searchConnectorResult.isEnabled());
  }

  @Test
  public void testGetEnabledConnectors() {
    InitParams params = new InitParams();
    ObjectParameter objectParameter = new ObjectParameter();
    SearchConnector searchConnector = new SearchConnector();
    searchConnector.setEnabled(true);
    searchConnector.setName("testConnector");
    searchConnector.setUri("uri");
    objectParameter.setObject(searchConnector);
    params.addParam(objectParameter);

    SearchConnectorPlugin connectorPlugin = new SearchConnectorPlugin(params);
    searchService.addConnector(connectorPlugin);

    assertNotNull(searchService.getEnabledConnectors(TEST_USER));
    assertEquals(1, searchService.getEnabledConnectors(TEST_USER).size());

    try {
      searchService.getEnabledConnectors(TEST_USER).iterator().remove();
      fail("Returned list must be unmodifiable");
    } catch (Exception e) {
      // Expected
    }

    SearchConnector searchConnectorResult = searchService.getEnabledConnectors(TEST_USER).iterator().next();
    assertNotNull(searchConnectorResult);

    assertTrue(searchConnectorResult.isEnabled());
    assertEquals("testConnector", searchConnectorResult.getName());
    assertEquals("uri", searchConnectorResult.getUri());

    searchService.setConnectorAsEnabled(searchConnector.getName(), false);
    assertNotNull(searchService.getEnabledConnectors(TEST_USER));
    assertEquals(0, searchService.getEnabledConnectors(TEST_USER).size());
  }

  @Test
  public void testSetConnectorAsEnabled() {
    InitParams params = new InitParams();
    ObjectParameter objectParameter = new ObjectParameter();
    SearchConnector searchConnector = new SearchConnector();
    searchConnector.setEnabled(false);
    searchConnector.setName("testConnector");
    searchConnector.setUri("uri");
    objectParameter.setObject(searchConnector);
    params.addParam(objectParameter);

    SearchConnectorPlugin connectorPlugin = new SearchConnectorPlugin(params);
    searchService.addConnector(connectorPlugin);

    assertNotNull(searchService.getConnectors());
    assertEquals(1, searchService.getConnectors().size());

    SearchConnector searchConnectorResult = searchService.getConnectors().iterator().next();
    assertNotNull(searchConnectorResult);
    searchService.setConnectorAsEnabled(searchConnectorResult.getName(), true);

    searchConnectorResult = searchService.getConnectors().iterator().next();
    assertNotNull(searchConnectorResult);
    assertTrue("modification made using setConnectorAsEnabled, should be applied on original objects of service",
               searchConnectorResult.isEnabled());
  }

  @Test
  public void testGetEnabledConnectorNames() {
    InitParams params = new InitParams();
    ObjectParameter objectParameter = new ObjectParameter();
    SearchConnector searchConnector = new SearchConnector();
    searchConnector.setEnabled(false);
    searchConnector.setName("testConnector");
    searchConnector.setUri("uri");
    objectParameter.setObject(searchConnector);
    params.addParam(objectParameter);

    SearchConnectorPlugin connectorPlugin = new SearchConnectorPlugin(params);
    searchService.addConnector(connectorPlugin);

    assertNotNull(searchService.getConnectors());
    assertEquals(1, searchService.getConnectors().size());

    assertNotNull(searchService.getEnabledConnectorNames());
    assertEquals(0, searchService.getEnabledConnectorNames().size());

    SearchConnector searchConnectorResult = searchService.getConnectors().iterator().next();
    assertNotNull(searchConnectorResult);
    searchService.setConnectorAsEnabled(searchConnectorResult.getName(), true);

    assertNotNull(searchService.getEnabledConnectorNames());
    assertEquals(1, searchService.getEnabledConnectorNames().size());
  }

}
