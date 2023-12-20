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
package org.exoplatform.social.rest.impl.tag;

import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class TagRestTest extends AbstractResourceTest {

  private Identity          rootIdentity;

  private Identity          johnIdentity;

  private Identity          maryIdentity;

  private TagService        tagService;

  protected SpaceService    spaceService;

  protected IdentityManager identityManager;

  private MetadataDAO       metadataDAO;

  private List<Space>       tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    tagService = getContainer().getComponentInstanceOfType(TagService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    tearDownSpaceList = new ArrayList<>();

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");

    TagRest tagRest = new TagRest(tagService);
    registry(tagRest);
  }

  @Override
  public void tearDown() throws Exception {
    restartTransaction();
    identityManager.hardDeleteIdentity(rootIdentity);
    identityManager.hardDeleteIdentity(johnIdentity);
    identityManager.hardDeleteIdentity(maryIdentity);
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

  public void testFindTags() throws Exception {
    long maryIdentityId = Long.parseLong(maryIdentity.getId());
    long johnIdentityId = Long.parseLong(johnIdentity.getId());

    List<Space> spaces = new ArrayList<>();
    List<Long> spaceIdentityIds = new ArrayList<>();
    List<Long> spaceCreators = new ArrayList<>();
    Space space = createSpace("FindTags1",
                              maryIdentity.getRemoteId(),
                              maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(maryIdentityId);
    spaceIdentityIds.add(Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId()));

    space = createSpace("FindTags2",
                        johnIdentity.getRemoteId(),
                        johnIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(johnIdentityId);
    spaceIdentityIds.add(Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId()));

    space = createSpace("FindTags3",
                        maryIdentity.getRemoteId(),
                        johnIdentity.getRemoteId(),
                        maryIdentity.getRemoteId());
    spaces.add(space);
    spaceCreators.add(maryIdentityId);
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

    int limit3 = 1;
    savedTags = tagService.saveTags(new TagObject("objectType", "objectId1"),
                                    tagNames,
                                    spaceIdentityIds.get(limit3),
                                    spaceCreators.get(limit3));
    assertEquals(savedTags, tagNames);

    tagNames = new HashSet<>();
    tagNames.add(new TagName("tagJohnMary1"));
    tagNames.add(new TagName("tagJohnMary2"));

    savedTags = tagService.saveTags(new TagObject("objectType", "objectId1"),
                                    tagNames,
                                    spaceIdentityIds.get(2),
                                    spaceCreators.get(2));
    assertEquals(savedTags, tagNames);

    List<TagName> marySavedTags = tagService.findTags(null, maryIdentityId);
    assertNotNull(marySavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2",
                               "tagMary1",
                               "tagMary2"),
                 marySavedTags.stream().map(TagName::getName).collect(Collectors.toList()));

    String term = "mar";

    marySavedTags = findTags(maryIdentity.getRemoteId(), term, 0);
    assertNotNull(marySavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2",
                               "tagMary1",
                               "tagMary2"),
                 marySavedTags.stream().map(TagName::getName).collect(Collectors.toList()));

    marySavedTags = findTags(maryIdentity.getRemoteId(), term, 3);
    assertNotNull(marySavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2",
                               "tagMary1"),
                 marySavedTags.stream().map(TagName::getName).collect(Collectors.toList()));

    List<TagName> johnSavedTags = findTags(johnIdentity.getRemoteId(), term, 0);
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohnMary1",
                               "tagJohnMary2"),
                 johnSavedTags.stream().map(TagName::getName).collect(Collectors.toList()));

    johnSavedTags = findTags(johnIdentity.getRemoteId(), term, 1);
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohnMary1"),
                 johnSavedTags.stream().map(TagName::getName).collect(Collectors.toList()));

    johnSavedTags = findTags(johnIdentity.getRemoteId(), "joh", 10);
    assertNotNull(johnSavedTags);
    assertEquals(Arrays.asList("tagJohn1",
                               "tagJohn2",
                               "tagJohnMary1",
                               "tagJohnMary2"),
                 johnSavedTags.stream().map(TagName::getName).collect(Collectors.toList()));
  }

  private List<TagName> findTags(String remoteId, String term, int limit) throws Exception { 
    startSessionAs(remoteId);
    ContainerResponse response = getResponse("GET",
                                             getURLResource("tags") + "?q=" + (term == null ? "" : term) + "&limit=" + limit,
                                             null);
    assertEquals(200, response.getStatus());
    @SuppressWarnings("unchecked")
    List<TagName> tagNames = (List<TagName>) response.getEntity();
    return tagNames;
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
