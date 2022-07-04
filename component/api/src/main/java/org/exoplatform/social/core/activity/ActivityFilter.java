package org.exoplatform.social.core.activity;

import java.util.List;

public class ActivityFilter {

  private boolean  isMyPosted;

  private boolean  isFavorite;

  private List<Long> spaceIds;

  public boolean isMyPosted() {
    return isMyPosted;
  }

  public void setMyPosted(boolean MyPosted) {
    isMyPosted = MyPosted;
  }

  public boolean isFavorite() {
    return isFavorite;
  }

  public void setFavorite(boolean favorite) {
    isFavorite = favorite;
  }

  public List<Long> getSpaceIds() {
    return spaceIds;
  }

  public void setSpaceIds(List<Long> spaceIds) {
    this.spaceIds = spaceIds;
  }
}
