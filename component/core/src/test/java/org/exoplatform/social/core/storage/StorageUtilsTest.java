/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.impl.StorageUtils;

public class StorageUtilsTest extends TestCase {

  public void testSubList() throws Exception {
    List<String> list = new ArrayList<String>();
    
    for (int i = 0; i < 20; i++) {
      list.add(""+i);
    }
    
    List<String> loaded = StorageUtils.subList(list, 0, 10);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 0, 25);
    
    assertEquals(20, loaded.size());
    
    loaded = StorageUtils.subList(list, 19 , 25);
    
    assertEquals(1, loaded.size());
    
    loaded = StorageUtils.subList(list, 10, 25);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 15 , 15);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 20 , 25);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 10);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 30);
    
    assertEquals(0, loaded.size());
    
  }

}
