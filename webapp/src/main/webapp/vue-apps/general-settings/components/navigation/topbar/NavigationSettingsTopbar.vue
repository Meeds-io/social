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
  <div v-if="topbarSettings">
    <div class="d-flex align-center">
      <help-label
        label="generalSettings.topbar"
        label-class="text-header"
        tooltip="generalSettings.topbar.helpTooltip">
        <template slot="helpContent">
          <p>
            {{ $t('generalSettings.topbar.helpDescription1') }}
          </p>
          <p>
            {{ $t('generalSettings.topbar.helpDescription2') }}
          </p>
          <div>
            {{ $t('generalSettings.topbar.helpDescription3') }}
          </div>
          <div>
            {{ $t('generalSettings.topbar.helpDescription4') }}
          </div>
          <div>
            {{ $t('generalSettings.topbar.helpDescription5') }}
          </div>
          <div>
            {{ $t('generalSettings.topbar.helpDescription6') }}
          </div>
        </template>
      </help-label>
      <v-btn
        :title="$t('generalSettings.topbar.switchDevicePreview')"
        class="ms-2"
        icon
        @click="mobilePreview = !mobilePreview">
        <v-icon size="20">{{ mobilePreview && 'fa-desktop' || 'fa-mobile-alt' }}</v-icon>
      </v-btn>
    </div>
    <!-- Preview -->
    <portal-general-settings-navigation-settings-topbar-preview
      :settings="settings"
      :mobile-preview="mobilePreview"
      class="elevation-3 mb-6 mt-4" />
    <!-- Branding Infos -->
    <div class="font-weight-bold mb-4">
      {{ $t('generalSettings.brandingInfos') }}
    </div>
    <div class="mb-2">
      {{ $t('generalSettings.displayCompanyName.desktop') }}
    </div>
    <v-radio-group
      v-model="topbarSettings.displayCompanyName"
      class="my-0 ms-n1 text-no-wrap width-fit-content"
      mandatory>
      <v-radio
        :value="true"
        class="mx-0 mt-0 mb-1">
        <template #label>
          <span class="text-body">{{ $t('generalSettings.displayCompanyName.desktop.choice1') }}</span>
        </template>
      </v-radio>
      <v-radio
        :value="false"
        class="mx-0 mt-0 mb-1">
        <template #label>
          <span class="text-body">{{ $t('generalSettings.displayCompanyName.desktop.choice2') }}</span>
        </template>
      </v-radio>
    </v-radio-group>

    <div class="my-2">
      {{ $t('generalSettings.displayCompanyName.mobile') }}
    </div>
    <v-switch
      v-model="topbarSettings.displayMobileCompanyLogo"
      :label="$t('generalSettings.displayCompanyName.mobile.choice')"
      class="my-0 width-fit-content" />

    <div class="mb-2 mt-4">
      {{ $t('generalSettings.displaySiteName') }}
    </div>
    <v-switch
      v-model="topbarSettings.displaySiteName"
      :label="$t('generalSettings.displaySiteName.label')"
      class="my-0 width-fit-content" />

    <!-- Topbar Options -->
    <div class="font-weight-bold mb-2 mt-4">
      {{ $t('generalSettings.topbarOptions') }}
    </div>
    <v-data-table
      :headers="headers"
      :items="applications"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort
      disable-pagination
      disable-filtering>
      <template slot="item.name" slot-scope="{item}">
        <div class="d-flex flex-column flex-sm-row align-center ms-n4 text-truncate">
          <v-icon size="20" class="me-0 me-sm-4">{{ item.icon }}</v-icon>
          {{ $t(item.name) }}
        </div>
      </template>
      <template slot="item.description" slot-scope="{item}">
        <div class="d-flex align-center text-start ms-n4 text-truncate-2">
          {{ $t(item.description) }}
        </div>
      </template>
      <template slot="item.move" slot-scope="{item}">
        <div class="d-flex justify-center">
          <v-card
            class="d-flex"
            width="88"
            flat>
            <v-btn
              v-if="applications.indexOf(item) > 0"
              :title="$t('generalSettings.moveUp')"
              class="ms-1 me-auto"
              icon
              @click="moveUp(applications.indexOf(item))">
              <v-icon size="20">fa-arrow-up</v-icon>
            </v-btn>
            <v-btn
              v-if="applications.indexOf(item) < (applications.length - 1)"
              :title="$t('generalSettings.moveDown')"
              class="me-1 ms-auto"
              icon
              @click="moveDown(applications.indexOf(item))">
              <v-icon size="20">fa-arrow-down</v-icon>
            </v-btn>
          </v-card>
        </div>
      </template>
      <template slot="item.enabled" slot-scope="{item}">
        <div class="d-flex justify-center">
          <v-switch v-model="item.enabled" class="ma-auto" />
        </div>
      </template>
      <template slot="item.mobile" slot-scope="{item}">
        <div v-if="item.enabled" class="d-flex justify-center">
          <v-switch v-model="item.mobile" class="ma-auto" />
        </div>
      </template>
    </v-data-table>
  </div>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    topbarSettings: null,
    mobilePreview: false,
  }),
  computed: {
    headers() {
      return this.$root.isMobile && [{
        text: this.$t('generalSettings.header.topbarApplicationName'),
        value: 'name',
        align: 'left',
        width: '35%',
        class: 'ps-0 text-no-wrap',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationMove'),
        value: 'move',
        align: 'center',
        width: '30%',
        class: 'ps-0 text-no-wrap',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationMobile'),
        value: 'mobile',
        align: 'center',
        width: '30%',
        class: 'text-no-wrap',
      }] || [{
        text: this.$t('generalSettings.header.topbarApplicationName'),
        value: 'name',
        align: 'left',
        class: 'ps-0',
        width: '20%',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationDescription'),
        value: 'description',
        align: 'left',
        width: '35%',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationMove'),
        value: 'move',
        align: 'center',
        width: '15%',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationStatus'),
        value: 'enabled',
        align: 'center',
        width: '15%',
      }, {
        text: this.$t('generalSettings.header.topbarApplicationMobile'),
        value: 'mobile',
        align: 'center',
        width: '15%',
      }];
    },
    applications() {
      return this.topbarSettings?.applications;
    },
  },
  watch: {
    settings: {
      deep: true,
      immediate: true,
      handler() {
        if (this.settings
            && (!this.topbarSettings || JSON.stringify(this.topbarSettings) !== JSON.stringify(this.settings.topbar))) {
          this.topbarSettings = JSON.parse(JSON.stringify(this.settings.topbar));
        }
      },
    },
    topbarSettings: {
      deep: true,
      handler() {
        if (this.topbarSettings
            && this.settings
            && JSON.stringify(this.topbarSettings) !== JSON.stringify(this.settings.topbar)) {
          this.$emit('changed', this.topbarSettings);
        }
      },
    },
  },
  methods: {
    moveUp(index) {
      const application = this.topbarSettings.applications[index];
      this.topbarSettings.applications.splice(index, 1);
      this.topbarSettings.applications.splice(index - 1, 0, application);
    },
    moveDown(index) {
      const application = this.topbarSettings.applications[index];
      this.topbarSettings.applications.splice(index, 1);
      this.topbarSettings.applications.splice(index + 1, 0, application);
    },
  }
};
</script>