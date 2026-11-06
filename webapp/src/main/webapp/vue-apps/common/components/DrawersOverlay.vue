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
    forcedOverlay: false,
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
      return this.overlay ? (baseZIndex + (this.openedDrawers || 1)) : baseZIndex;
    },
    /**
     * Tells whether the shared mask is displayed: when a drawer delegating
     * its overlay to this component is opened, or when the mask=true display
     * mode forces it, and never while a dialog is opened.
     * @returns {Boolean} true when the mask is displayed
     */
    overlay() {
      return (this.openedDrawers || this.forcedOverlay) && !this.openedModals;
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
      document.addEventListener('mouseover', this.forceShowOverlay);
      document.addEventListener('mouseout', this.forceHideOverlay);
    }
    // A drawer opened before this component listens, e.g. at page load,
    // fired its 'drawerOpened' event to nobody
    this.countOpenedDrawers();
    if (this.openedDrawers) {
      this.aboveDialog = !!document.querySelector('.v-dialog--active');
      this.uiPortalApplicationElement?.classList.add('decrease-z-index');
    }
  },
  methods: {
    /**
     * Closes the topmost drawer when the mask is clicked.
     * @returns {void}
     */
    closeDisplayedDrawerNoEvent() {
      this.closeDisplayedDrawer();
    },
    /**
     * Closes the topmost drawer on a mask click or on the Escape key.
     * @param {KeyboardEvent} [event] the key pressed, absent on a mask click
     * @returns {void}
     */
    closeDisplayedDrawer(event) {
      if (this.openedDrawers && (!event || event.key === 'Escape')) {
        this.closeDisplayedDrawerEffectively();
      }
    },
    /**
     * Asks the opened drawers to close the topmost one.
     * @returns {void}
     */
    closeDisplayedDrawerEffectively() {
      document.dispatchEvent(new CustomEvent('closeDisplayedDrawer'));
    },
    /**
     * Sets the number of opened drawers this mask dims for from the drawers
     * registry, eXo.openedDrawers, rather than from the events received: a
     * drawer opened before this component listens, or a 'drawerClosed' event
     * that no drawer sent, would otherwise leave the count wrong for the rest
     * of the page's life, and the mask under the drawers it should dim.
     * Only the drawers delegating their overlay to this component are counted.
     * @returns {void}
     */
    countOpenedDrawers() {
      this.openedDrawers = (eXo.openedDrawers || []).filter(drawer => drawer && !drawer.overlayDetail).length;
    },
    /**
     * Displays the shared mask when a drawer delegating its overlay to this
     * component is opened.
     * Probes the DOM for an active Vuetify dialog at that very moment, with the
     * same selector and in the same tick as ExoDrawer does when it decides to
     * climb above such a dialog, so that a drawer and its mask can never
     * disagree on which plane they are being displayed on.
     * The count is read once ExoDrawer has registered the drawer, which it
     * does right after dispatching the event.
     * @param {CustomEvent} event 'drawerOpened' event, its detail being true
     *          when the drawer displays an overlay of its own
     * @returns {void}
     */
    showOverlay(event) {
      this.uiPortalApplicationElement.classList.add('decrease-z-index');
      const showOverlay = !event?.detail;
      if (showOverlay) {
        this.aboveDialog = !!document.querySelector('.v-dialog--active');
        window.setTimeout(() => this.countOpenedDrawers(), 10);
      } else {
        window.setTimeout(() => {
          const openedOverlay = document.querySelector('.PORTLET-FRAGMENT .v-overlay--active');
          if (openedOverlay) {
            openedOverlay.onclick = this.closeDisplayedDrawerEffectively;
          }
        }, 200);
      }
    },
    /**
     * Displays the mask while the pointer is over the page, in the
     * mask=true display mode, whatever drawer is opened.
     * @returns {void}
     */
    forceShowOverlay() {
      this.uiPortalApplicationElement.classList.add('decrease-z-index');
      this.aboveDialog = !!document.querySelector('.v-dialog--active');
      this.forcedOverlay = true;
    },
    /**
     * Hides the mask displayed by the mask=true display mode, unless a drawer
     * still needs it.
     * @returns {void}
     */
    forceHideOverlay() {
      this.forcedOverlay = false;
      if (!this.openedDrawers) {
        this.resetOverlayPlane();
      }
    },
    /**
     * Hides the shared mask when a drawer delegating its overlay to this
     * component is closed, and gives the mask its drawer plane back once the
     * last drawer is closed.
     * The count is read once ExoDrawer has unregistered the drawer, which it
     * does right after dispatching the event.
     * @param {CustomEvent} event 'drawerClosed' event, its detail being true
     *          when the drawer displays an overlay of its own
     * @returns {void}
     */
    hideOverlay(event) {
      const showOverlay = !event?.detail;
      if (showOverlay) {
        window.setTimeout(() => {
          const wasDisplayed = this.openedDrawers > 0;
          this.countOpenedDrawers();
          if (wasDisplayed && !this.openedDrawers && !this.forcedOverlay) {
            this.resetOverlayPlane();
          }
        }, 10);
      }
    },
    /**
     * Gives the mask and the portal application their plane back once no
     * drawer is dimmed for anymore.
     * @returns {void}
     */
    resetOverlayPlane() {
      this.aboveDialog = false;
      this.uiPortalApplicationElement.classList.remove('decrease-z-index');
    },
    /**
     * Counts a dialog opening, during which the mask is hidden.
     * @returns {void}
     */
    modalOpened() {
      this.openedModals += 1;
    },
    /**
     * Counts a dialog closing.
     * @returns {void}
     */
    modalClosed() {
      if (this.openedModals > 0) {
        this.openedModals -= 1;
      }
    },
  },
};
</script>