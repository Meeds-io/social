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
package io.meeds.social.cms.service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.portal.config.UserACL;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.cms.model.ContentLink;
import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkIdentifier;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.model.ContentObject;
import io.meeds.social.cms.model.ContentObjectIdentifier;
import io.meeds.social.cms.storage.ContentLinkStorage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentLinkServiceImpl implements ContentLinkService {

  private final ContentLinkPluginService contentLinkPluginService;

  private final UserACL                  userAcl;

  private final ContentLinkStorage       contentLinkStorage;
  
  private ExecutorService                executorService;

  @PostConstruct
  public void init() {
    executorService = Executors.newCachedThreadPool();
  }

  @PreDestroy
  public void stop() {
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  @Override
  public List<ContentLinkExtension> getExtensions() {
    return contentLinkPluginService.getExtensions();
  }

  @Override
  public List<ContentLink> getLinks(ContentObject contentObject, Locale locale, String username) throws IllegalAccessException {
    if (!canView(contentObject, username)) {
      throw new IllegalAccessException(String.format("User %s can't view object of type %s with id %s",
                                                     username,
                                                     contentObject.getObjectType(),
                                                     contentObject.getObjectId()));
    }
    return getLinkIdentifiers(contentObject).stream()
                                            .filter(linkIdentifier -> canView(linkIdentifier, username))
                                            .map(this::getLink)
                                            .filter(Objects::nonNull)
                                            .toList();
  }

  @Override
  public List<ContentLinkSearchResult> searchLinks(String objectType,
                                                   String keyword,
                                                   String username,
                                                   Locale locale,
                                                   int offset,
                                                   int limit) throws ObjectNotFoundException{
    keyword = StringUtils.trim(keyword);
    if (contentLinkPluginService.getPlugin(objectType) == null) {
      throw new ObjectNotFoundException("Content Link Plugin with type '%s' doesn't exists".formatted(objectType));
    } else if (contentLinkPluginService.isId(objectType, keyword) ) {
      try {
        ContentLink link = getLink(new ContentLinkIdentifier(objectType, keyword, locale), username);
        return Collections.singletonList(new ContentLinkSearchResult(link));
      } catch (IllegalAccessException | ObjectNotFoundException e) {
        return Collections.emptyList();
      }
    } else {
      return contentLinkPluginService.searchLinks(objectType,
                                                  keyword,
                                                  userAcl.getUserIdentity(username),
                                                  locale,
                                                  offset,
                                                  limit);
    }
  }

  @Override
  public void saveLinks(ContentObject contentObject,
                        List<? extends ContentObjectIdentifier> links,
                        String username) throws IllegalAccessException {
    if (!canEdit(contentObject, username)) {
      throw new IllegalAccessException(String.format("User %s can't edit object of type %s with id %s",
                                                     username,
                                                     contentObject.getObjectType(),
                                                     contentObject.getObjectId()));
    }
    if (links.stream().anyMatch(l -> !canView(l, username))) {
      throw new IllegalAccessException(String.format("User %s can't access at least one of linked object: %s",
                                                     username,
                                                     links));
    }
    saveLinks(contentObject, links);
  }

  @Override
  public void saveLinks(ContentObject contentObject, List<? extends ContentObjectIdentifier> links) {
    contentLinkStorage.saveLinks(contentObject, links);
  }
  
  @Override
  public void saveLinksAsync(ContentObject contentObject, List<? extends ContentObjectIdentifier> links) {
    executorService.execute(() -> saveLinksTransactional(contentObject, links));
  }

  @Override
  public void deleteLinks(ContentObjectIdentifier contentObject) {
    contentLinkStorage.deleteLinks(contentObject);
  }

  @Override
  public List<ContentLinkIdentifier> getLinkIdentifiers(ContentObject contentObject) {
    return contentLinkStorage.getLinkIdentifiers(contentObject);
  }

  @Override
  public ContentLink getLink(ContentLinkIdentifier linkIdentifier, String username) throws IllegalAccessException,
                                                                                    ObjectNotFoundException {
    ContentLink link = getLink(linkIdentifier);
    if (link == null) {
      throw new ObjectNotFoundException(String.format("Content %s:%s doesn't exist",
                                                      linkIdentifier.getObjectType(),
                                                      linkIdentifier.getObjectId()));
    } else if (!canView(linkIdentifier, username)) {
      throw new IllegalAccessException(String.format("User %s can't view object of type %s with id %s",
                                                     username,
                                                     linkIdentifier.getObjectType(),
                                                     linkIdentifier.getObjectId()));
    }
    return link;
  }

  @Override
  public ContentLink getLink(ContentLinkIdentifier link) {
    return contentLinkPluginService.getLink(link);
  }

  @Override
  public boolean canView(ContentObjectIdentifier link, String username) {
    return userAcl.hasAccessPermission(link.getObjectType(),
                                       link.getObjectId(),
                                       username);
  }

  @Override
  public boolean canEdit(ContentObjectIdentifier contentObject, String username) {
    return userAcl.hasEditPermission(contentObject.getObjectType(),
                                     contentObject.getObjectId(),
                                     username);
  }

  @ContainerTransactional
  protected void saveLinksTransactional(ContentObject contentObject, List<? extends ContentObjectIdentifier> links) {
    saveLinks(contentObject, links);
  }

}
