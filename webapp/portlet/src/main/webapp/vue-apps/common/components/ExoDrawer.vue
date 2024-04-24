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
    class="drawerParent overflow-initial">
    <div
      v-if="initialized || eager"
      class="pa-0 fill-width fill-height">
      <v-layout class="fill-height" column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item
              :class="goBackButton && 'ps-1'"
              class="pe-0">
              <v-list-item-action v-if="goBackButton" class="drawerIcons me-2">
                <v-btn icon @click="close()">
                  <v-icon size="20">
                    {{ $vuetify.rtl && 'fa fa-arrow-right' || 'fa fa-arrow-left' }}
                  </v-icon>
                </v-btn>
              </v-list-item-action>
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
          <v-divider :class="!drawerLoading && d-hidden" class="my-0" />
          <div v-if="drawerLoading" class="position-relative z-index-two">
            <v-progress-linear
              indeterminate
              color="primary"
              class="position-absolute" />
          </div>
        </template>
        <v-flex :class="bottomDrawer && 'pt-4'" class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <slot name="content"></slot>
          <attachments-draggable-zone />    
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
    </div>
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
    detached: {
      type: Boolean,
      default: () => false,
    },
    eager: {
      type: Boolean,
      default: () => false,
    },
    goBackButton: {
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
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    initialized: false,
    drawer: false,
    drawerLoading: false,
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
      return this.$vuetify?.breakpoint?.smAndDown;
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
    loading() {
      this.drawerLoading = this.loading;
    },
    isMobile: {
      immediate: true,
      handler: function() {
        if (this.isMobile && this.expand) {
          this.expand = false;
        }
      }
    },
    expand() {
      this.$emit('expand-updated', this.expand);
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

    document.addEventListener('modalOpened', this.setModalOpened);
    document.addEventListener('modalClosed', this.setModalClosed);
    document.addEventListener('closeAllDrawers', this.close);
    document.addEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.addEventListener('close-editor-container', this.closeDisplayedDrawer);
  },
  mounted() {
    if (this.detached || this.$el.closest('.layout-sticky-application')) {
      document.querySelector('#vuetify-apps').appendChild(this.$el);
    }
  },
  beforeDestroy() {
    document.removeEventListener('modalOpened', this.setModalOpened);
    document.removeEventListener('modalClosed', this.setModalClosed);
    document.removeEventListener('closeAllDrawers', this.close);
    document.removeEventListener('closeDisplayedDrawer', this.closeDisplayedDrawer);
    document.removeEventListener('close-editor-container', this.closeDisplayedDrawer);
    if (this.drawer) {
      this.drawer = false;
    }
  },
  methods: {
    open() {
      this.drawer = true;
    },
    setModalOpened() {
      this.modalOpened = this.drawer;
    },
    setModalClosed() {
      this.modalOpened = false;
    },
    closeDisplayedDrawer() {
      const isLastOpenedDrawer = eXo.openedDrawers.indexOf(this) === eXo.openedDrawers.length - 1;
      const inputTextDisplayed = !!document.getElementById('inputURL');
      if (this.drawer && isLastOpenedDrawer && !inputTextDisplayed) {
        this.close();
      }
    },
    close(event) {
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
      this.drawerLoading = true;
    },
    endLoading() {
      this.drawerLoading = false;
    },
    toogleExpand() {
      if (!this.isMobile && this.allowExpand) {
        this.expand = !this.expand;
      }
    },
  },
};
</script>