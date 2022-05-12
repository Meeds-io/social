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

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.*;
import org.exoplatform.social.metadata.tag.model.*;

/**
 * Test class for {@link TagService}
 */
public class TagServiceTest extends AbstractCoreTest {

  private Identity        rootIdentity;

  private Identity        johnIdentity;

  private Identity        maryIdentity;

  private TagService      tagService;

  private MetadataService metadataService;

  private MetadataDAO     metadataDAO;

  private List<Space>     tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    tagService = getContainer().getComponentInstanceOfType(TagService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    tearDownSpaceList = new ArrayList<>();

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    metadataDAO.deleteAll();

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    super.tearDown();
  }

  public void testDetectTagNames() {
    Set<TagName> tagNames = tagService.detectTagNames(null);
    assertNotNull(tagNames);
    assertEquals(0, tagNames.size());

    String content =
                   "<div>Test tag #NoTagHere test test"
                       + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANew'TagHere</a>&nbsp;.</div>";
    tagNames = tagService.detectTagNames(content);
    assertNotNull(tagNames);
    assertEquals(1, tagNames.size());
    assertEquals(Collections.singleton(new TagName("ANew'TagHere")), tagNames);
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
    tagNames1.add(new TagName(tagName1.toLowerCase()));
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
    assertTrue(savedTagNames.contains(new TagName(tagName1)));
    assertTrue(savedTagNames.contains(new TagName(tagName2)));

    Metadata metadata = metadataService.getMetadataByKey(new MetadataKey(TagService.METADATA_TYPE.getName(),
                                                                         tagName1,
                                                                         audienceId));
    assertNotNull(metadata);
    assertEquals(tagName1, metadata.getName());
    assertEquals(TagService.METADATA_TYPE.getName(), metadata.getTypeName());
    assertEquals(audienceId, metadata.getAudienceId());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("tags", taggedObject1);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());
    Set<TagName> storedTagNames = metadataItems.stream()
                  .map(MetadataItem::getMetadata)
                  .map(Metadata::getName)
                  .map(TagName::new)
                  .collect(Collectors.toSet());
    assertTrue(storedTagNames.contains(new TagName(tagName1)));
    assertTrue(storedTagNames.contains(new TagName(tagName2)));

    Set<TagName> updatedTagNames = Collections.singleton(new TagName(tagName1));
    savedTagNames = tagService.saveTags(taggedObject1,
                                        updatedTagNames,
                                        audienceId,
                                        userIdentityId);
    assertNotNull(savedTagNames);
    assertEquals(1, savedTagNames.size());
    assertEquals(updatedTagNames, savedTagNames);

    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("tags", taggedObject1);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertTrue(storedTagNames.contains(new TagName(tagName1)));

    TagObject taggedObject2 = new TagObject(objectType, objectId2, parentObjectId);
    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("tags", taggedObject2);
    assertNotNull(metadataItems);
    assertEquals(0, metadataItems.size());

    Set<TagName> tagNames2 = new HashSet<>();
    tagNames2.add(new TagName(tagName1.toUpperCase()));
    tagNames2.add(new TagName("tag3"));

    savedTagNames = tagService.saveTags(taggedObject2, tagNames2, audienceId, userIdentityId);
    assertNotNull(savedTagNames);
    assertEquals(2, savedTagNames.size());

    metadataItems = metadataService.getMetadataItemsByMetadataTypeAndObject("tags", taggedObject2);
    assertNotNull(metadataItems);
    assertEquals(2, metadataItems.size());

