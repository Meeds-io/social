/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.search;

import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.processor.I18NActivityProcessor;

public class ActivityIndexingServiceConnector extends ElasticIndexingServiceConnector {
  private static final long             serialVersionUID = 4102484220845897854L;

  public static final String           TYPE             = "activity";

  private static final Log              LOG              = ExoLogger.getLogger(ActivityIndexingServiceConnector.class);

  private final ActivitySearchProcessor activitySearchProcessor;                                                       // NOSONAR

  private final I18NActivityProcessor   i18nActivityProcessor;                                                       // NOSONAR

  private final ActivityManager         activityManager;                                                               // NOSONAR

  private final IdentityManager         identityManager;                                                               // NOSONAR

  public ActivityIndexingServiceConnector(ActivitySearchProcessor activitySearchProcessor,
                                          I18NActivityProcessor i18nActivityProcessor,
                                          IdentityManager identityManager,
                                          ActivityManager activityManager,
                                          InitParams initParams) {
    super(initParams);

    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.activitySearchProcessor = activitySearchProcessor;
    this.i18nActivityProcessor = i18nActivityProcessor;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Document create(String id) {
    return getDocument(id);
  }

  @Override
  public Document update(String id) {
    return getDocument(id);
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    LOG.debug("Index document for activity id={}", id);

    ExoSocialActivity activity = activityManager.getActivity(id);
    Map<String, String> fields = new HashMap<>();
    fields.put("id", activity.getId().replace("comment", ""));

    if (activity.getTitleId() != null) {
      try {
        activity = i18nActivityProcessor.process(activity, Locale.ENGLISH);
      } catch (Exception e) {
        LOG.warn("Error while indexing I18N activity '{}' with type '{}'", activity.getId(), activity.getType(), e);
      }
    }
    String body = activity.getBody();
    if (StringUtils.isBlank(body)) {
      body = activity.getTitle();
    }
    fields.put("body", body);
    if (StringUtils.isNotBlank(activity.getParentId())) {
      fields.put("parentId", activity.getParentId());
    }
    if (StringUtils.isNotBlank(activity.getParentCommentId())) {
      fields.put("parentCommentId", activity.getParentCommentId().replace("comment", ""));
    }
    if (StringUtils.isNotBlank(activity.getType())) {
      fields.put("type", activity.getType());
    }
    if (StringUtils.isNotBlank(activity.getPosterId())) {
      fields.put("posterId", activity.getPosterId());
    }
    ActivityStream activityStream = activity.getActivityStream();
    String ownerIdentityId = null;
    if (activityStream == null || activityStream.getType() == null || StringUtils.isBlank(activityStream.getPrettyId())) {
      if (activity.getParentId() != null) {
        ExoSocialActivity parentActivity = activityManager.getActivity(activity.getParentId());
        activityStream = parentActivity.getActivityStream();
      }
    }

    if (activityStream != null && activityStream.getType() != null && StringUtils.isNotBlank(activityStream.getPrettyId())) {
      String prettyId = activityStream.getPrettyId();
      String providerId = activityStream.getType().getProviderId();

      Identity streamOwner = identityManager.getOrCreateIdentity(providerId, prettyId);
      ownerIdentityId = streamOwner.getId();

      fields.put("streamType", String.valueOf(activityStream.getType().ordinal()));
      fields.put("streamOwner", String.valueOf(ownerIdentityId));
    }
    if (activity.getPostedTime() != null) {
      fields.put("postedTime", activity.getPostedTime().toString());
    }
    Document document = new Document(TYPE, id, null, activity.getUpdated(), Collections.singleton(ownerIdentityId), fields);
    activitySearchProcessor.index(activity, document);
    body = document.getFields().get("body");

    // Ensure to index text only without html tags
    if (StringUtils.isNotBlank(body)) {
      body = StringEscapeUtils.unescapeHtml(body);
      body = body.replaceAll("<\\S+\\s*([^><])*>", "");
      document.addField("body", body);
    }

    return document;
  }

}
