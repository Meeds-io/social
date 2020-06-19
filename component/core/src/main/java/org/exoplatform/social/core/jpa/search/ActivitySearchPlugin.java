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
package org.exoplatform.social.core.jpa.search;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.social.core.activity.model.ActivitySearchResult;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public abstract class ActivitySearchPlugin extends BaseComponentPlugin {

  public abstract String getActivityType();

  /**
   * This is triggered before indexing an activity to enrich or alter an
   * attribute to index
   * 
   * @param activity {@link ExoSocialActivity} that will be indexed
   * @param document {@link Document} containing properties to index
   */
  public void index(ExoSocialActivity activity, Document document) {
    // Empty method by default
  }

  /**
   * This is triggered before displaying search result of an activity to enrich
   * or alter an attribute in the result
   * 
   * @param activity {@link ActivitySearchResult} that will be indexed
   */
  public void formatSearchResult(ActivitySearchResult activity) {
    // Empty method by default
  }

}
