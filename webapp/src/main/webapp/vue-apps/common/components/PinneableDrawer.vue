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
    :allow-expand="allowExpand && !docked && !standalone"
    :expanded="expanded || standalone"
    :permanent="docked || standalone"
    :attached="docked || standalone"
    :no-external-overlay="docked || standalone"
    :hide-close="standalone"
    @expand-updated="expandState = $event">
    <template v-for="(unusedSlot, name) in $slots" #[name]>
      <slot :name="name"></slot>
    </template>
    <template #expandAction>
      <v-btn
        v-if="docked"
        :title="$t('label.unstick')"
        icon
        @click="unstick">
        <v-icon size="20">fas fa-thumbtack</v-icon>
      </v-btn>
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
            v-on="on">
            <v-icon v-text="expandIcon" size="20" />
          </v-btn>
        </template>
        <v-list dense class="pa-0">
          <v-list-item
            v-if="allowExpand"
            dense
            @click="toogleExpand">
            <v-list-item-icon class="mx-1 justify-center">
              <v-icon
                v-text="expandIcon"
                size="14"
                class="icon-default-color" />
            </v-list-item-icon>
            <v-list-item-title class="pl-0">{{ expandTooltip }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="canDetach"
            dense
            @click="openInNewTab">
            <v-list-item-icon class="mx-1 justify-center">
              <v-icon size="14" class="icon-default-color">fas fa-external-link-alt</v-icon>
            </v-list-item-icon>
            <v-list-item-title class="pl-0">{{ $t('label.openInNewTab') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="canStick"
            dense
            @click="stickTo('right')">
            <v-list-item-icon class="mx-1 justify-center">
              <v-icon size="14" class="icon-default-color">far fa-window-maximize fa-rotate-90</v-icon>
            </v-list-item-icon>
            <v-list-item-title class="pl-0">{{ $t('label.stickRight') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="canStick"
            dense
            @click="stickTo('left')">
            <v-list-item-icon class="mx-1 justify-center">
              <v-icon size="14" class="icon-default-color">far fa-window-maximize fa-rotate-270</v-icon>
            </v-list-item-icon>
            <v-list-item-title class="pl-0">{{ $t('label.stickLeft') }}</v-list-item-title>
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
    expanded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    provider: null,
    eligibility: null,
    stuckSide: null,
    standalone: false,
    expandState: false,
    // held as data and re-synced from the global on each placement change:
    // eXo.env.portal.appPlacements is not reactive, a computed reading it
    // would be evaluated once and cached forever
    payload: null,
  }),
  computed: {
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
      return !this.standalone && (this.canStick || this.canDetach);
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
    panelWidth() {
      return this.$attrs['drawer-width'] || this.$attrs.drawerWidth || '420px';
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
  },
  created() {
    this.standalone = !!(this.appName && window.eXo?.env?.portal?.standaloneAppName === this.appName);
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
    },
    matchesPlacement(placementApplication) {
      if (!placementApplication) {
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
        anchor.appendChild(drawerElement);
        anchor.classList.add('stuck-app-panel');
        // the anchor is what reserves the space in the page body flex row:
        // a v-navigation-drawer never participates in flex flow (the skin
        // even forces position: fixed), so the shell is positioned inside
        // the relative anchor at a page-level stacking context
        anchor.style.position = 'relative';
        anchor.style.flex = `0 0 ${this.panelWidth}`;
        drawerElement.style.setProperty('position', 'relative', 'important');
        drawerElement.style.setProperty('z-index', 'auto', 'important');
        // Vuetify elevates open temporary drawers; a docked panel is part of
        // the page layout and must sit flat next to the content
        drawerElement.style.setProperty('box-shadow', 'none', 'important');
        this.$refs.drawer.open();
      });
    },
    undock() {
      const drawerElement = this.$refs.drawer?.$el;
      if (drawerElement?.dataset?.stuckApp) {
        delete drawerElement.dataset.stuckApp;
      }
      const anchor = drawerElement?.closest?.('#pageBodyLeftPanel, #pageBodyRightPanel');
      if (anchor) {
        drawerElement.style.removeProperty('position');
        drawerElement.style.removeProperty('z-index');
        drawerElement.style.removeProperty('box-shadow');
        this.$refs.drawer.close();
        document.querySelector('#vuetify-apps')?.appendChild(drawerElement);
        if (!anchor.querySelector('[data-stuck-app]')) {
          anchor.classList.remove('stuck-app-panel');
          anchor.style.removeProperty('flex');
          anchor.style.removeProperty('position');
        }
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
