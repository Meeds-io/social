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
package org.exoplatform.social.core.activity;

import java.util.Collections;
import java.util.List;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * The realtime list access for comments of activities.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since May 9, 2011
 */
public class CommentsRealtimeListAccess implements RealtimeListAccess<ExoSocialActivity> {

  /**
   * The activity activityStorage.
   */
  private ActivityStorage activityStorage;

  /**
   * The existing activity.
   */
  private ExoSocialActivity existingActivity;

  /**
   * whether load subComments or not
   */
  private boolean loadSubComments = false;

  /**
   * The constructor.
   *
   * @param theActivityStorage
   * @param theExistingActivity
   */
  public CommentsRealtimeListAccess(ActivityStorage theActivityStorage, ExoSocialActivity theExistingActivity, boolean loadSubComments) {
    this.activityStorage = theActivityStorage;
    this.existingActivity = theExistingActivity;
    this.loadSubComments = loadSubComments;
  }

  /**
   * The constructor.
   *
   * @param theActivityStorage
   * @param theExistingActivity
   */
  public CommentsRealtimeListAccess(ActivityStorage theActivityStorage, ExoSocialActivity theExistingActivity) {
    this.activityStorage = theActivityStorage;
    this.existingActivity = theExistingActivity;
  }
  
  @Override
  public List<String> loadIdsAsList(int index, int limit) {
    throw new UnsupportedOperationException("Unsupported this method.");
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadAsList(int index, int limit) {
    return activityStorage.getComments(existingActivity, loadSubComments, index, limit);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity[] load(int index, int limit) {
    return RealtimeListAccess.convertListToArray(loadAsList(index, limit), ExoSocialActivity.class);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() {
    return activityStorage.getNumberOfComments(existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadNewer(ExoSocialActivity baseComment, int length) {
    return activityStorage.getNewerComments(existingActivity, baseComment, length);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewer(ExoSocialActivity baseComment) {
    return activityStorage.getNumberOfNewerComments(existingActivity, baseComment);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewer(Long sinceTime) {
    return activityStorage.getNumberOfNewerComments(existingActivity, sinceTime);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadOlder(ExoSocialActivity baseComment, int length) {
    return activityStorage.getOlderComments(existingActivity, baseComment, length);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlder(ExoSocialActivity baseComment) {
    return activityStorage.getNumberOfOlderComments(existingActivity, baseComment);
  }

  @Override
  public List<ExoSocialActivity> getUpadtedActivities(Long sinceTime, int limit) {
    return Collections.emptyList();
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadNewer(Long sinceTime, int limit) {
    return activityStorage.getNewerComments(existingActivity, sinceTime, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadOlder(Long sinceTime, int limit) {
    return activityStorage.getOlderComments(existingActivity, sinceTime, limit);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlder(Long sinceTime) {
    return activityStorage.getNumberOfOlderComments(existingActivity, sinceTime);
  }

  @Override
  public int getNumberOfUpgrade() {
    return 0;
  }
}
