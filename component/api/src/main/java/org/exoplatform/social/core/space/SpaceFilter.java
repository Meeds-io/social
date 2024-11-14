/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.common.Utils;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.constant.SpaceRegistration;
import io.meeds.social.space.constant.SpaceVisibility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceFilter implements Cloneable {

  private String                spaceNameSearchCondition;

  private List<Space>           includeSpaces;

  private List<Long>            excludedIds;

  private String                remoteId;

  private long                  identityId;

  private long                  templateId;

  private List<Long>            managingTemplateIds;

  private SpaceMembershipStatus status;

  private SpaceMembershipStatus extraStatus;

  private SpaceRegistration     registration;

  private SpaceVisibility       visibility;

  private Sorting               sorting;

  private boolean               favorite;

  private List<String>          tagNames;

  public SpaceFilter(String spaceNameSearchCondition) {
    this.spaceNameSearchCondition = Utils.processUnifiedSearchCondition(spaceNameSearchCondition);
  }

  public void setSpaceNameSearchCondition(String spaceNameSearchCondition) {
    this.spaceNameSearchCondition = Utils.processUnifiedSearchCondition(spaceNameSearchCondition);
  }

  public List<SpaceMembershipStatus> getStatusList() {
    return Stream.of(status, extraStatus).filter(Objects::nonNull).toList();
  }

  public Sorting getSorting() {
    if (sorting == null) {
      sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    }
    return sorting;
  }

  public boolean isUnifiedSearch() {
    return (favorite && identityId > 0)
           || StringUtils.isNotBlank(spaceNameSearchCondition)
           || CollectionUtils.isNotEmpty(tagNames);
  }

  @Override
  public SpaceFilter clone() { // NOSONAR
    return new SpaceFilter(spaceNameSearchCondition,
                           includeSpaces,
                           excludedIds,
                           remoteId,
                           identityId,
                           templateId,
                           managingTemplateIds,
                           status,
                           extraStatus,
                           registration,
                           visibility,
                           sorting,
                           favorite,
                           tagNames == null ? null : new ArrayList<>(tagNames));
  }

}
