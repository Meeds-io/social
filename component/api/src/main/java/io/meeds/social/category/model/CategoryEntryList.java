/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.category.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryEntryList {

  private List<CategoryEntryItem> items;

  private long                      offset;

  private long                      limit;

  private boolean                   hasMore;

  /**
   * Raw storage offset to use for the next page request (i.e. "load more"),
   * as opposed to {@link #offset}, which is the storage offset used to build
   * this page. Since the number of items returned to the caller can differ
   * from the number of raw storage rows consumed to build them (some rows are
   * dropped by de-duplication and by the accessibility check), the caller
   * must not simply advance {@link #offset} by {@link #limit} when loading
   * the next page - doing so misaligns with the underlying storage and can
   * skip or repeat entries.
   */
  private long                      nextOffset;

}
