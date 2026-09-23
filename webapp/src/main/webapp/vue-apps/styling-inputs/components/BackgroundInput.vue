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
    <div v-if="!hideTitle || !hideSwitch" class="d-flex align-center">
      <slot v-if="$slots.title" name="title"></slot>
      <div
        v-else-if="!hideTitle"
        :class="textBold && 'font-weight-bold' || 'text-header'"
        class="me-auto">
        {{ $t('layout.background') }}
      </div>
      <v-switch
        v-if="!hideSwitch"
        v-model="enabled"
        class="ms-auto my-auto me-n2" />
    </div>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto align-start">
        <v-radio-group
          v-model="choice"
          class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
          mandatory>
          <v-radio
            value="color"
            class="mx-0">
            <template #label>
              <span>{{ $t('layout.color') }}</span>
            </template>
          </v-radio>
          <v-radio
            v-if="!noGradient"
            value="linear"
            class="mx-0">
            <template #label>
              <span>{{ $t('layout.linearGradient') }}</span>
            </template>
          </v-radio>
          <v-radio-group
            v-if="choice === 'linear'"
            v-model="gradientDirection"
            class="my-0 ms-8 text-no-wrap"
            mandatory>
            <v-radio
              value="to bottom"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientDirectionTopToBottom') }}</span>
              </template>
            </v-radio>
            <v-radio
              value="to right"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientDirectionLeftToRight') }}</span>
              </template>
            </v-radio>
          </v-radio-group>
          <v-radio
            v-if="!noGradient"
            value="radial"
            class="mx-0">
            <template #label>
              <span>{{ $t('layout.radialGradient') }}</span>
            </template>
          </v-radio>
          <v-radio
            v-if="!noGradient"
            value="angular"
            class="mx-0">
            <template #label>
              <span>{{ $t('layout.angularGradient') }}</span>
            </template>
          </v-radio>
          <v-radio-group
            v-if="choice === 'angular'"
            v-model="gradientCorner"
            class="my-0 ms-8 text-no-wrap"
            mandatory>
            <v-radio
              value="top left"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientCornerTopLeft') }}</span>
              </template>
            </v-radio>
            <v-radio
              value="top right"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientCornerTopRight') }}</span>
              </template>
            </v-radio>
            <v-radio
              value="bottom right"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientCornerBottomRight') }}</span>
              </template>
            </v-radio>
            <v-radio
              value="bottom left"
              class="mx-0">
              <template #label>
                <span>{{ $t('layout.gradientCornerBottomLeft') }}</span>
              </template>
            </v-radio>
          </v-radio-group>
        </v-radio-group>
      </v-list-item-content>
      <v-list-item-action
        :class="choice === 'color' && !scrollColor && 'mb-auto' || 'my-auto'"
        class="me-0 ms-auto">
        <styling-color-picker
          v-if="choice === 'color' && !scrollColor"
          v-model="container.backgroundColor"
          class="my-auto" />
        <div v-else-if="choice === 'color' && scrollColor">
          <styling-color-picker
            v-model="backgroundScrollTop"
            :label="$t('layout.scrollTopColor')"
            class="my-auto" />
          <styling-color-picker
            v-model="backgroundScrollMiddle"
            :label="$t('layout.scrollMiddleColor')"
            class="my-auto" />
        </div>
        <div v-else-if="isGradient">
          <styling-color-picker
            v-model="backgroundGradientFrom"
            :label="$t('layout.gradientFrom')"
            class="my-auto" />
          <styling-color-picker
            v-model="backgroundGradientTo"
            :label="$t('layout.gradientTo')"
            class="my-auto" />
        </div>
      </v-list-item-action>
    </v-list-item>

    <v-list-item
      v-if="isGradient"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('layout.gradientRatio') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        {{ gradientRatio }}/{{ 100 - gradientRatio }}
      </v-list-item-action>
    </v-list-item>
    <div
      v-if="isGradient"
      class="d-flex align-center px-1 pb-2">
      <span class="text-subtitle me-2">{{ $t('layout.gradientFrom') }}</span>
      <v-slider
        v-model="gradientRatio"
        min="0"
        max="100"
        hide-details
        class="flex-grow-1 mt-0" />
      <span class="text-subtitle ms-2">{{ $t('layout.gradientTo') }}</span>
    </div>

    <slot
      v-if="enabled"
      name="after-color"
      :container="container"></slot>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('layout.image') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        <slot
          name="image"
          :container="container"
          :object-id="objectId">
          <styling-background-image-attachment
            v-model="container.backgroundImage"
            ref="backgroundImage"
            :storage-id="objectId"
            :immediate-save="immediateSave"
            class="my-auto" />
        </slot>
      </v-list-item-action>
    </v-list-item>
    <template v-if="container.backgroundImage">
      <div class="text-subtitle mt-1">{{ $t('layout.imageSizeTitle') }}</div>
      <v-radio-group
        v-model="container.backgroundSize"
        class="my-0 text-no-wrap"
        mandatory
        hide-details>
        <div class="d-flex flex-wrap">
          <v-radio
            value="cover"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageSizeCover') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="contain"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageSizeContain') }}</span>
            </template>
          </v-radio>
        </div>
      </v-radio-group>

      <div class="text-subtitle mt-2">{{ $t('layout.imagePositionTitle') }}</div>
      <v-radio-group
        v-model="container.backgroundPosition"
        class="my-0 text-no-wrap"
        mandatory
        hide-details>
        <div class="d-flex flex-wrap">
          <v-radio
            value="center"
            class="col-12 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imagePositionCenter') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="top right"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imagePositionTopRight') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="top left"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imagePositionTopLeft') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="bottom right"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imagePositionBottomRight') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="bottom left"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imagePositionBottomLeft') }}</span>
            </template>
          </v-radio>
        </div>
      </v-radio-group>

      <div class="text-subtitle mt-2">{{ $t('layout.imageScrollingTitle') }}</div>
      <v-radio-group
        v-model="container.backgroundAttachment"
        class="my-0 text-no-wrap"
        mandatory
        hide-details>
        <div class="d-flex flex-wrap">
          <v-radio
            value="fixed"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageScrollingFixed') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="scroll"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageScrollingScroll') }}</span>
            </template>
          </v-radio>
        </div>
      </v-radio-group>

      <div class="text-subtitle mt-2">{{ $t('layout.imageRepeatTitle') }}</div>
      <v-radio-group
        v-model="container.backgroundRepeat"
        class="my-0 text-no-wrap"
        mandatory
        hide-details>
        <div class="d-flex flex-wrap">
          <v-radio
            value="no-repeat"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageNoRepeat') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="repeat"
            class="col-6 pa-0 mx-0 my-1">
            <template #label>
              <span>{{ $t('layout.imageRepeat') }}</span>
            </template>
          </v-radio>
        </div>
      </v-radio-group>
    </template>
    <styling-background-margin-input
      v-if="enabled && !noLayerOptions"
      :value="container"
      class="my-auto" />
    <styling-background-radius-input
      v-if="enabled && !noLayerOptions"
      :value="container"
      class="my-auto" />
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    defaultBackgroundColor: {
      type: String,
      default: () => '#FFFFFFFF',
    },
    immediateSave: {
      type: Boolean,
      default: false,
    },
    scrollColor: {
      type: Boolean,
      default: false,
    },
    textBold: {
      type: Boolean,
      default: false,
    },
    // Site top banner (Topbar): the gradient choices are removed, only a plain colour is offered,
    // because the fixed-on-scroll option already gives the Topbar two colour states
    noGradient: {
      type: Boolean,
      default: false,
    },
    // Areas that always have a background (Branding and Theme): no enable/disable switch
    hideSwitch: {
      type: Boolean,
      default: false,
    },
    // The consumer prints its own header (Branding drawers)
    hideTitle: {
      type: Boolean,
      default: false,
    },
    // Background-layer margins and radius are cssClass tokens of a layout container; a theme has none
    noLayerOptions: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    container: null,
    enabled: false,
    choice: null,
    backgroundScrollTop: null,
    backgroundScrollMiddle: null,
    backgroundGradientFrom: null,
    backgroundGradientTo: null,
    gradientDirection: null,
    gradientCorner: null,
    gradientRatio: null,
    initialized: false,
  }),
  computed: {
    isGradient() {
      return this.choice === 'linear' || this.choice === 'radial' || this.choice === 'angular';
    },
    id() {
      return this.container.storageId || this.container.id;
    },
    objectId() {
      // Attachment object of the layout editors; a consumer that replaces the image slot does not use it
      return this.$root.isSiteLayout ? `site_${this.$root.siteId}_${this.id}` : `page_${this.$root.pageId}_${this.id}`;
    },
    backgroundColor() {
      return this.container.backgroundColor;
    },
    backgroundColorChoice() {
      if (!this.enabled) {
        return null;
      } else if (this.choice === 'color'
        && !this.scrollColor) {
        return this.backgroundColor?.includes?.('@') ? this.backgroundColor.split('@')[0] : this.backgroundColor;
      } else if (this.choice === 'color'
        && this.scrollColor) {
        return `${this.backgroundScrollTop}@${this.backgroundScrollMiddle}`;
      } else {
        return '#FFFFFF00';
      }
    },
    angularStartAngle() {
      return {
        'top left': '90deg',
        'top right': '180deg',
        'bottom right': '270deg',
        'bottom left': '0deg',
      }[this.gradientCorner] || '0deg';
    },
    angularReversed() {
      // The gradient line always sweeps clockwise from angularStartAngle. For
      // these 2 corners, that start angle lands on the edge opposite to the
      // corner's own name (e.g. "top right" starts at its right edge), so the
      // From/To colors must be swapped for From to appear next to that edge.
      return this.gradientCorner === 'top right' || this.gradientCorner === 'bottom left';
    },
    backgroundEffect() {
      if (!this.backgroundGradientFrom || !this.backgroundGradientTo) {
        return null;
      }
      const ratio = this.gradientRatio ?? 50;
      if (this.choice === 'linear') {
        const stops = `${this.backgroundGradientFrom} 0%, ${this.backgroundGradientFrom} ${ratio}%, ${this.backgroundGradientTo} 100%`;
        return this.gradientDirection === 'to right' ?
          `linear-gradient(to right, ${stops})` :
          `linear-gradient(${stops})`;
      } else if (this.choice === 'radial') {
        return `radial-gradient(${this.backgroundGradientFrom} 0%, ${this.backgroundGradientFrom} ${ratio}%, ${this.backgroundGradientTo} 100%)`;
      } else if (this.choice === 'angular') {
        const edgeColor = this.angularReversed ? this.backgroundGradientTo : this.backgroundGradientFrom;
        const farColor = this.angularReversed ? this.backgroundGradientFrom : this.backgroundGradientTo;
        // The plateau (ratio-sized block of solid color) always sits next
        // to From's own edge, whichever end of the stop list that is.
        const midPercent = this.angularReversed ? (100 - ratio) : ratio;
        const midAngle = (midPercent / 100 * 90).toFixed(2);
        return this.angularReversed ?
          `conic-gradient(from ${this.angularStartAngle} at ${this.gradientCorner}, ${edgeColor} 0deg, ${farColor} ${midAngle}deg, ${farColor} 90deg)` :
          `conic-gradient(from ${this.angularStartAngle} at ${this.gradientCorner}, ${edgeColor} 0deg, ${edgeColor} ${midAngle}deg, ${farColor} 90deg)`;
      } else {
        return null;
      }
    },
  },
  watch: {
    container: {
      deep: true,
      handler() {
        if (this.container) {
          this.$emit('input', this.container);
        }
      },
    },
    scrollColor() {
      if (this.initialized) {
        this.container.backgroundColor = this.enabled && this.defaultBackgroundColor || null;
        this.backgroundScrollTop = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
        this.backgroundScrollMiddle = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
      }
    },
    enabled() {
      if (this.initialized) {
        this.container.backgroundImage = null;
        this.resetImageOptions();

        this.container.backgroundColor = this.enabled && this.defaultBackgroundColor || null;
        this.backgroundScrollTop = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
        this.backgroundScrollMiddle = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
        this.backgroundGradientFrom = null;
        this.backgroundGradientTo = null;
        this.gradientDirection = null;
        this.gradientCorner = null;
        this.gradientRatio = null;
      }
    },
    'container.backgroundImage'(newVal) {
      if (this.initialized && !newVal) {
        // The 4 image options below are meaningless without an image, and a
        // stale 'fixed'/'repeat' left behind would be re-applied as soon as
        // another image is picked.
        this.resetImageOptions();
      }
    },
    backgroundColorChoice: {
      immediate: true,
      handler(newVal, oldVal) {
        if (this.initialized && newVal !== oldVal) {
          this.container.backgroundColor = this.backgroundColorChoice;
        }
      },
    },
    backgroundEffect: {
      immediate: true,
      handler(newVal, oldVal) {
        if (this.initialized && newVal !== oldVal) {
          this.container.backgroundEffect = this.backgroundEffect;
        }
      },
    },
    choice() {
      if (this.initialized) {
        if (this.choice === 'color') {
          this.backgroundGradientFrom = null;
          this.backgroundGradientTo = null;
          this.gradientDirection = null;
          this.gradientCorner = null;
          this.gradientRatio = null;
          this.container.backgroundColor = this.enabled && this.defaultBackgroundColor || null;
          this.backgroundScrollTop = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
          this.backgroundScrollMiddle = this.enabled && this.scrollColor && this.defaultBackgroundColor || null;
        } else {
          this.backgroundGradientFrom = this.backgroundGradientFrom || this.defaultBackgroundColor;
          this.backgroundGradientTo = this.backgroundGradientTo || '#999999FF';
          this.gradientRatio = this.gradientRatio ?? 50;
          if (this.choice === 'linear') {
            this.gradientDirection = this.gradientDirection || 'to bottom';
          } else if (this.choice === 'angular') {
            this.gradientCorner = this.gradientCorner || 'top left';
          }
          this.container.backgroundColor = '#FFFFFF00';
          this.backgroundScrollTop = null;
          this.backgroundScrollMiddle = null;
        }
      }
    },
  },
  created() {
    this.container = this.value;
    if (this.noGradient && this.container.backgroundEffect) {
      // An existing gradient falls back to the plain colour once the option is removed for this area
      this.container.backgroundEffect = null;
      if (!this.container.backgroundColor || this.container.backgroundColor === '#FFFFFF00') {
        this.container.backgroundColor = this.defaultBackgroundColor;
      }
    }
    if (this.container.backgroundEffect?.startsWith('radial-gradient(')) {
      this.choice = 'radial';
      const stops = this.container.backgroundEffect.replace('radial-gradient(', '').replace(/\)$/, '').split(',').map(s => s.trim());
      this.backgroundGradientFrom = stops[0].split(' ')[0];
      if (stops.length === 3) {
        this.gradientRatio = parseFloat(stops[1].split(' ')[1]);
        this.backgroundGradientTo = stops[2].split(' ')[0];
      } else {
        this.gradientRatio = 50;
        this.backgroundGradientTo = stops[stops.length - 1].split(' ')[0];
      }
    } else if (this.container.backgroundEffect?.startsWith('conic-gradient(')) {
      this.choice = 'angular';
      const inner = this.container.backgroundEffect.replace('conic-gradient(', '').replace(/\)$/, '');
      const cornerMatch = inner.match(/at (top left|top right|bottom left|bottom right)/);
      this.gradientCorner = cornerMatch ? cornerMatch[1] : 'top left';
      const reversed = this.gradientCorner === 'top right' || this.gradientCorner === 'bottom left';
      const stops = inner.substring(inner.indexOf(',') + 1).split(',').map(s => s.trim());
      const firstColor = stops[0].split(' ')[0];
      const lastColor = stops[stops.length - 1].split(' ')[0];
      this.backgroundGradientFrom = reversed ? lastColor : firstColor;
      this.backgroundGradientTo = reversed ? firstColor : lastColor;
      if (stops.length === 3) {
        const midPercent = Math.round(parseFloat(stops[1].split(' ')[1]) / 90 * 100);
        this.gradientRatio = reversed ? (100 - midPercent) : midPercent;
      } else {
        this.gradientRatio = 50;
      }
    } else if (this.container.backgroundEffect?.startsWith('linear-gradient(')) {
      this.choice = 'linear';
      const inner = this.container.backgroundEffect.replace('linear-gradient(', '').replace(/\)$/, '');
      const parts = inner.split(',').map(s => s.trim());
      let stops = parts;
      if (parts[0].startsWith('to ')) {
        this.gradientDirection = parts[0];
        stops = parts.slice(1);
      } else {
        this.gradientDirection = 'to bottom';
      }
      this.backgroundGradientFrom = stops[0].split(' ')[0];
      if (stops.length === 3) {
        this.gradientRatio = parseFloat(stops[1].split(' ')[1]);
        this.backgroundGradientTo = stops[2].split(' ')[0];
      } else {
        this.gradientRatio = 50;
        this.backgroundGradientTo = stops[stops.length - 1].split(' ')[0];
      }
    } else {
      this.choice = 'color';
    }

    this.enabled = this.hideSwitch || !!this.container.backgroundColor || !!this.container.backgroundImage;
    if (this.enabled) {
      if (!this.container.backgroundColor) {
        this.container.backgroundColor = this.defaultBackgroundColor;
      } else if (this.scrollColor) {
        if (this.backgroundColor?.includes?.('@')) {
          this.backgroundScrollTop = this.container.backgroundColor.split('@')[0];
          this.backgroundScrollMiddle = this.container.backgroundColor.split('@')[1];
        } else {
          this.backgroundScrollTop = this.container.backgroundColor;
          this.backgroundScrollMiddle = this.container.backgroundColor;
        }
      } else {
        this.backgroundScrollTop = null;
        this.backgroundScrollMiddle = null;
      }
    }
    this.$nextTick().then(() => this.initialized = true);
  },
  methods: {
    resetImageOptions() {
      this.container.backgroundSize = null;
      this.container.backgroundPosition = null;
      this.container.backgroundAttachment = null;
      this.container.backgroundRepeat = null;
    },
    async apply() {
      if (this.enabled && this.$refs.backgroundImage) {
        const backgroundImage = await this.$refs.backgroundImage.save();
        if (backgroundImage) {
          this.container.backgroundImage = backgroundImage;
        }
      } else {
        this.container.backgroundImage = null;
      }
      return this.container;
    },
  },
};
</script>