/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.hidden.service;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.hidden.model.HiddenApplication;

/**
 * Lets each user hide, for themselves only and on every device, a page
 * application window that allows it, then show it again. The window types are
 * contributed by {@link io.meeds.social.hidden.plugin.HiddenApplicationPlugin}
 * beans.
 */
public interface HiddenApplicationService {

  /** Message code of the refusal to hide a window that doesn't allow it */
  String HIDE_NOT_ALLOWED = "hiddenApplication.hideNotAllowed";

  /**
   * Hides a window from its page for one user
   *
   * @param type application type
   * @param name window name
   * @param username user name
   * @throws ObjectNotFoundException when the type or the window doesn't exist,
   *           or the user can't view the window
   * @throws IllegalArgumentException with {@link #HIDE_NOT_ALLOWED} when the
   *           window doesn't allow hiding
   */
  void hide(String type, String name, String username) throws ObjectNotFoundException;

  /**
   * Shows again, for one user, a window this user hid
   *
   * @param type application type
   * @param name window name
   * @param username user name, nothing done when blank
   */
  void show(String type, String name, String username);

  /**
   * @param type application type
   * @param name window name
   * @param username user name, null for an anonymous user
   * @return true when the user hid the window. The caller combines it with
   *         whether the window still allows hiding.
   */
  boolean isHidden(String type, String name, String username);

  /**
   * @param username user name
   * @param language language of the returned titles and site labels
   * @return the windows the user hid which still allow hiding, are still
   *         visible to the user and, for a window on a page, are still on
   *         that page, in the order they were hidden. A window that fails to
   *         resolve is left out and logged.
   */
  List<HiddenApplication> getHiddenApplications(String username, String language);

  /**
   * Shows a window again to every user who hid it, called when its editor
   * turns its option off. It checks nothing: the caller checks that the user
   * may edit the window.
   *
   * @param type application type
   * @param name window name
   */
  void clearHiddenChoices(String type, String name);

  /**
   * Hides a window for every user who hid another one, keeping the order they
   * hid it in: called when a window's settings are copied under a new name
   * that the window will carry, so that its users' choices follow it. It
   * checks nothing: the caller checks that the user may edit the window and
   * read the source. The users are read page after page by offset, from a
   * query with no order: a user who shows the source again while the copy
   * runs can shift a later user out of it, whose choice is then not copied.
   *
   * @param type application type
   * @param fromName name of the window the users hid
   * @param toName name of the window to hide for them
   */
  void copyHiddenChoices(String type, String fromName, String toName);

  /**
   * Stores whether a window allows each user to hide it, for an application
   * type that keeps no such option of its own. Turning it off shows the window
   * again to every user who hid it: the choices are cleared first and the
   * option removed last, so that a failed clear leaves it on and saving it off
   * again clears the rest. The caller checks that the user may edit the
   * window. A hide that read the option on before it was turned off may
   * still write its choice after the clear; it hides the window again once
   * the option is turned back on.
   *
   * @param type application type
   * @param name window name
   * @param allowed whether the window allows hiding
   */
  void setHideAllowed(String type, String name, boolean allowed);

  /**
   * @param type application type
   * @param name window name
   * @return the option stored with {@link #setHideAllowed(String, String, boolean)},
   *         false when never stored
   */
  boolean isHideAllowed(String type, String name);

}
