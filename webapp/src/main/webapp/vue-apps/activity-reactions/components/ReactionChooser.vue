<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div
    class="reaction-chooser-wrapper position-relative d-inline-block"
    @mouseenter="startHoverTimer"
    @mouseleave="startCloseTimer"
    @touchstart="startLongPressTimer"
    @touchend="cancelLongPressTimer"
    @touchmove="cancelLongPressTimer"
    @click.capture="swallowLongPressClick"
    @keydown="handleActivatorKeydown"
    @focusout="handleFocusOut">
    <slot></slot>
    <v-card
      v-if="open"
      :aria-label="$t('UIActivity.label.reactions')"
      class="reaction-chooser position-absolute d-flex flex-nowrap align-center pa-1"
      role="menu"
      @mouseenter="cancelCloseTimer"
      @mouseleave="startCloseTimer">
      <v-btn
        v-for="(option, index) in options"
        :key="option.id"
        :ref="`option-${index}`"
        :title="$t(option.labelKey)"
        :aria-label="$t(option.labelKey)"
        :class="option.id === currentReactionId && 'reaction-chooser-selected light-grey-background'"
        role="menuitem"
        min-width="36"
        width="36"
        height="36"
        class="pa-0 me-1"
        icon
        @click.prevent.stop="selectOption(option)"
        @keydown="handleMenuKeydown($event, index)">
        <span class="reaction-emoji text-h6">{{ option.emoji }}</span>
      </v-btn>
    </v-card>
  </div>
</template>

<script>
export default {
  props: {
    objectType: {
      type: String,
      required: true,
    },
    currentReactionId: {
      type: String,
      default: null,
    },
    hoverDelay: {
      type: Number,
      default: () => 500,
    },
    longPressDelay: {
      type: Number,
      default: () => 500,
    },
  },
  data: () => ({
    options: [],
    open: false,
    hoverTimer: null,
    closeTimer: null,
    longPressTimer: null,
    longPressTriggered: false,
  }),
  created() {
    this.$reactionService.getReactionOptions(this.objectType)
      .then(options => this.options = options);
  },
  beforeDestroy() {
    this.cancelHoverTimer();
    this.cancelCloseTimer();
    this.cancelLongPressTimer();
  },
  methods: {
    openChooser(focusFirstOption) {
      this.cancelCloseTimer();
      this.open = true;
      if (focusFirstOption) {
        this.$nextTick(() => this.focusOption(0));
      }
    },
    closeChooser() {
      this.open = false;
      this.cancelHoverTimer();
      this.cancelCloseTimer();
    },
    selectOption(option) {
      this.$emit('reaction-select', option);
      this.closeChooser();
    },
    startHoverTimer() {
      this.cancelCloseTimer();
      if (!this.open && !this.hoverTimer) {
        this.hoverTimer = window.setTimeout(() => {
          this.hoverTimer = null;
          this.openChooser();
        }, this.hoverDelay);
      }
    },
    cancelHoverTimer() {
      if (this.hoverTimer) {
        window.clearTimeout(this.hoverTimer);
        this.hoverTimer = null;
      }
    },
    startCloseTimer() {
      this.cancelHoverTimer();
      if (this.open && !this.closeTimer) {
        this.closeTimer = window.setTimeout(() => {
          this.closeTimer = null;
          this.closeChooser();
        }, 200);
      }
    },
    cancelCloseTimer() {
      if (this.closeTimer) {
        window.clearTimeout(this.closeTimer);
        this.closeTimer = null;
      }
    },
    startLongPressTimer() {
      this.longPressTriggered = false;
      if (!this.longPressTimer) {
        this.longPressTimer = window.setTimeout(() => {
          this.longPressTimer = null;
          this.longPressTriggered = true;
          this.openChooser();
        }, this.longPressDelay);
      }
    },
    cancelLongPressTimer() {
      if (this.longPressTimer) {
        window.clearTimeout(this.longPressTimer);
        this.longPressTimer = null;
      }
    },
    swallowLongPressClick(event) {
      // a long-press opened the chooser: the click that follows the touch
      // release must not toggle the like underneath
      if (this.longPressTriggered) {
        this.longPressTriggered = false;
        event.preventDefault();
        event.stopPropagation();
      }
    },
    handleActivatorKeydown(event) {
      if (this.open) {
        if (event.key === 'Escape') {
          this.closeChooser();
        }
        return;
      }
      if (['ArrowDown', 'ArrowUp', 'ArrowRight'].includes(event.key)) {
        event.preventDefault();
        this.openChooser(true);
      }
    },
    handleMenuKeydown(event, index) {
      if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
        event.preventDefault();
        this.focusOption((index + 1) % this.options.length);
      } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
        event.preventDefault();
        this.focusOption((index - 1 + this.options.length) % this.options.length);
      } else if (event.key === 'Escape') {
        event.preventDefault();
        this.closeChooser();
        this.$el.querySelector('button, a, [tabindex]')?.focus?.();
      }
    },
    handleFocusOut(event) {
      if (this.open && !this.$el.contains(event.relatedTarget)) {
        this.closeChooser();
      }
    },
    focusOption(index) {
      const optionRef = this.$refs[`option-${index}`];
      const button = Array.isArray(optionRef) ? optionRef[0] : optionRef;
      button?.$el?.focus?.();
    },
  },
};
</script>
