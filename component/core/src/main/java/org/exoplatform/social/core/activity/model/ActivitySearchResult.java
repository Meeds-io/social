package org.exoplatform.social.core.activity.model;

import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;

public class ActivitySearchResult {

  private long                 id;

  private Identity             streamOwner;

  private Identity             poster;

  private String               type;

  private String               body;

  private List<String>         excerpts;

  private long                 postedTime;

  private long                 lastUpdatedTime;

  /**
   * Resulted comment containg searched text
   */
  private ActivitySearchResult comment;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Identity getStreamOwner() {
    return streamOwner;
  }

  public void setStreamOwner(Identity streamOwner) {
    this.streamOwner = streamOwner;
  }

  public Identity getPoster() {
    return poster;
  }

  public void setPoster(Identity poster) {
    this.poster = poster;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public long getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(long lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  public ActivitySearchResult getComment() {
    return comment;
  }

  public void setComment(ActivitySearchResult comment) {
    this.comment = comment;
  }

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
  }

}
