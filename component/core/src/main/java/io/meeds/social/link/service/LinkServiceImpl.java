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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import io.meeds.social.cms.service.CMSService;
import io.meeds.social.link.model.Link;
import io.meeds.social.link.model.LinkSetting;
import io.meeds.social.link.model.LinkWithIconAttachment;
import io.meeds.social.link.plugin.LinkSettingTranslationPlugin;
import io.meeds.social.link.plugin.LinkTranslationPlugin;
import io.meeds.social.link.storage.LinkStorage;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

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

  public static final String  LINK_SETTINGS_HEADER_FIELD     = "header";

  public static final String  LINK_NAME_FIELD                = "name";

  public static final String  LINK_DESCRIPTION_FIELD         = "description";

  private static final Log    LOG                            = ExoLogger.getLogger(LinkServiceImpl.class);

  private ListenerService     listenerService;

  private FileService         fileService;

  private UploadService       uploadService;

  private TranslationService  translationService;

  private LocaleConfigService localeConfigService;

  private CMSService          cmsService;

  private LinkStorage         linkStorage;

  public LinkServiceImpl(ListenerService listenerService, // NOSONAR
                         FileService fileService,
                         UploadService uploadService,
                         TranslationService translationService,
                         LocaleConfigService localeConfigService,
                         CMSService cmsService,
                         LinkStorage linkStorage) {
    this.listenerService = listenerService;
    this.fileService = fileService;
    this.uploadService = uploadService;
    this.translationService = translationService;
    this.localeConfigService = localeConfigService;
    this.cmsService = cmsService;
    this.linkStorage = linkStorage;
  }

  @Override
  public LinkSetting getLinkSetting(String linkSettingName, String language, Identity identity) throws IllegalAccessException {
    LinkSetting linkSetting = getLinkSetting(linkSettingName, language, true);
    if (linkSetting == null) {
      return null;
    }
    if (!hasAccessPermission(linkSettingName, identity)) {
      throw new IllegalAccessException(String.format(PAGE_NOT_ACCESSIBLE_FOR_USER,
                                                     linkSetting.getPageReference(),
                                                     identity == null ? IdentityConstants.ANONIM : identity.getUserId()));
    } else {
      return linkSetting;
    }
  }

  @Override
  public LinkSetting getLinkSetting(String linkSettingName) {
    return getLinkSetting(linkSettingName, null, false);
  }

  @Override
  public LinkSetting getLinkSetting(String linkSettingName, String language, boolean includeTranslations) {
    LinkSetting linkSetting = linkStorage.getLinkSetting(linkSettingName);
    if (linkSetting != null && includeTranslations) {
      Map<String, String> header = getTranslations(LinkSettingTranslationPlugin.LINK_SETTINGS_OBJECT_TYPE,
                                                   linkSetting.getId(),
                                                   LINK_SETTINGS_HEADER_FIELD,
                                                   language);
      linkSetting.setHeader(header);
    }
    return linkSetting;
  }

  @Override
  public LinkSetting getLinkSetting(long linkSettingId) {
    return linkStorage.getLinkSetting(linkSettingId);
  }

  @Override
  public boolean hasLinkSetting(String linkSettingName) {
    return linkStorage.hasLinkSetting(linkSettingName);
  }

  @Override
  public LinkSetting getLinkSettingByLinkId(long linkId) {
    return linkStorage.getLinkSettingByLinkId(linkId);
  }

  @Override
  public LinkSetting initLinkSetting(String name, String pageReference, long spaceId) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException(LINK_SETTING_NAME_IS_MANDATORY);
    }
    if (StringUtils.isBlank(pageReference)) {
      throw new IllegalArgumentException(LINK_SETTING_PAGE_IS_MANDATORY);
    }
    LinkSetting linkSetting = linkStorage.initLinkSetting(name, pageReference, spaceId);
    broadcast(LINKS_CREATED_EVENT, null, linkSetting);
    return linkSetting;
  }

  @Override
  public LinkSetting saveLinkSetting(LinkSetting linkSetting, List<Link> links, Identity identity) throws IllegalAccessException,
                                                                                                   ObjectNotFoundException {
    String linkSettingName = linkSetting.getName();
    LinkSetting existingLinkSetting = linkStorage.getLinkSetting(linkSettingName);
    if (existingLinkSetting == null) {
      throw new ObjectNotFoundException("Link setting not found");
    }
    if (!hasEditPermission(linkSettingName, identity)) {
      throw new IllegalAccessException(String.format(PAGE_NOT_EDITABLE_BY_USER,
                                                     existingLinkSetting.getPageReference(),
                                                     identity == null ? IdentityConstants.ANONIM : identity.getUserId()));
    }

    existingLinkSetting.setType(linkSetting.getType());
    existingLinkSetting.setLargeIcon(linkSetting.isLargeIcon());
    existingLinkSetting.setSeeMore(linkSetting.getSeeMore());
    existingLinkSetting.setShowName(linkSetting.isShowName());
    existingLinkSetting.setShowDescription(linkSetting.isShowDescription());
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
    saveLinkTranslationLabels(LinkSettingTranslationPlugin.LINK_SETTINGS_OBJECT_TYPE,
                              existingLinkSetting.getId(),
                              linkSetting.getHeader(),
                              LINK_SETTINGS_HEADER_FIELD);

    existingLinkSetting.setHeader(linkSetting.getHeader());
    broadcast(LINKS_UPDATED_EVENT, identity.getUserId(), existingLinkSetting);
    return existingLinkSetting;
  }

  @Override
  public List<Link> getLinks(String linkSettingName) {
    return getLinks(linkSettingName, null, false);
  }

  @Override
  public List<Link> getLinks(String linkSettingName, String language, boolean includeTranslations) {
    List<Link> links = linkStorage.getLinks(linkSettingName);
    if (CollectionUtils.isNotEmpty(links) && includeTranslations) {
      for (Link link : links) {
        Map<String, String> name = getTranslations(LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                                   link.getId(),
                                                   LINK_NAME_FIELD,
                                                   language);
        link.setName(name);

        Map<String, String> description = getTranslations(LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                                          link.getId(),
                                                          LINK_DESCRIPTION_FIELD,
                                                          language);
        link.setDescription(description);
      }
    }
    return links;
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
    if (linkSetting == null || StringUtils.isBlank(linkSetting.getPageReference())) {
      return false;
    }
    return cmsService.hasAccessPermission(identity, linkSetting.getPageReference(), linkSetting.getSpaceId());
  }

  @Override
  public boolean hasEditPermission(String linkSettingName, Identity identity) {
    LinkSetting linkSetting = getLinkSetting(linkSettingName);
    if (linkSetting == null || StringUtils.isBlank(linkSetting.getPageReference())) {
      return false;
    }
    return cmsService.hasEditPermission(identity, linkSetting.getPageReference(), linkSetting.getSpaceId());
  }

  private void processNewLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    links.stream().filter(l -> l.getId() == 0).forEach(link -> {
      processLinkIcon(link, existingLinks);
      Link createdLink = linkStorage.createLink(linkSettingName, link);
      try {
        saveLinkTranslationLabels(LinkTranslationPlugin.LINKS_OBJECT_TYPE, createdLink.getId(), link.getName(), LINK_NAME_FIELD);
        saveLinkTranslationLabels(LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                  createdLink.getId(),
                                  link.getDescription(),
                                  LINK_DESCRIPTION_FIELD);
      } catch (ObjectNotFoundException e) {
        throw new IllegalStateException("Error setting translation of newly created link " + createdLink.getId(), e);
      }
    });
  }

  private void processUpdatedLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    links.stream().filter(l -> existingLinks.stream().anyMatch(l2 -> l.getId() == l2.getId())).forEach(link -> {
      processLinkIcon(link, existingLinks);
      Link updatedLink = linkStorage.updateLink(linkSettingName, link);
      try {
        saveLinkTranslationLabels(LinkTranslationPlugin.LINKS_OBJECT_TYPE, updatedLink.getId(), link.getName(), LINK_NAME_FIELD);
        saveLinkTranslationLabels(LinkTranslationPlugin.LINKS_OBJECT_TYPE,
                                  updatedLink.getId(),
                                  link.getDescription(),
                                  LINK_DESCRIPTION_FIELD);
      } catch (ObjectNotFoundException e) {
        throw new IllegalStateException("Error setting translation of updated link " + updatedLink.getId(), e);
      }
    });
  }

  private void processDeletedLinks(String linkSettingName, List<Link> existingLinks, List<Link> links) {
    existingLinks.stream().filter(l -> links.stream().noneMatch(l2 -> l.getId() == l2.getId())).forEach(link -> {
      deleteLinkIconFile(link.getIconFileId());
      try {
        translationService.deleteTranslationLabels(LinkTranslationPlugin.LINKS_OBJECT_TYPE, link.getId());
      } catch (ObjectNotFoundException e) {
        throw new IllegalStateException("Error setting translation of deleted link " + link.getId(), e);
      } finally {
        linkStorage.deleteLink(linkSettingName, link.getId());
      }
    });
  }

  private void processLinkIcon(Link link, List<Link> existingLinks) {
    long oldFileId =
                   existingLinks.stream().filter(l -> l.getId() == link.getId()).map(Link::getIconFileId).findFirst().orElse(0l);
    if (link instanceof LinkWithIconAttachment linkWithIconAttachment
        && StringUtils.isNotBlank(linkWithIconAttachment.getUploadId())) {
      String uploadId = linkWithIconAttachment.getUploadId();
      UploadResource uploadResource = uploadService.getUploadResource(uploadId);
      try (InputStream inputStream = new FileInputStream(uploadResource.getStoreLocation())) {
        FileItem fileItem = new FileItem(null,
                                         uploadResource.getFileName(),
                                         uploadResource.getMimeType(),
                                         FILE_API_NAMESPACE,
                                         inputStream.available(),
                                         new Date(),
                                         IdentityConstants.SYSTEM,
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

  private void saveLinkTranslationLabels(String type,
                                         long id,
                                         Map<String, String> values,
                                         String fieldName) throws ObjectNotFoundException {
    if (MapUtils.isEmpty(values)) {
      translationService.saveTranslationLabels(type,
                                               id,
                                               fieldName,
                                               Collections.singletonMap(localeConfigService.getDefaultLocaleConfig().getLocale(),
                                                                        ""));
    } else {
      translationService.saveTranslationLabels(type,
                                               id,
                                               fieldName,
                                               values.entrySet()
                                                     .stream()
                                                     .collect(Collectors.toMap(e -> Locale.forLanguageTag(e.getKey()),
                                                                               Entry::getValue)));
    }
  }

  private Map<String, String> getTranslations(String objectType, long objectId, String fieldName, String language) {
    if (StringUtils.isBlank(language)) {
      TranslationField translationField;
      try {
        translationField = translationService.getTranslationField(objectType, objectId, fieldName);
      } catch (ObjectNotFoundException e) {
        translationField = null;
      }
      return toTranslations(translationField, "");
    } else {
      String label = translationService.getTranslationLabel(objectType, objectId, fieldName, Locale.forLanguageTag(language));
      if (StringUtils.isBlank(label)) {
        String defaultLanguage = localeConfigService.getDefaultLocaleConfig().getLocale().toLanguageTag();
        if (StringUtils.equals(defaultLanguage, language)) {
          if (!Locale.ENGLISH.toLanguageTag().equals(language)) {
            label = translationService.getTranslationLabel(objectType, objectId, fieldName, Locale.ENGLISH);
          }
          return Collections.singletonMap(Locale.ENGLISH.toLanguageTag(), !StringUtils.isBlank(label) ? label : "");
        } else {
          return Collections.singletonMap(language,
                                          getTranslations(objectType, objectId, fieldName, defaultLanguage).get(defaultLanguage));
        }
      } else {
        return Collections.singletonMap(language, label);
      }
    }
  }

  private Map<String, String> toTranslations(TranslationField translationField, String defaultValue) {
    boolean hasTranslation = translationField == null || MapUtils.isEmpty(translationField.getLabels());
    return hasTranslation ? Collections.singletonMap(localeConfigService.getDefaultLocaleConfig().getLocale().toLanguageTag(),
                                                     defaultValue) :
                          translationField.getLabels()
                                          .entrySet()
                                          .stream()
                                          .collect(Collectors.toMap(e -> e.getKey().toLanguageTag(), Entry::getValue));
  }

  private void broadcast(String eventName, Object source, Object data) {
    try {
      listenerService.broadcast(eventName, source, data);
    } catch (Exception e) {
      LOG.warn("Error while broadcasting event '{}' for object {}", eventName, data, e);
    }
  }

}
