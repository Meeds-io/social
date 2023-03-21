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
  <v-btn
    ref="button"
    :ripple="!ripple"
    :text="text"
    :icon="icon"
    @mousedown.stop.prevent="nop"
    @touchstart.stop.prevent="nop"
    @touchend.stop.prevent="emitAction($event, false)"
    @click.stop.prevent="emitAction($event, false)"
    @mouseover.stop.prevent="emitAction($event, true)">
    <slot></slot>
  </v-btn>
</template>
<script>
export default {
  props: {
    active: {
      type: Boolean,
      default: true,
    },
    text: {
      type: Boolean,
      default: false,
    },
    icon: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    ripple: false,
    emitted: false,
    mouseEvent: false,
  }),
  watch: {
    ripple() {
      if (this.ripple) {
        window.setTimeout(this.cleanRippleEffect, 500);
      }
    },
  },
  methods: {
    nop(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
    },
    emitAction(event, mouseover) {
      if (!this.active && mouseover) {
        return;
      }
      this.mouseEvent = event.pointerType === 'mouse' || event?.type === 'mouseover' || event?.type === 'click';
      if (mouseover) {
        this.toggleRippleEffect();
        // Differ opening the drawer
        window.setTimeout(() => {
          this.emitActionOnce(mouseover);
        }, 500);
      } else {
        this.cleanRippleEffect();
        window.setTimeout(() => {
          this.emitActionOnce(mouseover);
        }, 50);
      }
    },
    emitActionOnce(mouseover) {
      if (!this.emitted && (this.active || !mouseover)) {
        this.emitted = true;
        this.$emit('ripple-hover', this.mouseEvent);
        window.setTimeout(() => {
          this.emitted = false;
        }, 500);
      }
    },
    toggleRippleEffect() {
      this.addRippleEffect();
    },
    addRippleEffect() {
      if (!this.ripple) {
        this.ripple = true;
        this.simulateEventOnButton('mousedown');
        return true;
      } else {
        return false;
      }
    },
    cleanRippleEffect() {
      if (this.ripple) {
        this.simulateEventOnButton('mouseup');
        window.setTimeout(() => this.ripple = false, 50);
      }
    },
    simulateEventOnButton(eventType) {
      if (this.$refs.button) {
        this.$refs.button.$el.dispatchEvent(
          new MouseEvent(eventType, {
            bubbles: true,
            cancelable: true,
            view: window
          }));
      }
    },
  }
};
</script>
