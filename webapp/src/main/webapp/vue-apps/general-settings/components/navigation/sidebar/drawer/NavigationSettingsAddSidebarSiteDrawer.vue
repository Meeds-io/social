<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    :right="!$vuetify.rtl"
    :left="$vuetify.rtl"
    no-x-scroll>
    <template #title>
      {{ $t(isNew && 'generalSettings.addSideBarItemSite.drawerTitle' || 'generalSettings.updateSideBarItemSite.drawerTitle') }}
    </template>
    <template v-if="drawer && item" #content>
      <div class="pa-4">
        <v-radio-group
          v-model="option"
          class="mt-0 mb-4 ms-n1 text-no-wrap flex-grow-1 flex-shrink-0"
          mandatory>
          <v-radio
            value="SITE"
            class="mx-0 mt-0 mb-1">
            <template #label>
              <span class="text-body">{{ $t('generalSettings.siteOption') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="PAGE"
            class="mx-0 mt-0 mb-1">
            <template #label>
              <span class="text-body">{{ $t('generalSettings.pageOption') }}</span>
            </template>
          </v-radio>
        </v-radio-group>
        <div class="mb-2">
          {{ $t('generalSettings.selectASite') }}
        </div>
        <v-autocomplete
          v-model="siteId"
          :items="filteredSites"
          :placeholder="$t('generalSettings.selectASitePlaceholder')"
          item-text="displayName"
          item-value="siteId"
          class="ma-0 pa-0 elevation-0 no-border"
          hide-no-data
          hide-selected
          hide-details
          outlined
          dense />
        <template v-if="option === 'PAGE'">
          <div class="d-flex align-center mt-4 mb-2">
            {{ $t('generalSettings.selectAPage') }}
            <v-spacer />
            <v-card class="d-flex align-center" flat>
              <span class="me-1">{{ $t('generalSettings.anyPage') }}</span>
              <v-checkbox
                v-model="expandPages"
                false-value="false"
                true-value="true"
                class="ma-0 pa-0"
                hide-details
                dense />
            </v-card>
          </div>
          <v-autocomplete
            v-if="expandPages !== 'true' && pages"
            v-model="nodeId"
            :items="pages"
            :placeholder="$t('generalSettings.selectASitePlaceholder')"
            item-text="label"
            item-value="id"
            class="ma-0 pa-0 elevation-0 no-border"
            hide-no-data
            hide-selected
            hide-details
            outlined
            dense />
        </template>
        <portal-general-settings-navigation-settings-icon-input
          ref="nodeIcon"
          v-model="icon"
          :site-id="siteId"
          class="mt-4 mb-2" />
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('generalSettings.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified"
          class="btn-primary"
          elevation="0"
          @click="apply">
          {{ $t(isNew && 'generalSettings.add' || 'generalSettings.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    initialized: false,
    settings: null,
    option: null,
    item: null,
    icon: null,
    siteId: null,
    nodeId: null,
    sites: null,
    isNew: true,
    excludeSpaceSites: true,
    excludeEmptyNavigationSites: true,
    expandNavigations: false,
    filterByDisplayed: false,
    sortByDisplayOrder: false,
    displayed: false,
    filterByPermissions: false,
    excludeGroupNodesWithoutPageChildNodes: true,
    temporalCheck: false,
    visibility: ['displayed', 'system', 'temporal'],
    site: null,
    expandPages: 'false',
  }),
  computed: {
    filteredSites() {
      const sites = this.sites?.filter?.(s => s?.properties?.IS_SPACE_PUBLIC_SITE !== 'true') || [];
      return this.isPageOption ? sites : sites.filter(s => s.name !== 'global');
    },
    siteName() {
      return this.site?.name;
    },
    siteType() {
      return this.site?.siteType;
    },
    siteIcon() {
      return !this.loading
          && this.site?.siteNavigations
          && this.getFirstIcon(this.site.siteNavigations)
          || null;
    },
    pageIcon() {
      return this.nodeId && this.page?.icon || null;
    },
    modified() {
      return this.isNew
        || (this.isSiteOption && (this.siteId !== this.item?.properties?.siteId || this.item?.properties?.expandPages !== this.expandPages))
        || (this.isPageOption && this.nodeId !== this.item?.properties?.navigationNodeId);
    },
    isSiteOption() {
      return this.option === 'SITE' || this.expandPages === 'true';
    },
    isPageOption() {
      return !this.isSiteOption;
    },
    disabled() {
      return !this.modified
        || (this.isSiteOption && !this.siteId)
        || (this.isPageOption && (!this.siteId || !this.nodeId));
    },
    pages() {
      if (this.isPageOption && this.site) {
        const pages = [];
        this.addPages(pages, this.site.siteNavigations);
        return pages;
      } else {
        return null;
      }
    },
    page() {
      return this.pages?.find?.(p => Number(p.id) === Number(this.nodeId));
    },
  },
  watch: {
    siteId() {
      if (this.drawer && this.initialized) {
        this.nodeId = null;
        this.retrieveSite();
      }
    },
    option() {
      if (this.drawer && this.initialized) {
        this.nodeId = null;
        this.retrieveSite();
      }
    },
    isPageOption() {
      if (this.drawer && this.initialized) {
        this.nodeId = null;
        this.retrieveSite();
      }
    },
    siteIcon() {
      if (this.drawer && this.isSiteOption) {
        this.icon = this.siteIcon || this.icon || 'fa-project-diagram';
      }
    },
    pageIcon() {
      if (this.drawer && this.isPageOption) {
        this.icon = this.pageIcon || this.icon || 'fa-project-diagram';
      }
    },
  },
  created() {
    this.$root.$on('sidebar-item-add-site', this.open);
    this.$root.$on('sidebar-item-edit-site', this.open);
  },
  beforeDestroy() {
    this.$root.$off('sidebar-item-add-site', this.open);
    this.$root.$off('sidebar-item-edit-site', this.open);
  },
  methods: {
    async open(settings, item) {
      this.settings = settings;
      this.item = item || {
        name: null,
        url: null,
        target: null,
        avatar: null,
        icon: null,
        type: null,
        items: null,
        properties: {},
      };
      this.isNew = !item;
      this.icon = this.item.icon || 'fa-project-diagram';
      this.initialized = false;
      if (!this.item.properties) {
        this.item.properties = {};
      }
      this.siteId = this.item.properties?.siteId && Number(this.item.properties.siteId);
      this.nodeId = this.item.properties?.navigationNodeId;
      this.expandPages = this.item.properties?.expandPages || 'false';
      this.option = this.expandPages === 'true' ? 'PAGE' : this.item.type;
      this.$refs.drawer.open();
      try {
        if (!this.sites) {
          this.loading = true;
          this.sites = await this.$siteService.getSites(
            'PORTAL',
            null,
            null,
            this.excludeEmptyNavigationSites,
            this.excludeSpaceSites,
            this.expandNavigations,
            this.filterByDisplayed,
            this.sortByDisplayOrder,
            this.displayed,
            this.filterByPermissions,
            this.excludeGroupNodesWithoutPageChildNodes,
            this.temporalCheck
          );
          await this.retrieveSite();
        }
      } finally {
        this.loading = false;
        this.initialized = true;
      }
    },
    async apply() {
      this.saving = true;
      try {
        if (this.$refs?.nodeIcon) {
          try {
            this.icon = await this.$refs.nodeIcon?.save?.();
          } catch (e) {
            this.loading = false;
            this.$root.$emit('alert-message', this.$t('generalSettings.errorUpdatingNodeImage'), 'error');
            throw e;
          }
        }
        await this.retrieveSite();
        this.item.name = this.isPageOption ? this.page.label : this.site.displayName;
        this.item.url = this.isPageOption ? `/portal/${this.siteName}/${this.page.uri}` : `/portal/${this.siteName}`;
        this.item.target = this.isPageOption ? this.page.target : null;
        this.item.icon = this.icon;
        this.item.type = this.isPageOption ? 'PAGE' : 'SITE';
        this.item.properties = this.isPageOption ? {
          siteId: this.siteId,
          siteType: this.siteType,
          siteName: this.siteName,
          siteIcon: this.siteIcon,
          siteDisplayName: this.site.displayName,
          navigationNodeId: this.nodeId,
        } : {
          siteId: this.siteId,
          siteType: this.siteType,
          siteName: this.siteName,
          expandPages: this.expandPages,
        };
        if (this.isSiteOption && this.expandPages === 'true') {
          this.item.items = this.site.siteNavigations.map(n => (n.visibility?.toLowerCase?.() === 'displayed' || n.visibility?.toLowerCase?.() === 'temporal') && {
            name: n.label,
            url: `/portal/${this.siteName}/${n.uri}`,
            target: n.target,
            icon: n.icon,
            type: 'PAGE',
            properties: {
              siteId: this.siteId,
              siteType: this.siteType,
              siteName: this.siteName,
              siteIcon: this.siteIcon,
              siteDisplayName: this.site.displayName,
              navigationNodeId: n.id,
            },
          }).filter(n => n);
        } else {
          delete this.item.items;
        }
        if (this.isNew) {
          this.settings.sidebar.items.push(this.item);
        }
        this.close();
      } finally {
        this.saving = false;
      }
    },
    async retrieveSite() {
      this.site = null;
      if (this.siteId && this.sites) {
        const site = this.sites.find(s => s.siteId === this.siteId);
        if (site && !site.navigationsRetrieved) {
          this.loading = true;
          try {
            const siteWithNavigations = await this.$siteService.getSite(site.siteType, site.name, {
              lang: eXo.env.portal.language,
              expandNavigations: true,
              excludeEmptyNavigationSites: true,
              excludeGroupNodesWithoutPageChildNodes: true,
              temporalCheck: false,
              visibility: this.visibility,
            });
            site.siteNavigations = siteWithNavigations.siteNavigations;
            site.navigationsRetrieved = true;
          } finally {
            this.loading = false;
          }
        }
        this.site = site;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    addPages(pages, nodes) {
      if (nodes?.length) {
        pages.push(...nodes);
        nodes.forEach(n => this.addPages(pages, n.children));
      }
    },
    getFirstIcon(nodes) {
      if (nodes?.length) {
        const icon = nodes.map(n => n.icon).find(s => !!s);
        if (icon) {
          return icon;
        } else {
          return nodes.map(n => this.getFirstIcon(n?.children)).find(s => !!s);
        }
      } else {
        return null;
      }
    },
  },
};
</script>