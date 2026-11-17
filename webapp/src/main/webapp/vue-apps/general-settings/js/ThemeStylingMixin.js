/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

/**
 * The model the area drawers edit, for the one theme being edited: the
 * drawers answer through $root events, this mixin keeps their results as one
 * flat style and exports them back (see ThemeSettingsMapper). The host
 * component holds the edited theme's branding-shaped model in `branding`.
 */
export default {
  data: () => ({
    primaryColor: null,
    secondaryColor: null,
    fontFamily: null,
    defaultCustomPageWidth: '1320px',
    defaultPageBackground: '#F0F0F0',
    topBarStylingProperties: null,
    sideBarStylingProperties: null,
    drawerStylingProperties: null,
    pageStylingProperties: null,
    brandingStylingType: Object.freeze({
      TOP_BAR: 'topBar',
      SIDE_BAR: 'sideBar',
      DRAWER: 'drawer',
    }),
    // A platform-wide theme variable that is not set is simply absent from the theme style returned by the server
    // (an empty configured default); 'none' is the not-set value of the *BackgroundImage variables
    themeUnsetValue: 'initial',
    // Margins are on the platform scale at every level: 20 = no extra margin (a value of 32 renders 12px)
    marginNeutral: 20,
    applicationTextKeys: Object.freeze([
      'textTitleColor', 'textTitleFontSize', 'textTitleFontWeight', 'textTitleFontStyle',
      'textHeaderColor', 'textHeaderFontSize', 'textHeaderFontWeight', 'textHeaderFontStyle',
      'textColor', 'textFontSize', 'textFontWeight', 'textFontStyle',
      'textSubtitleColor', 'textSubtitleFontSize', 'textSubtitleFontWeight', 'textSubtitleFontStyle',
      // The platform-wide icon colour of page content rides the same read, save and preview paths as the text keys
      'iconColor',
    ]),
    sides: Object.freeze(['Top', 'Right', 'Bottom', 'Left']),
    textBackgroundTypes: Object.freeze(['Title', 'Header']),
    applicationBoxShadowValue: '0px 3px 3px -2px rgba(0, 0, 0, 0.2), 0px 3px 4px 0px rgba(0, 0, 0, 0.14), 0px 1px 8px 0px rgba(0, 0, 0, 0.12)',
  }),
  computed: {
    supportedFontFamilies() {
      return this.$root.branding?.supportedFontFamilies || [];
    },
  },
  watch: {
    '$root.defaultBrandingThemeStyle'() {
      if (this.branding && this.pageStylingProperties) {
        this.pageStylingProperties.applicationStyling = this.createApplicationStyling(this.branding.themeStyle, this.branding);
      }
    },
  },
  created() {
    this.$root.$on('update-branding-theme-colors', this.updateBrandingThemeColors);
    this.$root.$on('update-branding-font-family', this.updateBrandingFontFamily);
    this.$root.$on('update-top-bar-styling-properties', this.updateTopBarProperties);
    this.$root.$on('update-sidebar-styling-properties', this.updateSideBarProperties);
    this.$root.$on('update-drawer-styling-properties', this.updateDrawerProperties);
    this.$root.$on('reset-theme-colors', this.resetThemeStyleColors);
    this.$root.$on('reset-font-family', this.resetFontFamily);
    this.$root.$on('reset-top-bar-styling', this.resetTopBarStylingProperties);
    this.$root.$on('reset-sidebar-styling', this.resetSidebarStylingProperties);
    this.$root.$on('reset-drawer-styling', this.resetDrawerStylingProperties);
    this.$root.$on('update-page-styling-properties', this.updatePageStylingProperties);
    this.$root.$on('reset-page-styling', this.resetPageStyling);
  },
  beforeDestroy() {
    this.$root.$off('update-branding-theme-colors', this.updateBrandingThemeColors);
    this.$root.$off('update-branding-font-family', this.updateBrandingFontFamily);
    this.$root.$off('update-top-bar-styling-properties', this.updateTopBarProperties);
    this.$root.$off('update-sidebar-styling-properties', this.updateSideBarProperties);
    this.$root.$off('update-drawer-styling-properties', this.updateDrawerProperties);
    this.$root.$off('reset-theme-colors', this.resetThemeStyleColors);
    this.$root.$off('reset-font-family', this.resetFontFamily);
    this.$root.$off('reset-top-bar-styling', this.resetTopBarStylingProperties);
    this.$root.$off('reset-sidebar-styling', this.resetSidebarStylingProperties);
    this.$root.$off('reset-drawer-styling', this.resetDrawerStylingProperties);
    this.$root.$off('update-page-styling-properties', this.updatePageStylingProperties);
    this.$root.$off('reset-page-styling', this.resetPageStyling);
  },
  methods: {
    // Loads the model from the edited theme's branding-shaped style (`this.branding`)
    initStyling() {
      this.primaryColor = this.branding.themeStyle?.primaryColor;
      this.secondaryColor = this.branding.themeStyle?.secondaryColor;
      this.fontFamily = this.branding.themeStyle?.fontFamily;
      this.pageStylingProperties = {
        pageBackground: this.branding.pageBackground || null,
        pageBackgroundSize: this.branding.pageBackgroundSize || null,
        pageBackgroundRepeat: this.branding.pageBackgroundRepeat || null,
        pageBackgroundPosition: this.branding.pageBackgroundPosition || null,
        pageBackgroundAttachment: this.branding.pageBackgroundAttachment || null,
        pageBackgroundColor: this.branding.pageBackgroundColor || null,
        pageBackgroundEffect: this.branding.pageBackgroundEffect || null,
        pageWidth: this.branding.pageWidth,
        pageMargins: this.createPageMargins(this.branding.themeStyle),
        applicationStyling: this.createApplicationStyling(this.branding.themeStyle, this.branding),
      };
      this.topBarStylingProperties = this.createStylingProperties(this.branding.themeStyle, this.branding.topBarBackground, this.brandingStylingType.TOP_BAR);
      this.sideBarStylingProperties = this.createStylingProperties(this.branding.themeStyle, this.branding.sideBarBackground, this.brandingStylingType.SIDE_BAR);
      this.drawerStylingProperties = this.createStylingProperties(this.branding.themeStyle, this.branding.drawerBackground, this.brandingStylingType.DRAWER);
    },
    // The edited style: {themeStyle: flat theme variables, files: background file of each area}
    exportStyling() {
      const without = (properties, key) => {
        const copy = Object.assign({}, properties);
        delete copy[key];
        return copy;
      };
      const page = this.pageStylingProperties;
      const applicationStyling = page.applicationStyling;
      const themeStyle = Object.assign({},
        without(this.topBarStylingProperties, 'topBarBackground'),
        without(this.sideBarStylingProperties, 'sideBarBackground'),
        without(this.drawerStylingProperties, 'drawerBackground'),
        {
          primaryColor: this.primaryColor,
          secondaryColor: this.secondaryColor,
          fontFamily: this.fontFamily,
          pageWidth: page.pageWidth,
          pageBackgroundColor: page.pageBackgroundColor,
          pageBackgroundImage: page.pageBackgroundEffect,
          pageBackgroundSize: page.pageBackgroundSize,
          pageBackgroundRepeat: page.pageBackgroundRepeat,
          pageBackgroundPosition: page.pageBackgroundPosition,
          pageBackgroundAttachment: page.pageBackgroundAttachment,
        },
        this.toApplicationThemeStyle(applicationStyling),
        this.toPageMarginsThemeStyle(page.pageMargins));
      return {
        themeStyle,
        files: {
          topBarBackground: this.topBarStylingProperties.topBarBackground,
          sideBarBackground: this.sideBarStylingProperties.sideBarBackground,
          drawerBackground: this.drawerStylingProperties.drawerBackground,
          appBackground: applicationStyling?.appBackground,
          appTextTitleBackground: applicationStyling?.appTextTitleBackground,
          appTextHeaderBackground: applicationStyling?.appTextHeaderBackground,
          pageBackground: page.pageBackground,
        },
      };
    },
    // the preview keeps the fixed top bar through a body class: the other values come from the resolved theme
    refreshStickyTopBar() {
      this.$root.$emit('refresh-body-class', {
        name: 'topbar-sticky',
        enabled: this.topBarStylingProperties.topBarSticky === 'true',
      });
    },
    updateBrandingThemeColors(primary, secondary) {
      this.primaryColor = primary;
      this.secondaryColor = secondary;
    },
    updateBrandingFontFamily(fontFamily) {
      this.fontFamily = fontFamily;
      this.isFontFamilyReset = false;
    },
    updateTopBarProperties(topBarBackgroundProperties, topBarTextProperties) {
      this.topBarStylingProperties = this.updateStylingProperties(topBarBackgroundProperties, topBarTextProperties, this.brandingStylingType.TOP_BAR);
      this.isTopBarStylingPropertiesChanged = true;
      this.refreshStickyTopBar();
    },
    updateSideBarProperties(sideBarBackgroundProperties, sideBarTextProperties) {
      this.sideBarStylingProperties = this.updateStylingProperties(sideBarBackgroundProperties, sideBarTextProperties, this.brandingStylingType.SIDE_BAR);
      this.isSideBarStylingPropertiesChanged = true;
    },
    updateDrawerProperties(drawerBackgroundProperties, drawerTextProperties) {
      this.drawerStylingProperties = this.updateStylingProperties(drawerBackgroundProperties, drawerTextProperties, this.brandingStylingType.DRAWER);
      this.isDrawerStylingPropertiesChanged = true;
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
        [`${type}IconColor`]: themeStyle?.[`${type}IconColor`] || null,
      };
      if (type === this.brandingStylingType.TOP_BAR) {
        // off sends no key, so the configured value comes back
        properties.topBarSticky = themeStyle?.topBarSticky === 'true' ? 'true' : null;
        properties.topBarBackgroundScrollColor = this.themeValue(themeStyle, 'topBarBackgroundScrollColor');
      }
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
        [`${type}IconColor`]: textProperties.iconColor || null,
      };
      if (type === this.brandingStylingType.TOP_BAR) {
        properties.topBarSticky = backgroundProperties.sticky ? 'true' : null;
        properties.topBarBackgroundScrollColor = backgroundProperties.sticky && backgroundProperties.backgroundScrollColor || null;
      }
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
    },
    resetFontFamily() {
      this.fontFamily = this.$root.defaultBrandingThemeStyle?.fontFamily;
      this.isFontFamilyReset = true;
    },
    resetTopBarStylingProperties() {
      this.topBarStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.TOP_BAR);
      this.isTopBarStylingPropertiesChanged = true;
      this.refreshStickyTopBar();
    },
    resetSidebarStylingProperties() {
      this.sideBarStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.SIDE_BAR);
      this.isSideBarStylingPropertiesChanged = true;
    },
    resetDrawerStylingProperties() {
      this.drawerStylingProperties = this.createStylingProperties(this.$root.defaultBrandingThemeStyle, {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      }, this.brandingStylingType.DRAWER);
      this.isDrawerStylingPropertiesChanged = true;
    },
    updatePageStylingProperties(backgroundProperties, pageWidth, applicationStyling, pageMargins) {
      this.pageStylingProperties = {
        pageBackground: backgroundProperties?.background,
        // no image: the size and position are no stored value (the server accepts only its vocabulary, 'unset' is none of it)
        pageBackgroundSize: backgroundProperties?.backgroundSize || null,
        pageBackgroundRepeat: backgroundProperties?.backgroundRepeat || 'no-repeat',
        pageBackgroundPosition: backgroundProperties?.backgroundPosition || null,
        pageBackgroundAttachment: backgroundProperties?.backgroundAttachment || null,
        pageBackgroundColor: backgroundProperties?.backgroundColor ,
        pageBackgroundEffect: backgroundProperties?.backgroundEffect,
        pageWidth: pageWidth,
        pageMargins: pageMargins || null,
        applicationStyling: applicationStyling || null,
      };
      this.isPageStylingPropertiesChanged = true;
    },
    // Value of a theme variable, null when it is not set ('initial' is the
    // server-side marker of an unset platform-wide setting),
    themeValue(themeStyle, key) {
      const value = themeStyle?.[key];
      return value && value !== this.themeUnsetValue && value !== 'none' ? value : null;
    },
    themePx(themeStyle, key) {
      const value = this.themeValue(themeStyle, key);
      return value === null ? null : Number(String(value).replace('px', ''));
    },
    // Page Design > Page Margins: platform default of the page padding (site and page values keep precedence)
    createPageMargins(themeStyle) {
      const px = key => this.themePx(themeStyle, key);
      // an unset side stays null: it is neither shown as 0 nor written back on the next save (the skin default keeps applying)
      return ['pageMarginTop', 'pageMarginRight', 'pageMarginBottom', 'pageMarginLeft'].some(key => px(key) !== null) && {
        marginTop: px('pageMarginTop'),
        marginRight: px('pageMarginRight'),
        marginBottom: px('pageMarginBottom'),
        marginLeft: px('pageMarginLeft'),
      } || null;
    },
    toPageMarginsThemeStyle(pageMargins) {
      if (!pageMargins) {
        return {};
      }
      // only the sides that are set travel; a null side is left to the configured or skin default
      const themeStyle = {};
      this.sides.forEach(side => {
        const value = pageMargins[`margin${side}`];
        if (value === 0 || value) {
          themeStyle[`pageMargin${side}`] = `${value}px`;
        }
      });
      return themeStyle;
    },
    emptyBrandingFile() {
      return {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      };
    },
    themeKey(field) {
      return `app${field.charAt(0).toUpperCase()}${field.slice(1)}`;
    },
    // URL of a stored branding image, null when none is stored
    brandingImagePath(file, name) {
      return file?.imageUrl || file?.fileId && `/portal/rest/v1/platform/branding/${name}?v=${file.updatedDate || 0}` || null;
    },
    // Builds the Application Styling model edited by the shared styling inputs
    // (a container-shaped object, each group off when its fields are unset)
    // from the theme variables returned by the branding service,
    createApplicationStyling(themeStyle, branding) {
      const value = key => this.themeValue(themeStyle, key);
      const px = key => this.themePx(themeStyle, key);
      const has = key => value(key) !== null;
      const files = {
        appBackground: branding?.appBackground || this.emptyBrandingFile(),
        appTextTitleBackground: branding?.appTextTitleBackground || this.emptyBrandingFile(),
        appTextHeaderBackground: branding?.appTextHeaderBackground || this.emptyBrandingFile(),
      };
      const container = {};
      // Margins on the platform scale (20 = neutral); every side at the neutral value = toggle off
      this.sides.forEach(side => {
        container[`margin${side}`] = has(`appMargin${side}`) ? px(`appMargin${side}`) : this.marginNeutral;
      });
      // Border: a colour means on; the shared input reads the shadow flag as the string 'true'
      container.borderColor = value('appBorderColor');
      container.borderSize = has('appBorderColor') ? (px('appBorderSize') || 1) : 0;
      container.boxShadow = has('appBoxShadow') ? 'true' : null;
      // Corners: per-corner keys, or a uniform radius different from the configured default (null = off)
      const corners = ['TopLeft', 'TopRight', 'BottomRight', 'BottomLeft'];
      const defaultRadius = this.$root.defaultBrandingThemeStyle?.borderRadius;
      const uniformRadius = themeStyle?.borderRadius;
      if (corners.some(corner => has(`appBorderRadius${corner}`))) {
        corners.forEach(corner => container[`radius${corner}`] = px(`appBorderRadius${corner}`) || 0);
      } else if (uniformRadius && defaultRadius && uniformRadius !== defaultRadius) {
        const radius = Number(String(uniformRadius).replace('px', '')) || 0;
        corners.forEach(corner => container[`radius${corner}`] = radius);
      } else {
        corners.forEach(corner => container[`radius${corner}`] = null);
      }
      // Background: the gradient travels in the *BackgroundImage variable, the image is a branding file
      const backgroundOn = has('appBackgroundColor') || !!files.appBackground.fileId || has('appBackgroundImage');
      container.backgroundColor = backgroundOn ? (value('appBackgroundColor') || '#FFFFFFFF') : null;
      container.backgroundEffect = this.extractBackgroundEffect(themeStyle?.appBackgroundImage);
      container.backgroundImage = this.brandingImagePath(files.appBackground, 'appBackground');
      container.backgroundSize = value('appBackgroundSize');
      container.backgroundPosition = value('appBackgroundPosition');
      container.backgroundRepeat = value('appBackgroundRepeat');
      container.backgroundAttachment = null;
      // Text
      this.applicationTextKeys.forEach(key => container[key] = value(this.themeKey(key)));
      // Title and Header text backgrounds
      this.textBackgroundTypes.forEach(type => {
        const field = `text${type}Background`;
        const key = `appText${type}Background`;
        container[`${field}Color`] = value(`${key}Color`);
        container[`${field}Effect`] = this.extractBackgroundEffect(themeStyle?.[`${key}Image`]);
        container[`${field}Image`] = this.brandingImagePath(files[key], key);
        container[`${field}Position`] = value(`${key}Position`);
        container[`${field}Size`] = value(`${key}Size`);
        container[`${field}Repeat`] = value(`${key}Repeat`);
        container[`${field}Padding`] = this.sides.some(side => has(`${key}Padding${side}`))
          && this.sides.map(side => `${px(`${key}Padding${side}`) || 0}px`).join(' ')
          || null;
        container[`${field}Radius`] = value(`${key}Radius`);
      });
      return Object.assign({ container }, files);
    },
    currentBorderRadius() {
      return this.branding?.themeStyle?.borderRadius
        || this.$root.defaultBrandingThemeStyle?.borderRadius
        || '8px';
    },
    extractBackgroundEffect(backgroundImage) {
      if (!backgroundImage || backgroundImage === 'none' || backgroundImage === this.themeUnsetValue) {
        return null;
      }
      if (backgroundImage.includes('url(')) {
        const effect = backgroundImage.split('), ')[1];
        return effect || null;
      }
      return backgroundImage;
    },
    // Converts the Application Styling model into the theme variables the
    // branding service persists (a group that is off sets no key, which
    // removes the stored setting and lets the configured default apply)
    toApplicationThemeStyle(applicationStyling) {
      const themeStyle = {};
      const container = applicationStyling?.container || {};
      const isSet = v => v === 0 || !!v;
      // Margins: the theme variable carries the platform-scale value the shared input holds (20 = neutral);
      // no key when the input is off (null sides) or every side is neutral
      const margins = this.sides.map(side => container[`margin${side}`]);
      if (margins.every(isSet) && margins.some(margin => margin !== this.marginNeutral)) {
        this.sides.forEach(side => {
          themeStyle[`appMargin${side}`] = `${container[`margin${side}`]}px`;
        });
      }
      if (container.borderColor) {
        themeStyle.appBorderColor = container.borderColor;
        themeStyle.appBorderSize = `${container.borderSize || 1}px`;
        if (container.boxShadow === true || container.boxShadow === 'true') {
          themeStyle.appBoxShadow = this.applicationBoxShadowValue;
        }
      }
      if (isSet(container.radiusBottomLeft)) {
        const uniform = container.radiusTopLeft === container.radiusTopRight
          && container.radiusTopRight === container.radiusBottomLeft
          && container.radiusBottomLeft === container.radiusBottomRight;
        if (uniform) {
          // The uniform radius keeps the existing global variable (it also shapes buttons and components)
          themeStyle.borderRadius = `${container.radiusTopLeft || 0}px`;
        } else {
          themeStyle.borderRadius = this.currentBorderRadius();
          ['TopLeft', 'TopRight', 'BottomLeft', 'BottomRight'].forEach(corner => {
            themeStyle[`appBorderRadius${corner}`] = `${container[`radius${corner}`] || 0}px`;
          });
        }
      } else {
        // Closed toggle: the configured default applies (never omit the key, the global radius also shapes buttons
        // and components); the stored value is only kept while the defaults are not loaded yet
        themeStyle.borderRadius = this.$root.defaultBrandingThemeStyle?.borderRadius
          || this.branding?.themeStyle?.borderRadius
          || '8px';
      }
      if (container.backgroundColor || container.backgroundImage) {
        themeStyle.appBackgroundColor = container.backgroundColor || '#FFFFFFFF';
        // The gradient travels in the *BackgroundImage variable, the server prepends the uploaded image URL
        themeStyle.appBackgroundImage = container.backgroundEffect || null;
        if (container.backgroundImage) {
          themeStyle.appBackgroundSize = container.backgroundSize || 'unset';
          themeStyle.appBackgroundPosition = container.backgroundPosition || 'unset';
          themeStyle.appBackgroundRepeat = container.backgroundRepeat || 'no-repeat';
        }
      }
      if (this.applicationTextKeys.some(key => container[key])) {
        this.applicationTextKeys.forEach(key => {
          if (container[key]) {
            themeStyle[this.themeKey(key)] = container[key];
          }
        });
      }
      this.textBackgroundTypes.forEach(type => {
        const field = `text${type}Background`;
        const key = `appText${type}Background`;
        if (container[`${field}Color`] || container[`${field}Image`]) {
          themeStyle[`${key}Color`] = container[`${field}Color`] || '#FFFFFFFF';
          themeStyle[`${key}Image`] = container[`${field}Effect`] || null;
          if (container[`${field}Image`]) {
            themeStyle[`${key}Size`] = container[`${field}Size`] || 'unset';
            themeStyle[`${key}Position`] = container[`${field}Position`] || 'unset';
            themeStyle[`${key}Repeat`] = container[`${field}Repeat`] || 'no-repeat';
          }
          // The shared input writes a "top right bottom left" shorthand; the skin reads one variable per side
          if (container[`${field}Padding`]) {
            const parts = container[`${field}Padding`].trim().split(/\s+/);
            this.sides.forEach((side, index) => {
              themeStyle[`${key}Padding${side}`] = `${parseInt(parts[index] || parts[0]) || 0}px`;
            });
          }
          if (container[`${field}Radius`]) {
            themeStyle[`${key}Radius`] = container[`${field}Radius`];
          }
        }
      });
      return themeStyle;
    },
    resetPageStyling() {
      this.pageStylingProperties.pageBackground = {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      };
      // Configured defaults (exo.branding.page.*) when the server exposes them, built-in ones otherwise
      this.pageStylingProperties.pageWidth = this.$root.defaultBrandingThemeStyle?.pageWidth || this.defaultCustomPageWidth;
      this.pageStylingProperties.pageBackgroundColor = this.$root.defaultBrandingThemeStyle?.pageBackgroundColor || this.defaultPageBackground;
      this.pageStylingProperties.pageBackgroundEffect = this.$root.defaultBrandingThemeStyle?.pageBackgroundEffect || null;
      this.pageStylingProperties.pageMargins = this.createPageMargins(this.$root.defaultBrandingThemeStyle);
      // Application Styling: every toggle off, configured defaults apply
      this.pageStylingProperties.applicationStyling = this.createApplicationStyling(this.$root.defaultBrandingThemeStyle, null);
      this.isPageStylingPropertiesChanged = true;
    },
    // Live preview of the platform-wide application styling: the same custom
    // properties the branding stylesheet emits on :root
  }
};
