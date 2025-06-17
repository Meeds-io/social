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
  }),
  computed: {
    zIndex() {
      return this.overlay ? (this.drawerZIndex - 1 + (this.openedDrawers || 0)) : this.drawerZIndex - 1;
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
    showOverlay(event) {
      this.uiPortalApplicationElement.classList.add('decrease-z-index');
      const showOverlay = !event?.detail;
      if (showOverlay) {
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
    hideOverlay(event) {
      const showOverlay = !event?.detail;
      if (showOverlay && this.openedDrawers > 0) {
        window.setTimeout(() => {
          this.openedDrawers -= 1;
          if (this.openedDrawers === 0) {
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