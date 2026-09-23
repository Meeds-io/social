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
  <div>
    <div class="d-flex align-center">
      <div
        :class="textBold && 'font-weight-bold' || 'text-header'"
        class="me-auto">
        {{ $t('layout.textStyle') }}
      </div>
      <v-switch
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <v-list-item
      v-if="enabled && customTitle"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto text-body font-weight-bold">
        {{ $t('layout.textColorTitle') }}
      </v-list-item-content>
      <v-list-item-action
        v-if="textTitleStyle"
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textTitleStyle" multiple>
          <v-btn value="bold" small>
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small>
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
        <styling-color-picker
          v-model="textTitleColor"
          min-text-width="64"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <styling-text-background-input
      v-if="enabled && customTitle && textBackground"
      v-model="container"
      type-prefix="Title"
      class="mb-4">
      <template v-if="$slots['title-background-image']" #image>
        <slot name="title-background-image"></slot>
      </template>
    </styling-text-background-input>
    <v-list-item
      v-if="enabled && customHeader"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto text-body font-weight-bold">
        {{ $t('layout.textColorHeader') }}
      </v-list-item-content>
      <v-list-item-action
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textHeaderStyle" multiple>
          <v-btn value="bold" small>
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small>
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
        <styling-color-picker
          v-model="textHeaderColor"
          min-text-width="64"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <styling-text-background-input
      v-if="enabled && customHeader && textBackground"
      v-model="container"
      type-prefix="Header"
      class="mb-4">
      <template v-if="$slots['header-background-image']" #image>
        <slot name="header-background-image"></slot>
      </template>
    </styling-text-background-input>
    <v-list-item
      v-if="enabled && customText"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto text-body font-weight-bold">
        {{ $t('layout.textColorBody') }}
      </v-list-item-content>
      <v-list-item-action
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textStyle" multiple>
          <v-btn value="bold" small>
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small>
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
        <styling-color-picker
          v-model="textColor"
          min-text-width="64"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
    <v-list-item
      v-if="enabled && customSubTitle"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto text-body font-weight-bold">
        {{ $t('layout.textColorSubtitle') }}
      </v-list-item-content>
      <v-list-item-action
        class="me-0 my-auto ms-auto">
        <v-btn-toggle v-model="textSubtitleStyle" multiple>
          <v-btn value="bold" small>
            <v-icon size="18">fa-bold</v-icon>
          </v-btn>
          <v-btn value="italic" small>
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
        <styling-color-picker
          v-model="textSubtitleColor"
          min-text-width="64"
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
    pageStyle: {
      type: Boolean,
      default: false,
    },
    textBold: {
      type: Boolean,
      default: false,
    },
    // Sections offered (Branding and Theme hides the ones an area does not have)
    customTitle: {
      type: Boolean,
      default: true,
    },
    customHeader: {
      type: Boolean,
      default: true,
    },
    customText: {
      type: Boolean,
      default: true,
    },
    customSubTitle: {
      type: Boolean,
      default: true,
    },
    // Title and Header text backgrounds (the only two that exist); off for areas that have none (Branding Topbar, Sidebars, Drawers)
    textBackground: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    container: null,
    enabled: false,
    textTitleColor: null,
    textTitleFontSize: 0,
    textTitleStyle: ['bold'],
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
    container: {
      deep: true,
      handler() {
        if (this.container) {
          this.$emit('input', this.container);
        }
      },
    },
    textTitleColor() {
      if (this.initialized) {
        this.$set(this.container, 'textTitleColor', this.textTitleColor);
        this.$emit('refresh');
      }
    },
    textTitleFontSize() {
      if (this.initialized) {
        this.$set(this.container, 'textTitleFontSize', this.textTitleFontSize && `${this.textTitleFontSize}px`);
        this.$emit('refresh');
      }
    },
    textTitleStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.container, 'textTitleFontWeight', this.textTitleStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.container, 'textTitleFontStyle', this.textTitleStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
          this.$emit('refresh');
        }
      },
    },
    textHeaderColor() {
      if (this.initialized) {
        this.$set(this.container, 'textHeaderColor', this.textHeaderColor);
        this.$emit('refresh');
      }
    },
    textHeaderFontSize() {
      if (this.initialized) {
        this.$set(this.container, 'textHeaderFontSize', this.textHeaderFontSize && `${this.textHeaderFontSize}px`);
        this.$emit('refresh');
      }
    },
    textHeaderStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.container, 'textHeaderFontWeight', this.textHeaderStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.container, 'textHeaderFontStyle', this.textHeaderStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
          this.$emit('refresh');
        }
      }
    },
    textColor() {
      if (this.initialized) {
        this.$set(this.container, 'textColor', this.textColor);
        this.$emit('refresh');
      }
    },
    textFontSize() {
      if (this.initialized) {
        this.$set(this.container, 'textFontSize', this.textFontSize && `${this.textFontSize}px`);
        this.$emit('refresh');
      }
    },
    textStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.container, 'textFontWeight', this.textStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.container, 'textFontStyle', this.textStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
          this.$emit('refresh');
        }
      }
    },
    textSubtitleColor() {
      if (this.initialized) {
        this.$set(this.container, 'textSubtitleColor', this.textSubtitleColor);
        this.$emit('refresh');
      }
    },
    textSubtitleFontSize() {
      if (this.initialized) {
        this.$set(this.container, 'textSubtitleFontSize', this.textSubtitleFontSize && `${this.textSubtitleFontSize}px`);
        this.$emit('refresh');
      }
    },
    textSubtitleStyle: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.$set(this.container, 'textSubtitleFontWeight', this.textSubtitleStyle?.find?.(s => s === 'bold') && 'bold' || 'normal');
          this.$set(this.container, 'textSubtitleFontStyle', this.textSubtitleStyle?.find?.(s => s === 'italic') && 'italic' || 'normal');
          this.$emit('refresh');
        }
      }
    },
    enabled() {
      if (this.initialized) {
        if (this.enabled) {
          this.textTitleColor =  '#20282C';
          this.textTitleFontSize = 20;
          this.textTitleStyle = ['bold'];
          this.textHeaderColor = '#707070';
          this.textHeaderStyle = [];
          this.textHeaderFontSize = 18;
          this.textColor = '#20282C';
          this.textStyle = [];
          this.textFontSize = 16;
          this.textSubtitleColor = '#707070';
          this.textSubtitleStyle = [];
          this.textSubtitleFontSize = 12;
        } else {
          this.textTitleColor = null;
          this.textTitleFontSize = null;
          this.textTitleStyle = null;
          this.textHeaderColor = null;
          this.textHeaderStyle = null;
          this.textHeaderFontSize = null;
          this.textColor = null;
          this.textStyle = null;
          this.textFontSize = null;
          this.textSubtitleColor = null;
          this.textSubtitleStyle = null;
          this.textSubtitleFontSize = null;
        }
      }
    },
  },
  created() {
    this.container = this.value;

    this.textTitleColor = this.container.textTitleColor;
    this.textTitleFontSize = this.container.textTitleFontSize?.replace?.('px', '');
    this.textTitleStyle = (this.container.textTitleFontWeight || this.container.textTitleFontStyle) && [] || null;
    if (this.container.textTitleFontWeight === 'bold') {
      this.textTitleStyle.push('bold');
    }
    if (this.container.textTitleFontStyle === 'italic') {
      this.textTitleStyle.push('italic');
    }

    this.textHeaderColor = this.container.textHeaderColor;
    this.textHeaderFontSize = this.container.textHeaderFontSize?.replace?.('px', '');
    this.textHeaderStyle = (this.container.textHeaderFontWeight || this.container.textHeaderFontStyle) && [] || null;
    if (this.container.textHeaderFontWeight === 'bold') {
      this.textHeaderStyle.push('bold');
    }
    if (this.container.textHeaderFontStyle === 'italic') {
      this.textHeaderStyle.push('italic');
    }

    this.textColor = this.container.textColor;
    this.textFontSize = this.container.textFontSize?.replace?.('px', '');
    this.textStyle = (this.container.textFontWeight || this.container.textFontStyle) && [] || null;
    if (this.container.textFontWeight === 'bold') {
      this.textStyle.push('bold');
    }
    if (this.container.textFontStyle === 'italic') {
      this.textStyle.push('italic');
    }

    this.textSubtitleColor = this.container.textSubtitleColor;
    this.textSubtitleFontSize = this.container.textSubtitleFontSize?.replace?.('px', '');
    this.textSubtitleStyle = (this.container.textSubtitleFontWeight || this.container.textSubtitleFontStyle) && [] || null;
    if (this.container.textSubtitleFontWeight === 'bold') {
      this.textSubtitleStyle.push('bold');
    }
    if (this.container.textSubtitleFontStyle === 'italic') {
      this.textSubtitleStyle.push('italic');
    }

    this.enabled = !!this.textTitleColor || !!this.textHeaderColor || !!this.textColor || !!this.textSubtitleColor;
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>