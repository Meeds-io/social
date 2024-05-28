/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.PortletStatus;
import org.gatein.pc.api.info.CacheInfo;
import org.gatein.pc.api.info.CapabilitiesInfo;
import org.gatein.pc.api.info.EventingInfo;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.info.PreferencesInfo;
import org.gatein.pc.api.info.RuntimeOptionInfo;
import org.gatein.pc.api.info.SecurityInfo;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;

public class PortletInvokerMock implements PortletInvoker {

  @Override
  public Set<Portlet> getPortlets() throws PortletInvokerException {
    return new HashSet<>(Arrays.asList(
                                       new PortletImpl(new PortletInfoImpl("dashboard", "DashboardPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "SpaceActivityStreamPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "SpaceSettingPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "MembersPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "ForumPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "CalendarPortlet")),
                                       new PortletImpl(new PortletInfoImpl("social", "AnswersPortlet"))));
  }

  @Override
  public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException {
    return null;
  }

  @Override
  public PortletStatus getStatus(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException {
    return null;
  }

  @Override
  public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException {
    return null;
  }

  @Override
  public PortletContext createClone(PortletStateType stateType, PortletContext portletContext) throws IllegalArgumentException,
                                                                                               PortletInvokerException,
                                                                                               UnsupportedOperationException {
    return null;
  }

  @Override
  public List<DestroyCloneFailure> destroyClones(List<PortletContext> portletContexts) throws IllegalArgumentException,
                                                                                       PortletInvokerException,
                                                                                       UnsupportedOperationException {
    return null;
  }

  @Override
  public PropertyMap getProperties(PortletContext portletContext, Set<String> keys) throws IllegalArgumentException,
                                                                                    PortletInvokerException,
                                                                                    UnsupportedOperationException {
    return null;
  }

  @Override
  public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException,
                                                                  PortletInvokerException,
                                                                  UnsupportedOperationException {
    return null;
  }

  @Override
  public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes) throws IllegalArgumentException,
                                                                                               PortletInvokerException,
                                                                                               UnsupportedOperationException {
    return null;
  }

  @Override
  public PortletContext exportPortlet(PortletStateType stateType,
                                      PortletContext originalPortletContext) throws PortletInvokerException {
    return null;
  }

  @Override
  public PortletContext importPortlet(PortletStateType stateType, PortletContext contextToImport) throws PortletInvokerException {
    return null;
  }

  @Override
  public boolean isActive() {
    return false;
  }

  public static class MetaInfoImpl implements MetaInfo {
    @Override
    public LocalizedString getMetaValue(String key) {
      return null;
    }
  }

  public static class PortletInfoImpl implements PortletInfo {

    private MetaInfoImpl meta = new MetaInfoImpl();

    private String       applicationName;

    private String       name;

    public PortletInfoImpl(String applicationName, String name) {
      this.applicationName = applicationName;
      this.name = name;
    }

    @Override
    public String getApplicationName() {
      return applicationName;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public MetaInfo getMeta() {
      return meta;
    }

    @Override
    public CapabilitiesInfo getCapabilities() {
      return null;
    }

    @Override
    public PreferencesInfo getPreferences() {
      return null;
    }

    @Override
    public SecurityInfo getSecurity() {
      return null;
    }

    @Override
    public CacheInfo getCache() {
      return null;
    }

    @Override
    public EventingInfo getEventing() {
      return null;
    }

    @Override
    public NavigationInfo getNavigation() {
      return null;
    }

    @Override
    public <T> T getAttachment(Class<T> type) throws IllegalArgumentException {
      return null;
    }

    @Override
    public Map<String, RuntimeOptionInfo> getRuntimeOptionsInfo() {
      return null;
    }

  }

  public static class PortletImpl implements Portlet {

    private PortletInfo info;

    public PortletImpl(PortletInfo info) {
      this.info = info;
    }

    public PortletContext getContext() {
      return null;
    }

    public PortletInfo getInfo() {
      return info;
    }

    public boolean isRemote() {
      return false;
    }
  }

}
