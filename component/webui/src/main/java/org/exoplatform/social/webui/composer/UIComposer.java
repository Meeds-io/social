/*
getActivityComposers * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.webui.composer;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 *
 * @author    zun
 * @since   Apr 6, 2010
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/composer/UIComposer.gtmpl"
)
public class UIComposer extends UIForm {

  private static Log LOG = ExoLogger.getLogger(UIComposer.class);

  public enum PostContext {
    SPACE,
    USER,
    SINGLE
  }

  
  /**
   * Constructor
   * @throws Exception
   */
  public UIComposer() throws Exception {
    if(this.getId() == null) this.setId("UIComposer");
  }
  
  public String getSpaceGroupId() {
    Space space = SpaceUtils.getSpaceByContext();
    return space == null ? null : space.getGroupId();
  }
}
