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
package org.exoplatform.social.core.space;

import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

/**
 * Base class for a manageable space listener plugin.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class SpaceListenerPlugin implements
        SpaceLifeCycleListener {

  /**
   * {@inheritDoc}
   */
  public abstract void spaceCreated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void spaceRemoved(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationActivated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationAdded(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationDeactivated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationRemoved(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void grantedLead(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void joined(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void left(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void revokedLead(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public abstract void spaceRenamed(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public abstract void spaceDescriptionEdited(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public abstract void spaceAvatarEdited(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public abstract void addInvitedUser(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public abstract void addPendingUser(SpaceLifeCycleEvent event);

}
