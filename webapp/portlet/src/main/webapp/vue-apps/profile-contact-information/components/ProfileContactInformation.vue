<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 -->

<template>
  <v-app
    :class="owner && 'profileContactInformation' || 'profileContactInformationOther'"
    class="white">
    <widget-wrapper :title="title">
      <template #action>
        <v-btn
          v-if="owner"
          id="profileContactEditButton"
          icon
          outlined
          small
          @click="editContactInformation">
          <v-icon size="18">fas fa-edit</v-icon>
        </v-btn>
      </template>
      <v-list
        :flat="isMobile"
        class="list-no-selection">
        <v-list-item-group>
          <template
            v-for="property in filteredProperties">
            <v-list-item
              v-if="canShowProperty(property)"
              class="text-color not-clickable"
              :class="property.hidden && 'opacity-5'"
              :key="property.id"
              :ripple="false">
              <v-hover v-slot="{ hover }">
                <profile-multi-valued-property
                  v-if="property.children?.length"
                  :hover="hover"
                  :owner="owner"
                  :is-admin="isAdmin"
                  :property="property"
                  :is-mobile="isMobile"
                  :searchable="isSearchable(property)"
                  @quick-search="quickSearch" />
                <profile-single-valued-property
                  v-else
                  :hover="hover"
                  :property="property"
                  :is-mobile="isMobile"
                  :searchable="isSearchable(property)"
                  @quick-search="quickSearch" />
              </v-hover>
            </v-list-item>
            <v-divider
              v-if="canShowProperty(property)"
              :key="property.id" />
          </template>
        </v-list-item-group>
      </v-list>
    </widget-wrapper>
    <profile-contact-information-drawer
      v-if="owner"
      ref="contactInformationEdit"
      :upload-limit="uploadLimit" />
    <quick-search-users-list-drawer
      :properties="quickSearchSettingProperties" />
  </v-app>
</template>

<script>

export default {
  props: {
    uploadLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    properties: [],
    user: null,
    excludedSearchProps: [],
    settings: []
  }),
  computed: {
    isMobile() {
      return this.$vuetify?.breakpoint?.smAndDown;
    },
    isAdmin() {
      return this.user?.isAdmin;
    },
    filteredProperties() {
      return this.properties.filter(property => property.visible &&
               (property.value || (property.children.length && property.children.some(e => e.value))));
    },
    title() {
      return this.owner && this.$t('profileContactInformation.yourContactInformation') || this.$t('profileContactInformation.contactInformation');
    },
    quickSearchSettingProperties() {
      return this.settings.filter(settingProperty => this.isSearchable(settingProperty)).map(settingProperty => settingProperty.propertyName);
    }
  },
  beforeCreate() {
    return this.$profileSettingsService.getSettings()
      .then(settings => {
        this.settings = settings?.settings || [];
        this.excludedSearchProps = settings?.excludedQuickSearchProperties;
      });
  },
  created() {
    this.refreshProperties();
  },
  mounted() {
    document.addEventListener('userPropertiesModified', () => {
      this.refreshProperties(true);
    });

    if (this.properties) {
      this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    }
  },
  methods: {
    canShowProperty(property) {
      return !this.isPropertyHidden(property) || this.isPropertyHidden(property) && (this.isAdmin || this.owner);
    },
    isPropertyHidden(property) {
      return property.hidden || (property?.children?.length
                             && property.children.filter(child => child.value)
                               .every(child => child.hidden));
    },
    isSearchable(property) {
      return !this.excludedSearchProps?.includes(property.propertyName)
                         && !new RegExp(`^(${this.excludedSearchProps?.join('.|')}.)`)?.exec(property.propertyName) ;
    },
    quickSearch(property, childProperty) {
      if (this.excludedSearchProps.includes(property.propertyName)) {
        return;
      }
      const searchKey = {};
      searchKey[property.propertyName] = childProperty?.value || property.value;
      this.$root.$emit('open-quick-search-users-drawer', searchKey, searchKey[property.propertyName]);
    },
    refreshProperties(broadcast) {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'settings')
        .then(userdataEntity => {
          this.user = userdataEntity;
          this.properties = userdataEntity?.properties.filter(item => item.active).sort((s1, s2) => ((s1.order > s2.order) ? 1 : (s1.order < s2.order) ? -1 : 0));
          this.$nextTick().then(() => this.$root.$emit('application-loaded'));
          if (broadcast){
            document.dispatchEvent(new CustomEvent('userModified', {detail: this.user}));
          }
        })
        .finally(() => this.$root.$applicationLoaded());
    },
    editContactInformation() {
      this.$root.$emit('open-profile-contact-information-drawer', this.properties);
    }
  },
};
</script>