<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div>
    <v-list-item
      v-if="customTitle"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('generalSettings.textColorTitle') }}
      </v-list-item-content>
      <v-list-item-action
        v-if="textTitleStyle"
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textTitleStyle" multiple>
          <v-btn value="bold" small aria-label="Bold">
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small aria-label="Italic">
            <v-icon size="18">fa-italic</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <number-input
          v-if="textTitleFontSize"
          v-model="textTitleFontSize"
          :min="1"
          :max="60"
          :step="1" />
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <portal-general-settings-color-picker
          v-model="textTitleColor"
          :height="'36px'"
          :width="'36px'"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="customHeader"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('generalSettings.textColorHeader') }}
      </v-list-item-content>
      <v-list-item-action
        v-if="textHeaderStyle"
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textHeaderStyle" multiple>
          <v-btn value="bold" small aria-label="Bold">
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small aria-label="Italic">
            <v-icon size="18">fa-italic</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <number-input
          v-if="textHeaderFontSize"
          v-model="textHeaderFontSize"
          :min="1"
          :max="60"
          :step="1" />
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <portal-general-settings-color-picker
          v-model="textHeaderColor"
          :height="'36px'"
          :width="'36px'"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="customText"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('generalSettings.textColorBody') }}
      </v-list-item-content>
      <v-list-item-action
        v-if="textStyle"
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textStyle" multiple>
          <v-btn value="bold" small aria-label="Bold">
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small aria-label="Italic">
            <v-icon size="18">fa-italic</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <number-input
          v-if="textFontSize"
          v-model="textFontSize"
          :min="1"
          :max="60"
          :step="1" />
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <portal-general-settings-color-picker
          v-model="textColor"
          :height="'36px'"
          :width="'36px'"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="customSubTitle"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('generalSettings.textColorSubtitle') }}
      </v-list-item-content>
      <v-list-item-action
        v-if="textSubtitleStyle"
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textSubtitleStyle" multiple>
          <v-btn value="bold" small aria-label="Bold">
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small aria-label="Italic">
            <v-icon size="18">fa-italic</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <number-input
          v-if="textSubtitleFontSize"
          v-model="textSubtitleFontSize"
          :min="1"
          :max="60"
          :step="1" />
      </v-list-item-action>
      <v-list-item-action
        class="me-0 my-auto">
        <portal-general-settings-color-picker
          v-model="textSubtitleColor"
          :height="'36px'"
          :width="'36px'"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    customText: {
      type: Boolean,
      default: true
    },
    customTitle: {
      type: Boolean,
      default: true
    },
    customSubTitle: {
      type: Boolean,
      default: true
    },
    customHeader: {
      type: Boolean,
      default: true
    }

  },
  data: () => ({
    branding: null,
    textTitleColor: null,
    textTitleFontSize: 0,
    textTitleStyle: [],
    textHeaderColor: null,
    textHeaderFontSize: 0,
    textHeaderStyle: [],
    textColor: null,
    textFontSize: 0,
    textStyle: [],
    textSubtitleColor: null,
    textSubtitleFontSize: 0,
    textSubtitleStyle: [],
    initialized: false,
  }),
  watch: {
    branding: {
      deep: true,
      handler() {
        if (this.branding) {
          this.$emit('input', this.branding);
        }
      },
    },
    textTitleColor() {
      if (this.initialized) {
        this.$set(this.branding, 'textTitleColor', this.textTitleColor);
      }
    },
    textTitleFontSize() {
      if (this.initialized) {
        this.$set(this.branding, 'textTitleFontSize', this.textTitleFontSize && `${this.textTitleFontSize}px`);
      }
    },
    textTitleStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.branding, 'textTitleFontWeight', this.textTitleStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.branding, 'textTitleFontStyle', this.textTitleStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
        }
      },
    },
    textHeaderColor() {
      if (this.initialized) {
        this.$set(this.branding, 'textHeaderColor', this.textHeaderColor);
      }
    },
    textHeaderFontSize() {
      if (this.initialized) {
        this.$set(this.branding, 'textHeaderFontSize', this.textHeaderFontSize && `${this.textHeaderFontSize}px`);
      }
    },
    textHeaderStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.branding, 'textHeaderFontWeight', this.textHeaderStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.branding, 'textHeaderFontStyle', this.textHeaderStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
        }
      }
    },
    textColor() {
      if (this.initialized) {
        this.$set(this.branding, 'textColor', this.textColor);
      }
    },
    textFontSize() {
      if (this.initialized) {
        this.$set(this.branding, 'textFontSize', this.textFontSize && `${this.textFontSize}px`);
      }
    },
    textStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.branding, 'textFontWeight', this.textStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.branding, 'textFontStyle', this.textStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
        }
      }
    },
    textSubtitleColor() {
      if (this.initialized) {
        this.$set(this.branding, 'textSubtitleColor', this.textSubtitleColor);
      }
    },
    textSubtitleFontSize() {
      if (this.initialized) {
        this.$set(this.branding, 'textSubtitleFontSize', this.textSubtitleFontSize && `${this.textSubtitleFontSize}px`);
      }
    },
    textSubtitleStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.branding, 'textSubtitleFontWeight', this.textSubtitleStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.branding, 'textSubtitleFontStyle', this.textSubtitleStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
        }
      }
    },
  },
  created() {
    this.branding = this.value;
    this.textTitleColor = this.branding?.textTitleColor;
    this.textTitleFontSize = this.branding?.textTitleFontSize?.replace?.('px', '');
    this.textTitleStyle = (this.branding?.textTitleFontWeight || this.branding?.textTitleFontStyle) && [] || null;
    if (this.branding?.textTitleFontWeight === 'bold') {
      this.textTitleStyle.push('bold');
    }
    if (this.branding?.textTitleFontStyle === 'italic') {
      this.textTitleStyle.push('italic');
    }

    this.textHeaderColor = this.branding?.textHeaderColor;
    this.textHeaderFontSize = this.branding?.textHeaderFontSize?.replace?.('px', '');
    this.textHeaderStyle = (this.branding?.textHeaderFontWeight || this.branding.textHeaderFontStyle) && [] || null;
    if (this.branding?.textHeaderFontWeight === 'bold') {
      this.textHeaderStyle.push('bold');
    }
    if (this.branding?.textHeaderFontStyle === 'italic') {
      this.textHeaderStyle.push('italic');
    }

    this.textColor = this.branding?.textColor;
    this.textFontSize = this.branding?.textFontSize?.replace?.('px', '');
    this.textStyle = (this.branding?.textFontWeight || this.branding?.textFontStyle) && [] || null;
    if (this.branding?.textFontWeight === 'bold') {
      this.textStyle.push('bold');
    }
    if (this.branding?.textFontStyle === 'italic') {
      this.textStyle.push('italic');
    }
    this.textSubtitleColor = this.branding?.textSubtitleColor;
    this.textSubtitleFontSize = this.branding?.textSubtitleFontSize?.replace?.('px', '');
    this.textSubtitleStyle = (this.branding?.textSubtitleFontWeight || this.branding?.textSubtitleFontStyle) && [] || null;
    if (this.branding?.textSubtitleFontWeight === 'bold') {
      this.textSubtitleStyle.push('bold');
    }
    if (this.branding?.textSubtitleFontStyle === 'italic') {
      this.textSubtitleStyle.push('italic');
    }

    this.enabled = !!this.textTitleColor || !!this.textHeaderColor || !!this.textColor || !!this.textSubtitleColor;
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>