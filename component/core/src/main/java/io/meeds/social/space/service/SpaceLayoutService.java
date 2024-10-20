/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.space.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import lombok.SneakyThrows;

@Service
public class SpaceLayoutService {

  public static final String      DEFAULT_PUBLIC_SITE_TEMPLATE = "spacePublic";

  public static final String      DEFAULT_SITE_TEMPLATE        = "space";

  public static final String      DEFAULT_SITE_TEMPLATE_PATH   = "war:/conf/portal";

  private UserPortalConfigService portalConfigService;

  private LayoutService           layoutService;

  private SpaceService            spaceService;

  private SpaceTemplateService    spaceTemplateService;

  public SpaceLayoutService(SpaceService spaceService,
                            SpaceTemplateService spaceTemplateService,
                            UserPortalConfigService portalConfigService,
                            LayoutService layoutService) {
    this.portalConfigService = portalConfigService;
    this.layoutService = layoutService;
    this.spaceService = spaceService;
    this.spaceTemplateService = spaceTemplateService;
  }

  /**
   * Create a {@link Space} site switch designated templateId characteristics
   * 
   * @param space
   */
  public void createSpaceSite(Space space) {
    SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplate(space.getTemplateId());
    portalConfigService.createUserPortalConfig(PortalConfig.GROUP_TYPE,
                                               space.getGroupId(),
                                               StringUtils.firstNonBlank(spaceTemplate.getLayout(), DEFAULT_SITE_TEMPLATE),
                                               DEFAULT_SITE_TEMPLATE_PATH);
    PortalConfig portalConfig = layoutService.getPortalConfig(PortalConfig.GROUP_TYPE,
                                                              space.getGroupId());
    portalConfig.setEditPermission(StringUtils.join(spaceTemplate.getSpaceLayoutPermissions(), ","));
    portalConfig.setLabel(space.getDisplayName());
    layoutService.save(portalConfig);
  }

  /**
   * Saves the space public site characteristics
   * 
   * @param spaceId
   * @param publicSiteVisibility Visibility of public site, possible values:
   *          manager, member, internal, authenticated or everyone.
   * @param username user identifier who's making the operation
   * @throws IllegalAccessException when
   * @throws ObjectNotFoundException
   */
  public void saveSpacePublicSite(String spaceId,
                                  String publicSiteVisibility,
                                  String username) throws ObjectNotFoundException, IllegalAccessException {
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new ObjectNotFoundException("Space not found");
    } else if (!spaceService.canManageSpace(space, username)) {
      throw new IllegalAccessException("User isn't manager of the space");
    }
    space.setEditor(username);

    saveSpacePublicSite(space, publicSiteVisibility, username);
  }

  /**
   * Saves the space public site characteristics
   * 
   * @param space {@link Space}
   * @param publicSiteVisibility Visibility of public site, possible values:
   *          manager, member, internal, authenticated or everyone.
   */
  public void saveSpacePublicSite(Space space, String publicSiteVisibility) {
    saveSpacePublicSite(space, publicSiteVisibility, null);
  }

  /**
   * Removes Space Public Site
   * 
   * @param space {@link Space}
   */
  public void removeSpacePublicSite(Space space) {
    if (space.getPublicSiteId() == 0) {
      return;
    }
    PortalConfig portalConfig = layoutService.getPortalConfig(space.getPublicSiteId());
    if (portalConfig == null) {
      return;
    }
    layoutService.remove(portalConfig);
  }

  /**
   * @param space {@link Space}
   * @return Public site name if exists, else null
   */
  public String getSpacePublicSiteName(Space space) {
    if (space == null || space.getPublicSiteId() == 0) {
      return null;
    } else {
      PortalConfig portalConfig = layoutService.getPortalConfig(space.getPublicSiteId());
      return portalConfig == null ? null : portalConfig.getName();
    }
  }

  @SneakyThrows
  private long createSpacePublicSite(Space space,
                                     String name,
                                     String label,
                                     String[] accessPermissions) {
    String siteName = StringUtils.firstNonBlank(name, space.getPrettyName());
    portalConfigService.createUserPortalConfig(PortalConfig.PORTAL_TYPE,
                                               siteName,
                                               DEFAULT_PUBLIC_SITE_TEMPLATE,
                                               DEFAULT_SITE_TEMPLATE_PATH);
    PortalConfig portalConfig = layoutService.getPortalConfig(siteName);
    if (accessPermissions != null) {
      portalConfig.setAccessPermissions(accessPermissions);
      portalConfig.setEditPermission(SpaceUtils.MANAGER + ":" + space.getGroupId());
    }
    portalConfig.setLabel(StringUtils.firstNonBlank(label, space.getDisplayName()));
    portalConfig.setDefaultSite(false);
    portalConfig.setProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID, space.getId());
    portalConfig.setProperty(SpaceUtils.IS_PUBLIC_SITE_SPACE, "true");
    layoutService.save(portalConfig);
    return Long.parseLong(portalConfig.getStorageId().split("_")[1]);
  }

  private void saveSpacePublicSite(Space space, String publicSiteVisibility, String authenticatedUser) {
    boolean visibilityChanged = StringUtils.isNotBlank(publicSiteVisibility)
                                && !StringUtils.equals(space.getPublicSiteVisibility(), publicSiteVisibility);

    if (space.getPublicSiteId() == 0
        || layoutService.getPortalConfig(space.getPublicSiteId()) == null) {
      long siteId = createSpacePublicSite(space,
                                          space.getPrettyName(),
                                          space.getDisplayName(),
                                          getPublicSitePermissions(publicSiteVisibility,
                                                                   space.getGroupId()));
      space.setPublicSiteId(siteId);
      space.setPublicSiteVisibility(publicSiteVisibility);
      space.setEditor(authenticatedUser);
      spaceService.updateSpace(space);
    } else {
      PortalConfig portalConfig = layoutService.getPortalConfig(space.getPublicSiteId());
      if (visibilityChanged) {
        String[] publicSitePermissions = getPublicSitePermissions(publicSiteVisibility, space.getGroupId());
        portalConfig.setAccessPermissions(publicSitePermissions);
        portalConfig.setDefaultSite(false);
        layoutService.save(portalConfig);

        space.setPublicSiteVisibility(publicSiteVisibility);
        space.setEditor(authenticatedUser);
        spaceService.updateSpace(space);
      }
    }
  }

  private String[] getPublicSitePermissions(String publicSiteVisibility, String spaceGroupId) {
    if (StringUtils.isBlank(publicSiteVisibility)) {
      return null; // NOSONAR
    }
    switch (publicSiteVisibility) {
    case SpaceUtils.MANAGER: {
      return new String[] { SpaceUtils.MANAGER + ":" + spaceGroupId };
    }
    case SpaceUtils.MEMBER: {
      return new String[] { SpaceUtils.MEMBER + ":" + spaceGroupId };
    }
    case SpaceUtils.INTERNAL: {
      return new String[] { SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_USERS_GROUP };
    }
    case SpaceUtils.AUTHENTICATED: {
      return new String[] { SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_USERS_GROUP,
                            SpaceUtils.MEMBER + ":" + SpaceUtils.PLATFORM_EXTERNALS_GROUP };
    }
    case SpaceUtils.EVERYONE: {
      return new String[] { UserACL.EVERYONE };
    }
    default:
      throw new IllegalArgumentException("Unexpected value: " + publicSiteVisibility);
    }
  }

}
