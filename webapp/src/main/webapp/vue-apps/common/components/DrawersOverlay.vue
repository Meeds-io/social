<template>
  <v-app>
    <v-fade-transition>
      <v-overlay
        v-show="overlay"
        :z-index="zIndex"
        id="drawers-overlay"
        absolute />
    </v-fade-transition>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    openedModals: 0,
    openedDrawers: 0,
    uiPortalApplicationElement: null,
    drawerZIndex: 1035,
    // Mirrors @zindexModal, the plane the skin clamps an active Vuetify dialog
    // to inside #vuetify-apps, exactly as drawerZIndex mirrors @zindexDrawer
    modalZIndex: 1050,
    aboveDialog: false,
  }),
  computed: {
    /**
     * Computes the z-index of the shared drawers mask, which has to paint above
     * everything it dims and below the topmost opened drawer.
     * Its base plane is the drawer plane, unless the drawers were opened while
     * a Vuetify dialog was active: such a dialog is clamped by the skin to
     * @zindexModal, above the drawer plane, so basing the mask on the drawer
     * plane would bury it under the dialog instead of dimming it. In that case
     * the modal plane is used as base, which keeps the mask above the dialog
     * and still below a drawer climbing to @zindexSnackBar.
     * When no dialog is active, this returns the very same value as it did
     * before the computation was made dialog aware.
     * @returns {Number} z-index to apply on the mask
     */
    zIndex() {
      const baseZIndex = this.aboveDialog ? this.modalZIndex : this.drawerZIndex - 1;
      return this.overlay ? (baseZIndex + (this.openedDrawers || 0)) : baseZIndex;
    },
    overlay() {
      return this.openedDrawers && !this.openedModals;
    },
  },
  mounted() {
    document.addEventListener('drawerOpened', this.showOverlay);
    document.addEventListener('drawerClosed', this.hideOverlay);
    document.addEventListener('modalOpened', this.modalOpened);
    document.addEventListener('modalClosed', this.modalClosed);
    document.onkeydown = this.closeDisplayedDrawer;
    document.querySelector('#drawers-overlay').onclick = this.closeDisplayedDrawerNoEvent;
    this.uiPortalApplicationElement = document.querySelector('#UIPortalApplication');
    if (this.$utils.getQueryParam('mask') === 'true') {
      document.addEventListener('mouseover', this.showOverlay);
      document.addEventListener('mouseout', this.forceHideOverlay);
    }
  },
  methods: {
    closeDisplayedDrawerNoEvent() {
      this.closeDisplayedDrawer();
    },
    closeDisplayedDrawer(event) {
      if (this.openedDrawers && (!event || event.key === 'Escape')) {
        this.closeDisplayedDrawerEffectively();
      }
    },
    closeDisplayedDrawerEffectively() {
      document.dispatchEvent(new CustomEvent('closeDisplayedDrawer'));
    },
    /**
     * Displays the shared mask when a drawer delegating its overlay to this
     * component is opened.
     * Probes the DOM for an active Vuetify dialog at that very moment, with the
     * same selector and in the same tick as ExoDrawer does when it decides to
     * climb above such a dialog, so that a drawer and its mask can never
     * disagree on which plane they are being displayed on.
     * @param {CustomEvent} event 'drawerOpened' event, its detail being true
     *          when the drawer displays an overlay of its own
     * @returns {void}
     */
    showOverlay(event) {
      this.uiPortalApplicationElement.classList.add('decrease-z-index');
      const showOverlay = !event?.detail;
      if (showOverlay) {
        this.aboveDialog = !!document.querySelector('.v-dialog--active');
        window.setTimeout(() => {
          this.openedDrawers += 1;
        }, 10);
      } else {
        window.setTimeout(() => {
          const openedOverlay = document.querySelector('.PORTLET-FRAGMENT .v-overlay--active');
          if (openedOverlay) {
            openedOverlay.onclick = this.closeDisplayedDrawerEffectively;
          }
        }, 200);
      }
    },
    forceHideOverlay() {
      this.openedDrawers = 1;
      this.hideOverlay();
    },
    /**
     * Hides the shared mask when a drawer delegating its overlay to this
     * component is closed, and gives the mask its drawer plane back once the
     * last drawer is closed.
     * @param {CustomEvent} event 'drawerClosed' event, its detail being true
     *          when the drawer displays an overlay of its own
     * @returns {void}
     */
    hideOverlay(event) {
      const showOverlay = !event?.detail;
      if (showOverlay && this.openedDrawers > 0) {
        window.setTimeout(() => {
          this.openedDrawers -= 1;
          if (this.openedDrawers === 0) {
            this.aboveDialog = false;
            this.uiPortalApplicationElement.classList.remove('decrease-z-index');
          }
        }, 10);
      }
    },
    modalOpened() {
      this.openedModals += 1;
    },
    modalClosed() {
      if (this.openedModals > 0) {
        this.openedModals -= 1;
      }
    },
  },
};
</script>