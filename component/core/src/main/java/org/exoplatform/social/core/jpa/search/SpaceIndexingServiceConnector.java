/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.search;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.search.DocumentWithMetadata;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

public class SpaceIndexingServiceConnector extends ElasticIndexingServiceConnector {

  public static final String    TYPE                       = "space";

  public static final String    SPACE_METADATA_OBJECT_TYPE = "space";

  private static final Log      LOG                        = ExoLogger.getLogger(SpaceIndexingServiceConnector.class);

  private SpaceService          spaceService;

  private SpaceStorage          spaceStorage;

  private MetadataService    metadataService;

  public SpaceIndexingServiceConnector(InitParams initParams,
                                       SpaceService spaceService,
                                       SpaceStorage spaceStorage,
                                       MetadataService metadataService) {
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
    fields.put("description", space.getDescription());
    fields.put("visibility", space.getVisibility());
    fields.put("registration", space.getRegistration());

    Date createdDate = new Date(space.getCreatedTime());

    DocumentWithMetadata document = new DocumentWithMetadata();
    document.setId(id);
    document.setLastUpdatedDate(createdDate);
    document.setPermissions(new HashSet<>(Arrays.asList(space.getMembers())));
    document.setFields(fields);
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

    List<String> ids = new LinkedList<>();
    try {
      List<Space> spaces = spaceStorage.getAllSpaces();
      int to = offset + limit;
      to = to > spaces.size() ? spaces.size() : to;
      for (Space space : spaces.subList(offset, to)) {
        ids.add(space.getId());
      }
    } catch (Exception ex) {
      LOG.error(ex);
    }
    return ids;
  }

  @Override
  public String getMapping() {
    StringBuilder mapping = new StringBuilder()
                                               .append("{")
                                               .append("  \"properties\" : {\n")
                                               .append("    \"prettyName\" : {\"type\" : \"keyword\"},\n")
                                               .append("    \"displayName\" : {")
                                               .append("      \"type\" : \"text\",")
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

}
