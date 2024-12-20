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
import './initComponents.js';
import './extensions.js';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const lang = eXo && eXo.env.portal.language || 'en';
const url = `/social/i18n/locale.portlet.CategoryManagement?lang=${lang}`;
const appId = 'CategoryManagement';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
        categoryTree: null,
        categoryOwnerId: null,
        categoryRootId: null,
        extensionApp: 'category-management',
        menuItemExtensionType: 'menu-item',
        guestsPermission: '/platform/externals',
        usersPermission: '/platform/users',
        administratorsPermission: '/platform/administrators',
        identityIdPerGroup: {},
        groupPerIdentityId: {},
        depth: 4,
        pageSize: 10,
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
        chevonIcon() {
          return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
        },
        categories() {
          const categories = [];
          if (this.categoryTree) {
            this.addSubcategories(this.categoryTree, categories);
          }
          return categories;
        },
      },
      watch: {
        categories() {
          this.categories?.forEach?.(cat => {
            if (!cat.categories) {
              cat.categories = [];
            }
          });
        },
      },
      created() {
        document.addEventListener(`extension-${this.extensionApp}-${this.menuItemExtensionType}-updated`, this.refreshMenuExtensions);
        this.refreshMenuExtensions();
      },
      beforeDestroy() {
        document.removeEventListener(`extension-${this.extensionApp}-${this.menuItemExtensionType}-updated`, this.refreshMenuExtensions);
      },
      methods: {
        refreshMenuExtensions() {
          this.menuItemExtensions = extensionRegistry.loadExtensions(this.extensionApp, this.menuItemExtensionType);
        },
        async loadChildren(item) {
          const category = this.getCategory(item.id);
          if (category.limit) {
            return category.categories;
          } else {
            return await this.refreshTree(item, 1);
          }
        },
        async refreshTree(item, depth, offset, limit) {
          const parentId = item?.id || this.categoryRootId || 0;
          const ownerId = item?.ownerId || this.categoryOwnerId || 0;
          const categoryTree = await this.$categoryService.getCategoryTree({
            parentId,
            ownerId,
            depth,
            offset: offset || 0,
            limit: limit || this.pageSize,
          });
          if (!parentId) {
            this.categoryTree = categoryTree;
            return categoryTree;
          } else {
            Object.keys(categoryTree).forEach(key => item[key] = categoryTree[key]);
            return item;
          }
        },
        async loadMore(id) {
          const category = this.getCategory(id);
          category.loading = true;
          category.limit += this.pageSize;
          try {
            await this.refreshTree(category, Math.max(this.depth - category.depth, 1), 0, category.limit);
          } finally {
            category.loading = false;
          }
        },
        getCategory(id) {
          if (id === this.categoryTree?.id) {
            return this.categoryTree;
          }
          return this.categories.find(cat => cat.id === id);
        },
        addSubcategories(item, result, depth, itemIndex) {
          if (!item) {
            return;
          }
          item.index = itemIndex || 0;
          result.push(item);
          item.depth = depth || 0;
          if (item?.categories) {
            item.categories.forEach((cat, index) => this.addSubcategories(cat, result, item.depth + 1, index));
            if (item.size > item.limit && !item.categories.find(i => i.loadMore)) {
              item.categories.push({
                id: item.id + 100000,
                parentId: item.id,
                loadMore: true,
              });
            }
          }
        },
      },
      template: `<category-management id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Category Management');
  });
}