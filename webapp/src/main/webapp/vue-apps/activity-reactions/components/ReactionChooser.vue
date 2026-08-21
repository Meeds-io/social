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
    class="reaction-chooser-wrapper position-relative d-inline-flex align-center"
    @mouseenter="startHoverTimer"
    @mouseleave="startCloseTimer"
    @touchstart="startLongPressTimer"
    @touchend="cancelLongPressTimer"
    @touchmove="cancelLongPressTimer"
    @click.capture="swallowLongPressClick"
    @keydown="handleActivatorKeydown">
    <slot></slot>
    <v-menu
      v-model="open"
      :position-x="menuX"
      :position-y="menuY"
      :close-on-content-click="false"
      content-class="reaction-chooser rounded-xl"
      transition="fade-transition"
      z-index="2000"
      absolute
      top
      allow-overflow>
      <v-card
        :aria-label="$t('UIActivity.label.reactions')"
        class="d-flex flex-nowrap align-center pa-1 white"
        role="menu"
        flat
        @mouseenter="cancelCloseTimer"
        @mouseleave="startCloseTimer">
        <v-btn
          v-for="(option, index) in options"
          :key="option.id"
          :ref="`option-${index}`"
          :title="$t(option.labelKey)"
          :aria-label="$t(option.labelKey)"
          :class="[
            option.id === currentReactionId && 'reaction-chooser-selected light-grey-background',
            selectingId === option.id && 'reaction-selected-pop',
            selectingId && selectingId !== option.id && 'reaction-others-collapse',
          ]"
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
        <v-btn
          :ref="`option-${options.length}`"
          :title="$t('UIActivity.reaction.selectAnother')"
          :aria-label="$t('UIActivity.reaction.selectAnother')"
          :class="selectingId && 'reaction-others-collapse'"
          role="menuitem"
          min-width="36"
          width="36"
          height="36"
          class="pa-0 me-1"
          icon
          @click.prevent.stop="openEmojiBank"
          @keydown="handleMenuKeydown($event, options.length)">
          <v-icon size="16" class="icon-default-color">fas fa-plus</v-icon>
        </v-btn>
      </v-card>
    </v-menu>
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
    disabled: {
      type: Boolean,
      default: false,
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
    menuX: 0,
    menuY: 0,
    hoverTimer: null,
    closeTimer: null,
    longPressTimer: null,
    selectTimer: null,
    focusTimer: null,
    longPressTriggered: false,
    lastTouchTime: 0,
    selectingId: null,
  }),
  watch: {
    open(opened) {
      if (opened) {
        document.addEventListener('scroll', this.handleOutsideScroll, true);
        window.addEventListener('resize', this.closeChooser);
      } else {
        document.removeEventListener('scroll', this.handleOutsideScroll, true);
        window.removeEventListener('resize', this.closeChooser);
      }
    },
  },
  created() {
    this.$reactionService.getReactionOptions(this.objectType)
      .then(options => this.options = options);
    this.$on('select-emoji', this.selectCustomEmoji);
  },
  beforeDestroy() {
    this.cancelHoverTimer();
    this.cancelCloseTimer();
    this.cancelLongPressTimer();
    window.clearTimeout(this.selectTimer);
    window.clearTimeout(this.focusTimer);
    document.removeEventListener('scroll', this.handleOutsideScroll, true);
    window.removeEventListener('resize', this.closeChooser);
  },
  methods: {
    openChooser(focusFirstOption) {
      if (this.disabled) {
        return;
      }
      this.cancelCloseTimer();
      const anchorRect = this.$el.getBoundingClientRect();
      const popoverWidth = this.options.length * 40 + 8;
      this.menuX = Math.max(8, Math.min(anchorRect.left, window.innerWidth - popoverWidth - 8));
      this.menuY = anchorRect.top - 4;
      this.open = true;
      if (focusFirstOption) {
        this.focusOptionWhenRendered(0, 10);
      }
    },
    focusOptionWhenRendered(index, remainingAttempts) {
      this.$nextTick(() => {
        const optionRef = this.$refs[`option-${index}`];
        const button = Array.isArray(optionRef) ? optionRef[0] : optionRef;
        if (button?.$el) {
          button.$el.focus({preventScroll: true});
        } else if (remainingAttempts > 0) {
          this.focusTimer = window.setTimeout(() => this.focusOptionWhenRendered(index, remainingAttempts - 1), 50);
        }
      });
    },
    handleOutsideScroll(event) {
      if (event.target instanceof Element && event.target.closest('.reaction-chooser')) {
        return;
      }
      this.closeChooser();
    },
    closeChooser() {
      this.open = false;
      this.selectingId = null;
      this.cancelHoverTimer();
      this.cancelCloseTimer();
    },
    selectOption(option) {
      if (this.selectingId) {
        return;
      }
      this.$emit('reaction-select', option);
      this.selectingId = option.id;
      this.selectTimer = window.setTimeout(() => this.closeChooser(), 450);
    },
    selectCustomEmoji(emoji) {
      this.$emit('reaction-select', {id: emoji, emoji});
    },
    openEmojiBank() {
      const anchorRect = this.$el.getBoundingClientRect();
      this.closeChooser();
      document.dispatchEvent(new CustomEvent('show-emoji-picker', {detail: {
        top: `${anchorRect.bottom + 8}px`,
        left: `${anchorRect.left}px`,
        launcherInstance: this,
        options: {closeOnEmojiSelect: true},
      }}));
    },
    startHoverTimer() {
      this.cancelCloseTimer();
      const touchRecently = Date.now() - this.lastTouchTime < this.hoverDelay + this.longPressDelay;
      if (!this.disabled && !touchRecently && !this.open && !this.hoverTimer) {
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
      this.lastTouchTime = Date.now();
      this.longPressTriggered = false;
      this.cancelHoverTimer();
      if (!this.disabled && !this.longPressTimer) {
        this.longPressTimer = window.setTimeout(() => {
          this.longPressTimer = null;
          this.longPressTriggered = true;
          this.openChooser();
        }, this.longPressDelay);
      }
    },
    cancelLongPressTimer() {
      this.lastTouchTime = Date.now();
      if (this.longPressTimer) {
        window.clearTimeout(this.longPressTimer);
        this.longPressTimer = null;
      }
    },
    swallowLongPressClick(event) {
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
      const itemsCount = this.options.length + 1;
      if (event.key === 'ArrowRight' || event.key === 'ArrowDown') {
        event.preventDefault();
        this.focusOption((index + 1) % itemsCount);
      } else if (event.key === 'ArrowLeft' || event.key === 'ArrowUp') {
        event.preventDefault();
        this.focusOption((index - 1 + itemsCount) % itemsCount);
      } else if (event.key === 'Escape') {
        event.preventDefault();
        this.closeChooser();
        this.$el.querySelector('button, a, [tabindex]')?.focus?.();
      } else if (event.key === 'Tab') {
        this.closeChooser();
      }
    },
    focusOption(index) {
      const optionRef = this.$refs[`option-${index}`];
      const button = Array.isArray(optionRef) ? optionRef[0] : optionRef;
      button?.$el?.focus?.({preventScroll: true});
    },
  },
};
</script>
