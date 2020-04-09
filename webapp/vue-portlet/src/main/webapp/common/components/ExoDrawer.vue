<template>
  <v-navigation-drawer
    v-model="drawer"
    :right="right"
    :left="!right"
    :class="!drawer && 'd-none d-sm-flex'"
    absolute
    temporary
    touchless
    height="100vh"
    max-height="100vh"
    width="420px"
    max-width="100vw"
    class="drawerParent">
    <v-container fill-height class="pa-0">
      <v-layout column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="pr-0">
              <v-list-item-content class="drawerTitle align-start text-truncate">
                <slot name="title"></slot>
              </v-list-item-content>
              <v-list-item-action class="drawerIcons align-end">
                <slot name="titleIcons"></slot>
                <v-icon @click="drawer = false">mdi-close</v-icon>
              </v-list-item-action>
            </v-list-item>
          </v-flex>
          <v-divider class="my-1" />
        </template>
        <v-flex class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <slot name="content"></slot>
        </v-flex>
        <template v-if="$slots.footer">
          <v-divider class="my-0" />
          <v-flex v-if="$slots.footer" class="drawerFooter flex-grow-0 px-4 py-3">
            <slot name="footer"></slot>
          </v-flex>
        </template>
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
  },
  data: () => ({
    drawer: false,
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        $('body').addClass('hide-scroll');
        this.$emit('opened');
      } else {
        $('body').removeClass('hide-scroll');
        this.$emit('closed');
      }
      this.$nextTick().then(() => {
        $('.v-overlay').off('click').on('click', () => {
          this.drawer = false;
        });
      });
    },
  },
  created() {
    $(document).on('keydown', (event) => {
      if (event.key === 'Escape') {
        this.drawer = false;
      }
    });
  },
  methods: {
    open() {
      this.drawer = true;
    },
    close() {
      this.drawer = false;
    },
  },
};
</script>