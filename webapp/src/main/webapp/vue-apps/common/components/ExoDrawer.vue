<template>
  <v-navigation-drawer
    v-model="drawer"
    :right="rightDrawer"
    :left="leftDrawer"
    :bottom="bottomDrawer"
    :class="{
      'd-none d-sm-flex': !drawer,
      'v-navigation-drawer--is-mobile snippet-mobile-menu rounded-tr-xl rounded-tl-xl pt-5': bottom,
      'layout-drawer': isBrandingLayout,
      'z-index-snackbar': increaseZindex,
    }"
    :style="{
      'z-index': zIndex,
    }"
    :absolute="!fixed"
    :fixed="fixed"
    :width="width"
    :hide-overlay="!showOverlay"
    temporary
    touchless
    stateless
    height="100%"
    max-height="100%"
    max-width="100vw"
    class="drawerParent overflow-initial"
    ref="drawerParent"
    tabindex="0">
    <div
      v-if="initialized || eager"
      class="pa-0 fill-width fill-height">
      <v-layout class="fill-height" column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="px-0">
              <v-card
                v-if="expand && $slots.fullAppLeftTitle"
                :width="drawerWidth"
                :max-width="drawerWidth"
                min-height="60"
                class="px-4 border-box-sizing text-header-title d-flex align-center light-grey-background-color"
                flat>
                <slot name="fullAppLeftTitle"></slot>
              </v-card>
              <v-list-item-action v-if="goBackButton && !useFilter" class="drawerIcons ps-1 me-2">
                <v-btn
                  icon
                  @click="goBack">
                  <v-icon size="20">
                    {{ $vuetify.rtl && 'fa fa-arrow-right' || 'fa fa-arrow-left' }}
                  </v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-content :class="goBackButton ? 'ps-2' : 'ps-4'" class="drawerTitle align-start text-header-title">
                <div
                  v-if="!showFilter"
                  class="text-truncate full-width">
                  <slot name="title"></slot>
                </div>
                <v-text-field
                  ref="filter"
                  v-if="showFilter"
                  v-model="filterText"
                  :placeholder="resolvedFilterPlaceholder"
                  class="d-flex my-0 ms-0 me-4 pa-0"
                  hide-details
                  @focus="filterFocused = true"
                  @blur="filterFocused = false">
                  <template #prepend-inner>
                    <v-icon
                      :class="{'primary--text': !!filterText || filterFocused }"
                      class="mt-1"
                      size="16">
                      fa-filter
                    </v-icon>
                  </template>
                  <template #prepend>
                    <v-btn
                      icon
                      class="pa-0 mb-n6px mx-0 mt-0"
                      @click="showFilter = !showFilter">
                      <v-icon
                        class="icon-default-color"
                        size="20">
                        fa-arrow-left
                      </v-icon>
                    </v-btn>
                  </template>
                  <template
                    v-if="!!filterText"
                    #append>
                    <v-btn
                      class="pa-0 mt-1 mx-0 mb-0"
                      width="24"
                      height="24"
                      icon
                      @click="filterText = ''">
                      <v-icon
                        class="primary--text"
                        size="16">
                        fa-times
                      </v-icon>
                    </v-btn>
                  </template>
                </v-text-field>
              </v-list-item-content>
              <v-list-item-action
                v-if="!showFilter"
                class="drawerIcons align-end d-flex flex-row pe-3">
                <slot name="titleIcons"></slot>
                <v-btn
                  v-if="useFilter"
                  icon
                  @click="openFilter">
                  <v-icon
                    :class="{
                      'primary--text': !!filterText,
                      'icon-default-color': !filterText
                    }"
                    size="20">
                    fa-filter
                  </v-icon>
                </v-btn>
                <v-btn
                  v-if="allowExpand && !isMobile"
                  :title="expandTooltip"
                  icon
                  @click="toogleExpand">
                  <v-icon v-text="expandIcon" size="20" />
                </v-btn>
                <v-btn
                  :title="$t('label.close')"
                  icon>
                  <v-icon
                    class="icon-default-color"
                    size="20"
                    @click="close()">
                    fa-times
                  </v-icon>
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
        <v-flex class="flex-grow-1 border-box-sizing overflow-hidden">
          <div class="d-flex flex-row fill-height">
            <v-card
              v-if="expand && $slots.fullAppLeftContent"
              :width="drawerWidth"
              class="flex-grow-0 flex-shrink-0 fill-height light-grey-background-color overflow-x-auto overflow-y-auto specific-scrollbar"
              flat>
              <slot name="fullAppLeftContent"></slot>
            </v-card>
            <div class="d-flex flex-column fill-height flex-grow-1 flex-shrink-1 overflow-hidden">
              <div
                :class="{
                  'overflow-x-auto': !noXScroll,
                  'overflow-x-hidden': noXScroll,
                  'overflow-y-auto': !noYScroll,
                  'overflow-y-hidden': noYScroll,
                  'pt-4': bottomDrawer,
                }"
                class="drawerContent flex-grow-1 flex-shrink-1 fill-height specific-scrollbar">
                <slot name="content"></slot>
                <attachments-draggable-zone />
              </div>
              <v-divider
                v-show="$slots.footer && !hideFooterDivider"
                class="my-0" />
              <v-flex
                v-show="$slots.footer"
                :class="footerClass"
                class="drawerFooter border-box-sizing flex-grow-0 px-4 py-3">
                <slot v-if="$slots.footer" name="footer"></slot>
              </v-flex>
            </div>
          </div>
        </v-flex>
        <exo-confirm-dialog
          v-if="confirmClose"
          ref="closeConfirmDialog"
          :title="confirmCloseLabels.title"
          :message="confirmCloseLabels.message"
          :ok-label="confirmCloseLabels.ok"
          :cancel-label="confirmCloseLabels.cancel"
          persistent
          @ok="confirmCloseDrawer" />
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
    attached: {
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
    expanded: {
      type: Boolean,
      default: false,
    },
    showOverlay: {
      type: Boolean,
      default: false,
    },
    noExternalOverlay: {
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
    noXScroll: {
      type: Boolean,
      default: false,
    },
    noYScroll: {
      type: Boolean,
      default: false,
    },
    permanent: {
      type: Boolean,
      default: false,
    },
    isBrandingLayout: {
      type: Boolean,
      default: true,
    },
    hideFooterDivider: {
      type: Boolean,
      default: false,
    },
    useFilter: {
      type: Boolean,
      default: false
    },
    filterPlaceholder: {
      type: String,
      default: null
    },
    footerClass: {
      type: String,
      default: null
    },
    autofocus: {
      type: Boolean,
      default: true
    }
  },
  data: () => ({
    initialized: false,
    drawer: false,
    drawerLoading: false,
    expand: false,
    modalOpened: false,
    increaseZindex: false,
    drawerZIndex: 1035,
    showFilter: false,
    filterText: '',
    filterFocused: false
  }),
  computed: {
    zIndex() {
      return this.drawer ? (this.drawerZIndex + (eXo.openedDrawers?.length || 0)) : this.drawerZIndex;
    },
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
      return this.expand && 'fas fa-compress-alt' || 'fas fa-expand-alt';
    },
    expandTooltip() {
      return this.expand && this.$t('label.collapse') || this.$t('label.expand');
    },
    resolvedFilterPlaceholder() {
      return this.filterPlaceholder || this.$t('label.filter');
    }
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
      if (!this.permanent) {
        if (this.drawer) {
          document.dispatchEvent(new CustomEvent('drawerOpened', {detail: this.showOverlay || this.noExternalOverlay}));
          if (!this.initialized) {
            this.initialized = true;
          }
          eXo.openedDrawers.push(this);
          this.$emit('opened');
          if (this.disablePullToRefresh) {
            document.body.style.overscrollBehaviorY = 'contain';
          }
        } else {
          document.dispatchEvent(new CustomEvent('drawerClosed', {detail: this.showOverlay || this.noExternalOverlay}));
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
      } else if (!this.initialized) {
        this.initialized = true;
      }
      if (this.drawer) {
        this.increaseZindex = !!document.querySelector('.v-dialog--active');
      }

      this.$emit('input', this.drawer);
      this.expand = this.expanded;
    },
    filterText() {
      this.$emit('filter-updated', this.filterText);
    }
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
    if (!this.attached) {
      this.mountOnParent();
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
      if (!this.attached) {
        // Re-append the drawer to open in order
        // to ensure to attribute an adequate z-index
        // Which makes it displayed on top of other already
        // opened drawers
        this.mountOnParent();
        this.$nextTick().then(() => this.drawer = true);
        this.expand = this.expanded;
      } else {
        this.drawer = true;
      }
      this.resetFilter();
      if (this.autofocus) {
        this.$nextTick(() => {
          window.setTimeout(() =>
            this.$refs.drawerParent?.$el.focus(), 50);
        });
      }
    },
    mountOnParent() {
      // Re-append the drawer to open in order
      // to ensure to attribute an adequate z-index
      // Which makes it displayed on top of other already
      // opened drawers
      document.querySelector('#vuetify-apps').appendChild(this.$el);
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
    goBack(event) {
      if (this.$listeners?.['go-back']) {
        this.$emit('go-back');
      } else {
        this.close(event);
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
    confirmCloseDrawer() {
      this.closeEffectively();
      this.$emit('confirm-close');
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
    openFilter() {
      this.showFilter = !this.showFilter;
      this.$nextTick(() => {
        this.$refs?.filter?.focus?.();
      });
    },
    resetFilter() {
      this.showFilter = false;
      this.filterText = '';
    }
  },
};
</script>
