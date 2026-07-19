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
package io.meeds.social.space.plugin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.CategoryPlugin;

@Component
public class SpaceCategoryPlugin implements CategoryPlugin {

  public static final String OBJECT_TYPE = SpaceAclPlugin.OBJECT_TYPE;

  @Autowired
  private SpaceService       spaceService;

  @Autowired
  private IdentityManager    identityManager;

  @Override
  public String getType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(String spaceId, String username) {
    return spaceService.canViewSpace(spaceService.getSpaceById(Long.parseLong(spaceId)), username);
  }

  @Override
  public boolean canEdit(String spaceId, String username) {
    return spaceService.canManageSpace(spaceService.getSpaceById(Long.parseLong(spaceId)), username);
  }

  @Override
  public List<Long> getCategoryIds() {
    return spaceService.getSpaceCategoryIds();
  }

  /**
   * Normalizes the identifier carried by the incoming {@link CategoryObject} to
   * the canonical space technical id ({@link Space#getSpaceId()}) before the
   * link is stored, read back or broadcast. Category links are stored generically
   * on {@code (type, id)}, but the space UI/REST relies on the denormalized
   * {@code space.getCategoryIds()} that is only kept in sync by
   * {@code CategoryLinkModifiedListener}, whose SPACE branch resolves the space
   * through {@code spaceService.getSpaceById(object.getId())} and therefore
   * requires the technical id. Callers (e.g. the MCP tooling) may pass the space
   * identity id or its pretty name instead; normalizing here makes the write, the
   * read-back and the denormalization listener all agree whichever id form was
   * supplied.
   *
   * @param categoryObject The incoming {@link CategoryObject} whose id may be a
   *          space technical id, a social {@link Identity} id or a pretty name
   * @return A {@link CategoryObject} carrying the canonical space technical id,
   *         or the object unchanged when the id cannot be resolved to a space
   **/
  @Override
  public CategoryObject getObject(CategoryObject categoryObject) {
    Space space = resolveSpace(categoryObject.getId());
    if (space == null) {
      return categoryObject;
    }
    return new CategoryObject(categoryObject.getType(),
                              String.valueOf(space.getSpaceId()),
                              categoryObject.getParentId(),
                              categoryObject.getSpaceId());
  }

  /**
   * Resolves a {@link Space} from any of the id forms a space can be referenced
   * by across the platform: a space technical id, a social {@link Identity} id
   * (mapped to its space through the space identity provider's remote id, i.e.
   * the space pretty name) or a pretty name. This mirrors the space resolution
   * idiom used by {@code SpacePermanentLinkPlugin#getDirectAccessUrl}.
   *
   * @param id A space technical id, social {@link Identity} id or pretty name
   * @return The resolved {@link Space}, or {@code null} when none matches
   **/
  private Space resolveSpace(String id) {
    if (StringUtils.isBlank(id)) {
      return null;
    }
    if (StringUtils.isNumeric(id)) {
      Space space = spaceService.getSpaceById(id);
      if (space != null) {
        return space;
      }
      // Fall back to a social Identity id pointing at the space
      Identity identity = identityManager.getIdentity(id);
      if (identity != null && SpaceIdentityProvider.NAME.equals(identity.getProviderId())) {
        return spaceService.getSpaceByPrettyName(identity.getRemoteId());
      }
      return null;
    }
    return spaceService.getSpaceByPrettyName(id);
  }

}
