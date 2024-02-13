package org.exoplatform.social.core.space.impl;

import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceRenamedListenerImpl extends SpaceListenerPlugin {
  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    SpaceUtils.renamePageNode(space);
    SpaceUtils.renameGroupLabel(space);
    SpaceUtils.updateDefaultSpaceAvatar(space);
  }
}
