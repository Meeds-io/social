<template>
  <v-navigation-drawer
    v-model="drawer"
    :right="rightDrawer"
    :left="leftDrawer"
    :bottom="bottomDrawer"
    :class="[!drawer && 'd-none d-sm-flex', bottom && 'v-navigation-drawer--is-mobile snippet-mobile-menu rounded-tr-xl rounded-tl-xl pt-5']"
    :absolute="!fixed"
    :fixed="fixed"
    :width="width"
    :hide-overlay="!showOverlay"
    :temporary="!showOverlay"
    touchless
    stateless
    height="100%"
    max-height="100%"
    max-width="100vw"
    class="drawerParent">
    <v-container
      v-if="initialized || eager"
      fill-height
      class="pa-0">
      <v-layout column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="pe-0">
              <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
                <slot name="title"></slot>
              </v-list-item-content>
              <v-list-item-action class="drawerIcons align-end d-flex flex-row">
                <slot name="titleIcons"></slot>
                <v-btn
                  v-if="allowExpand && !isMobile"
                  :title="expandTooltip"
                  icon
                  @click="toogleExpand">
                  <v-icon v-text="expandIcon" size="18" />
                </v-btn>
                <v-btn
                  :title="$t('label.close')"
                  icon>
                  <v-icon @click="close()">mdi-close</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-flex>
          <v-progress-linear
            v-if="loading"
            indeterminate
            color="primary" />
          <v-divider v-else class="my-0" />
        </template>
        <v-flex :class="bottomDrawer && 'pt-4'" class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <slot name="content"></slot>
        </v-flex>
        <template v-if="$slots.footer">
          <v-divider class="my-0" />
          <v-flex v-if="$slots.footer" class="drawerFooter border-box-sizing flex-grow-0 px-4 py-3">
            <slot name="footer"></slot>
          </v-flex>
        </template>
        <exo-confirm-dialog
          v-if="confirmClose"
          ref="closeConfirmDialog"
          :title="confirmCloseLabels.title"
          :message="confirmCloseLabels.message"
          :ok-label="confirmCloseLabels.ok"
          :cancel-label="confirmCloseLabels.cancel"
          persistent
          @ok="closeEffectively" />
      </v-layout>
    </v-container>
  </v-navigation-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: () => false,
    },
    right: {
      type: Boolean,
      default: () => false,
    },
    left: {
      type: Boolean,
      default: () => false,
    },
    bottom: {
      type: Boolean,
      default: () => false,
    },
    fixed: {
      type: Boolean,
      default: () => false,
    },
    eager: {
      type: Boolean,
      default: () => false,
    },
    confirmClose: {
      type: Boolean,
      default: () => false,
    },
    drawerWidth: {
      type: String,
      default: () => '420px',
    },
    allowExpand: {
      type: Boolean,
      default: false,
    },
    showOverlay: {
      type: Boolean,
      default: false,
    },
    confirmCloseLabels: {
      type: Object,
      default: () => ({
        title: null,
        message: null,
        ok: null,
        cancel: null,
      }),
    },
    disablePullToRefresh: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    initialized: false,
    drawer: false,
    loading: false,
    expand: false,
    modalOpened: false,
  }),
  computed: {
    rightDrawer() {
      return (this.right && eXo.env.portal.orientation === 'ltr') || (this.left && eXo.env.portal.orientation === 'rtl');
    },
    leftDrawer() {
      return !this.rightDrawer && !this.bottomDrawer;
    },
    bottomDrawer() {
      return this.bottom && this.isMobile;
    },
    width() {
      return this.expand && '100%' || this.drawerWidth;
    },
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    expandIcon() {
      return this.expand && 'mdi-arrow-collapse' || 'mdi-arrow-expand';
    },
    expandTooltip() {
      return this.expand && this.$t('label.collapse') || this.$t('label.expand');
    },
  },
  watch: {
    value() {
      if (this.value && !this.drawer) {
        this.open();
      } else if (!this.value && this.drawer) {
        this.close();
      }
    },
    drawer() {
      if (this.drawer) {
        document.dispatchEvent(new CustomEvent('drawerOpened'));
        if (!this.initialized) {
          this.initialized = true;
        }
        eXo.openedDrawers.push(this);
        this.$emit('opened');
        if (this.disablePullToRefresh) {
          document.body.style.overscrollBehaviorY = 'contain';
        }
      } else {
        document.dispatchEvent(new CustomEvent('drawerClosed'));
        if (eXo.openedDrawers) {
          const currentOpenedDrawerIndex = eXo.openedDrawers.indexOf(this);
          if (currentOpenedDrawerIndex >= 0) {
            eXo.openedDrawers.splice(currentOpenedDrawerIndex, 1);
          }
        }
        this.$emit('closed');
        if (this.disablePullToRefresh) {
          document.body.style.overscrollBehaviorY = '';
        }
      }
      this.$emit('input', this.drawer);
      this.expand = false;
    },
  },
  created() {
    if (!eXo.openedDrawers) {
      eXo.openedDrawers = [];
    }

    document.addEventListener('modalOpened', () => this.modalOpened = this.drawer);
    document.addEventListener('modalClosed', () => this.modalOpened = false);
    document.addEventListener('closeAllDrawers', this.close);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
  },
  methods: {
    open() {
      this.drawer = true;
    },
    closeDisplayedDrawer() {
      const isLastOpenedDrawer = eXo.openedDrawers.indexOf(this) === eXo.openedDrawers.length - 1;
      if (this.drawer && isLastOpenedDrawer) {
        this.close();
      }
    },
    close() {
      if (this.confirmClose) {
        if (this.$refs.closeConfirmDialog) {
          if (event) {
            event.preventDefault();
            event.stopPropagation();
          }

          if (this.$refs.closeConfirmDialog.dialog) {
            this.$nextTick(this.$refs.closeConfirmDialog.close);
          } else {
            this.$nextTick(this.$refs.closeConfirmDialog.open);
          }
        }
      } else {
        if (event) {
          event.preventDefault();
          event.stopPropagation();
        }

        this.closeEffectively();
      }
    },
    closeEffectively() {
      this.drawer = false;
    },
    startLoading() {
      this.loading = true;
    },
    endLoading() {
      this.loading = false;
    },
    toogleExpand() {
      this.expand = !this.expand;
    },
  },
};
</script>