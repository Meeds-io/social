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

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceTemplatesManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const lang = eXo?.env.portal.language || 'en';
const url = `/social/i18n/locale.portlet.SpaceTemplatesManagement?lang=${lang}`;

const appId = 'SpaceTemplatesManagement';
export function init(isExternalFeatureEnabled) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n =>
      Vue.createApp({
        data: {
          spaceTemplates: [],
          isExternalFeatureEnabled,
          spacesCountByTemplates: null,
          usersPermission: '*:/platform/users',
          administratorsPermission: '*:/platform/administrators',
          everyonePermission: 'Everyone',
          collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
          allSpaceTemplatesSelected: false,
          selectedSpaceTemplates: [],
          spaceTemplatesSize: 0,
          processedSpaceTemplates: 0,
          isBulkProcessing: false,
          loading: 0,
          extensionApp: 'space-templates',
          menuItemExtensionType: 'space-templates-item-action',
          mainExtensionType: 'space-templates-main',
          menuItemExtensions: [],
          mainExtensions: [],
          subspacesTemplateIds: []
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.mobile;
          },
          systemSelectedSpaceTemplates() {
            return this.selectedSpaceTemplates.every(template => template.system);
          }
        },
        async created() {
          this.$root.$on('space-templates-list-refresh', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-deleted', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-created', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-updated', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-enabled', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-disabled', this.refreshSpaceTemplates);
          this.$root.$on('space-templates-saved', this.refreshSpaceTemplates);
          document.addEventListener(`extension-${this.extensionApp}-${this.mainExtensionType}-updated`, this.refreshMainExtensions);
          document.addEventListener(`extension-${this.extensionApp}-${this.menuItemExtensionType}-updated`, this.refreshMenuExtensions);
          await this.init();
          this.refreshMainExtensions();
          this.refreshMenuExtensions();
        },
        beforeDestroy() {
          this.$root.$off('space-templates-list-refresh', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-deleted', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-created', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-updated', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-enabled', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-disabled', this.refreshSpaceTemplates);
          this.$root.$off('space-templates-saved', this.refreshSpaceTemplates);
          document.removeEventListener(`extension-${this.extensionApp}-${this.menuItemExtensionType}-updated`, this.refreshMenuExtensions);
          document.removeEventListener(`extension-${this.extensionApp}-${this.mainExtensionType}-updated`, this.refreshMainExtensions);
        },
        methods: {
          async refreshSpaceTemplates() {
            this.loading = true;
            try {
              this.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true) || [];
              this.subspacesTemplateIds = await this.$spaceTemplateService.getSubspaceTemplateIds() || [];
            } finally {
              this.loading = false;
            }
          },
          refreshMenuExtensions() {
            this.menuItemExtensions = extensionRegistry.loadExtensions(this.extensionApp, this.menuItemExtensionType);
          },
          refreshMainExtensions() {
            this.mainExtensions = extensionRegistry.loadExtensions(this.extensionApp, this.mainExtensionType);
          },
          async applyOperationInBulk(callback, params, onFinish, onCancel) {
            this.processedSpaceTemplates = 0;
            this.isBulkProcessing = true;
            this.$emit('space-templates-bulk-operation-status', null, 'disabled');
            try {
              if (this.allSpaceTemplatesSelected) {
                let index = 0;
                do {
                  while (index < this.spaceTemplates.length && this.isBulkProcessing) {
                    // eslint-disable-next-line no-await-in-loop
                    await this.applyOperationOnSpaceTemplate(this.spaceTemplates[index++], params, callback);
                  }
                  if (index >= this.spaceTemplates.length && this.isBulkProcessing) {
                    // eslint-disable-next-line no-await-in-loop
                    this.selectedSpaceTemplates = this.spaceTemplates;
                  }
                } while (index < this.spaceTemplates.length && this.isBulkProcessing);
              } else {
                for (const element of this.spaceTemplates) {
                  if (!this.isBulkProcessing) {
                    break;
                  }
                  const spaceTemplate = element;
                  if (this.selectedSpaceTemplates.find(s => s.id === spaceTemplate.id)) {
                    // eslint-disable-next-line no-await-in-loop
                    await this.applyOperationOnSpaceTemplate(spaceTemplate, params, callback);
                  }
                }
              }
            } finally {
              this.allSpaceTemplatesSelected = false;
              this.selectedSpaceTemplates = [];
              this.$emit('space-templates-bulk-operation-status', null, null);
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
          async applyOperationOnSpaceTemplate(spaceTemplate, params, callback) {
            this.$emit('space-templates-bulk-operation-status', spaceTemplate.id, 'processing');
            try {
              await callback(spaceTemplate, params);
              this.$emit('space-templates-bulk-operation-status', spaceTemplate.id, 'done');
            } catch (e) {
              // eslint-disable-next-line no-console
              console.error('Error processing space template ', spaceTemplate.id, '. Error: ', e);
              this.$emit('space-templates-bulk-operation-status', spaceTemplate.id, 'error');
            } finally {
              this.processedSpaceTemplates++;
            }
          },
          async init() {
            await this.refreshSpaceTemplates();
            this.spacesCountByTemplates = await this.$spaceService.getSpacesCountByTemplates();
          }
        },
        template: `<space-templates-management id="${appId}"/>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, 'Space Templates')
    )
    .finally(() => Vue.prototype.$utils.includeExtensions('SpaceTemplateManagementExtension'));
}
