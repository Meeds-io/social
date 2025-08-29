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
    :right="!$vuetify.rtl"
    :left="$vuetify.rtl"
    no-x-scroll>
    <template #title>
      {{ $t(isNew && 'generalSettings.addSideBarItemLink.drawerTitle' || 'generalSettings.updateSideBarItemLink.drawerTitle') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <div class="mb-2">
          {{ $t('generalSettings.sidebar.linkName') }}
        </div>
        <translation-text-field
          v-model="names"
          :placeholder="$t('generalSettings.sidebar.linkNamePlaceHolder')"
          :maxlength="maxNameLength"
          :rules="rules.name"
          drawer-title="generalSettings.sidebar.linkNameDrawerTitle" />
        <div class="mt-4 mb-2">
          {{ $t('generalSettings.sidebar.linkDescription') }}
        </div>
        <translation-text-field
          v-model="descriptions"
          :rules="rules.description"
          :placeholder="$t('generalSettings.sidebar.linkDescriptionPlaceHolder')"
          :maxlength="50"
          :required="false"
          drawer-title="generalSettings.sidebar.linkDescriptionDrawerTitle"
          class="width-auto flex-grow-1 mb-4"
          no-expand-icon
          back-icon />
        <div class="mt-4 mb-2">
          {{ $t('generalSettings.sidebar.linkLabel') }}
        </div>
        <v-text-field
          name="link"
          v-model="link"
          :placeholder="$t('generalSettings.sidebar.linkPlaceholder')"
          :rules="rules.url"
          class="border-box-sizing width-auto pt-0"
          type="text"
          outlined
          dense
          mandatory />
        <div class="d-flex align-center justify-space-between full-width mt-4 mb-2">
          <div>
            {{ $t('generalSettings.sidebar.linkTarget') }}
          </div>
          <v-switch
            v-model="target"
            true-value="_self"
            false-value="_blank"
            class="ma-0 width-fit-content" />
        </div>
        <portal-general-settings-navigation-settings-icon-input
          ref="nodeIcon"
          v-model="icon"
          :site-id="defaultSiteId"
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
          :disabled="disabled"
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
    settings: null,
    isNew: false,
    item: null,
    defaultSiteId: null,
    names: {},
    descriptions: {},
    link: null,
    icon: null,
    target: null,
    maxNameLength: 50,
    maxDescriptionLength: 50,
  }),
  computed: {
    modified() {
      return this.drawer
        || this.isNew
        || this.link !== this.item?.url
        || this.icon !== this.item?.icon
        || this.target !== this.item?.target
        || JSON.stringify(this.names) !== this.item?.properties?.names;
    },
    isValidLink() {
      try {
        return !!this.$utils.toLinkUrl(this.link, {
          urls: true,
          email: true,
          phone: true,
        })?.length;
      } catch (e) {
        return false;
      }
    },
    disabled() {
      return !this.modified
        || !this.isValidLink
        || !this.icon
        || !this.target
        || !this.names
        || !this.names[eXo.env.portal.defaultLanguage]?.trim?.()?.length
        || Object.values(this.names).find(name => name?.length > this.maxNameLength)
        || (this.descriptions && Object.values(this.descriptions).find(description => description?.length > this.maxDescriptionLength));
    },
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length < this.maxNameLength || this.$t('generalSettings.sidebar.exceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
        description: [
          v => !v?.length || v.length < this.maxDescriptionLength || this.$t('generalSettings.sidebar.exceedsMaxLength', {
            0: this.maxDescriptionLength,
          }),
        ],
        url: [
          v => !!v?.length || ' ',
          () => this.isValidLink || this.$t('generalSettings.sidebar.invalidLink'),
        ],
      };
    },
  },
  created() {
    this.$root.$on('sidebar-item-add-link', this.open);
    this.$root.$on('sidebar-item-edit-link', this.open);
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('sidebar-item-add-link', this.open);
    this.$root.$off('sidebar-item-edit-link', this.open);
  },
  methods: {
    async init() {
      const globalSite = await this.$siteService.getSite('PORTAL', 'global');
      this.defaultSiteId = globalSite?.siteId;
    },
    open(settings, item) {
      this.settings = settings;
      this.item = item || {
        name: null,
        url: null,
        target: null,
        avatar: null,
        icon: null,
        type: 'LINK',
        items: null,
        properties: {},
      };
      this.isNew = !item;
      this.reset(this.item);
      this.$refs.drawer.open();
    },
    reset(item) {
      this.modified = false;
      this.link = item?.url;
      this.icon = item?.icon || 'fa-globe';
      this.target = item?.target || '_self';
      this.names = item?.properties?.names && JSON.parse(item?.properties?.names) || {};
      this.descriptions = item?.properties?.descriptions && JSON.parse(item?.properties?.descriptions) || {};
    },
    async apply() {
      this.item.name = this.names[eXo.env.portal.language] || this.names[eXo.env.portal.defaultLanguage];
      this.item.icon = this.icon || 'fa-globe';
      this.item.url = this.link;
      this.item.target = this.target === '_blank' ? '_blank' : null;
      Object.keys(this.names).forEach(k => {
        if (!this.names[k]?.trim?.()?.length) {
          delete this.names[k];
        }
      });
      this.item.properties = {
        names: JSON.stringify(this.names),
        descriptions: JSON.stringify(this.descriptions),
      };
      if (this.isNew) {
        this.settings.sidebar.items.push(this.item);
      }
      if (this.$refs?.nodeIcon) {
        try {
          await this.$refs.nodeIcon?.save?.();
        } catch (e) {
          this.loading = false;
          this.$root.$emit('alert-message', this.$t('generalSettings.errorUpdatingNodeImage'), 'error');
          throw e;
        }
      }
      this.close();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>