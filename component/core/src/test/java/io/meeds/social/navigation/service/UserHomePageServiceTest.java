/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.portal.config.UserPortalConfigService;

import io.meeds.portal.navigation.service.NavigationConfigurationService;

@SpringBootTest(classes = {
  UserHomePageService.class,
})
@RunWith(SpringRunner.class)
public class UserHomePageServiceTest {

  private static final String            USERNAME = "john";

  @MockitoBean
  private UserPortalConfigService        userPortalConfigService;

  @MockitoBean
  private NavigationConfigurationService navigationConfigurationService;

  @Autowired
  private UserHomePageService            userHomePageService;

  @Before
  public void setup() {
    when(navigationConfigurationService.isAllowUserHome()).thenReturn(true);
  }

  @Test
  public void saveUserHomePageStoresThePagesTheUiOffers() throws IllegalAccessException {
    assertSaved("/portal/s/12");
    assertSaved("/portal/dw/stream");
    assertSaved("/portal/s/12/tasks?view=board#top");
    assertSaved("/portal/dw/%C3%A9t%C3%A9");
    assertSaved("https://www.example.com/path?a=1&b=2");
    assertSaved("HTTP://www.example.com");
    assertSaved("/" + "a".repeat(UserHomePageService.MAX_URI_LENGTH - 1));
  }

  @Test
  public void saveUserHomePageRefusesWhenUsersCantChooseTheirHome() {
    when(navigationConfigurationService.isAllowUserHome()).thenReturn(false);
    assertThrows(IllegalAccessException.class, () -> userHomePageService.saveUserHomePage(USERNAME, "/portal/s/12"));
    verify(userPortalConfigService, never()).setUserHomePage(anyString(), any());
  }

  @Test
  public void saveUserHomePageRefusesAnInvalidUri() {
    assertInvalidUri(null);
    assertInvalidUri("");
    assertInvalidUri("portal/s/12");
    assertInvalidUri("//evil.example.com/portal");
    assertInvalidUri("/\\evil.example.com");
    assertInvalidUri("javascript:alert(1)");
    assertInvalidUri("data:text/html,x");
    assertInvalidUri("ftp://example.com");
    assertInvalidUri("/portal/`;alert(1);`");
    assertInvalidUri("/portal/${alert(1)}");
    assertInvalidUri("/portal/\"onmouseover=\"x");
    assertInvalidUri("/portal/'x");
    assertInvalidUri("/portal/<script>");
    assertInvalidUri("/portal/a b");
    assertInvalidUri("/portal/a\nb");
    assertInvalidUri("/portal/a\u0000b");
    assertInvalidUri("/portal/a\\b");
    assertInvalidUri("/" + "a".repeat(UserHomePageService.MAX_URI_LENGTH));
    verify(userPortalConfigService, never()).setUserHomePage(anyString(), any());
  }

  @Test
  public void saveUserHomePageRefusesABlankUser() {
    assertThrows(IllegalArgumentException.class, () -> userHomePageService.saveUserHomePage(" ", "/portal/s/12"));
    verify(userPortalConfigService, never()).setUserHomePage(anyString(), any());
  }

  private void assertSaved(String uri) throws IllegalAccessException {
    userHomePageService.saveUserHomePage(USERNAME, uri);
    verify(userPortalConfigService).setUserHomePage(USERNAME, uri);
  }

  private void assertInvalidUri(String uri) {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> userHomePageService.saveUserHomePage(USERNAME, uri));
    assertEquals(UserHomePageService.INVALID_URI_MESSAGE, exception.getMessage());
  }

}
