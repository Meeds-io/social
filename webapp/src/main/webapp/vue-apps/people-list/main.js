/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `/social/i18n/locale.portlet.social.PeopleListApplication?lang=${lang}`;

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

export function init({appId, settings, saveSettingsUrl, settingsResourceUrl, usersResourceUrl, attributeValuesResourceUrl, groupsResourceUrl}) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      data: {
        appId,
        canEdit: !!settings?.canEdit,
        filter: settings?.filter || 'all',
        headerTranslations: settings?.headerTranslations || {},
        pageSize: settings?.pageSize || 12,
        // the attribute filter as the settings drawer shows it: an editor's only
        attributes: settings?.attributes || [],
        attributeFilterInvalid: !!settings?.attributeFilterInvalid,
        // the group filter as the settings drawer shows it: an editor's only,
        // and only the groups the editor can select, never the others' names
        canSetGroupFilter: !!settings?.canSetGroupFilter,
        groups: settings?.groups || [],
        otherGroupsApplied: !!settings?.otherGroupsApplied,
        // how many of them: they count against the bound of the filter
        otherGroupsCount: settings?.otherGroupsCount || 0,
        unresolvableGroups: !!settings?.unresolvableGroups,
        directoryFiltered: !!settings?.directoryFiltered,
        saveSettingsUrl,
        settingsResourceUrl,
        usersResourceUrl,
        attributeValuesResourceUrl,
        groupsResourceUrl,
        language: eXo?.env?.portal?.language,
        defaultLanguage: eXo?.env?.portal?.defaultLanguage,
        hover: false,
        focusWithin: false,
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
        // the custom header in the viewer's language, else the default
        // language's, else any stored label; none when the header is OFF
        headerTitle() {
          return this.headerTranslations?.[this.language]
            || this.headerTranslations?.[this.defaultLanguage]
            || Object.values(this.headerTranslations || {}).find(Boolean)
            || null;
        },
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      methods: {
        // re-reads the stored settings: a refused save is answered with a
        // 200 by the portal, so only the stored values tell what was applied
        refreshSettings() {
          return fetch(this.settingsResourceUrl, {
            method: 'GET',
            credentials: 'include',
          }).then(resp => {
            if (!resp?.ok) {
              throw new Error('Error while reading the people directory settings');
            }
            return resp.json();
          }).then(stored => {
            this.headerTranslations = stored?.headerTranslations || {};
            this.pageSize = stored?.pageSize || 12;
            this.attributes = stored?.attributes || [];
            this.attributeFilterInvalid = !!stored?.attributeFilterInvalid;
            this.canSetGroupFilter = !!stored?.canSetGroupFilter;
            this.groups = stored?.groups || [];
            this.otherGroupsApplied = !!stored?.otherGroupsApplied;
            this.otherGroupsCount = stored?.otherGroupsCount || 0;
            this.unresolvableGroups = !!stored?.unresolvableGroups;
            this.directoryFiltered = !!stored?.directoryFiltered;
          });
        },
      },
      template: `<people-list id="${appId}" filter="${settings?.filter || 'all'}"></people-list>`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'People List');
  });
}
