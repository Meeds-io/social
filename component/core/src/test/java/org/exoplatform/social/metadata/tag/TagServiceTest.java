/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.metadata.tag;

import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.*;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

/**
 * Test class for {@link TagService}
 */
public class TagServiceTest extends AbstractCoreTest {

  private Identity        johnIdentity;

  private IdentityManager identityManager;

  private TagService      tagService;

  private MetadataService metadataService;

  private MetadataDAO     metadataDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    tagService = getContainer().getComponentInstanceOfType(TagService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);

    try {
      InitParams params = new InitParams();
      ObjectParameter parameter = new ObjectParameter();
      parameter.setName("metadataType");
      parameter.setObject(FavoriteService.METADATA_TYPE);
      params.addParameter(parameter);
      metadataService.addMetadataTypePlugin(new MetadataTypePlugin(params));
    } catch (UnsupportedOperationException e) {
      // Expected when already added
    }

    johnIdentity = identityManager.getOrCreateUserIdentity("john");
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(johnIdentity);
    metadataDAO.deleteAll();

    super.tearDown();
  }

  public void testDetectTagNames() {
    Set<TagName> tagNames = tagService.detectTagNames(null);
    assertNotNull(tagNames);
    assertEquals(0, tagNames.size());

    String content =
                   "<div>Test tag #NoTagHere test test"
                       + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere</a>&nbsp;.</div>";
    tagNames = tagService.detectTagNames(content);
    assertNotNull(tagNames);
    assertEquals(1, tagNames.size());
    assertEquals(Collections.singleton(new TagName("ANewTagHere")), tagNames);
  }

  public void testSaveTags() { // NOSONAR
    String objectType = "type";
    String objectId1 = "1";
    String objectId2 = "2";
    String parentObjectId = "2";
    long userIdentityId = Long.parseLong(johnIdentity.getId());
    long audienceId = 5000l;

    Set<TagName> tagNames1 = new HashSet<>();
    String tagName1 = "tag1";
    String tagName2 = "tag2";
    tagNames1.add(new TagName(tagName1));
    tagNames1.add(new TagName(tagName2));

    try {
      tagService.saveTags(null,
                          tagNames1,
                          audienceId,
                          userIdentityId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      tagService.saveTags(new TagObject(null,
                                        objectId1,
                                        parentObjectId),
                          tagNames1,
                          audienceId,
                          userIdentityId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      tagService.saveTags(new TagObject(objectType,
                                        null,
                                        parentObjectId),
                          tagNames1,
                          audienceId,
                          userIdentityId);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }

    TagObject taggedObject1 = new TagObject(objectType, objectId1, parentObjectId);
    Set<TagName> savedTagNames = tagService.saveTags(taggedObject1,
                                                     tagNames1,
                                                     audienceId,
                                                     userIdentityId);
    assertNotNull(savedTagNames);
    assertEquals(2, savedTagNames.size());
    assertEquals(tagNames1, savedTagNames);

    Metadata metadata = metadataService.getMetadataByKey(new MetadataKey(TagService.METADATA_TYPE.getName(),
                                                                         tagName1,
                                                                         audienceId));
    assertNotNull(metadata);
    assertEquals(tagName1, metadata.getName());
    assertEquals(TagService.METADATA_TYPE.getName(), metadata.getTypeName());
    assertEquals(audienceId, metadata.getAudienceId());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(taggedObject1);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    assertEquals(tagNames1,
                 metadataItems.stream()
                              .map(MetadataItem::getMetadata)
                              .map(Metadata::getName)
                              .map(TagName::new)
                              .collect(Collectors.toSet()));

    Set<TagName> updatedTagNames = Collections.singleton(new TagName(tagName1));
    savedTagNames = tagService.saveTags(taggedObject1,
                                        updatedTagNames,
                                        audienceId,
                                        userIdentityId);
    assertNotNull(savedTagNames);
    assertEquals(1, savedTagNames.size());
    assertEquals(updatedTagNames, savedTagNames);

    metadataItems = metadataService.getMetadataItemsByObject(taggedObject1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(updatedTagNames,
                 metadataItems.stream()
                              .map(MetadataItem::getMetadata)
                              .map(Metadata::getName)
                              .map(TagName::new)
                              .collect(Collectors.toSet()));

    TagObject taggedObject2 = new TagObject(objectType, objectId2, parentObjectId);
    metadataItems = metadataService.getMetadataItemsByObject(taggedObject2);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    Set<TagName> tagNames2 = new HashSet<>();
    tagNames2.add(new TagName(tagName1));
    tagNames2.add(new TagName("tag3"));

    savedTagNames = tagService.saveTags(taggedObject2, tagNames2, audienceId, userIdentityId);
    assertNotNull(savedTagNames);
    assertEquals(2, savedTagNames.size());

    metadataItems = metadataService.getMetadataItemsByObject(taggedObject2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    assertEquals(tagNames2,
                 metadataItems.stream()
                              .map(MetadataItem::getMetadata)
                              .map(Metadata::getName)
                              .map(TagName::new)
                              .collect(Collectors.toSet()));
  }

}
