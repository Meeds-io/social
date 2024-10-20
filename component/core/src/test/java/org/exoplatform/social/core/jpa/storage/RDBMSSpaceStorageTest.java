/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;

public class RDBMSSpaceStorageTest extends SpaceStorageTest {
  private SpaceStorage spaceStorage;
  private SocialStorageCacheService cacheService;

  private List<Space> tearDownSpaceList = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    cacheService = getService(SocialStorageCacheService.class);
  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  public void testVisited() throws Exception {
    Space space0 = getSpaceInstance(5);
    spaceStorage.saveSpace(space0, true);
    tearDownSpaceList.add(space0);
    Space space1 = getSpaceInstance(6);
    spaceStorage.saveSpace(space1, true);
    tearDownSpaceList.add(space1);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    
    List<Space> result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());
        
    restartTransaction();
    spaceStorage.updateSpaceAccessed("ghost", space1);
    
    //getVisitedSpaces return a list of spaces that
    //order by visited space then others
    result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());
  }
  
  public void testLastAccess() throws Exception {
    Space space2 = getSpaceInstance(7);
    spaceStorage.saveSpace(space2, true);
    tearDownSpaceList.add(space2);
    Space space3 = getSpaceInstance(8);
    spaceStorage.saveSpace(space3, true);
    tearDownSpaceList.add(space3);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    
    List<Space> result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space2.getId(), result.get(0).getId());

    restartTransaction();
    spaceStorage.updateSpaceAccessed("ghost", space3);

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space3.getId(), result.get(0).getId());
  }

  public void testGetLastAccessedSpace() {
    //create a new space
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space,true);
    tearDownSpaceList.add(space);
    // Update space accessed
    spaceStorage.updateSpaceAccessed("raul",space);
    cacheService.getSpacesCache().clearCache();

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("raul");
    // Get last accessed space list , this will fill all the caches with a complete SpaceData
    spaceStorage.getLastAccessedSpace(filter,0,10);
    // clear SpaceCache and SpaceSimpleCache
    cacheService.getSpaceCache().clearCache();
    cacheService.getSpaceSimpleCache().clearCache();
    //Get last accessed space list again, this will fill both SimpleSpaceCache and SpaceCache with a SpaceSimpleData
    //that has members and managers set to null by the function putSpaceInCacheIfNotExists
    spaceStorage.getLastAccessedSpace(filter,0,10);

    //Get the space from SpaceCache, this will retrieve a SpaceSimpleData object with managers and members set to null
    Space spaceFromCache = spaceStorage.getSpaceById(space.getId());

    // Check that the space should have 3 members and 2 managers
    assertNotNull(spaceFromCache.getMembers());
    assertEquals(5,spaceFromCache.getMembers().length);
    assertNotNull(spaceFromCache.getManagers());
    assertEquals(2,spaceFromCache.getManagers().length);
  }

  public void testGetLastAccessedPagination() throws Exception {
    assertEquals(0, spaceService.getLastAccessedSpace("raul").getSize());

    int numberOfSpaces = 5;
    for(int i = 0; i < numberOfSpaces; i++) {
      Space s = getSpaceInstance(i);
      spaceStorage.saveSpace(s, true);
      s = spaceStorage.getSpaceByPrettyName(s.getPrettyName());
      assertNotNull(s);
      tearDownSpaceList.add(s);
      restartTransaction();

      spaceService.updateSpaceAccessed("raul", s);
      restartTransaction();

      SpaceFilter filter = new SpaceFilter();
      filter.setRemoteId("raul");
      List<Space> spaces = spaceStorage.getLastAccessedSpace(filter, 0, 10);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());

      spaces = spaceStorage.getLastAccessedSpace(filter, 0, i + 1);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());
    }

    ListAccess<Space> spacesListAccess = spaceService.getLastAccessedSpace("raul");
    Space[] spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_4", spaces[0].getPrettyName());
    assertEquals("my_space_test_3", spaces[1].getPrettyName());

    Space space6 = getSpaceInstance(6);
    spaceStorage.saveSpace(space6, true);
    tearDownSpaceList.add(space6);
    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_4", spaces[0].getPrettyName());
    assertEquals("my_space_test_3", spaces[1].getPrettyName());

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(2, 10);
    assertEquals(4, spaces.length);
    assertEquals("my_space_test_2", spaces[0].getPrettyName());
    assertEquals("my_space_test_1", spaces[1].getPrettyName());
    assertEquals("my_space_test_0", spaces[2].getPrettyName());
    assertEquals("my_space_test_6", spaces[3].getPrettyName());

    spaceService.updateSpaceAccessed("raul", space6);

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(0, 2);
    assertEquals(2, spaces.length);
    assertEquals("my_space_test_6", spaces[0].getPrettyName());
    assertEquals("my_space_test_4", spaces[1].getPrettyName());

    spacesListAccess = spaceService.getLastAccessedSpace("raul");
    spaces = spacesListAccess.load(2, 10);
    assertEquals(4, spaces.length);
    assertEquals("my_space_test_3", spaces[0].getPrettyName());
    assertEquals("my_space_test_2", spaces[1].getPrettyName());
    assertEquals("my_space_test_1", spaces[2].getPrettyName());
    assertEquals("my_space_test_0", spaces[3].getPrettyName());
  }


  public void testGetCommonSpaces() throws Exception {

    //Get instances of 3 spaces with different members and 1 common manager
    Space space1 = getSpaceInstance( 11, "public", "open", "demo",  "ghost", "dragon");
    Space space2 = getSpaceInstance( 12, "public", "open", "demo", "ghost", "dragon","raul");
    Space space3 = getSpaceInstance( 13, "public", "open", "demo", "ghost", "dragon","raul");
    Space space4 = getSpaceInstance( 14, "public", "open", "demo", "ghost", "dragon","raul");
    Space space5 = getSpaceInstance( 15, "public", "open", "demo", "ghost", "dragon","raul");
    Space space6 = getSpaceInstance( 16, "public", "open", "demo", "ghost", "dragon","raul");

    spaceStorage.saveSpace(space1,true);
    spaceStorage.saveSpace(space2,true);
    spaceStorage.saveSpace(space3,true);
    spaceStorage.saveSpace(space4,true);
    spaceStorage.saveSpace(space5,true);
    spaceStorage.saveSpace(space6,true);

    List<Space> resultListCommonSpaces1 = spaceStorage.getCommonSpaces("demo","raul",0,2);
    assertEquals(2, resultListCommonSpaces1.size());
    Space testSpace2 = resultListCommonSpaces1.get(0);
    assertEquals(space2,testSpace2);
    Space testSpace3 = resultListCommonSpaces1.get(1);
    assertEquals(space3,testSpace3);

    List<Space> resultListCommonSpaces2 = spaceStorage.getCommonSpaces("demo","raul",2,2);
    assertEquals(2, resultListCommonSpaces2.size());
    Space testSpace4 = resultListCommonSpaces2.get(0);
    assertEquals(space4,testSpace4);
    Space testSpace5 = resultListCommonSpaces2.get(1);
    assertEquals(space5,testSpace5);

    List<Space> resultListCommonSpaces3 = spaceStorage.getCommonSpaces("demo","raul",4,2);
    assertEquals(1,resultListCommonSpaces3.size());
    Space testSpace6 = resultListCommonSpaces3.get(0);
    assertEquals(space6,testSpace6);


  }

  public void testCountCommonSpaces() throws Exception {

    //Get instances of 3 spaces with different members and 1 common manager
    Space space3 = getSpaceInstance( 11, "public", "open", "demo",  "ghost", "dragon");
    Space space1 = getSpaceInstance( 12, "public", "open", "demo", "ghost", "dragon","raul");
    Space space2 = getSpaceInstance( 13, "public", "open", "demo", "ghost", "dragon","raul");

    spaceStorage.saveSpace(space3,true);
    spaceStorage.saveSpace(space1,true);
    spaceStorage.saveSpace(space2,true);

    int resultListCommonSpaces = spaceStorage.countCommonSpaces("demo","raul");
    assertEquals(2, resultListCommonSpaces);

  }


}