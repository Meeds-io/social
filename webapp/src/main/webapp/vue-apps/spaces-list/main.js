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

// Page URL parameters carrying the search and the selected category, so that a
// filtered directory can be shared
const KEYWORD_PARAM = 'keyword';
const CATEGORY_PARAM = 'category';

export function init(
  appId,
  filter,
  canCreateSpace,
  isExternalFeatureEnabled,
  canEdit,
  settings,
  settingsSaveUrl,
  settingNameUrl,
  settingName,
  registrationType,
  isAdministrator,
  isPublicPage) {

  // Read before any asynchronous load: a search drawer stuck to a side
  // replaces the page URL with its own once it opens
  const urlParams = new URLSearchParams(window.location.search);

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
    let settingsSubcategoryIds;
    let settingsExcludeSubcategoryIds;
    if (settings.filterType === 'category') {
      if (!settings.categoryIds) {
        settings.categoryIds = [];
      }
      if (!settings?.excludeCategoryIds) {
        settings.excludeCategoryIds = [];
      }
      settingsSubcategoryIds = await getSubcategoryIds(settings.categoryIds, 1, settingName);
      settingsExcludeSubcategoryIds = await getSubcategoryIds(settings.excludeCategoryIds, 1, settingName);
    } else {
      settings.categoryIds = null;
    }
    const urlKeyword = urlParams.get(KEYWORD_PARAM)?.trim() || null;
    const urlCategoryId = await getUrlCategoryId(urlParams.get(CATEGORY_PARAM), settings, settingsSubcategoryIds, settingsExcludeSubcategoryIds, settingName);
    const urlCategoryIds = urlCategoryId && await getSubcategoryIds([urlCategoryId], -1, settingName).catch(() => null);
    const selectedCategoryId = urlCategoryIds?.length ? urlCategoryId : null;
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        canCreateSpace,
        isExternalFeatureEnabled,
        filter: filter || 'all',
        canEdit,
        settings,
        settingsSubcategoryIds,
        settingsExcludeSubcategoryIds,
        settingsSaveUrl,
        settingNameUrl,
        settingName,
        isAdministrator,
        isPublicPage,
        registrationType,
        invitationsCount: 0,
        pendingCount: 0,
        requestsCount: 0,
        hover: false,
        unreadPerSpace: null,
        keyword: urlKeyword,
        selectedCategoryId,
        selectedCategoryIds: selectedCategoryId ? urlCategoryIds : null,
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
        filterType() {
          return this.settings.filterType;
        },
        categoryDepth() {
          return this.settings.categoryDepth || 4;
        },
        categoryIds() {
          return this.filterType === 'category' ? (this.settingsSubcategoryIds || this.settings.categoryIds) : null;
        },
        excludeCategoryIds() {
          return this.filterType === 'category' ? (this.settingsExcludeSubcategoryIds || this.settings.excludeCategoryIds) : null;
        },
        templateIds() {
          return this.filterType === 'template' ? this.settings.templateIds : null;
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
        keyword() {
          this.updateUrl(KEYWORD_PARAM, this.keyword?.trim());
        },
        async selectedCategoryId() {
          this.updateUrl(CATEGORY_PARAM, this.selectedCategoryId);
          if (this.selectedCategoryId) {
            this.selectedCategoryIds = await getSubcategoryIds([this.selectedCategoryId], -1, this.settingName);
          } else if (this.filterType === 'category') {
            this.selectedCategoryIds = await getSubcategoryIds(this.settings.categoryIds || [], -1, this.settingName);
          } else {
            this.selectedCategoryIds = [];
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
        // Rewrites only the parameter that changed: Spaces directories placed on
        // one page share the page URL, so a change in one keeps the other's
        updateUrl(name, value) {
          const pageUrl = new URL(window.location.href);
          setUrlParam(pageUrl.searchParams, name, value);
          if (pageUrl.href !== window.location.href) {
            window.history.replaceState(window.history.state, '', pageUrl.href);
          }
        },
        async handleSettingsUpdate() {
          this.settings = JSON.parse(JSON.stringify(this.settings)); // Force update
          if (this.filterType === 'category') {
            this.settingsSubcategoryIds = await getSubcategoryIds(this.settings.categoryIds, 1, this.settingName);
            this.settingsExcludeSubcategoryIds = await getSubcategoryIds(this.settings.excludeCategoryIds, 1, this.settingName);
            this.selectedCategoryIds = await getSubcategoryIds(this.settings.categoryIds || [], -1, this.settingName);
          } else {
            this.settingsSubcategoryIds = [];
            this.selectedCategoryIds = [];
          }
          this.$root.$emit('spaces-list-refresh');
        },
      },
      template: `<spaces-list id="${appId}" :filter="filter" :can-create-space="${canCreateSpace}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces List');
  });
}

async function getSubcategoryIds(categoryIds, depth, token) {
  if (!categoryIds?.length) {
    return [];
  }
  const subcategoyIds = await Promise.all(categoryIds.map(id => Vue.prototype.$categoryService.getSubcategoryIds(id, {
    offset: 0,
    limit: -1,
    depth,
    token,
  })));
  const subcategoyIdsFlat = subcategoyIds.flatMap(s => s);
  subcategoyIdsFlat.push(...categoryIds);
  return [...new Set(subcategoyIdsFlat)];
}

function setUrlParam(params, name, value) {
  if (value) {
    params.set(name, value);
  } else {
    params.delete(name);
  }
}

// The category given in the page URL, kept only when the category filter of this
// directory displays it, so a shared URL never widens a pre-filtered directory;
// null when absent, not an id, out of the filter or not retrievable
async function getUrlCategoryId(value, settings, categoryIds, excludeCategoryIds, token) {
  const categoryId = /^[1-9]\d*$/.test(value || '') && Number(value);
  if (!settings.allowFilteringPerCategory || !Number.isSafeInteger(categoryId)) {
    return null;
  }
  try {
    const displayedCategoryIds = await getDisplayedCategoryIds(settings, categoryIds, excludeCategoryIds, token);
    return displayedCategoryIds.includes(categoryId) ? categoryId : null;
  } catch (e) {
    return null;
  }
}

// The ids of the categories the categories-filter component displays for these
// settings, read the way CategoriesFilter.init and addSubcategories read them
async function getDisplayedCategoryIds(settings, categoryIds, excludeCategoryIds, token) {
  const options = {
    objectType: 'space',
    depth: settings.categoryDepth,
    offset: 0,
    limit: -1,
    token,
  };
  const filterCategoryIds = settings.filterType === 'category' ? (categoryIds || settings.categoryIds) : null;
  const filterExcludeCategoryIds = settings.filterType === 'category' ? (excludeCategoryIds || settings.excludeCategoryIds) : null;
  let categories;
  if (filterCategoryIds?.length) {
    categories = (await Promise.all(filterCategoryIds.map(parentId => Vue.prototype.$categoryService.getCategoryTree({
      ...options,
      parentId,
    }).catch(() => null)))).filter(c => c);
  } else {
    categories = (await Vue.prototype.$categoryService.getCategoryTree(options))?.categories || [];
  }
  const displayedCategoryIds = [];
  const addCategories = list => list
    .filter(category => category && !filterExcludeCategoryIds?.includes?.(category.id))
    .forEach(category => {
      displayedCategoryIds.push(category.id);
      addCategories(category.categories || []);
    });
  addCategories(categories);
  return displayedCategoryIds;
}
