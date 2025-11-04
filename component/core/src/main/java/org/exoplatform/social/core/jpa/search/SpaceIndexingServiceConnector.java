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
package org.exoplatform.social.core.jpa.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.search.DocumentWithMetadata;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

public class SpaceIndexingServiceConnector extends ElasticIndexingServiceConnector {

  public static final String TEMPLATE_MANAGER_PREFIX    = "@templateManager@";

  public static final String TEMPLATE_MANAGER_PATTERN   = TEMPLATE_MANAGER_PREFIX + "%s";

  public static final String TYPE                       = "space";

  public static final String SPACE_METADATA_OBJECT_TYPE = "space";

  private static final Log   LOG                        = ExoLogger.getLogger(SpaceIndexingServiceConnector.class);

  private SpaceService       spaceService;

  private SpaceStorage       spaceStorage;

  private MetadataService    metadataService;

  public SpaceIndexingServiceConnector(SpaceService spaceService,
                                       SpaceStorage spaceStorage,
                                       MetadataService metadataService,
                                       InitParams initParams) {
    super(initParams);
    this.spaceService = spaceService;
    this.spaceStorage = spaceStorage;
    this.metadataService = metadataService;
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  @Override
  public Document create(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }

    long ts = System.currentTimeMillis();
    LOG.debug("get space document for space id={}", id);

    Space space = spaceService.getSpaceById(id);
    if (space == null) {
      return null;
    }

    Map<String, String> fields = new HashMap<>();
    fields.put("prettyName", space.getPrettyName());
    fields.put("displayName", space.getDisplayName());
    fields.put("description", htmlToText(space.getDescription()));
    fields.put("visibility", space.getVisibility());
    fields.put("registration", space.getRegistration());
    fields.put("templateId", String.valueOf(space.getTemplateId()));
    fields.put("sovereign", String.valueOf(space.isSovereign()));

    Date updatedDate = new Date(space.getLastUpdatedTime());

    DocumentWithMetadata document = new DocumentWithMetadata();
    document.setId(id);
    document.setLastUpdatedDate(updatedDate);
    document.setFields(fields);

    setPermissions(space, document);
    setMembers(space, document);
    setManagers(space, document);
    document.addListField("pending",
                          new HashSet<>(space.getPendingUsers() == null ? Collections.emptyList() :
                                                                        Arrays.asList(space.getPendingUsers())));
    document.addListField("invited",
                          new HashSet<>(space.getInvitedUsers() == null ? Collections.emptyList() :
                                                                        Arrays.asList(space.getInvitedUsers())));
    document.addListField("publisher",
                          new HashSet<>(space.getPublishers() == null ? Collections.emptyList() :
                                                                      Arrays.asList(space.getPublishers())));
    document.addListField("redactor",
                          new HashSet<>(space.getRedactors() == null ? Collections.emptyList() :
                            Arrays.asList(space.getRedactors())));
    document.addListField("categoryId",
                          new HashSet<>(space.getCategoryIds() == null ? Collections.emptyList() :
                                                                       space.getCategoryIds()
                                                                            .stream()
                                                                            .map(String::valueOf)
                                                                            .toList()));

    addDocumentMetadata(document, id);
    LOG.info("space document generated for id={} name={} duration_ms={}",
             id,
             space.getPrettyName(),
             System.currentTimeMillis() - ts);

    return document;
  }

  @Override
  public Document update(String id) {
    return create(id);
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    return spaceStorage.getSpaces(offset, limit)
                       .stream()
                       .map(Space::getId)
                       .toList();
  }

  @Override
  public String getMapping() {
    StringBuilder mapping = new StringBuilder()
                                               .append("{")
                                               .append("  \"properties\" : {\n")
                                               .append("    \"prettyName\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"displayName\" : {")
                                               .append("      \"type\" : \"text\",")
                                               .append("      \"analyzer\": \"ngram_analyzer\",")
                                               .append("      \"search_analyzer\": \"ngram_analyzer_search\",")
                                               .append("      \"index_options\": \"offsets\",")
                                               .append("      \"fields\": {")
                                               .append("        \"raw\": {")
                                               .append("          \"type\": \"keyword\"")
                                               .append("        }")
                                               .append("      }")
                                               .append("    },\n")
                                               .append("    \"description\" : {\"type\" : \"text\", \"index_options\": \"offsets\"},\n")
                                               .append("    \"visibility\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"registration\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"permissions\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"pending\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"invited\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"manager\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"member\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"publisher\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"templateId\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"categoryId\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"redactor\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"sovereign\" : {\"type\" : \"boolean\"},\n")
                                               .append("    \"lastUpdatedDate\" : {\"type\" : \"date\", \"format\": \"epoch_millis\"}\n")
                                               .append("  }\n")
                                               .append("}");

    return mapping.toString();
  }

  private void addDocumentMetadata(DocumentWithMetadata document, String spaceId) {
    MetadataObject metadataObject = new MetadataObject(SPACE_METADATA_OBJECT_TYPE, spaceId);
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    document.setMetadataItems(metadataItems);
  }

  private void setManagers(Space space, DocumentWithMetadata document) {
    HashSet<String> managers = new HashSet<>(space.getManagers() == null ? Collections.emptyList() :
                                                                         Arrays.asList(space.getManagers()));
    if (space.getTemplateId() > 0) {
      managers.add(String.format(TEMPLATE_MANAGER_PATTERN, space.getTemplateId()));
    }
    document.addListField("manager", managers);
  }

  private void setMembers(Space space, DocumentWithMetadata document) {
    HashSet<String> members = new HashSet<>(space.getMembers() == null ? Collections.emptyList() :
                                                                       Arrays.asList(space.getMembers()));
    if (space.getTemplateId() > 0) {
      members.add(String.format(TEMPLATE_MANAGER_PATTERN, space.getTemplateId()));
    }
    document.addListField("member", members);
  }

  private void setPermissions(Space space, DocumentWithMetadata document) {
    Set<String> permissions = new HashSet<>(Space.HIDDEN.equals(space.getVisibility()) ? Arrays.asList(space.getMembers()) :
                                                                                       Collections.singleton("all"));
    if (space.getTemplateId() > 0) {
      permissions.add(String.format(TEMPLATE_MANAGER_PATTERN, space.getTemplateId()));
    }
    document.setPermissions(permissions);
  }

  private String htmlToText(String source) {
    return source == null ? "" : Jsoup.parse(source).text();
  }

}
