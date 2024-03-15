<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2023 Meeds Association contact@meeds.io

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
  <v-toolbar class="z-index-one" flat>
    <div id="applicationToolbar" class="d-flex flex-grow-1 align-center content-box-sizing position-relative">
      <!-- Left Content -->
      <div
        v-if="showLeftContent"
        id="applicationToolbarLeft"
        :class="leftCols"
        class="flex-grow-1 text-start text-truncate pa-0">
        <v-btn
          v-if="showLeftButton"
          id="applicationToolbarLeftButton"
          :href="leftButton.href"
          :class="isCompact && 'px-0'"
          class="btn btn-primary text-truncate"
          @click="$emit('left-button-click', $event)">
          <v-icon
            v-if="leftButton.icon"
            :class="!isCompact && 'me-2'"
            size="18"
            dark>
            {{ leftButton.icon }}
          </v-icon>
          <span
            v-if="leftButton.text && (!isCompact || !leftButton.icon)"
            class="text-truncate text-none">
            {{ leftButton.text }}
          </span>
        </v-btn>
        <template v-if="leftText">
          {{ leftText }}
        </template>
        <slot v-if="$slots.left" name="left"></slot>
        <v-spacer />
      </div>

      <!-- Center Content -->
      <div
        v-if="showCenterContent"
        :class="centerCols"
        id="applicationToolbarCenter"
        class="flex-grow-1 align-center justify-center text-truncate pa-0 mx-auto">
        <v-btn-toggle
          v-if="hasCenterButtonToggle"
          v-model="toggle"
          id="applicationToolbarToggle"
          color="primary"
          mandatory
          outlined
          dense>
          <v-btn
            v-for="buttonToggle in centerButtonToggle.buttons"
            :key="buttonToggle.value"
            :id="`applicationToolbar-${buttonToggle.value}`"
            :value="buttonToggle.value"
            :class="isCompact && 'width-auto px-4'"
            text
            @click="emitToggle(buttonToggle.value)">
            <v-icon
              v-if="buttonToggle.icon"
              :class="buttonToggle.value === toggle && 'primary--text' || 'icon-default-color'"
              small>
              {{ buttonToggle.icon }}
            </v-icon>
            <span v-if="!isCompact && buttonToggle.icon && buttonToggle.text" class="ms-2"></span>
            <span
              v-if="!isCompact && buttonToggle.text"
              :class="buttonToggle.value === toggle && 'primary--text' || 'dark-grey-color'"
              class="text-truncate text-none">
              {{ buttonToggle.text }}
            </span>
          </v-btn>
        </v-btn-toggle>
        <slot v-if="$slots.center" name="center"></slot>
      </div>

      <v-scale-transition>
        <!-- Right Content and Expanded content -->
        <div
          v-if="showRightContent"
          id="applicationToolbarRight"
          :class="rightCols"
          class="d-flex flex-nowrap flex-grow-1 align-center justify-end text-truncate pa-0">
          <v-btn
            v-if="expandFilter"
            id="applicationToolbarBackButton"
            class="px-0 me-auto"
            small
            icon
            @click="expandFilter = false">
            <v-icon size="26" class="icon-default-color">fa-arrow-left</v-icon>
          </v-btn>
          <div
            v-if="$slots.right"
            :class="isCompact && 'flex-grow-1'"
            class="d-flex width-auto">
            <slot name="right"></slot>
          </div>
          <v-card
            v-if="showTextFilter"
            id="applicationToolbarFilter"
            :min-width="rightTextFilter.minWidth"
            :width="rightTextFilter.width || 'auto'"
            :max-width="rightTextFilter.maxWidth"
            :class="expandFilter && 'flex-grow-1'"
            flat>
            <v-tooltip :value="showTextTooltip" bottom>
              <template #activator="{on}">
                <v-text-field
                  id="applicationToolbarFilterInput"
                  ref="applicationToolbarFilterInput"
                  v-model="term"
                  :placeholder="rightTextFilter.placeholder"
                  :autofocus="autofocusTextFilter"
                  :height="isCompact && 24 || 36"
                  :prepend-inner-icon="term && 'fa-filter primary--text' || 'fa-filter icon-default-color'"
                  class="flex-grow-1 full-height pa-0 ms-4"
                  clear-icon="fa-times fa-1x primary--text position-absolute absolute-vertical-center"
                  autocomplete="off"
                  hide-details
                  clearable
                  v-on="on" />
              </template>
              <span>{{ rightTextFilter.tooltip }}</span>
            </v-tooltip>
          </v-card>
          <select
            v-if="showSelectBoxFilter"
            id="applicationToolbarFilterSelect"
            v-model="select"
            class="flex-grow-0 ignore-vuetify-classes py-2 height-auto width-auto text-truncate my-auto ms-4"
            @change="$emit('filter-select-change', select)">
            <option
              v-for="item in rightSelectBox.items"
              :key="item.value"
              :value="item.value">
              {{ item.text }}
            </option>
          </select>
          <v-btn
            v-if="showFilterButton"
            id="applicationToolbarAdvancedFilterButton"
            :class="filterButtonClass"
            :small="isCompact"
            text
            @click="$emit('filter-button-click', $event)">
            <v-icon
              :size="isCompact && 24 || 16"
              :class="filtersCount && 'primary--text' || 'icon-default-color'">
              fa-sliders-h
            </v-icon>
            <span
              v-if="!isCompact"
              class="ms-2 caption">
              {{ rightFilterButton.text }}
            </span>
            <span v-if="filtersCount" class="caption">
              ({{ filtersCount }})
            </span>
          </v-btn>
          <v-btn
            v-if="showConeButton"
            id="applicationToolbarConeButton"
            class="px-0 ms-4"
            small
            icon
            @click="expandFilter = true">
            <v-icon :class="coneColor" size="20">
              fa-filter
            </v-icon>
          </v-btn>
        </div>
      </v-scale-transition>
    </div>
  </v-toolbar>
