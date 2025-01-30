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
    <v-col
      cols="12"
      lg="7"
      class="pa-0 mb-4">
      <div class="text-header mb-6">
        {{ $t('generalSettings.companyNameTitle') }}
      </div>
      <v-card
        max-width="450px"
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
      lg="2"
      class="pa-0 mb-4 d-flex flex-column">
      <div class="mb-2 text-header">
        <help-label
          label="generalSettings.companyLogo.label"
          label-class="text-header"
          tooltip="generalSettings.companyLogo.tooltip">
          <template slot="helpContent">
            <p>
              {{ $t('generalSettings.companyLogo.help1') }}
            </p>
            <p>
              {{ $t('generalSettings.companyLogo.help2') }}
            </p>
            <p>
              {{ $t('generalSettings.companyLogo.help3') }}
            </p>
          </template>
        </help-label>
      </div>
      <portal-general-settings-company-logo
        ref="companyLogo"
        v-model="logoUploadId"
        :branding="branding"
        class="my-auto" />
    </v-col>
    <v-col
      cols="12"
      lg="3"
      class="pa-0 mb-4 d-flex flex-column">
      <div class="mb-2 text-header">
        {{ $t('generalSettings.companyFaviconTitle') }}
      </div>
      <portal-general-settings-company-favicon
        ref="companyFavicon"
        v-model="faviconUploadId"
        :branding="branding"
        class="my-auto" />
    </v-col>
    <v-col
      cols="12"
      lg="8"
      class="pa-0">
      <portal-general-settings-branding-site-preview />
    </v-col>
    <v-col
      cols="12"
      lg="4"
      class="pa-0">
      <portal-general-settings-branding-options />
    </v-col>
    <v-col
      cols="12"
      class="pa-0">
      <div :class="!isMobile && 'position-absolute b-0 r-0' || ''" class="d-flex justify-end mt-2 pb-2">
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
    <portal-general-settings-branding-update-colors-drawer
      :theme-colors="{
        'primaryColor': primaryColor,
        'secondaryColor': secondaryColor,
        'tertiaryColor': tertiaryColor
      }" />
    <portal-general-settings-branding-top-bar-styling-drawer :top-bar-styling-properties="topBarStylingProperties" />
    <portal-general-settings-branding-sidebar-styling-drawer :side-bar-styling-properties="sideBarStylingProperties" />
    <portal-general-settings-branding-drawer-styling :drawer-styling-properties="drawerStylingProperties" />
  </v-row>
