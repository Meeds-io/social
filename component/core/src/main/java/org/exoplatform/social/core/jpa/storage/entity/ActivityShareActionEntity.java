package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.*;

import jakarta.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocActivityShareAction")
@ExoEntity
@Table(name = "SOC_ACTIVITY_SHARE_ACTIONS")
@NamedQueries({
  @NamedQuery(
    name = "SocActivityShareAction.getShareActionsByActivityId",
    query = "SELECT s from SocActivityShareAction s WHERE s.activityId = :activityId ORDER BY s.id DESC"
  ),
})
public class ActivityShareActionEntity implements Serializable {

  private static final long serialVersionUID  = 4119504597873573962L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_ACTIVITY_SHARE_ACTIONS_ID", sequenceName = "SEQ_SOC_ACTIVITY_SHARE_ACTIONS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_ACTIVITY_SHARE_ACTIONS_ID")
  @Column(name = "ACTIVITY_SHARE_ACTION_ID")
  private Long              id;

  @Column(name = "ACTIVITY_ID", nullable = false)
  private Long              activityId;

  @Column(name = "TITLE")
  private String            title;

  @Column(name = "USER_ID", nullable = false)
  private Long              userId;

  @Column(name = "SHARE_DATE", nullable = false)
  private Date              shareDate;

  @ElementCollection
  @CollectionTable(
      name = "SOC_ACTIVITY_SHARE_ACTION_ACTIVITY",
      joinColumns = @JoinColumn(name = "ACTIVITY_SHARE_ACTION_ID")
  )
  @Column(name = "SHARED_ACTIVITY_ID", nullable = false)
  private Set<Long>         sharedActivityIds = new HashSet<>();

  @ElementCollection
  @CollectionTable(
      name = "SOC_ACTIVITY_SHARE_ACTION_SPACE",
      joinColumns = @JoinColumn(name = "ACTIVITY_SHARE_ACTION_ID")
  )
  @Column(name = "SHARED_SPACE_ID", nullable = false)
  private Set<Long>         sharedSpaceIds    = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getActivityId() {
    return activityId;
  }

  public void setActivityId(Long activityId) {
    this.activityId = activityId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Date getShareDate() {
    return shareDate;
  }

  public void setShareDate(Date shareDate) {
    this.shareDate = shareDate;
  }

  public Set<Long> getSharedActivityIds() {
    return sharedActivityIds;
  }

  public void setSharedActivityIds(Set<Long> sharedActivityIds) {
    this.sharedActivityIds = sharedActivityIds;
  }

  public Set<Long> getSharedSpaceIds() {
    return sharedSpaceIds;
  }

  public void setSharedSpaceIds(Set<Long> sharedSpaceIds) {
    this.sharedSpaceIds = sharedSpaceIds;
  }

}
