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
package org.exoplatform.social.core.search;

import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;

import junit.framework.TestCase;

/**
 * Test {@link Sorting} enum class
 */
public class SortingTest extends TestCase {

  public void testValueOf() {
    Sorting sortLastNameAsc = Sorting.valueOf("lastname", "asc");
    assertEquals(sortLastNameAsc, new Sorting(SortBy.LASTNAME, OrderBy.ASC));
    Sorting sortLastNameDesc = Sorting.valueOf("lastname", "desc");
    assertEquals(sortLastNameDesc, new Sorting(SortBy.LASTNAME, OrderBy.DESC));

    Sorting sortFirstNameAsc = Sorting.valueOf("firstname", "asc");
    assertEquals(sortFirstNameAsc, new Sorting(SortBy.FIRSTNAME, OrderBy.ASC));
    Sorting sortFirstNameDesc = Sorting.valueOf("firstname", "desc");
    assertEquals(sortFirstNameDesc, new Sorting(SortBy.FIRSTNAME, OrderBy.DESC));

    Sorting sortFullNameAsc = Sorting.valueOf("fullname", "asc");
    assertEquals(sortFullNameAsc, new Sorting(SortBy.FULLNAME, OrderBy.ASC));
    Sorting sortFullNameDesc = Sorting.valueOf("fullname", "desc");
    assertEquals(sortFullNameDesc, new Sorting(SortBy.FULLNAME, OrderBy.DESC));
  }
}
