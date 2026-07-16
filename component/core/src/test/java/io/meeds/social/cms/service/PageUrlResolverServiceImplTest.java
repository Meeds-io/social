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
package io.meeds.social.cms.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.mop.page.PageKey;

import io.meeds.social.cms.plugin.PageUrlResolver;

@RunWith(MockitoJUnitRunner.class)
public class PageUrlResolverServiceImplTest {

  private static final PageKey       PAGE_KEY = PageKey.parse("portal::site::page");

  private PageUrlResolverServiceImpl urlResolverService;

  @Before
  public void setup() {
    urlResolverService = new PageUrlResolverServiceImpl();
  }

  @Test
  public void shouldReturnNullWhenNoResolverRegistered() {
    assertNull(urlResolverService.resolvePath(PAGE_KEY));
  }

  @Test
  public void shouldReturnPathFromFirstResolverThatFindsOne() {
    PageUrlResolver blankResolver = mock(PageUrlResolver.class);
    when(blankResolver.resolvePath(PAGE_KEY)).thenReturn(null);
    PageUrlResolver resolver = mock(PageUrlResolver.class);
    when(resolver.resolvePath(PAGE_KEY)).thenReturn("/portal/site/page");

    urlResolverService.addPlugin(blankResolver);
    urlResolverService.addPlugin(resolver);

    assertEquals("/portal/site/page", urlResolverService.resolvePath(PAGE_KEY));
  }

  @Test
  public void shouldIgnoreResolverThatThrows() {
    PageUrlResolver failingResolver = mock(PageUrlResolver.class);
    when(failingResolver.resolvePath(PAGE_KEY)).thenThrow(new RuntimeException("boom"));
    PageUrlResolver resolver = mock(PageUrlResolver.class);
    when(resolver.resolvePath(PAGE_KEY)).thenReturn("/portal/site/page");

    urlResolverService.addPlugin(failingResolver);
    urlResolverService.addPlugin(resolver);

    assertEquals("/portal/site/page", urlResolverService.resolvePath(PAGE_KEY));
  }

}
