/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.webui.activity;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * @deprecated Kept for backward compatibility for conf of other community
 *             addons deployed on Tribe
 */
@Deprecated
public abstract class BaseUIActivityBuilder {
  public BaseUIActivity populateData(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    return null;
  }

  private void initBaseUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
  }

  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
  }
}
