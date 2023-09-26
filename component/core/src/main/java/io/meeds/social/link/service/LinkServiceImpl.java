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
package io.meeds.social.link.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.model.LinkWithIconAttachment;
import io.meeds.social.link.storage.LinkStorage;

public class LinkServiceImpl implements LinkService {

  public static final String  LINKS_CREATED_EVENT            = "links.created";

  public static final String  LINKS_UPDATED_EVENT            = "links.updated";

  public static final String  NO_ASSOCIATED_PAGE_TO_LINK     = "Link setting with name '%s' doesn't have an associated page";

  public static final String  PAGE_NOT_ACCESSIBLE_FOR_USER   = "Page %s isn't accessible for user %s";

  public static final String  PAGE_NOT_EDITABLE_BY_USER      = "Page %s isn't modifiable by user %s";

  public static final String  LINK_SETTING_PAGE_IS_MANDATORY = "link setting page is mandatory";

  public static final String  LINK_SETTING_NAME_IS_MANDATORY = "link setting name is mandatory";

  public static final String  ERROR_READING_ICON             = "Error while reading link icon fir setting '%s' with id '%s'";

  private static final String FILE_API_NAMESPACE             = "links";

  private static final Log    LOG                            = ExoLogger.getLogger(LinkServiceImpl.class);

  private ListenerService     listenerService;

  private UserACL             userACL;

  private LayoutService       layoutService;

  private FileService         fileService;

  private UploadService       uploadService;

  private LinkStorage         linkStorage;

  public LinkServiceImpl(ListenerService listenerService,
                         UserACL userACL,
                         LayoutService layoutService,
                         FileService fileService,
                         UploadService uploadService,
                         LinkStorage linkStorage) {
    this.listenerService = listenerService;
    this.userACL = userACL;
    this.layoutService = layoutService;
    this.fileService = fileService;
    this.uploadService = uploadService;
    this.linkStorage = linkStorage;
  }

  @Override
  public LinkSetting getLinkSetting(String linkSettingName, Identity identity) throws IllegalAccessException {
    LinkSetting linkSetting = getLinkSetting(linkSettingName);
    if (linkSetting == null) {
      return null;
    }
    String pageId = linkSetting.getPageId();
    if (StringUtils.isBlank(pageId)) {
      throw new IllegalAccessException(String.format(NO_ASSOCIATED_PAGE_TO_LINK, linkSetting.getName()));
    }
    if (hasAccessPermissionOnPage(identity, pageId)) {
      return linkSetting;
    } else {
      throw new IllegalAccessException(String.format(PAGE_NOT_ACCESSIBLE_FOR_USER,
                                                     pageId,
                                                     identity == null ? IdentityConstants.ANONIM : identity.getUserId()));
    }
  }

  @Override
  public LinkSetting getLinkSetting(String linkSettingName) {
    return linkStorage.getLinkSetting(linkSettingName);
  }

  @Override
  public LinkSetting getLinkSetting(long linkSettingId) {
    return linkStorage.getLinkSetting(linkSettingId);
  }

  @Override
  public LinkSetting getLinkSettingByLinkId(long linkId) {
    return linkStorage.getLinkSettingByLinkId(linkId);
  }

