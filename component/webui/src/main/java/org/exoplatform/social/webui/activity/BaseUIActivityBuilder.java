/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.webui.activity;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * BaseUIActivityBuilder.java
 *
 * @author    zun
 * @since     Jul 22, 2010
 */
public abstract class BaseUIActivityBuilder {
  public BaseUIActivity populateData(BaseUIActivity uiActivity, ExoSocialActivity activity){
    initBaseUIActivity(uiActivity, activity);
    extendUIActivity(uiActivity, activity);
    return uiActivity;
  }

  private void initBaseUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    uiActivity.setActivity(activity);
  }

  protected abstract void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity);
}