</template>
<script>
/*
   Possible Triggered Events:
   - @left-button-click:
      Left Primary button click
   - @toggle-select:
      Centered Toggle Group Button Choice click
   - @filter-expand:
      Right section expanded
   - @filter-text-typing:
      Right Text Filter, start typing
   - @filter-text-input: 
      Right Text Filter, instant change even when typing is still in progress
   - @filter-text-input-end-typing:
      Right Text Filter, the user is not typing any more
   - @filter-select-change:
      Right SelectBox change event
   - @filter-button-click
      Right Filter Button click to open Advanced Filter Drawer
 */
export default {
  props: {
    /* Left side, whether a '<template slot="left">' (to add specific elements), or a Primary Button or a Label */
    leftButton: { // Clickable primary button in left side
      type: Object,
      default: () => ({
        hide: true,
        href: null,
        icon: null,
        text: null,
      }),
    },
    leftText: { // Text label in left side
      type: String,
      default: null,
    },
    hideLeft: { // Force hide left side, but generally useless since it's handled
      type: Boolean,
      default: false,
    },
    /* Center side, whether a '<template slot="center">' (to add specific elements), or a Toggle Button with multiple choices */
    centerButtonToggle: { // Toggle buttons attributes
      type: Object,
      default: () => ({
        hide: true,
        selected: 'example',
        buttons: [{
          value: 'example',
          text: 'Item',
          icon: null,
        }],
      }),
    },
    hideCenter: { // Force hide center side, but generally useless since it's handled
      type: Boolean,
      default: false,
    },
    /* Right side, Can have multiple choices, Text filter and/or SelectBox and/or Filter Button and other custom elements in a '<template slot="right">' */
    rightTextFilter: { // Text field attributes
      type: Object,
      default: () => ({
        hide: true,
        minWidth: 'unset',
        width: 'auto',
        maxWidth: 'unset',
        minCharacters: 3,
        placeholder: 'example',
        tooltip: 'Item',
      }),
    },
    rightSelectBox: { // Select box field attributes
      type: Object,
      default: () => ({
        hide: true,
        selected: 'example',
        label: '',
        items: [{
          value: 'example',
          text: 'Item',
        }],
      }),
    },
    rightFilterButton: { // Filter button field attributes
      type: Object,
      default: () => ({
        hide: true,
        text: null,
      }),
    },
    filtersCount: { // Filter button: applied filter count
      type: Number,
      default: () => 0,
    },
    hideRight: { // Force hide right side, but generally useless since it's handled
      type: Boolean,
      default: false,
    },
    hideConeButton: { // Force hide cone button, but generally useless since it's handled
      type: Boolean,
      default: false,
    },
    compact: { // Force the compact display
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    expandFilter: false,
    toggle: null,
    select: null,
    term: null,
    typing: false,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    maxRightElementsExpand: 2,
  }),
  computed: {
    isCompact() {
      return this.isMobile || this.compact;
    },
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    showLeftButton() {
      return !this.leftButton?.hide
          && (this.leftButton?.icon || this.leftText?.length);
    },
    hasLeftContent() {
      return this.showLeftButton
             || this.leftText?.length
             || this.$slots.left;
    },
    showLeftContent() {
      return !this.hideLeft && !this.expandFilter && (!this.isCompact || this.hasLeftContent || (this.hasCenterContent && this.hasRightContent));
    },
    hasCenterButtonToggle() {
      return this.centerButtonToggle
          && !this.centerButtonToggle?.hide
          && !!this.centerButtonToggle?.buttons?.length;
    },
    hasCenterContent() {
      return this.hasCenterButtonToggle || this.$slots.center;
    },
    showCenterContent() {
      return !this.hideCenter && !this.expandFilter && (!this.isCompact || this.hasCenterContent);
    },
    hasTextFilter() {
      return this.rightTextFilter
          && !this.rightTextFilter?.hide;
    },
    hasSelectBoxFilter() {
      return this.rightSelectBox
          && !this.rightSelectBox?.hide
          && this.rightSelectBox?.items?.length;
    },
    hasButtonFilter() {
      return this.rightFilterButton
          && !this.rightFilterButton?.hide
          && this.rightFilterButton?.text;
    },
    rightInputsCount() {
      return (this.hasTextFilter && 1 || 0)
          + (this.hasSelectBoxFilter && 1 || 0)
          + (this.hasButtonFilter && 1 || 0)
          + (this.$slots.right && 1 || 0);
    },
    hideRightInputs() {
      return !this.hideConeButton && !this.expandFilter && (this.isCompact || this.rightInputsCount > this.maxRightElementsExpand);
    },
    showConeButton() {
      return this.hasRightContent && this.hideRightInputs;
    },
    coneColor() {
      return (this.filtersCount > 0 || this.term?.length) && 'primary--text' || 'icon-default-color';
    },
    showTextFilter() {
      return this.hasTextFilter && !this.hideRightInputs;
    },
    isTermValid() {
      return !this.term?.length
          || !this.rightTextFilter.minCharcters
          || this.term.length >= this.rightTextFilter.minCharcters;
    },
    showTextTooltip() {
      return this.rightTextFilter?.tooltip && !this.isTermValid;
    },
    autofocusTextFilter() {
      return this.showTextFilter && (this.isCompact || this.expandFilter);
    },
    showSelectBoxFilter() {
      return this.hasSelectBoxFilter && !this.hideRightInputs;
    },
    showFilterButton() {
      return this.hasButtonFilter && !this.hideRightInputs;
    },
    filterButtonClass() {
      return `${this.isCompact && 'width-auto ms-4 px-0' || 'ms-4 px-2'} ${this.filtersCount && 'primary--text' || 'text-color'} ${!this.isCompact && (this.filtersCount && 'primary-border-color' || 'border-color') || ''}`;
    },
    hasRightContent() {
      return this.rightInputsCount > 0;
    },
    showRightContent() {
      return !this.hideRight && (!this.isCompact || this.hasRightContent);
    },
    leftCols() {
      return this.expandFilter && 'd-none'
        || (!this.showRightContent && !this.showCenterContent
            && 'col-12'
            || ((!this.showRightContent !== !this.showCenterContent)
                && 'col-6' || 'col-4'));
    },
    centerCols() {
      return this.expandFilter && 'col-12'
        || (!this.showLeftContent && !this.showRightContent
            && 'col-12'
            || ((!this.showLeftContent !== !this.showRightContent)
                && 'col-6' || 'col-4'));
    },
    rightCols() {
      return this.expandFilter && 'col-12'
        || (!this.showLeftContent && !this.showCenterContent
            && 'col-12'
            || ((!this.showLeftContent !== !this.showCenterContent)
                && 'col-6' || 'col-4'));
    },
  },
  watch: {
    centerButtonToggle: {
      immediate: true,
      handler() {
        if (this.hasCenterButtonToggle && this.centerButtonToggle.selected && this.centerButtonToggle.selected !== this.toggle) {
          this.toggle = this.centerButtonToggle.selected;
        }
      },
    },
    rightSelectBox: {
      immediate: true,
      handler() {
        if (this.hasSelectBoxFilter && this.rightSelectBox.selected && this.rightSelectBox.selected !== this.select) {
          this.select = this.rightSelectBox.selected;
        }
      },
    },
    typing()  {
      this.$emit('filter-text-typing', this.typing);
      if (this.typing) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        this.$nextTick(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
      }
    },
    expandFilter()  {
      this.$emit('filter-expand', this.expandFilter);
    },
    term()  {
      this.$emit('filter-text-input', this.term);
      if (!this.term?.length) {
        this.$emit('filter-text-input-end-typing', this.term);
        return;
      } else if (!this.isTermValid) {
        return;
      }
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.$root.$on('reset-filter', this.reset);
    if (this.showTextFilter) {
      document.addEventListener('keydown', this.clearSearch);
    } else {
      document.addEventListener('keydown', this.closeFilter);
    }
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.clearSearch);
    document.removeEventListener('keydown', this.closeFilter);
  },
  methods: {
    reset() {
      this.term = null;
      this.expandFilter = false;
    },
    clearSearch(event) {
      if (event?.key === 'Escape' && this.$refs?.applicationToolbarFilterInput?.isFocused) {
        this.term = null;
      }
    },
    closeFilter(event) {
      if (this.expandFilter && event.key === 'Escape' && this.isOverlayVisible()) {
        this.expandFilter = false;
      }
    },
    isOverlayVisible() {
      const elementsOverlays = document.querySelectorAll('.v-overlay--active');
      for (const elementOverlay of elementsOverlays) {
        if (getComputedStyle(elementOverlay).display !== 'none') {
          return false;
        }
      }
      return true;
    },
    setTerm(value) {
      this.term = value;
    },
    selectToggle(value) {
      this.toggle = value;
    },
    emitToggle(value) {
      // Differ emitting event to not block button status change
      window.setTimeout(
        () => this.$nextTick().then(() => this.$emit('toggle-select', value))
        ,50);
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.$emit('filter-text-input-end-typing', this.term);
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>
