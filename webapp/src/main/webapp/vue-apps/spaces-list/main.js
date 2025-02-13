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
document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`;

export function init(appId, filter, canCreateSpace, isExternalFeatureEnabled, canEdit, settings, settingsSaveUrl) {
  exoi18n.loadLanguageAsync(lang, url).then(async i18n => {
    if (!settings.filterType) {
      settings.filterType = 'any';
    }
    if (!settings.categoryDepth) {
      settings.categoryDepth = 4;
    }
    if (settings.allowFilteringPerCategory !== false) {
      settings.allowFilteringPerCategory = true;
    }
    if (!settings.sortBy || settings.sortBy !== 'lastVisited') {
      settings.sortBy = 'title';
    }
    if (settings.hideQuickActions !== true) {
      settings.hideQuickActions = false;
    }
    if (!settings.nameTranslations) {
      settings.nameTranslations = {};
    }
    if (!settings.pageSize) {
      settings.pageSize = 12;
    }
    if (settings.filterType !== 'template') {
      settings.templateIds = null;
    } else if (!settings.templateIds) {
      settings.templateIds = [];
    }
    let settingsSubcategories;
    if (settings.filterType === 'category') {
      if (!settings.categoryIds) {
        settings.categoryIds = [];
      }
      settingsSubcategories = await getSubcategoryIds(settings.categoryIds);
    } else {
      settings.categoryIds = null;
    }
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        canCreateSpace,
        isExternalFeatureEnabled,
        filter: filter || 'all',
        canEdit,
        settings,
        settingsSubcategories,
        settingsSaveUrl,
        invitationsCount: 0,
        pendingCount: 0,
        requestsCount: 0,
        hover: false,
        unreadPerSpace: null,
        selectedCategoryId: null,
        selectedCategoryIds: null,
        spaceTemplates: null,
        collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
        id: Math.random() + Math.random(),
        anonymous: !eXo?.env?.portal?.userName?.length,
        displayNotPubliclyVisible: eXo.env.portal.portalName === 'public' && canEdit,
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
        categoryIds() {
          return this.settings.filterType === 'category' ? (this.settingsSubcategories || this.settings.categoryIds) : null;
        },
        templateIds() {
          return this.settings.filterType === 'template' ? this.settings.templateIds : null;
        },
        categoryDepth() {
          return this.settings.categoryDepth || 4;
        },
        hideQuickActions() {
          return this.settings.hideQuickActions;
        },
        title() {
          return this.hideQuickActions && (this.settings.nameTranslations?.[eXo.env.portal.language] || this.settings.nameTranslations?.[eXo.env.portal.defaultLanguage]);
        },
        sortBy() {
          return this.settings.sortBy;
        },
        allowFilteringPerCategory() {
          return this.settings.allowFilteringPerCategory;
        },
      },
      watch: {
        invitationsCount() {
          if (!this.invitationsCount) {
            this.filter = 'all';
          }
        },
        pendingCount() {
          if (!this.pendingCount) {
            this.filter = 'all';
          }
        },
        async selectedCategoryId() {
          if (this.selectedCategoryId) {
            this.selectedCategoryIds = await getSubcategoryIds([this.selectedCategoryId]);
          } else {
            this.selectedCategoryIds = null;
          }
        },
      },
      created() {
        this.$root.$on('spaces-list-filter-update', this.updateFilter);
        this.$root.$on('spaces-list-settings-updated', this.handleSettingsUpdate);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      beforeDestroy() {
        this.$root.$off('spaces-list-filter-update', this.updateFilter);
        this.$root.$off('spaces-list-settings-updated', this.handleSettingsUpdate);
      },
      methods: {
        updateFilter(filter) {
          this.filter = filter;
        },
        async handleSettingsUpdate() {
          this.settingsSubcategories = await getSubcategoryIds(this.settings.categoryIds);
          this.$root.$emit('spaces-list-refresh');
        },
      },
      template: `<spaces-list id="${appId}" :filter="filter" :can-create-space="${canCreateSpace}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces List');
  });
}

async function getSubcategoryIds(categoryIds) {
  if (!categoryIds?.length) {
    return [];
  }
  const subcategoyIds = await Promise.all(categoryIds.map(id => Vue.prototype.$categoryService.getSubcategoryIds(id, {
    offset: 0,
    limit: -1,
    depth: -1,
  })));
  const subcategoyIdsFlat = subcategoyIds.flatMap(s => s);
  subcategoyIdsFlat.push(...categoryIds);
  return [...new Set(subcategoyIdsFlat)];
}
