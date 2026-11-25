<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <!-- a card of its own above the Language card, as far from it as two applications of the page are -->
  <v-list
    v-if="displayed"
    :style="cardStyle"
    class="application-layout-style"
    two-line>
    <v-list-item class="userSettingDisplayMode">
      <v-list-item-content>
        <v-list-item-title
          :id="titleId"
          class="text-title">
          {{ $t('UserSettings.displayMode') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action class="flex-row align-center my-0">
        <div
          role="radiogroup"
          :aria-labelledby="titleId"
          class="d-flex align-center">
          <v-btn
            v-for="option in options"
            :key="option.mode"
            :class="option.mode === mode && 'primary--text light-grey-background-color displayModeSelected'"
            :aria-label="$t(option.label)"
            :title="$t(option.label)"
            :aria-checked="String(option.mode === mode)"
            :id="`displayMode${option.mode}`"
            role="radio"
            class="ms-1"
            small
            icon
            tile
            @click="select(option.mode)">
            <v-icon size="18" :class="option.mode !== mode && 'icon-default-color'">{{ option.icon }}</v-icon>
          </v-btn>
        </div>
      </v-list-item-action>
    </v-list-item>
  </v-list>
</template>
<script>
// the space the page puts between two applications of a column: the gap, and the margins the theme or the page give
// them beyond the default 20px
const APPLICATIONS_SPACE = 'calc(var(--grid-layout-gap, 20px) + var(--appMarginBottom, var(--allPagesAppMarginBottom, 20px)) + var(--appMarginTop, var(--allPagesAppMarginTop, 20px)) - 40px)';
// the height the page gives the application sizes the Language card only
const CARD_STYLE = {
  'marginBottom': APPLICATIONS_SPACE,
  '--appHeight': 'initial',
  '--appMinHeight': 'initial',
  '--appMaxHeight': 'initial',
  '--appHeightScroll': 'initial',
};

export default {
  data: () => ({
    cardStyle: CARD_STYLE,
    titleId: `displayModeTitle${parseInt(Math.random() * 10000)}`,
    mode: null,
    // the last mode the server stored: the one a refused save goes back to
    storedMode: null,
    modesOffered: null,
    // the saves run one after another, in the order of the clicks
    saving: Promise.resolve(),
    options: [{
      mode: 'AUTOMATIC',
      icon: 'fas fa-desktop',
      label: 'UserSettings.displayMode.automatic',
    }, {
      mode: 'LIGHT',
      icon: 'fas fa-sun',
      label: 'UserSettings.displayMode.light',
    }, {
      mode: 'DARK',
      icon: 'fas fa-moon',
      label: 'UserSettings.displayMode.dark',
    }],
  }),
  computed: {
    // the row exists only when users choose: with one mode offered, every page shows that mode
    displayed() {
      return this.modesOffered === 'LIGHT_AND_DARK' && !!this.mode;
    },
  },
  created() {
    this.$brandingService.getDisplayMode()
      .then(displayMode => {
        this.mode = displayMode?.mode;
        this.storedMode = this.mode;
        this.modesOffered = displayMode?.modesOffered;
      })
      .catch(() => this.modesOffered = null);
  },
  methods: {
    select(mode) {
      if (mode === this.mode) {
        return;
      }
      // applied at once, no reload; a refused save restores the last mode the server stored
      this.mode = mode;
      this.$displayMode.applyMode(mode);
      this.saving = this.saving
        .then(() => this.$brandingService.setDisplayMode(mode))
        .then(() => this.storedMode = mode)
        .catch(() => {
          if (this.mode === mode) {
            this.mode = this.storedMode;
            this.$displayMode.applyMode(this.storedMode);
          }
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'error',
            alertMessage: this.$t('UserSettings.displayMode.error'),
          }}));
        });
    },
  },
};
</script>
