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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import io.meeds.social.cms.plugin.PageUrlResolver;

@Service
public class PageUrlResolverServiceImpl implements PageUrlResolverService {

  private static final Log      LOGGER    = ExoLogger.getExoLogger(PageUrlResolverServiceImpl.class);

  private List<PageUrlResolver> resolvers = new CopyOnWriteArrayList<>();

  @Override
  public void addPlugin(PageUrlResolver plugin) {
    resolvers.add(plugin);
  }

  @Override
  public String resolvePath(PageKey pageKey) {
    for (PageUrlResolver resolver : resolvers) {
      try {
        String path = resolver.resolvePath(pageKey);
        if (StringUtils.isNotBlank(path)) {
          return path;
        }
      } catch (Exception e) {
        LOGGER.debug("Page url resolver {} failed for page {}", resolver.getClass(), pageKey, e);
      }
    }
    return null;
  }

}
