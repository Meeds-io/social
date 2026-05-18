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
package io.meeds.social.resource.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.portal.resource.SkinConfig;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.SkinURL;
import org.exoplatform.services.resources.Orientation;

import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { SkinRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class SkinRestTest {

  private static final String SKIN_ID = "test";

  private static final String SKIN_URL = "skin-url";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private SkinService           skinService;

  @Mock
  private SkinConfig            skin;
  @Mock
  private SkinURL               skinUrl;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void getSkinWhenPortalAndNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get("/skins/portal/test"));
    response.andExpect(status().isNotFound());
  }
  
  @Test
  public void getSkinWhenPortalSkin() throws Exception {
    when(skinService.getPortalSkin(SKIN_ID, null)).thenReturn(skin);
    when(skin.createURL()).thenReturn(skinUrl);
    when(skinUrl.toString(Orientation.LT)).thenReturn(SKIN_URL);
    ResultActions response = mockMvc.perform(get("/skins/portal/test"));
    response.andExpect(status().is3xxRedirection());
  }

  @Test
  public void getSkinWhenPortletSkin() throws Exception {
    when(skinService.getSkin("layout/test", null)).thenReturn(skin);
    when(skin.createURL()).thenReturn(skinUrl);
    when(skinUrl.toString(Orientation.LT)).thenReturn(SKIN_URL);
    ResultActions response = mockMvc.perform(get("/skins/layout/test"));
    response.andExpect(status().is3xxRedirection());
  }

}
