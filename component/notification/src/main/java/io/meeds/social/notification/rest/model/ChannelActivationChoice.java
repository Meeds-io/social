/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.notification.rest.model;

public class ChannelActivationChoice {

  private String  channelId;

  private String  pluginId;

  private boolean allowed;

  private boolean active;

  private boolean channelActive;

  public ChannelActivationChoice(String channelId,
                                 String pluginId,
                                 boolean allowed,
                                 boolean active,
                                 boolean channelActive) {
    this.channelId = channelId;
    this.pluginId = pluginId;
    this.allowed = allowed;
    this.active = active;
    this.channelActive = channelActive;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public void setAllowed(boolean allowed) {
    this.allowed = allowed;
  }

  public boolean isChannelActive() {
    return channelActive;
  }

  public void setChannelActive(boolean channelActive) {
    this.channelActive = channelActive;
  }

}
