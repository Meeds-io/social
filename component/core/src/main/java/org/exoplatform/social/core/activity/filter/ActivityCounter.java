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
package org.exoplatform.social.core.activity.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityCounter implements Collection<ExoSocialActivity> {

  private String[] ids;
  private List<ExoSocialActivity> gotList = null;

  /**
   * Constructor with offset and limit
   */
  public ActivityCounter() {
    ids = new String[0];
    gotList = new ArrayList<ExoSocialActivity>();
  }
  
 

  @Override
  public int size() {
    return gotList.size();
  }

  @Override
  public boolean isEmpty() {
    return gotList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof ExoSocialActivity) {
      ExoSocialActivity a = (ExoSocialActivity) o;
      return ArrayUtils.indexOf(ids, a.getId()) >= 0;
    }
    
    //
    return false;
  }
  

  @Override
  public Iterator<ExoSocialActivity> iterator() {
    return gotList.iterator();
  }

  @Override
  public Object[] toArray() {
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return null;
  }

  @Override
  public boolean add(ExoSocialActivity e) {
    if (contains(e)) {
      return false;
    }
    
    //
    ids = (String[]) ArrayUtils.add(ids, e.getId());
    gotList.add(e);
    return true;
  }
  
  public List<ExoSocialActivity> result() {
    return gotList;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof ExoSocialActivity) {
      ExoSocialActivity a = (ExoSocialActivity) o;
      ids = (String[]) ArrayUtils.removeElement(ids, a.getId());
      return gotList.remove(o);
    }
    
    //
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends ExoSocialActivity> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {
    ids = new String[0];
    gotList = new ArrayList<ExoSocialActivity>();
  }
}
