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
    id="SpacesListFilterDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.filterSpaces') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <div class="text-header mb-2">
          {{ $t('social.spaces.administration.manageSpaces.template') }}
        </div>
        <select
          v-model="selectedTemplateId"
          :aria-label="$t('social.spaces.administration.manageSpaces.template')"
          class="mt-0 full-width ignore-vuetify-classes py-2 height-auto text-truncate">
          <option
            v-for="item in spaceTemplateItems"
            :key="item.value"
            :value="item.value"
            class="mt-0 full-width text-truncate">
            <div class="text-body full-width">
              {{ item.text }}
            </div>
          </option>
        </select>
        <div class="text-header mt-2 mb-2">
          {{ $t('social.spaces.administration.manageSpaces.access') }}
        </div>
        <v-radio-group
          v-model="selectedRegistration"
          class="ms-n1 mt-0"
          mandatory
          inset>
          <v-radio
            v-for="item in spaceAccessItems"
            :key="item.value"
            :value="item.value"
            class="mt-0">
            <template #label>
              <div class="text-body full-width">
                {{ item.text }}
              </div>
            </template>
          </v-radio>
        </v-radio-group>
        <div class="text-header mt-2 mb-2">
          {{ $t('social.spaces.administration.manageSpaces.visibility') }}
        </div>
        <v-radio-group
          v-model="selectedVisibility"
          class="ms-n1 mt-0"
          mandatory
          inset>
          <v-radio
            v-for="item in spaceVisibilityItems"
            :key="item.value"
            :value="item.value"
            class="mt-0">
            <template #label>
              <div class="text-body full-width">
                {{ item.text }}
              </div>
            </template>
          </v-radio>
        </v-radio-group>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          class="btn ms-auto me-2"
          @click="close">
          {{ $t('social.spaces.administration.manageSpaces.cancel') }}
        </v-btn>
        <v-btn
          color="primary"
          elevation="0"
          @click="apply">
          {{ $t('social.spaces.administration.manageSpaces.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    selectedTemplateId: null,
    selectedRegistration: null,
    selectedVisibility: null,
  }),
  computed: {
    spaceTemplateItems() {
      const spaceTemplateItems = [];
      if (this.$root.spaceTemplates?.length) {
        spaceTemplateItems.push(...this.$root.spaceTemplates.map(t => ({
          text: t.name,
          value: t.id,
        })));
      }
      spaceTemplateItems.sort((a, b) => this.$root.collator.compare(a.text.toLowerCase(), b.text.toLowerCase()));
      spaceTemplateItems.splice(0, 0, {
        text: this.$t('social.spaces.administration.manageSpaces.spaceTemplates.all'),
        value: '0',
      });
      return spaceTemplateItems;
    },
    spaceAccessItems() {
      return [{
        text: this.$t('social.spaces.administration.manageSpaces.spaceTemplates.anyAccess'),
        value: '',
      }, {
        text: this.$t('social.spaces.administration.manageSpaces.registration.open'),
        value: 'open',
      }, {
        text: this.$t('social.spaces.administration.manageSpaces.registration.validation'),
        value: 'validation',
      }, {
        text: this.$t('social.spaces.administration.manageSpaces.registration.closed'),
        value: 'closed',
      }];
    },
    spaceVisibilityItems() {
      return [{
        text: this.$t('social.spaces.administration.manageSpaces.anyVisibility'),
        value: '',
      }, {
        text: this.$t('social.spaces.administration.manageSpaces.visibility.private'),
        value: 'private',
      }, {
        text: this.$t('social.spaces.administration.manageSpaces.visibility.hidden'),
        value: 'hidden',
      }];
    },
  },
  created() {
    this.$root.$on('spaces-administration-filter-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('spaces-administration-filter-drawer-open', this.open);
  },
  methods: {
    open() {
      this.selectedTemplateId = this.$root.selectedTemplateId;
      this.selectedRegistration = this.$root.selectedRegistration;
      this.selectedVisibility = this.$root.selectedVisibility;
      this.$refs.drawer.open();
    },
    apply() {
      this.$root.selectedTemplateId = this.selectedTemplateId || '0';
      this.$root.selectedRegistration = this.selectedRegistration || '';
      this.$root.selectedVisibility = this.selectedVisibility || '';
      this.$root.$emit('spaces-administration-list-refresh', true);
      this.close();
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>