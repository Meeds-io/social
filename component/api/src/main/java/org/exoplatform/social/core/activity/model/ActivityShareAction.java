package org.exoplatform.social.core.activity.model;

import java.util.Set;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityShareAction {

  private Long      id;

  private long      activityId;

  private String    message;

  private long      userIdentityId;

  private long      shareDate;

  private Set<Long> spaceIds;

  private Set<Long> sharedActivityIds;

}
