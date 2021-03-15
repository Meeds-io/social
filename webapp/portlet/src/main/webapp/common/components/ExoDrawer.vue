<template>
  <v-navigation-drawer
    v-model="drawer"
    :right="right"
    :left="!right"
    :class="!drawer && 'd-none d-sm-flex'"
    :absolute="!fixed"
    :fixed="fixed"
    :temporary="temporary"
    touchless
    height="100%"
    max-height="100%"
    width="420px"
    max-width="100vw"
    class="drawerParent">
    <v-container v-if="initialized" fill-height class="pa-0">
      <v-layout column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="pr-0">
              <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
                <slot name="title"></slot>
              </v-list-item-content>
              <v-list-item-action class="drawerIcons align-end d-flex flex-row">
                <slot name="titleIcons"></slot>
                <v-btn icon>
                  <v-icon @click="close()">mdi-close</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-flex>
          <v-progress-linear v-if="loading" indeterminate color="primary" />
          <v-divider v-else class="my-0" />
        </template>
        <v-flex class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
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
    right: {
      type: Boolean,
      default: () => false,
    },
    fixed: {
      type: Boolean,
      default: () => false,
    },
    bodyClasses: {
      type: String,
      default: () => 'hide-scroll decrease-z-index',
    },
    confirmClose: {
      type: Boolean,
      default: () => false,
    },
    temporary: {
      type: Boolean,
      default: () => true,
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
  },
  data: () => ({
    initialized: false,
    drawer: false,
    loading: false,
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        if (!this.initialized) {
          this.initialized = true;
        }
        $('body').addClass(this.bodyClasses);
        this.$emit('opened');
      } else {
        window.setTimeout(() => {
          $('body').removeClass(this.bodyClasses);
        }, 200);
        this.$emit('closed');
      }
      this.$nextTick().then(() => {
        $('.v-overlay').off('click').on('click', () => {
          this.close();
        });
      });
    },
  },
  created() {
    $(document).on('keydown', this.closeByEscape);
  },
  methods: {
    open() {
      this.drawer = true;
    },
    closeByEscape(event) {
      if (event.key === 'Escape' && this.drawer) {
        this.close(event);
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
  },
};
</script>