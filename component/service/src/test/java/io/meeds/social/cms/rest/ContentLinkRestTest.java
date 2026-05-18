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
package io.meeds.social.cms.rest;

import static io.meeds.social.util.JsonUtils.toJsonString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkList;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.model.ContentObjectIdentifier;
import io.meeds.social.cms.service.ContentLinkService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;
import lombok.SneakyThrows;

@SpringBootTest(classes = { ContentLinkRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class ContentLinkRestTest {

  private static final String   REST_PATH     = "/contentLinks/%s/%s?fieldName=%s";

  private static final String   CONTENT_TYPE  = "contentTest";

  private static final String   CONTENT_ID    = "contentId";

  private static final String   FIELD_NAME    = "description";

  private static final String   TEST_USER     = "simple";

  private static final String   TEST_PASSWORD = "testPassword";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private ContentLinkService    contentLinkService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  @SneakyThrows
  public void getLinksWhenForbidden() {
    when(contentLinkService.getLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID, FIELD_NAME),
                                     Locale.ENGLISH,
                                     TEST_USER)).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(get(String.format(REST_PATH,
                                                               CONTENT_TYPE,
                                                               CONTENT_ID,
                                                               FIELD_NAME)).with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  @SneakyThrows
  public void getLinks() {
    ContentLink link = new ContentLink(CONTENT_TYPE, CONTENT_ID, Locale.ENGLISH, "title", "uri");
    List<ContentLink> links = Collections.singletonList(link);
    when(contentLinkService.getLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID, FIELD_NAME),
                                     Locale.ENGLISH,
                                     TEST_USER)).thenReturn(links);
    ResultActions response = mockMvc.perform(get(String.format(REST_PATH,
                                                               CONTENT_TYPE,
                                                               CONTENT_ID,
                                                               FIELD_NAME)).with(testSimpleUser()));
    response.andExpect(status().isOk());
    response.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    response.andExpect(content().json(toJsonString(links)));
  }

  @Test
  @SneakyThrows
  public void testSaveLinksWhenForbidden() {

    ContentObjectIdentifier link = new ContentObjectIdentifier(CONTENT_TYPE, CONTENT_ID);
    ContentLinkList linkList = new ContentLinkList(Collections.singletonList(link));
    doThrow(IllegalAccessException.class).when(contentLinkService)
                                         .saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID, FIELD_NAME),
                                                    linkList.getLinks(),
                                                    TEST_USER);
    ResultActions response = mockMvc.perform(put(String.format(REST_PATH,
                                                               CONTENT_TYPE,
                                                               CONTENT_ID,
                                                               FIELD_NAME)).content(toJsonString(linkList))
                                                                           .contentType(MediaType.APPLICATION_JSON)
                                                                           .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  @SneakyThrows
  public void testSaveLinks() {
    ContentObjectIdentifier link = new ContentObjectIdentifier(CONTENT_TYPE, CONTENT_ID);
    ContentLinkList linkList = new ContentLinkList(Collections.singletonList(link));
    ResultActions response = mockMvc.perform(put(String.format(REST_PATH,
                                                               CONTENT_TYPE,
                                                               CONTENT_ID,
                                                               FIELD_NAME)).content(toJsonString(linkList))
                                                                           .contentType(MediaType.APPLICATION_JSON)
                                                                           .with(testSimpleUser()));
    response.andExpect(status().isNoContent());
    verify(contentLinkService).saveLinks(new ContentObject(CONTENT_TYPE, CONTENT_ID, FIELD_NAME),
                                         linkList.getLinks(),
                                         TEST_USER);
  }

  private RequestPostProcessor testSimpleUser() {
    return user(TEST_USER).password(TEST_PASSWORD)
                          .authorities(new SimpleGrantedAuthority("users"));
  }

}
