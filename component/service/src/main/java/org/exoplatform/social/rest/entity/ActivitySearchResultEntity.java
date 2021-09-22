package org.exoplatform.social.rest.entity;

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.activity.model.ActivitySearchResult;

public class ActivitySearchResultEntity extends BaseEntity {

  private static final long                     serialVersionUID = 5467324200693836628L;

  private IdentityEntity                        streamOwner;

  private IdentityEntity                        poster;

  private String                                type;

  private String                                body;

  private List<String>                          excerpts;

  private long                                  postedTime;

  private long                                  likesCount;

  private long                                  commentsCount;

  private ActivitySearchResultEntity            comment;

  private Map<String, List<MetadataItemEntity>> metadatas;                              // NOSONAR

  public ActivitySearchResultEntity() {
  }

  public ActivitySearchResultEntity(ActivitySearchResult activitySearchResult) {
    this.setId(String.valueOf(activitySearchResult.getId()));
    this.type = activitySearchResult.getType();
    this.body = activitySearchResult.getBody();
    this.excerpts = activitySearchResult.getExcerpts();
    this.postedTime = activitySearchResult.getPostedTime();
    this.setLastUpdatedTime(activitySearchResult.getLastUpdatedTime());
  }

  public IdentityEntity getStreamOwner() {
    return streamOwner;
  }

  public void setStreamOwner(IdentityEntity streamOwner) {
    this.streamOwner = streamOwner;
  }

  public IdentityEntity getPoster() {
    return poster;
  }

  public void setPoster(IdentityEntity poster) {
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

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public ActivitySearchResultEntity getComment() {
    return comment;
  }

  public void setComment(ActivitySearchResultEntity comment) {
    this.comment = comment;
  }

  public long getLikesCount() {
    return likesCount;
  }

  public void setLikesCount(long likesCount) {
    this.likesCount = likesCount;
  }

  public long getCommentsCount() {
    return commentsCount;
  }

  public void setCommentsCount(long commentsCount) {
    this.commentsCount = commentsCount;
  }

  public void setMetadatas(Map<String, List<MetadataItemEntity>> metadatas) {
    this.metadatas = metadatas;
  }

  public Map<String, List<MetadataItemEntity>> getMetadatas() {
    return this.metadatas;
  }

}