</template>
<script>
export default {
  props: {
    branding: {
      type: Object,
      default: null,
    },
    customCss: {
      type: String,
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
    fullWindowWidth: '100%',
    fullWindow: null,
    faviconUploadId: null,
    originalBackgroundProperties: null,
    backgroundProperties: null,
    defaultBackgroundColor: '#F2F2F2FF',
    topBarStylingProperties: null,
    isTopBarStylingPropertiesChanged: false,
    sideBarStylingProperties: null,
    isSideBarStylingPropertiesChanged: false,
    drawerStylingProperties: null,
    isDrawerStylingPropertiesChanged: false,
    brandingStylingType: Object.freeze({
      TOP_BAR: 'topBar',
      SIDE_BAR: 'sideBar',
      DRAWER: 'drawer',
    }),
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
      if (this.logoUploadId || this.faviconUploadId || this.isTopBarStylingPropertiesChanged
          || this.isSideBarStylingPropertiesChanged || this.isDrawerStylingPropertiesChanged ) {
        return true;
      }
      const oldBranding = Object.assign(JSON.parse(JSON.stringify(this.branding)), {
        ...this.originalBackgroundProperties,
      });
      const newBranding = Object.assign(JSON.parse(JSON.stringify(this.branding)), {
        companyName: this.companyName,
        ...this.backgroundProperties,
        pageWidth: this.fullWindow && this.fullWindowWidth || null,
        customCss: this.customCss,
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
    backgroundProperties: {
      deep: true,
      handler() {
        this.setBackgroungPropertiesPreview();
      },
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
  created() {
    this.$root.$on('update-branding-theme-colors', this.updateBrandingThemeColors);
    this.$root.$on('update-top-bar-styling-properties', this.updateTopBarProperties);
    this.$root.$on('update-sidebar-styling-properties', this.updateSideBarProperties);
    this.$root.$on('update-drawer-styling-properties', this.updateDrawerProperties);
    this.$root.$on('reset-theme-colors', this.resetThemeStyleColors);
    this.$root.$on('reset-top-bar-styling', this.resetTopBarStylingProperties);
    this.$root.$on('reset-sidebar-styling', this.resetSidebarStylingProperties);
    this.$root.$on('reset-drawer-styling', this.resetDrawerStylingProperties);
  },
  mounted() {
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('update-branding-theme-colors', this.updateBrandingThemeColors);
    this.$root.$off('update-top-bar-styling-properties', this.updateTopBarProperties);
    this.$root.$off('update-sidebar-styling-properties', this.updateSideBarProperties);
    this.$root.$off('update-drawer-styling-properties', this.updateDrawerProperties);
    this.$root.$off('reset-theme-colors', this.resetThemeStyleColors);
    this.$root.$off('reset-top-bar-styling', this.resetTopBarStylingProperties);
    this.$root.$off('reset-sidebar-styling', this.resetSidebarStylingProperties);
    this.$root.$off('reset-drawer-styling', this.resetDrawerStylingProperties);
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
      this.backgroundProperties = {
        pageBackground: this.branding?.pageBackground || null,
        pageBackgroundSize: this.branding?.pageBackgroundSize || null,
        pageBackgroundRepeat: this.branding?.pageBackgroundRepeat || null,
        pageBackgroundPosition: this.branding?.pageBackgroundPosition || null,
        pageBackgroundColor: this.branding?.pageBackgroundColor || null,
      };
      this.logoUploadId = null;
      this.faviconUploadId = null;
      this.errorMessage = null;
      this.fullWindow = !!this.branding?.pageWidth;
      this.topBarStylingProperties = this.createStylingProperties(this.branding.themeStyle,this.branding.topBarBackground, this.brandingStylingType.TOP_BAR);
      this.sideBarStylingProperties = this.createStylingProperties(this.branding.themeStyle, this.branding.sideBarBackground, this.brandingStylingType.SIDE_BAR);
      this.drawerStylingProperties = this.createStylingProperties(this.branding.themeStyle, this.branding.drawerBackground, this.brandingStylingType.DRAWER);
    },
    setBackgroungPropertiesPreview() {
      if (this.changed && this.originalBackgroundProperties) {
        this.$root.$emit('refresh-body-style-property', {
          name: '--allPagesBackgroundColor',
          value: this.backgroundProperties.pageBackgroundColor || this.defaultBackgroundColor,
        });
        this.$root.$emit('refresh-body-style-property', {
          name: '--allPagesBackgroundRepeat',
          value: this.backgroundProperties.pageBackgroundRepeat || 'no-repeat',
        });
        this.$root.$emit('refresh-body-style-property', {
          name: '--allPagesBackgroundSize',
          value: this.backgroundProperties.pageBackgroundSize || 'unset',
        });
        this.$root.$emit('refresh-body-style-property', {
          name: '--allPagesBackgroundPosition',
          value: this.backgroundProperties.pageBackgroundPosition || 'unset',
        });
        this.$root.$emit('refresh-body-style-property', {
          name: 'background-image',
          value: null,
        });
        if (this.backgroundProperties.pageBackground?.data) {
          this.$root.$emit('refresh-body-style-property', {
            name: 'background-image',
            value: `url(${this.$utils.convertImageDataAsSrc(this.backgroundProperties.pageBackground?.data)})`,
          });
        } else if (this.backgroundProperties.pageBackgroundColor
            || (!this.backgroundProperties.pageBackground?.uploadId && !this.backgroundProperties.pageBackground?.fileId)) {
          this.$root.$emit('refresh-body-style-property', {
            name: '--allPagesBackgroundImage',
            value: 'none',
          });
        }
      }
    },
    setAsInitialized() {
      this.originalBackgroundProperties = JSON.parse(JSON.stringify(this.backgroundProperties));
    },
    save() {
      this.errorMessage = null;
      const topBarBackground = this.topBarStylingProperties.topBarBackground;
      delete this.topBarStylingProperties.topBarBackground;
      const sideBarBackground = this.sideBarStylingProperties.sideBarBackground;
      delete this.sideBarStylingProperties.sideBarBackground;
      const drawerBackground = this.drawerStylingProperties.drawerBackground;
      delete this.drawerStylingProperties.drawerBackground;
      let themeStyle = {
        primaryColor: this.primaryColor,
        secondaryColor: this.secondaryColor,
        tertiaryColor: this.tertiaryColor,
        borderRadius: `${this.borderRadius}px`,
      };
      themeStyle = Object.assign(this.topBarStylingProperties, themeStyle);
      themeStyle = Object.assign(this.sideBarStylingProperties, themeStyle);
      themeStyle = Object.assign(this.drawerStylingProperties, themeStyle);
      const branding = Object.assign({}, this.branding);
      Object.assign(branding, {
        companyName: this.companyName,
        logo: {
          uploadId: this.logoUploadId,
        },
        favicon: {
          uploadId: this.faviconUploadId,
        },
        themeStyle: themeStyle,
        topBarBackground: topBarBackground,
        sideBarBackground: sideBarBackground,
        drawerBackground: drawerBackground,
        pageBackground: {
          uploadId: this.backgroundProperties.pageBackground?.uploadId,
        },
        pageBackgroundSize: this.backgroundProperties.pageBackgroundSize || null,
        pageBackgroundRepeat: this.backgroundProperties.pageBackgroundRepeat || null,
        pageBackgroundPosition: this.backgroundProperties.pageBackgroundPosition || null,
        pageBackgroundColor: this.backgroundProperties.pageBackgroundColor || null,
        pageWidth: this.fullWindow && this.fullWindowWidth || null,
        customCss: this.customCss,
      });
      this.$root.loading = true;
      return this.$brandingService.updateBrandingInformation(branding)
        .then(() => this.$emit('saved'))
        .then(() => {
          this.$root.$emit('alert-message', this.$t('generalSettings.savedSuccessfully'), 'success');
          this.$root.$emit('refresh-iframe');
        })
        .catch(e => this.errorMessage = String(e))
        .finally(() => this.$root.loading = false);
    },
    updateBrandingThemeColors(primary, secondary, tertiary) {
      this.primaryColor = primary;
      this.secondaryColor = secondary;
      this.tertiaryColor = tertiary;
    },
    updateTopBarProperties(topBarBackgroundProperties, topBarTextProperties) {
      this.topBarStylingProperties = this.updateStylingProperties(topBarBackgroundProperties, topBarTextProperties, this.brandingStylingType.TOP_BAR);
      this.isTopBarStylingPropertiesChanged = true;
      this.refreshTopBarPreview();
    },
    refreshTopBarPreview() {
      const properties = {
        '--allPagesTopBarTextColor': this.topBarStylingProperties.topBarTextColor,
        '--allPagesTopBarTextFontSize': this.topBarStylingProperties.topBarTextFontSize,
        '--allPagesTopBarTextFontStyle': this.topBarStylingProperties.topBarTextFontStyle,
        '--allPagesTopBarTextFontWeight': this.topBarStylingProperties.topBarTextFontWeight,
        '--allPagesTopBarBackgroundColor': this.topBarStylingProperties.topBarBackgroundColor,
        '--allPagesTopBarBackgroundPosition': this.topBarStylingProperties.topBarBackgroundPosition,
        '--allPagesTopBarBackgroundRepeat': this.topBarStylingProperties.topBarBackgroundRepeat,
        '--allPagesTopBarBackgroundSize': this.topBarStylingProperties.topBarBackgroundSize,
      };

      // Determine the background image property
      if (this.topBarStylingProperties.topBarBackground?.data) {
        let url = `url(${this.$utils.convertImageDataAsSrc(this.topBarStylingProperties.topBarBackground?.data)})`;
        if (this.topBarStylingProperties?.topBarBackgroundImage) {
          url = `${url}, ${this.topBarStylingProperties.topBarBackgroundImage}`;
        }
        properties['--allPagesTopBarBackgroundImage'] = url;
      } else if (this.topBarStylingProperties.topBarBackground?.fileId) {
        properties['--allPagesTopBarBackgroundImage'] = `url(/portal/rest/v1/platform/branding/topBarBackground?v=") ${', ' && this.topBarStylingProperties?.topBarBackgroundImage || ''}`;
      } else if (this.topBarStylingProperties?.topBarBackgroundImage) {
        properties['--allPagesTopBarBackgroundImage'] = this.topBarStylingProperties?.topBarBackgroundImage;
      } else {
        properties['--allPagesTopBarBackgroundImage'] = 'none';
      }
      this.$root.$emit('refresh-style-properties', { detail: properties });
    },
    updateSideBarProperties(sideBarBackgroundProperties, sideBarTextProperties) {
      this.sideBarStylingProperties = this.updateStylingProperties(sideBarBackgroundProperties, sideBarTextProperties, this.brandingStylingType.SIDE_BAR);
      this.isSideBarStylingPropertiesChanged = true;
      this.refreshSideBarPreview();
    },
    refreshSideBarPreview() {
      const properties = {
        '--allPagesSideBarTextColor': this.sideBarStylingProperties.sideBarTextColor,
        '--allPagesSideBarTextFontSize': this.sideBarStylingProperties.sideBarTextFontSize,
        '--allPagesSideBarTextFontStyle': this.sideBarStylingProperties.sideBarTextFontStyle,
        '--allPagesSideBarTextFontWeight': this.sideBarStylingProperties.sideBarTextFontWeight,
        '--allPagesSideBarTextSubtitleColor': this.sideBarStylingProperties.sideBarTextSubtitleColor,
        '--allPagesSideBarTextSubtitleFontSize': this.sideBarStylingProperties.sideBarTextSubtitleFontSize,
        '--allPagesSideBarTextSubtitleFontStyle': this.sideBarStylingProperties.sideBarTextSubtitleFontStyle,
        '--allPagesSideBarTextSubtitleFontWeight': this.sideBarStylingProperties.sideBarTextSubtitleFontWeight,
        '--allPagesSideBarBackgroundColor': this.sideBarStylingProperties.sideBarBackgroundColor,
        '--allPagesSideBarBackgroundPosition': this.sideBarStylingProperties.sideBarBackgroundPosition,
        '--allPagesSideBarBackgroundRepeat': this.sideBarStylingProperties.sideBarBackgroundRepeat,
        '--allPagesSideBarBackgroundSize': this.sideBarStylingProperties.sideBarBackgroundSize,
      };

      // Determine the background image property
      if (this.sideBarStylingProperties.sideBarBackground?.data) {
        let url = `url(${this.$utils.convertImageDataAsSrc(this.sideBarStylingProperties.sideBarBackground?.data)})`;
        if (this.sideBarStylingProperties?.sideBarBackgroundImage) {
          url = `${url}, ${this.sideBarStylingProperties.sideBarBackgroundImage}`;
        }
        properties['--allPagesSideBarBackgroundImage'] = url;
      } else if (this.sideBarStylingProperties.sideBarBackground?.fileId) {
        properties['--allPagesSideBarBackgroundImage'] = `url(/portal/rest/v1/platform/branding/sideBarBackground?v=") ${', ' && this.sideBarStylingProperties?.backgroundImage || ';'}`;
      } else if (this.sideBarStylingProperties?.sideBarBackgroundImage) {
        properties['--allPagesSideBarBackgroundImage'] = this.sideBarStylingProperties?.sideBarBackgroundImage;
      } else {
        properties['--allPagesSideBarBackgroundImage'] = 'none';
      }
      this.$root.$emit('refresh-style-properties', { detail: properties });
    },
    updateDrawerProperties(drawerBackgroundProperties, drawerTextProperties) {
      this.drawerStylingProperties = this.updateStylingProperties(drawerBackgroundProperties, drawerTextProperties, this.brandingStylingType.DRAWER);
      this.isDrawerStylingPropertiesChanged = true;
      this.refreshDrawerPreview();
    },
    refreshDrawerPreview() {
      const properties = {
        '--allPagesDrawerTextColor': this.drawerStylingProperties.drawerTextColor,
        '--allPagesDrawerTextFontSize': this.drawerStylingProperties.drawerTextFontSize,
        '--allPagesDrawerTextFontStyle': this.drawerStylingProperties.drawerTextFontStyle,
        '--allPagesDrawerTextFontWeight': this.drawerStylingProperties.drawerTextFontWeight,
        '--allPagesDrawerTextSubtitleColor': this.drawerStylingProperties.drawerTextSubtitleColor,
        '--allPagesDrawerTextSubtitleFontSize': this.drawerStylingProperties.drawerTextSubtitleFontSize,
        '--allPagesDrawerTextSubtitleFontStyle': this.drawerStylingProperties.drawerTextSubtitleFontStyle,
        '--allPagesDrawerTextSubtitleFontWeight': this.drawerStylingProperties.drawerTextSubtitleFontWeight,
        '--allPagesDrawerTextTitleColor': this.drawerStylingProperties.drawerTextTitleColor,
        '--allPagesDrawerTextTitleFontSize': this.drawerStylingProperties.drawerTextTitleFontSize,
        '--allPagesDrawerTextTitleFontStyle': this.drawerStylingProperties.drawerTextTitleFontStyle,
        '--allPagesDrawerTextTitleFontWeight': this.drawerStylingProperties.drawerTextTitleFontWeight,
        '--allPagesDrawerTextHeaderColor': this.drawerStylingProperties.drawerTextHeaderColor,
        '--allPagesDrawerTextHeaderFontSize': this.drawerStylingProperties.drawerTextHeaderFontSize,
        '--allPagesDrawerTextHeaderFontStyle': this.drawerStylingProperties.drawerTextHeaderFontStyle,
        '--allPagesDrawerTextHeaderFontWeight': this.drawerStylingProperties.drawerTextHeaderFontWeight,
        '--allPagesDrawerBackgroundColor': this.drawerStylingProperties.drawerBackgroundColor,
        '--allPagesDrawerBackgroundPosition': this.drawerStylingProperties.drawerBackgroundPosition,
        '--allPagesDrawerBackgroundRepeat': this.drawerStylingProperties.drawerBackgroundRepeat,
        '--allPagesDrawerBackgroundSize': this.drawerStylingProperties.drawerBackgroundSize,
      };

      // Determine the background image property
      if (this.drawerStylingProperties.drawerBackground?.data) {
        let url = `url(${this.$utils.convertImageDataAsSrc(this.drawerStylingProperties.drawerBackground?.data)})`;
        if (this.drawerStylingProperties?.drawerBackgroundImage) {
          url = `${url}, ${this.drawerStylingProperties.drawerBackgroundImage}`;
        }
        properties['--allPagesDrawerBackgroundImage'] = url;
      } else if (this.drawerStylingProperties.drawerBackground?.fileId && this.drawerStylingProperties?.drawerBackgroundImage) {
        properties['--allPagesDrawerBackgroundImage'] = `url(/portal/rest/v1/platform/branding/drawerBackground?v="), ${this.drawerStylingProperties?.drawerBackgroundImage}`;
      } else if (this.drawerStylingProperties?.drawerBackgroundImage) {
        properties['--allPagesDrawerBackgroundImage'] = this.drawerStylingProperties?.drawerBackgroundImage;
      } else {
        properties['--allPagesDrawerBackgroundImage'] = 'none';
      }
      this.$root.$emit('refresh-style-properties', { detail: properties });
    },
    createStylingProperties(themeStyle, background, type) {
      const properties =  {
        [`${type}BackgroundColor`]: themeStyle?.[`${type}BackgroundColor`] || null,
        [`${type}BackgroundPosition`]: themeStyle?.[`${type}BackgroundPosition`] || null,
        [`${type}BackgroundRepeat`]: themeStyle?.[`${type}BackgroundRepeat`] || null,
        [`${type}BackgroundSize`]: themeStyle?.[`${type}BackgroundSize`] || null,
        [`${type}BackgroundImage`]: themeStyle?.[`${type}BackgroundImage`] || null,
        [`${type}Background`]: background || null,
        [`${type}TextColor`]: themeStyle?.[`${type}TextColor`] || null,
        [`${type}TextFontSize`]: themeStyle?.[`${type}TextFontSize`] || null,
        [`${type}TextFontStyle`]: themeStyle?.[`${type}TextFontStyle`] || null,
        [`${type}TextFontWeight`]: themeStyle?.[`${type}TextFontWeight`] || null,
      };
      if (type !== 'topBar') {
        properties[`${type}TextSubtitleColor`] = themeStyle?.[`${type}TextSubtitleColor`] || null;
        properties[`${type}TextSubtitleFontSize`] = themeStyle?.[`${type}TextSubtitleFontSize`] || null;
        properties[`${type}TextSubtitleFontStyle`] = themeStyle?.[`${type}TextSubtitleFontStyle`] || null;
        properties[`${type}TextSubtitleFontWeight`] = themeStyle?.[`${type}TextSubtitleFontWeight`] || null;
      }
      if (type !== 'topBar' && type !== 'sideBar') {
        properties[`${type}TextTitleColor`] = themeStyle?.[`${type}TextTitleColor`] || null;
        properties[`${type}TextTitleFontSize`] = themeStyle?.[`${type}TextTitleFontSize`] || null;
        properties[`${type}TextTitleFontStyle`] = themeStyle?.[`${type}TextTitleFontStyle`] || null;
        properties[`${type}TextTitleFontWeight`] = themeStyle?.[`${type}TextTitleFontWeight`] || null;
        properties[`${type}TextHeaderColor`] = themeStyle?.[`${type}TextHeaderColor`] || null;
        properties[`${type}TextHeaderFontSize`] = themeStyle?.[`${type}TextHeaderFontSize`] || null;
        properties[`${type}TextHeaderFontStyle`] = themeStyle?.[`${type}TextHeaderFontStyle`] || null;
        properties[`${type}TextHeaderFontWeight`] = themeStyle?.[`${type}TextHeaderFontWeight`] || null;
      }
      return properties;
    },
    updateStylingProperties(backgroundProperties, textProperties, type) {
      const properties =  {
        [`${type}BackgroundColor`]: backgroundProperties.backgroundColor,
        [`${type}BackgroundPosition`]: backgroundProperties.backgroundPosition,
        [`${type}BackgroundRepeat`]: backgroundProperties.backgroundRepeat || 'unset',
        [`${type}BackgroundSize`]: backgroundProperties.backgroundSize || 'unset',
        [`${type}BackgroundImage`]: backgroundProperties.backgroundEffect || null,
        [`${type}Background`]: backgroundProperties.background,
        [`${type}TextColor`]: textProperties.textColor,
        [`${type}TextFontSize`]: textProperties.textFontSize,
        [`${type}TextFontStyle`]: textProperties.textFontStyle,
        [`${type}TextFontWeight`]: textProperties.textFontWeight,
      };
      if (type !== this.brandingStylingType.TOP_BAR) {
        properties[`${type}TextSubtitleColor`] = textProperties.textSubtitleColor;
        properties[`${type}TextSubtitleFontSize`] = textProperties.textSubtitleFontSize;
        properties[`${type}TextSubtitleFontStyle`] = textProperties.textSubtitleFontStyle;
        properties[`${type}TextSubtitleFontWeight`] = textProperties.textSubtitleFontWeight;
      }
      // Add title and header properties if type is drawer
      if (type !== this.brandingStylingType.TOP_BAR && type !== this.brandingStylingType.SIDE_BAR) {
        properties[`${type}TextTitleColor`] = textProperties.textTitleColor;
        properties[`${type}TextTitleFontSize`] = textProperties.textTitleFontSize;
        properties[`${type}TextTitleFontStyle`] = textProperties.textTitleFontStyle;
        properties[`${type}TextTitleFontWeight`] = textProperties.textTitleFontWeight;
        properties[`${type}TextHeaderColor`] = textProperties.textHeaderColor;
        properties[`${type}TextHeaderFontSize`] = textProperties.textHeaderFontSize;
        properties[`${type}TextHeaderFontStyle`] = textProperties.textHeaderFontStyle;
        properties[`${type}TextHeaderFontWeight`] = textProperties.textHeaderFontWeight;
      }
      return properties;
    },
    resetThemeStyleColors() {
      this.primaryColor = this.$root.defaultBrandingThemeStyle?.primaryColor;
      this.secondaryColor = this.$root.defaultBrandingThemeStyle?.secondaryColor;
      this.tertiaryColor = this.$root.defaultBrandingThemeStyle?.tertiaryColor;
    },
    resetTopBarStylingProperties() {
      this.topBarStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.TOP_BAR);
      this.isTopBarStylingPropertiesChanged = true;
      this.refreshTopBarPreview();
    },
    resetSidebarStylingProperties() {
      this.sideBarStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.SIDE_BAR);
      this.isSideBarStylingPropertiesChanged = true;
      this.refreshSideBarPreview();
    },
    resetDrawerStylingProperties() {
      this.drawerStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.DRAWER);
      this.isDrawerStylingPropertiesChanged = true;
      this.refreshDrawerPreview();
    }

  }
};
</script>
