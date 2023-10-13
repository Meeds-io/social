/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.social.link.storage;

import static io.meeds.social.link.storage.util.EntityMapper.*;

import java.time.Instant;
import java.util.List;

import io.meeds.social.link.constant.LinkDisplayType;
import io.meeds.social.link.dao.LinkDAO;
import io.meeds.social.link.dao.LinkSettingDAO;
import io.meeds.social.link.entity.LinkEntity;
import io.meeds.social.link.entity.LinkSettingEntity;
import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.storage.util.EntityMapper;

public class LinkStorage {

  private LinkSettingDAO linkSettingDAO;

  private LinkDAO        linkDAO;

  public LinkStorage(LinkSettingDAO linkSettingDAO, LinkDAO linkDAO) {
    this.linkSettingDAO = linkSettingDAO;
    this.linkDAO = linkDAO;
  }

  public LinkSetting getLinkSetting(String name) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.findByName(name);
    return toModel(linkSettingEntity);
  }

  public LinkSetting getLinkSetting(Long linkSettingId) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.find(linkSettingId);
    return toModel(linkSettingEntity);
  }

  public boolean hasLinkSetting(String linkSettingName) {
    // Reuse cached DTO instead of requesting Database using DAO
    return getLinkSetting(linkSettingName) != null;
  }

  public LinkSetting initLinkSetting(String name, String pageReference, long spaceId) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.findByName(name);
    if (linkSettingEntity == null) {
      linkSettingEntity = new LinkSettingEntity();
      linkSettingEntity.setName(name);
      linkSettingEntity.setPageReference(pageReference);
      linkSettingEntity.setSpaceId(spaceId);
      linkSettingEntity.setType(LinkDisplayType.ROW);
      linkSettingEntity.setLastModified(Instant.now());
      return toModel(linkSettingDAO.create(linkSettingEntity));
    } else {
      linkSettingEntity.setPageReference(pageReference);
      return toModel(linkSettingDAO.update(linkSettingEntity));
    }
  }

  public LinkSetting saveLinkSetting(LinkSetting linkSetting) {
    LinkSettingEntity existingLinkSettingEntity = linkSettingDAO.findByName(linkSetting.getName());
    LinkSettingEntity linkSettingEntity = fromModel(linkSetting, existingLinkSettingEntity);
    return toModel(linkSettingDAO.update(linkSettingEntity));
  }

  public List<Link> getLinks(String linkSettingName) {
    List<LinkEntity> linkEntities = linkDAO.getLinks(linkSettingName);
    return linkEntities.stream().map(EntityMapper::toModel).toList();
  }

  public Link createLink(String linkSettingName, Link link) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.findByName(linkSettingName);
    LinkEntity linkEntity = fromModel(link, linkSettingEntity);
    linkEntity.setId(null);
    linkEntity = linkDAO.create(linkEntity);
    updateLastModifiedTime(linkSettingEntity);
    return toModel(linkEntity);
  }

  public Link updateLink(String linkSettingName, Link link) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.findByName(linkSettingName);
    LinkEntity linkEntity = fromModel(link, linkSettingEntity);
    linkEntity = linkDAO.update(linkEntity);
    updateLastModifiedTime(linkSettingEntity);
    return toModel(linkEntity);
  }

  public void deleteLink(String linkSettingName, long id) {
    LinkSettingEntity linkSettingEntity = linkSettingDAO.findByName(linkSettingName);
    LinkEntity linkEntity = linkDAO.find(id);
    linkDAO.delete(linkEntity);
    updateLastModifiedTime(linkSettingEntity);
  }

  private void updateLastModifiedTime(LinkSettingEntity linkSettingEntity) {
    linkSettingEntity.setLastModified(Instant.now());
    linkSettingDAO.update(linkSettingEntity);
  }

  public LinkSetting getLinkSettingByLinkId(long linkId) {
    long linkSettingId = linkDAO.getLinkSettingByLinkId(linkId);
    return getLinkSetting(linkSettingId);
  }

}
