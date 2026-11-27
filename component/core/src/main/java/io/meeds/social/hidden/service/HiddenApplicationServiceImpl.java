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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;

import io.meeds.social.cms.utils.PageContentBlockUtils;
import io.meeds.social.hidden.model.HiddenApplication;
import io.meeds.social.hidden.plugin.HiddenApplicationPlugin;

@Service
public class HiddenApplicationServiceImpl implements HiddenApplicationService {

  public static final Scope                       HIDDEN_APPLICATIONS_SCOPE = Scope.APPLICATION.id("HiddenApplications");

  public static final Scope                       HIDE_ALLOWED_SCOPE        = Scope.APPLICATION.id("HideAllowedApplications");

  private static final Log                        LOG                       = ExoLogger.getLogger(HiddenApplicationServiceImpl.class);

  private static final int                        CLEAR_PAGE_SIZE           = 100;

  private static final String                     KEY_SEPARATOR             = ":";

  @Autowired
  private SettingService                          settingService;

  @Autowired
  private LayoutService                           layoutService;

  @Autowired
  private ResourceBundleManager                   resourceBundleManager;

  @Autowired
  private IdentityRegistry                        identityRegistry;

  // Resolved at each call, so that a plugin contributed by any add-on is found
  @Autowired
  private ObjectProvider<HiddenApplicationPlugin> plugins;

  @Override
  public void hide(String type, String name, String username) throws ObjectNotFoundException {
    HiddenApplicationPlugin plugin = getPlugin(type);
    HiddenApplication application = plugin == null ? null : plugin.getApplication(name, null);
    if (application == null) {
      throw new ObjectNotFoundException(String.format("Application %s of type %s doesn't exist", name, type));
    }
    Identity identity = getIdentity(username);
    if (identity == null || !plugin.canView(name, identity)) {
      // A window the user can't view is, for this user, a window that doesn't
      // exist: the answer tells no window name apart
      throw new ObjectNotFoundException(String.format("Application %s of type %s doesn't exist", name, type));
    } else if (!application.isAllowHide()) {
      throw new IllegalArgumentException(HIDE_NOT_ALLOWED);
    }
    // One setting per hidden window, so that two requests never overwrite each
    // other; its value orders the user's hidden windows
    settingService.set(Context.USER.id(username),
                       HIDDEN_APPLICATIONS_SCOPE,
                       toKey(type, name),
                       SettingValue.create(String.valueOf(System.currentTimeMillis())));
  }

  @Override
  public void show(String type, String name, String username) {
    if (StringUtils.isBlank(username)) {
      return;
    }
    settingService.remove(Context.USER.id(username), HIDDEN_APPLICATIONS_SCOPE, toKey(type, name));
  }

  @Override
  public boolean isHidden(String type, String name, String username) {
    return StringUtils.isNotBlank(username)
           && settingService.get(Context.USER.id(username), HIDDEN_APPLICATIONS_SCOPE, toKey(type, name)) != null;
  }

  @Override
  public List<HiddenApplication> getHiddenApplications(String username, String language) {
    Identity identity = getIdentity(username);
    if (identity == null) {
      return Collections.emptyList();
    }
    return getHiddenKeys(username).stream()
                                              .map(key -> getListedHiddenApplication(key, identity, language))
                                              .filter(Objects::nonNull)
                                              .toList();
  }

