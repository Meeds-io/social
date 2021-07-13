<template>
  <v-app>
    <v-fade-transition>
      <v-overlay
        v-show="overlay"
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
  }),
  computed: {
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
  },
  methods: {
    closeDisplayedDrawerNoEvent() {
      this.closeDisplayedDrawer();
    },
    closeDisplayedDrawer(event) {
      if (this.openedDrawers && (!event || event.key === 'Escape')) {
        document.dispatchEvent(new CustomEvent('closeDisplayedDrawer'));
      }
    },
    showOverlay() {
      document.querySelector('#UIPortalApplication').classList.add('decrease-z-index');
      window.setTimeout(() => {
        this.openedDrawers += 1;
      }, 10);
    },
    hideOverlay() {
      if (this.openedDrawers > 0) {
        this.openedDrawers -= 1;
      }
      if (this.openedDrawers === 0) {
        document.querySelector('#UIPortalApplication').classList.remove('decrease-z-index');
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