    storedTagNames = metadataItems.stream()
                                  .map(MetadataItem::getMetadata)
                                  .map(Metadata::getName)
                                  .map(TagName::new)
                                  .collect(Collectors.toSet());
    assertEquals(tagNames2, storedTagNames);
  }

  public void testFindTags() throws Exception {
    List<Space> spaces = new ArrayList<>();
    List<Long> spaceIdentityIds = new ArrayList<>();
    List<Long> spaceCreators = new ArrayList<>();
    Space space = createSpace("FindTags1",
                              maryIdentity.getRemoteId(),
                              maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));
    spaceIdentityIds.add(Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId()));

    space = createSpace("FindTags2",
                        johnIdentity.getRemoteId(),
                        johnIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(johnIdentity.getId()));
    spaceIdentityIds.add(Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId()));

    space = createSpace("FindTags3",
                        maryIdentity.getRemoteId(),
                        johnIdentity.getRemoteId(),
                        maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(Long.parseLong(maryIdentity.getId()));
    spaceIdentityIds.add(Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId()));

    Set<TagName> tagNames = new HashSet<>();
    tagNames.add(new TagName("tagMary1"));
    tagNames.add(new TagName("tagMary2"));

    Set<TagName> savedTags = tagService.saveTags(new TagObject("objectType", "objectId1"),
                                                 tagNames,
                                                 spaceIdentityIds.get(0),
                                                 spaceCreators.get(0));
    assertEquals(savedTags, tagNames);

    tagNames = new HashSet<>();
    tagNames.add(new TagName("tagJohn1"));
    tagNames.add(new TagName("tagJohn2"));

    savedTags = tagService.saveTags(new TagObject("objectType", "objectId1"),
                                    tagNames,
                                    spaceIdentityIds.get(1),
                                    spaceCreators.get(1));
    assertEquals(savedTags, tagNames);

    tagNames = new HashSet<>();
    tagNames.add(new TagName("tagJohnMary1"));
    tagNames.add(new TagName("tagJohnMary2"));

    savedTags = tagService.saveTags(new TagObject("objectType", "objectId1"),
                                    tagNames,
                                    spaceIdentityIds.get(2),
                                    spaceCreators.get(2));
    assertEquals(savedTags, tagNames);

    List<TagName> marySavedTags = tagService.findTags(null, Long.parseLong(maryIdentity.getId()));
    assertNotNull(marySavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2",
                               "tagMary1",
                               "tagMary2"),
                 marySavedTags.stream().map(TagName::getName).sorted().collect(Collectors.toList()));

    marySavedTags = tagService.findTags(new TagFilter("mar", 0), Long.parseLong(maryIdentity.getId()));
    assertNotNull(marySavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2",
                               "tagMary1",
                               "tagMary2"),
                 marySavedTags.stream().map(TagName::getName).sorted().collect(Collectors.toList()));

    marySavedTags = tagService.findTags(new TagFilter("mar", 3), Long.parseLong(maryIdentity.getId()));
    assertNotNull(marySavedTags);
    assertEquals(3, marySavedTags.size());

    List<TagName> johnSavedTags = tagService.findTags(new TagFilter("mar", 0), Long.parseLong(johnIdentity.getId()));
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2"),
                 johnSavedTags.stream().map(TagName::getName).sorted().collect(Collectors.toList()));

    johnSavedTags = tagService.findTags(new TagFilter("mar", 2), Long.parseLong(johnIdentity.getId()));
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2"),
                 johnSavedTags.stream().map(TagName::getName).sorted().collect(Collectors.toList()));

    johnSavedTags = tagService.findTags(new TagFilter("joh", 10), Long.parseLong(johnIdentity.getId()));
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohn1",
                               "tagJohn2",
                               "tagJohnMary1",
                               "tagJohnMary2"),
                 johnSavedTags.stream().map(TagName::getName).sorted().collect(Collectors.toList()));
  }

  public void testFindTagsCaseInsensitive() throws Exception {
    Set<TagName> tagNames = new HashSet<>();
    tagNames.add(new TagName("tagMary1"));
    tagNames.add(new TagName("tagMary2"));

    long maryIdentityId = Long.parseLong(maryIdentity.getId());

    TagObject tagObject = new TagObject("objectType", "objectId3");

    Set<TagName> savedTags = tagService.saveTags(tagObject,
                                                 tagNames,
                                                 maryIdentityId,
                                                 maryIdentityId);
    assertEquals(savedTags, tagNames);

    Set<TagName> tagNamesUpperCase = new HashSet<>();
    tagNamesUpperCase.add(new TagName("tagMary1".toUpperCase()));
    tagNamesUpperCase.add(new TagName("tagMary2".toUpperCase()));

    savedTags = tagService.saveTags(tagObject,
                                    tagNamesUpperCase,
                                    maryIdentityId,
                                    maryIdentityId);
    assertEquals(tagNamesUpperCase, savedTags);

    List<TagName> marySavedTags = tagService.findTags(null, maryIdentityId);
    assertNotNull(marySavedTags);
    assertEquals(2, marySavedTags.size());
    assertTrue(marySavedTags.contains(new TagName("tagMary1".toUpperCase())));
    assertTrue(marySavedTags.contains(new TagName("tagMary2".toUpperCase())));

    Set<TagName> savedTagNames = tagService.getTagNames(tagObject);
    assertEquals(tagNames, savedTagNames);
  }

  @SuppressWarnings("deprecation")
  private Space createSpace(String spaceName, String creator, String... members) throws Exception {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] spaceMembers = members == null ? new String[] { creator } : members;
    space.setManagers(managers);
    space.setMembers(spaceMembers);
    spaceService.saveSpace(space, true); // NOSONAR
    tearDownSpaceList.add(space);
    return space;
  }
}
