/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.rest.impl.complementaryfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.jpa.search.ComplementaryFilterSearchConnector;

@RunWith(MockitoJUnitRunner.class)
public class ComplementaryFilterRestTest {

  @Mock
  private ComplementaryFilterSearchConnector connector;

  private ComplementaryFilterRest            rest;

  @Before
  public void setUp() {
    rest = new ComplementaryFilterRest(connector);
  }

  @Test
  public void testSuggestionsDelegateToTheConnectorWithoutAnyIndex() {
    List<Map<String, String>> rows = List.of(Map.of("key", "city", "value", "Ariana", "count", "4"));
    when(connector.search(List.of("city"), List.of("1", "2"), 2)).thenReturn(rows);

    Response response = rest.getComplementaryFilterSuggestions(List.of("1", "2"), List.of("city"), 2);

    assertEquals(200, response.getStatus());
    assertEquals(rows, response.getEntity());
  }

  /**
   * The endpoint carries no path parameter: the index alias is not part of
   * the contract, so no request can aim the aggregation at another index.
   * Mutant: adding a {@code @PathParam} back to the method, or a
   * placeholder to its {@code @Path}.
   */
  @Test
  public void testEndpointExposesNoIndexParameter() throws Exception {
    Method method = ComplementaryFilterRest.class.getMethod("getComplementaryFilterSuggestions",
                                                              List.class,
                                                              List.class,
                                                              int.class);
    assertEquals("suggestions", method.getAnnotation(Path.class).value());
    assertFalse(method.getAnnotation(Path.class).value().contains("{"));
    for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
      for (Annotation annotation : parameterAnnotations) {
        assertFalse("no parameter may come from the path", annotation instanceof PathParam);
      }
    }
  }

  @Test
  public void testValidationFailureAnswers400WithTheMessageCode() {
    when(connector.search(anyList(), anyList(), anyInt())).thenThrow(new IllegalArgumentException("complementaryFilter.unknownAttribute"));

    Response response = rest.getComplementaryFilterSuggestions(List.of("1", "2"), List.of("activity"), 2);

    assertEquals(400, response.getStatus());
    assertEquals("complementaryFilter.unknownAttribute", response.getEntity());
  }

  @Test
  public void testUnexpectedFailureAnswers500WithoutTheEngineMessage() {
    when(connector.search(anyList(), anyList(), anyInt())).thenThrow(new IllegalStateException("es said: index [x] not found"));

    Response response = rest.getComplementaryFilterSuggestions(List.of("1", "2"), List.of("city"), 2);

    assertEquals(500, response.getStatus());
    assertNull(response.getEntity());
  }

  /**
   * An empty or missing list is the connector's refusal, like every other
   * invalid input: one contract, the message code as the 400 body.
   */
  @Test
  public void testEmptyInputsAnswer400WithTheConnectorCode() {
    when(connector.search(any(), any(), anyInt())).thenThrow(new IllegalArgumentException(ComplementaryFilterSearchConnector.INVALID_OBJECT_ID_MESSAGE));

    Response response = rest.getComplementaryFilterSuggestions(List.of(), List.of("city"), 2);
    assertEquals(400, response.getStatus());
    assertEquals(ComplementaryFilterSearchConnector.INVALID_OBJECT_ID_MESSAGE, response.getEntity());
    assertEquals(400, rest.getComplementaryFilterSuggestions(null, List.of("city"), 2).getStatus());
    assertEquals(400, rest.getComplementaryFilterSuggestions(List.of("1"), List.of(), 2).getStatus());
    assertEquals(400, rest.getComplementaryFilterSuggestions(List.of("1"), null, 2).getStatus());
  }
}
