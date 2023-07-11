/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.activity;

import org.exoplatform.social.common.lifecycle.LifeCycleListener;

public interface ActivityListener extends LifeCycleListener<ActivityLifeCycleEvent> {
  
  default void saveActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void updateActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void saveComment(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void updateComment(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }
  
  default void likeActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void deleteLikeActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void likeComment(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void deleteLikeComment(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void deleteComment(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void deleteActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void shareActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void hideActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void pinActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

  default void unpinActivity(ActivityLifeCycleEvent event) {
    // By default, no behavior
  }

}