  @Override
  public LinkSetting initLinkSetting(String name, String pageId) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException(LINK_SETTING_NAME_IS_MANDATORY);
    }
    if (StringUtils.isBlank(pageId)) {
      throw new IllegalArgumentException(LINK_SETTING_PAGE_IS_MANDATORY);
    }
    LinkSetting linkSetting = linkStorage.initLinkSetting(name, pageId);
    broadcast(LINKS_CREATED_EVENT, null, linkSetting);
    return linkSetting;
  }

  @Override
  public LinkSetting saveLinkSetting(LinkSetting linkSetting, List<Link> links, Identity identity) throws IllegalAccessException {
    String linkSettingName = linkSetting.getName();
    LinkSetting existingLinkSetting = linkStorage.getLinkSetting(linkSettingName);
    if (existingLinkSetting == null) {
      throw new IllegalAccessException("Link setting not found");
    }
    String pageId = existingLinkSetting.getPageId();
    if (StringUtils.isBlank(pageId)) {
      throw new IllegalAccessException(String.format(NO_ASSOCIATED_PAGE_TO_LINK, linkSetting.getName()));
    }
    if (!hasEditPermissionOnPage(identity, pageId)) {
      throw new IllegalAccessException(String.format(PAGE_NOT_EDITABLE_BY_USER,
                                                     pageId,
                                                     identity == null ? IdentityConstants.ANONIM : identity.getUserId()));
    }

    existingLinkSetting.setType(linkSetting.getType());
    existingLinkSetting.setHeader(linkSetting.getHeader());
    existingLinkSetting.setLargeIcon(linkSetting.isLargeIcon());
    existingLinkSetting.setSeeMore(linkSetting.getSeeMore());
    existingLinkSetting.setShowName(linkSetting.isShowName());
    existingLinkSetting = linkStorage.saveLinkSetting(existingLinkSetting);

    List<Link> existingLinks = getLinks(linkSettingName);
    if (CollectionUtils.isEmpty(existingLinks)) {
      existingLinks = Collections.emptyList();
    }
    if (CollectionUtils.isEmpty(links)) {
      links = Collections.emptyList();
    }
    processNewLinks(linkSettingName, existingLinks, links);
    processUpdatedLinks(linkSettingName, existingLinks, links);
    processDeletedLinks(linkSettingName, existingLinks, links);

    LinkSetting savedLinkSetting = getLinkSetting(linkSettingName);
    broadcast(LINKS_UPDATED_EVENT, identity.getUserId(), existingLinkSetting);
    return savedLinkSetting;
  }

  @Override
  public List<Link> getLinks(String linkSettingName) {
    return linkStorage.getLinks(linkSettingName);
  }

  @Override
  public InputStream getLinkIconStream(String linkSettingName, long linkId) throws IOException {
    List<Link> links = getLinks(linkSettingName);
    Link link = CollectionUtils.isEmpty(links) ? null : links.stream().filter(l -> l.getId() == linkId).findFirst().orElse(null);
    if (link == null || link.getIconFileId() == 0) {
      return null;
    }
    try {
      FileItem file = fileService.getFile(link.getIconFileId());
      return file == null ? null : file.getAsStream();
    } catch (FileStorageException e) {
      throw new IOException(String.format(ERROR_READING_ICON, linkSettingName, linkId), e);
    }
  }

  @Override
  public boolean hasAccessPermission(String linkSettingName, Identity identity) {
    LinkSetting linkSetting = getLinkSetting(linkSettingName);
    if (linkSetting == null || StringUtils.isBlank(linkSetting.getPageId())) {
      return false;
    }
    String pageId = linkSetting.getPageId();
    return hasAccessPermissionOnPage(identity, pageId);
  }

  @Override
  public boolean hasEditPermission(String linkSettingName, Identity identity) {
    LinkSetting linkSetting = getLinkSetting(linkSettingName);
    if (linkSetting == null || StringUtils.isBlank(linkSetting.getPageId())) {
      return false;
    }
    String pageId = linkSetting.getPageId();
    return hasEditPermissionOnPage(identity, pageId);
  }

  private boolean hasAccessPermissionOnPage(Identity identity, String pageId) {
    Page page = layoutService.getPage(pageId);
    if (page == null) {
      return false;
    }
    String[] accessPermissions = page.getAccessPermissions();
    return accessPermissions == null || accessPermissions.length == 0
        || StringUtils.equals(UserACL.EVERYONE, accessPermissions[0])
        || (identity != null && Arrays.stream(accessPermissions).anyMatch(perm -> userACL.hasPermission(identity, perm)));
  }

  private boolean hasEditPermissionOnPage(Identity identity, String pageId) {
    Page page = layoutService.getPage(PageKey.parse(pageId));
    if (page == null) {
      return false;
    }
    String editPermission = page.getEditPermission();
    return editPermission != null && identity != null && userACL.hasPermission(identity, editPermission);
  }

  private void processNewLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    links.stream().filter(l -> l.getId() == 0).forEach(l -> {
      processLinkIcon(l, existingLinks);
      linkStorage.createLink(linkSettingName, l);
    });
  }

  private void processUpdatedLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    links.stream().filter(l -> existingLinks.stream().anyMatch(l2 -> l.getId() == l2.getId())).forEach(l -> {
      processLinkIcon(l, existingLinks);
      linkStorage.updateLink(linkSettingName, l);
    });
  }

  private void processDeletedLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    existingLinks.stream().filter(l -> links.stream().noneMatch(l2 -> l.getId() == l2.getId())).forEach(l -> {
      deleteLinkIconFile(l.getIconFileId());
      linkStorage.deleteLink(linkSettingName, l.getId());
    });
  }

  private void processLinkIcon(Link link, List<Link> existingLinks) {
    if (link.getId() == 0 && !(link instanceof LinkWithIconAttachment)) {
      return;
    }
    long oldFileId =
                   existingLinks.stream().filter(l -> l.getId() == link.getId()).map(Link::getIconFileId).findFirst().orElse(0l);
    if (link instanceof LinkWithIconAttachment iconAttachment) {
      String uploadId = iconAttachment.getUploadId();
      UploadResource uploadResource = uploadService.getUploadResource(uploadId);
      try (InputStream inputStream = new FileInputStream(uploadResource.getStoreLocation())) {
        FileItem fileItem = new FileItem(null,
                                         uploadResource.getFileName(),
                                         uploadResource.getMimeType(),
                                         FILE_API_NAMESPACE,
                                         inputStream.available(),
                                         new Date(),
                                         userACL.getSuperUser(),
                                         false,
                                         inputStream);
        fileItem = fileService.writeFile(fileItem);
        link.setIconFileId(fileItem.getFileInfo().getId());
        if (oldFileId > 0) {
          deleteLinkIconFile(oldFileId);
        }
      } catch (Exception e) {
        LOG.warn("Error while writing link {} icon file", link.getUrl(), e);
      }
    } else if (oldFileId > 0 && link.getIconFileId() == 0) {
      deleteLinkIconFile(oldFileId);
    } else {
      link.setIconFileId(oldFileId);
    }
  }

  private void deleteLinkIconFile(long oldFileId) {
    fileService.deleteFile(oldFileId);
  }

  private void broadcast(String eventName, Object source, Object data) {
    try {
      listenerService.broadcast(eventName, source, data);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event '{}' for object {}", eventName, data, e);
    }
  }

}
