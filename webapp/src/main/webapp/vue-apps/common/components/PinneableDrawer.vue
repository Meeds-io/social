<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
-->
<template>
  <exo-drawer
    ref="drawer"
    v-bind="$attrs"
    v-on="$listeners"
    :allow-expand="allowExpand && !standalone"
    :expanded="expanded || standalone"
    :permanent="docked || standalone"
    :attached="docked || standalone"
    :no-external-overlay="docked || standalone"
    :autofocus="!docked && !standalone"
    :hide-close="docked || standalone"
    @expand-updated="expandState = $event">
    <template v-for="(unusedSlot, name) in $slots" #[name]>
      <slot :name="name"></slot>
    </template>
    <template #expandAction>
      <v-menu
        v-if="docked"
        open-on-hover
        offset-y
        bottom
        left>
        <template #activator="{ on, attrs }">
          <v-btn
            :title="$t('label.unstick')"
            icon
            v-bind="attrs"
            v-on="on"
            @click="unstick">
            <v-icon size="20">fas fa-thumbtack</v-icon>
          </v-btn>
        </template>
        <v-list dense class="pa-0">
          <v-list-item
            v-if="allowExpand"
            class="px-3"
            dense
            @click="toggleDockedExpand">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon
                v-text="dockedExpanded && 'fas fa-compress-alt' || 'fas fa-expand-alt'"
                size="16"
                class="icon-default-color" />
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ dockedExpanded && $t('label.collapse') || $t('label.expand') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            v-if="canDetach"
            class="px-3"
            dense
            @click="openInNewTab">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon size="16" class="icon-default-color">fas fa-external-link-alt</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ $t('label.openInNewTab') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item dense @click="unstick">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon size="16" class="icon-default-color">fas fa-thumbtack</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ $t('label.unstick') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-menu>
      <v-menu
        v-else-if="displayPlacementMenu"
        open-on-hover
        offset-y
        bottom
        left>
        <template #activator="{ on, attrs }">
          <v-btn
            :title="$t('label.expandDisplay')"
            icon
            v-bind="attrs"
            v-on="on"
            @click="allowExpand && toogleExpand()">
            <v-icon v-text="expandIcon" size="20" />
          </v-btn>
        </template>
        <v-list dense class="pa-0">
          <v-list-item
            v-if="allowExpand"
            class="px-3"
            dense
            @click="toogleExpand">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon
                v-text="expandIcon"
                size="16"
                class="icon-default-color" />
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ expandTooltip }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            v-if="canDetach"
            class="px-3"
            dense
            @click="openInNewTab">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon size="16" class="icon-default-color">fas fa-external-link-alt</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ $t('label.openInNewTab') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            v-if="canStick"
            class="px-3"
            dense
            @click="stickTo('right')">
            <v-list-item-icon class="d-flex align-center justify-center ma-auto">
              <v-icon size="16" class="icon-default-color">far fa-window-maximize fa-rotate-90</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="ms-2">
              <v-list-item-title class="menu-text-color">{{ $t('label.stickRight') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-menu>
      <v-btn
        v-else-if="allowExpand && !standalone"
        :title="expandTooltip"
        icon
        @click="toogleExpand">
        <v-icon v-text="expandIcon" size="20" />
      </v-btn>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  inheritAttrs: false,
  props: {
    appName: {
      type: String,
      default: () => null,
    },
    placementApp: {
      type: Object,
      default: () => null,
    },
    allowExpand: {
      type: Boolean,
      default: false,
    },
    placementDisabled: {
      type: Boolean,
      default: false,
    },
    expanded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    provider: null,
    eligibility: null,
    stuckSide: null,
    expandState: false,
    dockedExpanded: false,
    // held as data and re-synced from the global on each placement change:
    // eXo.env.portal.appPlacements is not reactive, a computed reading it
    // would be evaluated once and cached forever
    payload: null,
  }),
  computed: {
    standalone() {
      return !!(this.appName && window.eXo?.env?.portal?.standaloneAppName === this.appName);
    },
    stuckAllowed() {
      return (this.$vuetify?.breakpoint?.width || 0) >= (this.$vuetify?.breakpoint?.thresholds?.lg || 1264);
    },
    docked() {
      return !!this.stuckSide && this.stuckAllowed;
    },
    canStick() {
      return !!this.provider && !!this.eligibility?.allowStick && !!this.eligibility?.siteEligible;
    },
    canDetach() {
      return !!this.provider && !!this.eligibility?.allowDetach;
    },
    displayPlacementMenu() {
      return !this.standalone && !this.placementDisabled && (this.canStick || this.canDetach);
    },
    expandIcon() {
      return this.expandState && 'fas fa-compress-alt' || 'fas fa-expand-alt';
    },
    expandTooltip() {
      return this.expandState && this.$t('label.collapse') || this.$t('label.expand');
    },
    dockedAppName() {
      return this.appName || `app-${this.placementApp?.id || ''}`;
    },
  },
  watch: {
    docked() {
      if (this.docked) {
        this.dock();
      } else {
        this.undock();
      }
    },
    placementApp() {
      this.refreshEligibility();
    },
    appName() {
      this.refreshPlacementState();
      this.refreshEligibility();
    },
  },
  created() {
    document.addEventListener('extension-Drawer-placementProvider-updated', this.resolveProvider);
    document.addEventListener('app-placement-changed', this.refreshPlacementState);
    document.addEventListener('page-layout-rendered', this.onPageLayoutRendered);
    this.resolveProvider();
  },
  beforeDestroy() {
    document.removeEventListener('extension-Drawer-placementProvider-updated', this.resolveProvider);
    document.removeEventListener('app-placement-changed', this.refreshPlacementState);
    document.removeEventListener('page-layout-rendered', this.onPageLayoutRendered);
    this.undock();
  },
  methods: {
    onPageLayoutRendered() {
      if (this.docked) {
        this.dock();
      }
    },
    toggleDockedExpand() {
      // the docked expand is the standard drawer expand: the shell floats
      // back over the page at full width, collapse returns it to its panel
      const drawerElement = this.$refs.drawer?.$el;
      if (!drawerElement) {
        return;
      }
      this.dockedExpanded = !this.dockedExpanded;
      if (this.dockedExpanded) {
        document.querySelector('#vuetify-apps')?.appendChild(drawerElement);
        drawerElement.style.removeProperty('position');
        drawerElement.style.removeProperty('box-shadow');
        if (!this.expandState) {
          this.$refs.drawer.toogleExpand();
        }
      } else {
        if (this.expandState) {
          this.$refs.drawer.toogleExpand();
        }
        const anchor = document.querySelector(`#pageBody${this.stuckSide === 'left' && 'Left' || 'Right'}Panel`);
        if (anchor) {
          (anchor.querySelector('.v-application--wrap') || anchor).appendChild(drawerElement);
          drawerElement.style.setProperty('position', 'relative', 'important');
          drawerElement.style.setProperty('box-shadow', 'none', 'important');
        }
      }
    },
    resolveProvider() {
      this.provider = extensionRegistry.loadExtensions('Drawer', 'placementProvider')?.[0] || null;
      if (this.provider) {
        this.refreshPlacementState();
        this.refreshEligibility();
      }
    },
    refreshEligibility() {
      if (this.placementApp) {
        this.eligibility = {
          applicationId: this.placementApp.id,
          allowStick: this.placementApp.allowStick,
          allowDetach: this.placementApp.allowDetach,
          siteEligible: this.payload?.siteEligible || false,
        };
      } else if (this.appName && this.provider?.getEligibility) {
        this.provider.getEligibility(this.appName)
          .then(eligibility => this.eligibility = eligibility && {
            ...eligibility,
            siteEligible: this.payload?.siteEligible || false,
          } || null)
          .catch(() => this.eligibility = null);
      } else {
        this.eligibility = null;
      }
    },
    refreshPlacementState() {
      this.payload = window.eXo?.env?.portal?.appPlacements || null;
      const payload = this.payload;
      if (!payload?.siteEligible) {
        this.stuckSide = null;
        return;
      }
      if (this.matchesPlacement(payload.left)) {
        this.stuckSide = 'left';
      } else if (this.matchesPlacement(payload.right)) {
        this.stuckSide = 'right';
      } else {
        this.stuckSide = null;
      }
      if (this.docked) {
        // the anchor is created by the placement host on the same event:
        // re-dock once the DOM settles, dock() is idempotent on its target
        this.$nextTick(this.dock);
      }
    },
    matchesPlacement(placementApplication) {
      if (!placementApplication || placementApplication.type === 'PORTLET') {
        // a stuck portlet instance is rendered by the placement host into
        // its own panel: its preview drawer must never dock for it
        return false;
      } else if (this.placementApp) {
        return `${placementApplication.id}` === `${this.placementApp.id}`;
      } else if (this.appName) {
        return placementApplication.type === 'DRAWER' && placementApplication.url === this.appName;
      }
      return false;
    },
    dock() {
      const anchor = document.querySelector(`#pageBody${this.stuckSide === 'left' && 'Left' || 'Right'}Panel`);
      if (!anchor) {
        return;
      }
      this.$nextTick(() => {
        const drawerElement = this.$refs.drawer?.$el;
        if (!drawerElement) {
          return;
        }
        const dockedSameApp = anchor.querySelector(`[data-stuck-app="${window.CSS.escape(this.dockedAppName)}"]`);
        if (dockedSameApp && dockedSameApp !== drawerElement) {
          this.stuckSide = null;
          return;
        }
        drawerElement.dataset.stuckApp = this.dockedAppName;
        (anchor.querySelector('.v-application--wrap') || anchor).appendChild(drawerElement);
        anchor.classList.add('stuck-app-panel');
        // the anchor owns its geometry (width reservation, sticky position,
        // stacking context) through the layout app's own style binding: the
        // drawer only neutralizes the Vuetify positioning of its shell
        drawerElement.style.setProperty('position', 'relative', 'important');
        // Vuetify elevates open temporary drawers; a docked panel is part of
        // the page layout and must sit flat next to the content
        drawerElement.style.setProperty('box-shadow', 'none', 'important');
        this.$refs.drawer.open();
      });
    },
    undock() {
      if (this.dockedExpanded && this.expandState) {
        this.$refs.drawer.toogleExpand();
      }
      this.dockedExpanded = false;
      const drawerElement = this.$refs.drawer?.$el;
      if (!drawerElement?.dataset?.stuckApp) {
        return;
      }
      // the placement host may already have taken the anchor back (a side
      // switching to another application): clean the shell wherever it is
      delete drawerElement.dataset.stuckApp;
      const anchor = drawerElement.closest?.('#pageBodyLeftPanel, #pageBodyRightPanel');
      drawerElement.style.removeProperty('position');
      drawerElement.style.removeProperty('z-index');
      drawerElement.style.removeProperty('box-shadow');
      this.$refs.drawer.close();
      document.querySelector('#vuetify-apps')?.appendChild(drawerElement);
      if (anchor && !anchor.querySelector('[data-stuck-app]')) {
        anchor.classList.remove('stuck-app-panel');
      }
    },
    stickTo(side) {
      if (this.$refs.drawer?.drawer) {
        this.$refs.drawer.close();
      }
      this.provider.stick(this.eligibility.applicationId, side)
        .catch(this.dispatchPlacementError);
    },
    unstick() {
      this.provider.unstick(this.stuckSide)
        .catch(this.dispatchPlacementError);
    },
    openInNewTab() {
      this.provider.openDetached(this.eligibility.applicationId);
    },
    dispatchPlacementError() {
      document.dispatchEvent(new CustomEvent('alert-message', {detail: {
        alertType: 'error',
        alertMessage: this.$t('label.placementActionError'),
      }}));
    },
    open(...args) {
      return this.$refs.drawer?.open?.(...args);
    },
    close(...args) {
      return this.$refs.drawer?.close?.(...args);
    },
    startLoading() {
      return this.$refs.drawer?.startLoading?.();
    },
    endLoading() {
      return this.$refs.drawer?.endLoading?.();
    },
    toogleExpand() {
      return this.$refs.drawer?.toogleExpand?.();
    },
  },
};
</script>
