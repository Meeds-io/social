package org.exoplatform.social.core.activity;

import lombok.*;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityFilter {

  private boolean    isMyPosted;

  private boolean    isFavorite;

  private List<Long> spaceIds;

  public boolean isEmpty() {
    return !isMyPosted && !isFavorite && spaceIds.isEmpty();
  }
}
