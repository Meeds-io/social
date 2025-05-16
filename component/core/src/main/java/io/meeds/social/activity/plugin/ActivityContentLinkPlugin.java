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
package io.meeds.social.activity.plugin;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.filter.ActivitySearchFilter;
import org.exoplatform.social.core.activity.model.ActivitySearchResult;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.jpa.search.ActivitySearchConnector;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.cms.model.ContentLinkExtension;
import io.meeds.social.cms.model.ContentLinkSearchResult;
import io.meeds.social.cms.plugin.ContentLinkPlugin;
import io.meeds.social.cms.service.ContentLinkPluginService;
import io.meeds.social.html.utils.HtmlUtils;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
public class ActivityContentLinkPlugin implements ContentLinkPlugin {

  public static final String                OBJECT_TYPE      = ActivityPermanentLinkPlugin.OBJECT_TYPE;

  private static final String               TITLE_KEY        = "contentLink.activity";

  private static final String               ICON             = "fa fa-stream";

  private static final String               COMMAND          = "post";

  private static final int                  MAX_TITLE_LENGTH = 50;

  private static final ContentLinkExtension EXTENSION        = new ContentLinkExtension(OBJECT_TYPE,
                                                                                        TITLE_KEY,
                                                                                        ICON,
                                                                                        COMMAND);

  @Autowired
  private ContentLinkPluginService          contentLinkPluginService;

  @Autowired
  private UserACL                           userAcl;

  @Autowired
  private IdentityManager                   identityManager;

  @Autowired
  private ActivityManager                   activityManager;

  @Autowired
  private ActivitySearchConnector           activitySearchConnector;

  @PostConstruct
  public void init() {
    contentLinkPluginService.addPlugin(this);
  }

  @Override
  public ContentLinkExtension getExtension() {
    return EXTENSION;
  }

  @Override
  @SneakyThrows
  public List<ContentLinkSearchResult> search(String keyword, Identity identity, Locale locale, int offset, int limit) {
    org.exoplatform.social.core.identity.model.Identity userIdentity = userAcl.isAnonymousUser(identity) ? null :
                                                                                                         identityManager.getOrCreateUserIdentity(identity.getUserId());
    List<ActivitySearchResult> results = activitySearchConnector.search(userIdentity,
                                                                        new ActivitySearchFilter(keyword),
                                                                        offset,
                                                                        limit);
    return results.stream()
                  .map(searchResult -> toContentLink(searchResult, identity))
                  .filter(Objects::nonNull)
                  .toList();
  }

  @Override
  @SneakyThrows
  public String getContentTitle(String objectId, Locale locale) {
    ExoSocialActivity activity = activityManager.getActivity(objectId);
    return activity == null ? null : getActivityTitle(activity);
  }

  @SneakyThrows
  private ContentLinkSearchResult toContentLink(ActivitySearchResult searchResult, Identity identity) {
    ExoSocialActivity activity = activityManager.getActivity(String.valueOf(searchResult.getId()));
    if (activity == null) {
      return null;
    } else {
      return new ContentLinkSearchResult(OBJECT_TYPE,
                                         String.valueOf(searchResult.getId()),
                                         getActivityTitle(activity),
                                         EXTENSION.getIcon());
    }
  }

  private String getActivityTitle(ExoSocialActivity activity) {
    String content = activityManager.getActivityTitle(activity);
    // Avoid cyclic transformation on
    // self object
    if (!StringUtils.contains(content,
                              String.format("%s:%s",
                                            OBJECT_TYPE,
                                            activity.getId()))) {
      content = HtmlUtils.transform(content, null);
    }
    return content == null ? "" : StringUtils.abbreviate(Jsoup.parse(content).text(), MAX_TITLE_LENGTH);
  }

}
