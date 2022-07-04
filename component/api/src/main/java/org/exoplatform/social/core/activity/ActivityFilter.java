package org.exoplatform.social.core.activity;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityFilter {

  private boolean    isMyPosted;

  private boolean    isFavorite;

  private List<Long> spaceIds;
}
