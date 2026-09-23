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
      <div class="me-auto">
        {{ $t('layout.textBackground') }}
      </div>
      <v-switch
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
            value="radial"
            class="mx-0">
            <template #label>
              <span>{{ $t('layout.radialGradient') }}</span>
            </template>
          </v-radio>
          <v-radio
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
        :class="choice === 'color' && 'mb-auto' || 'my-auto'"
        class="me-0 ms-auto">
        <styling-color-picker
          v-if="choice === 'color'"
          v-model="color"
          class="my-auto" />
        <div v-else-if="isGradient">
          <styling-color-picker
            v-model="gradientFrom"
            :label="$t('layout.gradientFrom')"
            class="my-auto" />
          <styling-color-picker
            v-model="gradientTo"
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
          :image-field="imageField"
          :object-id="objectId"
          :object-type="objectType">
          <styling-background-image-attachment
            v-model="container[imageField]"
            ref="backgroundImage"
            :storage-id="objectId"
            :object-type="objectType"
            immediate-save
            class="my-auto" />
        </slot>
      </v-list-item-action>
    </v-list-item>
    <div v-if="container[imageField]" class="d-flex">
      <v-radio-group
        v-model="imageStyle"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="cover"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imageSizeCover') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="contain"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imageSizeContain') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="repeat"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imageRepeat') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="no-repeat"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imageNoRepeat') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
      <v-radio-group
        v-model="container[positionField]"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="top left"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imagePositionTopLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="top right"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imagePositionTopRight') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom left"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imagePositionBottomLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom right"
          class="mx-0">
          <template #label>
            <span>{{ $t('layout.imagePositionBottomRight') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
    </div>
    <styling-background-margin-input
      v-if="enabled"
      :value="container"
      :field="paddingField"
      :min="-400"
      class="my-auto" />
    <styling-background-radius-input
      v-if="enabled"
      :value="container"
      :field="radiusField"
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
    // 'Title' | 'Header' | '' (Body) | 'Subtitle'
    typePrefix: {
      type: String,
      default: '',
    },
  },
  data: () => ({
    container: null,
    enabled: false,
    choice: null,
    imageStyle: null,
    color: null,
    gradientFrom: null,
    gradientTo: null,
    gradientDirection: null,
    gradientCorner: null,
    gradientRatio: null,
    initialized: false,
  }),
  computed: {
    colorField() {
      return `text${this.typePrefix}BackgroundColor`;
    },
    imageField() {
      return `text${this.typePrefix}BackgroundImage`;
    },
    effectField() {
      return `text${this.typePrefix}BackgroundEffect`;
    },
    positionField() {
      return `text${this.typePrefix}BackgroundPosition`;
    },
    sizeField() {
      return `text${this.typePrefix}BackgroundSize`;
    },
    repeatField() {
      return `text${this.typePrefix}BackgroundRepeat`;
    },
    paddingField() {
      return `text${this.typePrefix}BackgroundPadding`;
    },
    radiusField() {
      return `text${this.typePrefix}BackgroundRadius`;
    },
    objectType() {
      return `containerText${this.typePrefix || 'Body'}Background`;
    },
    isGradient() {
      return this.choice === 'linear' || this.choice === 'radial' || this.choice === 'angular';
    },
    objectId() {
      const id = this.container.storageId || this.container.id;
      return this.$root.isSiteLayout ? `site_${this.$root.siteId}_${id}` : `page_${this.$root.pageId}_${id}`;
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
      return this.gradientCorner === 'top right' || this.gradientCorner === 'bottom left';
    },
    backgroundEffect() {
      if (!this.gradientFrom || !this.gradientTo) {
        return null;
      }
      const ratio = this.gradientRatio ?? 50;
      if (this.choice === 'linear') {
        const stops = `${this.gradientFrom} 0%, ${this.gradientFrom} ${ratio}%, ${this.gradientTo} 100%`;
        return this.gradientDirection === 'to right' ?
          `linear-gradient(to right, ${stops})` :
          `linear-gradient(${stops})`;
      } else if (this.choice === 'radial') {
        return `radial-gradient(${this.gradientFrom} 0%, ${this.gradientFrom} ${ratio}%, ${this.gradientTo} 100%)`;
      } else if (this.choice === 'angular') {
        const edgeColor = this.angularReversed ? this.gradientTo : this.gradientFrom;
        const farColor = this.angularReversed ? this.gradientFrom : this.gradientTo;
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
    enabled() {
      if (this.initialized) {
        this.container[this.imageField] = null;
        this.imageStyle = null;
        this.color = this.enabled ? '#FFFFFFFF' : null;
        this.gradientFrom = null;
        this.gradientTo = null;
        this.gradientDirection = null;
        this.gradientCorner = null;
        this.gradientRatio = null;
      }
    },
    imageStyle() {
      if (this.initialized) {
        if (this.imageStyle === 'cover' || this.imageStyle === 'contain') {
          this.container[this.sizeField] = this.imageStyle;
          this.container[this.repeatField] = null;
        } else {
          this.container[this.sizeField] = null;
          this.container[this.repeatField] = this.imageStyle;
        }
      }
    },
    color: {
      immediate: true,
      handler(newVal, oldVal) {
        if (this.initialized && newVal !== oldVal) {
          this.container[this.colorField] = this.enabled && this.choice === 'color' ? this.color : (this.enabled ? '#FFFFFF00' : null);
        }
      },
    },
    backgroundEffect: {
      immediate: true,
      handler(newVal, oldVal) {
        if (this.initialized && newVal !== oldVal) {
          this.container[this.effectField] = this.backgroundEffect;
        }
      },
    },
    choice() {
      if (this.initialized) {
        if (this.choice === 'color') {
          this.gradientFrom = null;
          this.gradientTo = null;
          this.gradientDirection = null;
          this.gradientCorner = null;
          this.gradientRatio = null;
          this.container[this.colorField] = this.enabled ? (this.color || '#FFFFFFFF') : null;
          this.container[this.effectField] = null;
        } else {
          this.gradientFrom = this.gradientFrom || this.color || '#FFFFFFFF';
          this.gradientTo = this.gradientTo || '#999999FF';
          this.gradientRatio = this.gradientRatio ?? 50;
          if (this.choice === 'linear') {
            this.gradientDirection = this.gradientDirection || 'to bottom';
          } else if (this.choice === 'angular') {
            this.gradientCorner = this.gradientCorner || 'top left';
          }
          this.container[this.colorField] = '#FFFFFF00';
        }
      }
    },
  },
  created() {
    this.container = this.value;
    if (this.container[this.sizeField] || this.container[this.repeatField]) {
      if (this.container[this.sizeField] === 'cover'
          || this.container[this.sizeField] === 'contain') {
        this.imageStyle = this.container[this.sizeField];
      } else {
        this.imageStyle = this.container[this.repeatField];
      }
    }
    const effect = this.container[this.effectField];
    if (effect?.startsWith('radial-gradient(')) {
      this.choice = 'radial';
      const stops = effect.replace('radial-gradient(', '').replace(/\)$/, '').split(',').map(s => s.trim());
      this.gradientFrom = stops[0].split(' ')[0];
      if (stops.length === 3) {
        this.gradientRatio = parseFloat(stops[1].split(' ')[1]);
        this.gradientTo = stops[2].split(' ')[0];
      } else {
        this.gradientRatio = 50;
        this.gradientTo = stops[stops.length - 1].split(' ')[0];
      }
    } else if (effect?.startsWith('conic-gradient(')) {
      this.choice = 'angular';
      const inner = effect.replace('conic-gradient(', '').replace(/\)$/, '');
      const cornerMatch = inner.match(/at (top left|top right|bottom left|bottom right)/);
      this.gradientCorner = cornerMatch ? cornerMatch[1] : 'top left';
      const reversed = this.gradientCorner === 'top right' || this.gradientCorner === 'bottom left';
      const stops = inner.substring(inner.indexOf(',') + 1).split(',').map(s => s.trim());
      const firstColor = stops[0].split(' ')[0];
      const lastColor = stops[stops.length - 1].split(' ')[0];
      this.gradientFrom = reversed ? lastColor : firstColor;
      this.gradientTo = reversed ? firstColor : lastColor;
      if (stops.length === 3) {
        const midPercent = Math.round(parseFloat(stops[1].split(' ')[1]) / 90 * 100);
        this.gradientRatio = reversed ? (100 - midPercent) : midPercent;
      } else {
        this.gradientRatio = 50;
      }
    } else if (effect?.startsWith('linear-gradient(')) {
      this.choice = 'linear';
      const inner = effect.replace('linear-gradient(', '').replace(/\)$/, '');
      const parts = inner.split(',').map(s => s.trim());
      let stops = parts;
      if (parts[0].startsWith('to ')) {
        this.gradientDirection = parts[0];
        stops = parts.slice(1);
      } else {
        this.gradientDirection = 'to bottom';
      }
      this.gradientFrom = stops[0].split(' ')[0];
      if (stops.length === 3) {
        this.gradientRatio = parseFloat(stops[1].split(' ')[1]);
        this.gradientTo = stops[2].split(' ')[0];
      } else {
        this.gradientRatio = 50;
        this.gradientTo = stops[stops.length - 1].split(' ')[0];
      }
    } else {
      this.choice = 'color';
    }

    this.color = this.container[this.colorField];
    this.enabled = !!this.container[this.colorField] || !!this.container[this.imageField];
    if (this.enabled && !this.color && this.choice === 'color') {
      this.color = '#FFFFFFFF';
    }
    this.$nextTick().then(() => this.initialized = true);
  },
};
</script>