  @Override
  public void clearHiddenChoices(String type, String name) {
    String key = toKey(type, name);
    Set<String> removedUsers = new HashSet<>();
    List<Context> contexts;
    boolean removed;
    do {
      // Each pass reads from the start: the removals of the previous pass
      // shifted the remaining contexts there
      contexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                        HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                        HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                        key,
                                                                        0,
                                                                        CLEAR_PAGE_SIZE);
      removed = false;
      for (Context context : contexts) {
        // A pass reading only users already cleared means the removals did not
        // take: stop rather than read the same page again
        if (removedUsers.add(context.getId())) {
          settingService.remove(Context.USER.id(context.getId()), HIDDEN_APPLICATIONS_SCOPE, key);
          removed = true;
        }
      }
    } while (removed && contexts.size() == CLEAR_PAGE_SIZE);
  }

  @Override
  public void copyHiddenChoices(String type, String fromName, String toName) {
    String fromKey = toKey(type, fromName);
    String toKey = toKey(type, toName);
    if (StringUtils.isBlank(fromName) || StringUtils.isBlank(toName) || fromKey.equals(toKey)) {
      return;
    }
    int offset = 0;
    List<Context> contexts;
    do {
      // Reading only: the choices of the source stay, so the pages are read
      // one after the other
      contexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                        HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                        HIDDEN_APPLICATIONS_SCOPE.getId(),
                                                                        fromKey,
                                                                        offset,
                                                                        CLEAR_PAGE_SIZE);
      for (Context context : contexts) {
        Context userContext = Context.USER.id(context.getId());
        SettingValue<?> hiddenAt = settingService.get(userContext, HIDDEN_APPLICATIONS_SCOPE, fromKey);
        if (hiddenAt != null) {
          settingService.set(userContext, HIDDEN_APPLICATIONS_SCOPE, toKey, hiddenAt);
        }
      }
      offset += contexts.size();
    } while (contexts.size() == CLEAR_PAGE_SIZE);
  }

  @Override
  public void setHideAllowed(String type, String name, boolean allowed) {
    String key = toKey(type, name);
    if (allowed) {
      settingService.set(Context.GLOBAL, HIDE_ALLOWED_SCOPE, key, SettingValue.create(Boolean.TRUE.toString()));
    } else if (isHideAllowed(type, name)) {
      // Turned off, the option clears every user's choice: turned on again,
      // the window shows to everyone until they hide it again. The option is
      // removed last, so that a failure in between leaves it on, and saving
      // it off again clears the remaining choices
      clearHiddenChoices(type, name);
      settingService.remove(Context.GLOBAL, HIDE_ALLOWED_SCOPE, key);
    }
  }

  @Override
  public boolean isHideAllowed(String type, String name) {
    return settingService.get(Context.GLOBAL, HIDE_ALLOWED_SCOPE, toKey(type, name)) != null;
  }

  /**
   * A window that fails to resolve, in a plugin or in its page reference, is
   * left out of the list: the user's other hidden windows stay listed, and
   * shown again from it
   */
  private HiddenApplication getListedHiddenApplication(String key, Identity identity, String language) {
    try {
      return getHiddenApplication(key, identity, language);
    } catch (RuntimeException e) {
      LOG.warn("Hidden application {} of user {} can't be listed", key, identity.getUserId(), e);
      return null;
    }
  }

  private HiddenApplication getHiddenApplication(String key, Identity identity, String language) {
    String type = StringUtils.substringBefore(key, KEY_SEPARATOR);
    String name = StringUtils.substringAfter(key, KEY_SEPARATOR);
    HiddenApplicationPlugin plugin = getPlugin(type);
    if (plugin == null || StringUtils.isBlank(name)) {
      return null;
    }
    HiddenApplication application = plugin.getApplication(name, language);
    if (application == null
        || !application.isAllowHide()
        || !plugin.canView(name, identity)) {
      return null;
    }
    application.setType(type);
    application.setName(name);
    // A window with no page reference is not on a page, the shared layout of
    // a site for instance: its plugin answers for it
    if (StringUtils.isNotBlank(application.getPageReference())) {
      Page page = layoutService.getPage(PageKey.parse(application.getPageReference()));
      // A setting outlives the window an editor removed from its page
      if (page == null || !PageContentBlockUtils.hasWidgetWithSettingName(layoutService, page, name)) {
        return null;
      }
      application.setPageTitle(page.getTitle());
      application.setSiteLabel(getSiteLabel(application.getPageReference(), language));
    }
    return application;
  }

  private List<String> getHiddenKeys(String username) {
    Map<String, SettingValue> settings = settingService.getSettingsByContextAndScope(Context.USER.getName(),
                                                                                     username,
                                                                                     HIDDEN_APPLICATIONS_SCOPE.getName(),
                                                                                     HIDDEN_APPLICATIONS_SCOPE.getId());
    if (MapUtils.isEmpty(settings)) {
      return Collections.emptyList();
    }
    return settings.entrySet()
                   .stream()
                   .sorted(Comparator.comparingLong(entry -> NumberUtils.toLong(String.valueOf(entry.getValue().getValue()))))
                   .map(Entry::getKey)
                   .toList();
  }

  private String getSiteLabel(String pageReference, String language) {
    SiteKey siteKey = PageKey.parse(pageReference).getSite();
    PortalConfig portalConfig = layoutService.getPortalConfig(siteKey);
    if (portalConfig == null) {
      return null;
    }
    String label = portalConfig.getLabel();
    if (ExpressionUtil.isResourceBindingExpression(label)) {
      ResourceBundle bundle = resourceBundleManager.getNavigationResourceBundle(StringUtils.isBlank(language) ? Locale.ENGLISH.toLanguageTag() :
                                                                                                             language,
                                                                                 siteKey.getTypeName(),
                                                                                 siteKey.getName());
      label = bundle == null ? null : ExpressionUtil.getExpressionValue(bundle, label);
    }
    return StringUtils.isBlank(label) || ExpressionUtil.isResourceBindingExpression(label) ? portalConfig.getName() : label;
  }

  private HiddenApplicationPlugin getPlugin(String type) {
    return StringUtils.isBlank(type) ? null :
                                     plugins.stream()
                                            .filter(plugin -> StringUtils.equals(type, plugin.getApplicationType()))
                                            .findFirst()
                                            .orElse(null);
  }

  private Identity getIdentity(String username) {
    return StringUtils.isBlank(username) ? null : identityRegistry.getIdentity(username);
  }

  private String toKey(String type, String name) {
    return type + KEY_SEPARATOR + name;
  }

}
