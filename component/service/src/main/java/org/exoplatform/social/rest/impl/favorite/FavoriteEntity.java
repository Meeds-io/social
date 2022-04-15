package org.exoplatform.social.rest.impl.favorite;

import org.exoplatform.social.metadata.model.MetadataItem;

import java.util.List;

public class FavoriteEntity {

  private List<MetadataItem> favoritesItem;
  private Integer offset;

  private Integer limit;

  public List<MetadataItem> getFavoritesItem() {
    return favoritesItem;
  }

  public void setFavoritesItem(List<MetadataItem> favoritesItem) {
    this.favoritesItem=favoritesItem;
  }

  private Integer size;

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset=offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit=limit;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size=size;
  }
}
