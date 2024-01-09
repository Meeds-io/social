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
      <h4 class="font-weight-bold mt-0">
        {{ $t('generalSettings.displayCharacteristics') }}
      </h4>
    </v-col>
    <v-col
      cols="12"
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
    <v-col 
      cols="12"
      class="pa-0">
      <h4 class="mb-0 mt-4">
        {{ $t('generalSettings.widgetAndAppStyle.title') }}
      </h4>
      <h6 class="text-subtitle grey--text me-2 mb-3">
        {{ $t('generalSettings.widgetAndAppStyle.subtitle') }}
      </h6>
      <portal-general-settings-border-radius
        v-model="borderRadius"
        ref="borderRadius"
        @input="borderRadius = $event" />
    </v-col>
    <v-col
      cols="12"
      class="pa-0">
      <div :class="!isMobile && 'position-absolute b-0 r-0' || ''" class="d-flex justify-end pb-5">
        <v-btn
          :aria-label="$t('generalSettings.cancel')"
          :disabled="loading"
          class="btn cancel-button me-4"
          elevation="0"
          @click="$emit('close')">
          <span class="text-none">
            {{ $t('generalSettings.cancel') }}
          </span>
        </v-btn>
        <v-btn
          :aria-label="$t('generalSettings.apply')"
          :disabled="!validForm"
          :loading="loading"
          color="primary"
          class="btn btn-primary register-button"
          elevation="0"
          @click="save">
          <span class="text-capitalize">
            {{ $t('generalSettings.apply') }}
          </span>
        </v-btn>
      </div>
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
    borderRadius: null,
    errorMessage: null,
    logoUploadId: null,
    faviconUploadId: null,
  }),
  computed: {
    defaultCompanyName() {
      return this.branding?.companyName;
    },
    defaultPrimaryColor() {
      return this.branding?.themeStyle?.primaryColor;
    },
    defaultSecondaryColor() {
      return this.branding?.themeStyle?.secondaryColor;
    },
    defaultTertiaryColor() {
      return this.branding?.themeStyle?.tertiaryColor;
    },
    defaultBorderRadius() {
      return this.branding?.themeStyle?.borderRadius && Number(this.branding.themeStyle.borderRadius.split('px')[0]);
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    validForm() {
      return this.changed && this.isValidForm;
    },
    isValidForm() {
      return this.companyName?.length
          && this.primaryColor?.length
          && this.secondaryColor?.length
          && this.tertiaryColor?.length
          && this.borderRadius >= 0;
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
      newBranding.themeStyle.primaryColor = this.primaryColor;
      newBranding.themeStyle.secondaryColor = this.secondaryColor;
      newBranding.themeStyle.tertiaryColor = this.tertiaryColor;
      newBranding.themeStyle.borderRadius = `${this.borderRadius}px`;
      return JSON.stringify(oldBranding) !== JSON.stringify(newBranding);
    },
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t(this.errorMessage), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
    changed() {
      this.$emit('changed', this.changed);
    },
    primaryColor() {
      this.$root.$emit('refresh-style-property', {
        detail: {
          propertyName: '--allPagesPrimaryColor',
          propertyValue: this.primaryColor
        }
      });
    },
    secondaryColor() {
      this.$root.$emit('refresh-style-property', {
        detail: {
          propertyName: '--allPagesSecondaryColor',
          propertyValue: this.secondaryColor
        }
      });
    },
    tertiaryColor() {
      this.$root.$emit('refresh-style-property', {
        detail: {
          propertyName: '--allPagesTertiaryColor',
          propertyValue: this.tertiaryColor
        }
      });
    },
    borderRadius() {
      this.$root.$emit('refresh-style-property', {
        detail: {
          propertyName: '--allPagesBorderRadius',
          propertyValue: `${this.borderRadius}px`
        }
      });
    },
    companyName() {
      this.$root.$emit('refresh-company-name', this.companyName);
    },
    branding() {
      this.init();
    },
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.$refs.companyLogo?.resetLogo();
      this.$refs.companyFavicon?.resetFavicon();
      this.companyName = this.defaultCompanyName;
      this.primaryColor = this.defaultPrimaryColor;
      this.secondaryColor = this.defaultSecondaryColor;
      this.tertiaryColor = this.defaultTertiaryColor;
      this.borderRadius = this.defaultBorderRadius;
      this.logoUploadId = null;
      this.faviconUploadId = null;
      this.errorMessage = null;
    },
    save() {
      this.errorMessage = null;

      const branding = Object.assign({}, this.branding);
      Object.assign(branding, {
        companyName: this.companyName,
        themeStyle: {
          primaryColor: this.primaryColor,
          secondaryColor: this.secondaryColor,
          tertiaryColor: this.tertiaryColor,
          borderRadius: `${this.borderRadius}px`,
        },
        logo: {
          uploadId: this.logoUploadId,
        },
        favicon: {
          uploadId: this.faviconUploadId,
        },
      });

      this.$root.loading = true;
      return this.$brandingService.updateBrandingInformation(branding)
        .then(() => this.$emit('saved'))
        .then(() =>  {
          this.$root.$emit('alert-message', this.$t('generalSettings.savedSuccessfully'), 'success');
          this.$root.$emit('refresh-iframe');
        })
        .catch(e => this.errorMessage = String(e))
        .finally(() => this.$root.loading = false);
    },
  }
};
</script>
