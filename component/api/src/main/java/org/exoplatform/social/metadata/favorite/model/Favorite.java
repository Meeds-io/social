package org.exoplatform.social.metadata.favorite.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {

  private String objectType;

  private String objectId;

  private String parentObjectId;

  private long   userIdentityId;

}
