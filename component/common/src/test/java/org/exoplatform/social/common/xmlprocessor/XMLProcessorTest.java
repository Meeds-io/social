/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.common.xmlprocessor;


import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.AbstractCommonTest;

/**
 * Unit Test for {@link XMLProcessor}.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class XMLProcessorTest extends AbstractCommonTest {

  private XMLProcessor xmlProcessor;

  private PortalContainer portalContainer;

  private List<Filter> tearDownFilter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    portalContainer = PortalContainer.getInstance();
    xmlProcessor = portalContainer.getComponentInstanceOfType(XMLProcessor.class);
    tearDownFilter = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    for (Filter filter : tearDownFilter) {
      xmlProcessor.removeFilter(filter);
    }
    super.tearDown();
  }

  public void testShouldReturnNullWhenProcessNullInput() {
    // Given
    Test1FilterPlugin test1FilterPlugin = new Test1FilterPlugin();
    xmlProcessor.addFilter(test1FilterPlugin);
    tearDownFilter.add(test1FilterPlugin);

    // When
    Object processedText = xmlProcessor.process(null);

    // Then
    assertNull(processedText);
  }

  public void testShouldApplyFilterWhenOneFilterAdded() {
    // Given
    Test1FilterPlugin test1FilterPlugin = new Test1FilterPlugin();
    xmlProcessor.addFilter(test1FilterPlugin);
    tearDownFilter.add(test1FilterPlugin);

    // When
    Object processedText = xmlProcessor.process("test");

    // Then
    assertNotNull(processedText);
    assertEquals("test-test1", (String) processedText);
  }

  public void testShouldApplyFiltersWhenTwoFiltersAdded() {
    // Given
    Test1FilterPlugin test1FilterPlugin = new Test1FilterPlugin();
    xmlProcessor.addFilter(test1FilterPlugin);
    tearDownFilter.add(test1FilterPlugin);
    Test2FilterPlugin test2FilterPlugin = new Test2FilterPlugin();
    xmlProcessor.addFilter(test2FilterPlugin);
    tearDownFilter.add(test2FilterPlugin);

    // When
    Object processedText = xmlProcessor.process("test");

    // Then
    assertNotNull(processedText);
    assertEquals("test-test1-test2", (String) processedText);
  }

  public void testShouldApplyFiltersWhenTwoFiltersAddedAndOneRemoved() {
    // Given
    Test1FilterPlugin test1FilterPlugin = new Test1FilterPlugin();
    xmlProcessor.addFilter(test1FilterPlugin);
    Test2FilterPlugin test2FilterPlugin = new Test2FilterPlugin();
    xmlProcessor.addFilter(test2FilterPlugin);
    tearDownFilter.add(test2FilterPlugin);
    xmlProcessor.removeFilter(test1FilterPlugin);

    // When
    Object processedText = xmlProcessor.process("test");

    // Then
    assertNotNull(processedText);
    assertEquals("test-test2", (String) processedText);
  }

  public void testShouldApplyFiltersByPluginWhenTwoFiltersAddedAndOneRemoved() {
    // Given
    Test1FilterPlugin test1FilterPlugin = new Test1FilterPlugin();
    xmlProcessor.addFilterPlugin(test1FilterPlugin);
    Test2FilterPlugin test2FilterPlugin = new Test2FilterPlugin();
    xmlProcessor.addFilterPlugin(test2FilterPlugin);
    tearDownFilter.add(test2FilterPlugin);
    xmlProcessor.removeFilterPlugin(test1FilterPlugin);

    // When
    Object processedText = xmlProcessor.process("test");

    // Then
    assertNotNull(processedText);
    assertEquals("test-test2", (String) processedText);
  }

  class Test1FilterPlugin extends BaseXMLFilterPlugin {
    @Override
    public Object doFilter(Object input) {
      if(input != null) {
        String text = (String) input;
        return text + "-test1";
      }
      return null;
    }
  }

  class Test2FilterPlugin extends BaseXMLFilterPlugin {
    @Override
    public Object doFilter(Object input) {
      if(input != null) {
        String text = (String) input;
        return text + "-test2";
      }
      return null;
    }
  }

}
