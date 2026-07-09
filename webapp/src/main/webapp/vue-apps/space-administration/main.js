/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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
import './services.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesAdministration');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

export function init(isExternalFeatureEnabled) {
  const appId = 'spacesAdministration';
  const lang = eXo?.env?.portal?.language || 'en';
  exoi18n.loadLanguageAsync(lang, [
    `/social/i18n/locale.portlet.social.SpacesAdministrationPortlet?lang=${lang}`,
    `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`,
    `/social/i18n/locale.portlet.SpaceTemplatesManagement?lang=${lang}`,
    `/social/i18n/locale.portal.webui?lang=${lang}`,
  ])
    .then(i18n => Vue.createApp({
      data: {
        isExternalFeatureEnabled,
        spaceTemplates: null,
        mainExtensions: [],
        tableColumnExtensions: [],
        itemMenuExtensions: [],
        bulkExtensions: [],
        usersPermission: '/platform/users',
        externalsPermission: '/platform/externals',
        administratorsPermission: '/platform/administrators',
        collator: new Intl.Collator(eXo.env.portal.language, {
          numeric: true,
          sensitivity: 'base'
        }),
        allSpacesSelected: false,
        selectedSpaces: [],
        selectedTemplateId: '0',
        selectedRegistration: '',
        selectedVisibility: '',
        isBulkProcessing: false,
        initialized: false,
        keyword: null,
        loadingSpaces: false,
        loadingDisplay: false,
        offset: 0,
        pageSize: 25,
        spacesSize: 0,
        processedSpaces: 0,
        sortBy: 'title',
        sortDesc: false,
        expand: 'managers,groupBinding,extendedProperties',
        spaces: [],
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
        canShowMore() {
          return this.spaces?.length && this.spacesSize > this.spaces.length;
        },
        loading() {
          return this.loadingDisplay || this.loadingSpaces;
        },
        isFilteredByTemplate() {
          return this.selectedTemplateId && Number(this.selectedTemplateId);
        },
        isFilteredByRegistration() {
          return this.selectedRegistration && this.selectedRegistration !== '';
        },
        isFilteredByVisibility() {
          return this.selectedVisibility && this.selectedVisibility !== '';
        },
      },
      watch: {
        keyword() {
          if (this.initialized && !this.isBulkProcessing) {
            this.searchSpaces(true, true);
          }
        },
        selectedSpaces() {
          if (!this.isBulkProcessing) {
            this.allSpacesSelected = false;
          }
        },
        isBulkProcessing() {
          if (this.isBulkProcessing) {
            window.addEventListener('beforeunload', this.displayConfirmReload);
            document.dispatchEvent(new CustomEvent('alert-message', {detail: {
              alertComponent: 'spaces-administration-processing-alert',
              alertComponentParams: {
                options: this.$root,
              },
              alertDismissible: false,
              alertTimeout: 864000000,
              alertLinkText: this.$t('social.spaces.administration.manageSpaces.cancel'),
              alertLinkCallback: () => {
                this.isBulkProcessing = false;
                document.dispatchEvent(new CustomEvent('close-alert-message'));
              },
            }}));
          } else {
            window.removeEventListener('beforeunload', this.displayConfirmReload);
            document.dispatchEvent(new CustomEvent('close-alert-message'));
          }
        },
      },
      created() {
        document.addEventListener('extension-spaces-administration-bulk-action-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
        this.$on('spaces-administration-list-refresh', this.refreshSpaces);
        this.refreshSpaceTemplates();
        this.refreshExtensions();
      },
      beforeDestroy() {
        document.removeEventListener('extension-spaces-administration-bulk-action-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
        this.$off('spaces-administration-list-refresh', this.refreshSpaces);
      },
      methods: {
        displayLoading() {
          this.loadingDisplay = true;
        },
        hideLoading() {
          this.loadingDisplay = false;
        },
        async refreshSpaceTemplates() {
          const spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true);
          spaceTemplates.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
          this.spaceTemplates = spaceTemplates;
        },
        refreshExtensions() {
          this.bulkExtensions = extensionRegistry.loadExtensions('spaces-administration', 'bulk-action') || [];
          this.itemMenuExtensions = extensionRegistry.loadExtensions('spaces-administration', 'menu-action') || [];
          this.mainExtensions = extensionRegistry.loadExtensions('spaces-administration', 'main') || [];
          this.tableColumnExtensions = extensionRegistry.loadExtensions('spaces-administration', 'table-column') || [];
        },
        refreshSpaces(clean) {
          this.searchSpaces(true, clean);
        },
        async searchSpaces(refresh, clean) {
          if (this.loadingSpaces) {
            return;
          }
          if (refresh) {
            this.allSpacesSelected = false;
            this.selectedSpaces = [];
          }
          const offset = refresh ? 0 : this.spaces.length;
          const limit = refresh ? this.offset + this.pageSize : this.pageSize;

          this.loadingSpaces = true;
          try {
            const customSort = this.tableColumnExtensions?.find(e => e.sortBy === this.sortBy)?.customSort;
            if (customSort
                && !this.keyword?.length) {
              this.spaces = await customSort({
                offset: offset,
                limit: limit,
                expand: this.expand,
                templateId: (this.selectedTemplateId && Number(this.selectedTemplateId)) ? this.selectedTemplateId : null,
                sortDesc: this.sortDesc,
                currentSpaces: this.spaces,
                currentSpacesSize: this.spacesSize,
              });
            } else {
              if (!offset && clean) {
                this.spaces = [];
              }
              const data = await this.$spaceService.getSpacesByFilter({
                query: this.keyword,
                visibility: this.selectedVisibility?.length ? this.selectedVisibility : null,
                registration: this.selectedRegistration?.length ? this.selectedRegistration : null,
                templateId: this.selectedTemplateId && Number(this.selectedTemplateId),
                filter: 'all',
                expand: this.expand,
                offset: offset,
                limit: limit,
                sortBy: 'title',
                sortDirection: this.sortDesc ? 'desc' : 'asc',
              });
              if (offset) {
                this.spaces.push(...data.spaces);
              } else {
                this.spaces = data.spaces || [];
              }
              this.spacesSize = data?.size || 0;
              this.$emit('loaded', this.spacesSize);
            }
          } finally {
            this.loadingSpaces = false;
            this.loadingDisplay = false;
            this.initialized = true;
          }
        },
        async loadNextPage() {
          this.offset += this.pageSize;
          await this.searchSpaces();
        },
        async applyOperationInBulk(callback, params, onFinish, onCancel) {
          this.processedSpaces = 0;
          this.isBulkProcessing = true;
          this.$emit('spaces-administration-bulk-operation-status', null, 'disabled');
          try {
            if (this.allSpacesSelected) {
              let index = 0;
              do {
                while (index < this.spaces.length && this.isBulkProcessing) {
                  // eslint-disable-next-line no-await-in-loop
                  await this.applyOperationOnSpace(this.spaces[index++], params, callback);
                }
                if (index >= this.spaces.length && this.isBulkProcessing) {
                  // eslint-disable-next-line no-await-in-loop
                  await this.loadNextPage();
                  this.selectedSpaces = this.spaces;
                }
              } while (index < this.spaces.length && this.isBulkProcessing);
            } else {
              for (let i = 0; i < this.spaces.length; i++) {
                if (!this.isBulkProcessing) {
                  break;
                }
                // Done on purpose to iterate over spaces instead of selection
                // in order to preserve display order processing
                // instead of selection order
                const space = this.spaces[i];
                if (this.selectedSpaces.find(s => s.id === space.id)) {
                  // eslint-disable-next-line no-await-in-loop
                  await this.applyOperationOnSpace(space, params, callback);
                }
              }
            }
          } finally {
            this.allSpacesSelected = false;
            this.selectedSpaces = [];
            this.$emit('spaces-administration-bulk-operation-status', null, null);
            if (this.isBulkProcessing) {
              this.isBulkProcessing = false;
              await this.$nextTick();
              if (onFinish) {
                onFinish(params);
              }
            } else if (onCancel) {
              onCancel(params);
            }
          }
        },
        async applyOperationOnSpace(space, params, callback) {
          this.$emit('spaces-administration-bulk-operation-status', space.id, 'processing');
          try {
            await callback(space, params);
            this.$emit('spaces-administration-bulk-operation-status', space.id, 'done');
          } catch (e) {
            // eslint-disable-next-line no-console
            console.error('Error processing space ', space.id, '. Error: ', e);
            this.$emit('spaces-administration-bulk-operation-status', space.id, 'error');
          } finally {
            this.processedSpaces++;
          }
        },
        displayConfirmReload(event) {
          event.preventDefault();
          event.returnValue = true;
        },
      },
      template: `<spaces-administration id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces Administration'))
    .finally(() => Vue.prototype.$utils.includeExtensions('ManageSpacesExtension'));
}