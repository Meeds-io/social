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

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.UserSetting;

import lombok.Getter;
import lombok.Setter;

public class UserNotificationSettings {
  private List<GroupProvider>           groups;

  private Map<String, String>           groupsLabels;

  private Map<String, String>           pluginLabels;

  private Map<String, String>           channelLabels;

  private Map<String, String>           channelDescriptions;

  private Map<String, String>           digestLabels;

  private Map<String, String>           digestDescriptions;

  private boolean                       hasActivePlugin     = false;

  private List<EmailDigestChoice>       emailDigestChoices  = null;

  private List<ChannelActivationChoice> channelCheckBoxList = null;

  private Map<String, Boolean>          channelStatus;

  private List<String>                  channels;

  private List<Long>                    mutedSpaces;

  private String                        emailChannel        = UserSetting.EMAIL_CHANNEL;

  @Getter
  @Setter
  private String                        senderEmail;

  @Getter
  @Setter
  private String                        senderName;

  public UserNotificationSettings(List<GroupProvider> groups, // NOSONAR
                                  Map<String, String> groupsLabels,
                                  Map<String, String> pluginLabels,
                                  Map<String, String> channelLabels,
                                  Map<String, String> channelDescriptions,
                                  Map<String, String> digestLabels,
                                  Map<String, String> digestDescriptions,
                                  boolean hasActivePlugin,
                                  List<EmailDigestChoice> emailDigestChoices,
                                  List<ChannelActivationChoice> channelCheckBoxList,
                                  Map<String, Boolean> channelStatus,
                                  List<String> channels,
                                  List<Long> mutedSpaces) {
    this.groups = groups;
    this.groupsLabels = groupsLabels;
    this.pluginLabels = pluginLabels;
    this.channelLabels = channelLabels;
    this.channelDescriptions = channelDescriptions;
    this.digestLabels = digestLabels;
    this.digestDescriptions = digestDescriptions;
    this.hasActivePlugin = hasActivePlugin;
    this.emailDigestChoices = emailDigestChoices;
    this.channelCheckBoxList = channelCheckBoxList;
    this.channelStatus = channelStatus;
    this.channels = channels;
    this.mutedSpaces = mutedSpaces;
  }

  public List<GroupProvider> getGroups() {
    return groups;
  }

  public void setGroups(List<GroupProvider> groups) {
    this.groups = groups;
  }

  public Map<String, String> getChannelLabels() {
    return channelLabels;
  }

  public void setChannelLabels(Map<String, String> channelLabels) {
    this.channelLabels = channelLabels;
  }

  public Map<String, String> getChannelDescriptions() {
    return channelDescriptions;
  }

  public void setChannelDescriptions(Map<String, String> channelDescriptions) {
    this.channelDescriptions = channelDescriptions;
  }

  public Map<String, String> getDigestLabels() {
    return digestLabels;
  }

  public void setDigestLabels(Map<String, String> digestLabels) {
    this.digestLabels = digestLabels;
  }

  public Map<String, String> getDigestDescriptions() {
    return digestDescriptions;
  }

  public void setDigestDescriptions(Map<String, String> digestDescriptions) {
    this.digestDescriptions = digestDescriptions;
  }

  public boolean isHasActivePlugin() {
    return hasActivePlugin;
  }

  public void setHasActivePlugin(boolean hasActivePlugin) {
    this.hasActivePlugin = hasActivePlugin;
  }

  public List<EmailDigestChoice> getEmailDigestChoices() {
    return emailDigestChoices;
  }

  public void setEmailDigestChoices(List<EmailDigestChoice> emailDigestChoices) {
    this.emailDigestChoices = emailDigestChoices;
  }

  public Map<String, String> getGroupsLabels() {
    return groupsLabels;
  }

  public void setGroupsLabels(Map<String, String> groupsLabels) {
    this.groupsLabels = groupsLabels;
  }

  public Map<String, String> getPluginLabels() {
    return pluginLabels;
  }

  public void setPluginLabels(Map<String, String> pluginLabels) {
    this.pluginLabels = pluginLabels;
  }

  public List<ChannelActivationChoice> getChannelCheckBoxList() {
    return channelCheckBoxList;
  }

  public void setChannelCheckBoxList(List<ChannelActivationChoice> channelCheckBoxList) {
    this.channelCheckBoxList = channelCheckBoxList;
  }

  public Map<String, Boolean> getChannelStatus() {
    return channelStatus;
  }

  public void setChannelStatus(Map<String, Boolean> channelStatus) {
    this.channelStatus = channelStatus;
  }

  public List<String> getChannels() {
    return channels;
  }

  public void setChannels(List<String> channels) {
    this.channels = channels;
  }

  public String getEmailChannel() {
    return emailChannel;
  }

  public void setEmailChannel(String emailChannel) {
    this.emailChannel = emailChannel;
  }

  public List<Long> getMutedSpaces() {
    return mutedSpaces;
  }

  public void setMutedSpaces(List<Long> mutedSpaces) {
    this.mutedSpaces = mutedSpaces;
  }
}
