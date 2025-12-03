<template>
  <v-list-item-icon v-show="enabledActions && enabledActions.length" class="mx-0 mb-0 mt-n1 flex-shrink-0">
    <v-menu
      v-model="menu"
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      :close-on-content-click="!$root.isMobile"
      content-class="white"
      bottom
      offset-y
      attach>
      <template #activator="{ on, attrs }">
        <v-btn
          icon
          small
          :aria-label="$t('activity.head.menu.title.open')"
          v-bind="attrs"
          v-on="on">
          <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
        </v-btn>
      </template>
      <v-list class="pa-0" dense>
        <v-menu
          v-for="action of enabledActions"
          :key="action.id"
          :disabled="!action.children.length"
          :left="!$vuetify.rtl"
          :right="$vuetify.rtl"
          open-on-hover
          offset-x>
          <template #activator="{ on, attrs }">
            <v-list-item
              v-on="action.click && {
                ...on,
                click: () => clickOnAction(action),
              } || on"
              v-bind="attrs"
              class="px-3"
              dense>
              <v-list-item-icon class="d-flex align-center justify-center ma-auto">
                <v-card
                  class="d-flex align-center justify-center"
                  color="transparent"
                  min-height="24"
                  min-width="20"
                  flat>
                  <v-img
                    v-if="action.icon?.includes?.('base64') || action.icon?.includes?.('/')"
                    :src="action.icon"
                    max-height="16"
                    height="16"
                    max-width="16"
                    contain
                    eager />
                  <v-icon
                    v-else
                    size="16"
                    class="icon-default-color">
                    {{ $t(action.icon) }}
                  </v-icon>
                </v-card>
              </v-list-item-icon>
              <v-list-item-content class="mx-2">
                <v-list-item-title class="menu-text-color">{{ $t(action.labelKey) }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-icon
                v-if="action.children.length"
                class="ms-2 me-0 width-auto">
                <v-icon size="16">{{ $vuetify.rtl ? 'fa-caret-left' : 'fa-caret-right' }}</v-icon>
              </v-list-item-icon>
            </v-list-item>
          </template>
          <v-list
            v-if="action.children.length"
            class="pa-0"
            dense>
            <v-list-item
              v-for="act of action.children"
              :key="act.id"
              class="px-3"
              dense
              @click="clickOnAction(act)">
              <v-list-item-icon class="d-flex align-center justify-center ma-auto">
                <v-card
                  class="d-flex align-center justify-center"
                  color="transparent"
                  min-height="24"
                  min-width="20"
                  flat>
                  <v-img
                    v-if="act.icon?.includes?.('base64') || act.icon?.includes?.('/')"
                    :src="act.icon"
                    max-height="16"
                    height="16"
                    max-width="16"
                    contain
                    eager />
                  <v-icon
                    v-else
                    size="16"
                    class="icon-default-color">
                    {{ $t(act.icon) }}
                  </v-icon>
                </v-card>
              </v-list-item-icon>
              <v-list-item-content class="mx-2">
                <v-list-item-title class="menu-text-color">{{ $t(act.labelKey) }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-menu>
      </v-list>
    </v-menu>
  </v-list-item-icon>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
    actions: {
      type: Object,
      default: null,
    },
    commentTypeExtension: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    menu: false,
  }),
  computed: {
    enabledActions() {
      const enabledActions = this.actions && Object.values(this.actions).filter(action => action.isEnabled && action.id && !action.parentId && (action.click || action.type === 'group') && action.isEnabled(this.activity, this.comment, this.commentTypeExtension));
      enabledActions.sort((a, b) => a.rank - b.rank);
      enabledActions.forEach(ext => {
        ext.children = Object.values(this.actions).filter(e => e.parentId === ext.id);
      });
      return enabledActions;
    },
  },
  watch: {
    menu() {
      if (!this.$root.isMobile) {
        if (this.menu) {
          document.addEventListener('mousedown', this.closeMenu);
        } else {
          document.removeEventListener('mousedown', this.closeMenu);
        }
      }
    },
  },
  methods: {
    clickOnAction(action) {
      this.closeMenu();
      if (action.confirmDialog) {
        this.$root.$emit('activity-stream-display-confirm', {
          title: action.confirmTitleKey,
          message: action.confirmMessageKey,
          ok: action.confirmOkKey,
          cancel: action.confirmCancelKey,
          callback: () => action.click(this.activity, this.comment, this.commentTypeExtension),
        });
      } else {
        action.click(this.activity, this.comment, this.commentTypeExtension);
      }
    },
    closeMenu() {
      window.setTimeout(() => {
        this.menu = false;
      },200);
    },
  },
};
</script>