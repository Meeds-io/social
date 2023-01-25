<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-row class="ma-0">
    <v-col cols="12" class="pa-0">
      <h4 class="font-weight-bold mb-0 mt-8">
        {{ $t('generalSettings.displayCharacteristics') }}
      </h4>
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <h4 class="mb-0 mt-4">
        {{ $t('generalSettings.companyNameTitle') }}
      </h4>
      <h6 class="text-subtitle grey--text me-2">
        {{ $t('generalSettings.companyNameSubtitle') }}
      </h6>
      <v-card
        max-width="350px"
        class="me-2"
        flat>
        <v-text-field
          id="companyName"
          v-model="companyName"
          :placeholder="$t('generalSettings.companyNamePlaceholder')"
          class="setup-company-name border-box-sizing pt-0"
          name="companyName"
          type="text"
          autofocus="autofocus"
          aria-required="true"
          required="required"
          outlined
          dense />
      </v-card>
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <h4 class="mb-0 mt-4">
        {{ $t('generalSettings.companyLogoTitle') }}
      </h4>
      <h6 class="text-subtitle grey--text me-2">
        {{ $t('generalSettings.companyLogoSubtitle') }}
      </h6>
      <portal-general-settings-company-logo
        ref="companyLogo"
        v-model="logoUploadId"
        :branding="branding"
        class="mt-n2" />
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <h4 class="mb-0 mt-4">
        {{ $t('generalSettings.themeColorsTitle') }}
      </h4>
      <h6 class="text-subtitle grey--text me-2">
        {{ $t('generalSettings.themeColorsSubtitle') }}
      </h6>
      <div class="d-flex flex-wrap mt-n2">
        <div>
          <portal-general-settings-color-picker
            v-model="primaryColor"
            :label="$t('generalSettings.primaryColor.label')" />
        </div>
        <div>
          <portal-general-settings-color-picker
            v-model="secondaryColor"
            :label="$t('generalSettings.secondaryColor.label')" />
        </div>
        <div>
          <portal-general-settings-color-picker
            v-model="tertiaryColor"
            :label="$t('generalSettings.tertiaryColor.label')" />
        </div>
      </div>
    </v-col>
    <v-col
      cols="12"
      lg="6"
      class="pa-0">
      <h4 class="mb-0 mt-4">
        {{ $t('generalSettings.companyFaviconTitle') }}
      </h4>
      <h6 class="text-subtitle grey--text me-2 mb-3">
        {{ $t('generalSettings.companyFaviconSubtitle') }}
      </h6>
      <portal-general-settings-company-favicon
        ref="companyFavicon"
        v-model="faviconUploadId"
        :branding="branding" />
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    branding: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    companyName: null,
    primaryColor: null,
    secondaryColor: null,
    tertiaryColor: null,
    errorMessage: null,
    logoUploadId: null,
    faviconUploadId: null,
  }),
  computed: {
    defaultCompanyName() {
      return this.branding?.companyName;
    },
    defaultPrimaryColor() {
      return this.branding?.themeColors?.primaryColor;
    },
    defaultSecondaryColor() {
      return this.branding?.themeColors?.secondaryColor;
    },
    defaultTertiaryColor() {
      return this.branding?.themeColors?.tertiaryColor;
    },
    isValidForm() {
      return this.companyName?.length
          && this.primaryColor?.length
          && this.secondaryColor?.length
          && this.tertiaryColor?.length;
    },
    changed() {
      if (!this.branding) {
        return false;
      }
      if (this.logoUploadId || this.faviconUploadId) {
        return true;
      }
      const oldBranding = JSON.parse(JSON.stringify(this.branding));
      const newBranding = Object.assign(JSON.parse(JSON.stringify(this.branding)), {
        companyName: this.companyName,
      });
      newBranding.themeColors.primaryColor = this.primaryColor;
      newBranding.themeColors.secondaryColor = this.secondaryColor;
      newBranding.themeColors.tertiaryColor = this.tertiaryColor;
      return JSON.stringify(oldBranding) !== JSON.stringify(newBranding);
    },
  },
  watch: {
    isValidForm() {
      this.$emit('validity-check', this.isValidForm);
    },
    changed() {
      this.$emit('changed', this.changed);
    },
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t(this.errorMessage), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },
  methods: {
    init() {
      this.$refs.companyLogo?.resetLogo();
      this.$refs.companyFavicon?.resetFavicon();
      this.companyName = this.defaultCompanyName;
      this.primaryColor = this.defaultPrimaryColor;
      this.secondaryColor = this.defaultSecondaryColor;
      this.tertiaryColor = this.defaultTertiaryColor;
      this.logoUploadId = null;
      this.faviconUploadId = null;
      this.errorMessage = null;
    },
    preSave(branding) {
      Object.assign(branding, {
        companyName: this.companyName,
        themeColors: {
          primaryColor: this.primaryColor,
          secondaryColor: this.secondaryColor,
          tertiaryColor: this.tertiaryColor,
        },
        logo: {
          uploadId: this.logoUploadId,
        },
        favicon: {
          uploadId: this.faviconUploadId,
        },
      });
    },
  }
};
</script>
