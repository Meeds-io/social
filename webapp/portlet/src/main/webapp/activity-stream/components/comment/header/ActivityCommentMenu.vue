<template>
  <v-list-item-icon v-show="enabledActions && enabledActions.length" class="ma-0">
    <v-menu
      v-model="menu"
      bottom
      left
      offset-y
      attach>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          icon
          small
          v-bind="attrs"
          v-on="on">
          <v-icon size="18" class="primary--text">mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense class="pa-0">
        <v-list-item
          v-for="action of enabledActions"
          :key="action.id"
          dense
          @click="clickOnAction(action)">
          <v-list-item-title>{{ $t(action.labelKey) }}</v-list-item-title>
        </v-list-item>
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
      return this.actions && Object.values(this.actions).filter(action => action.isEnabled && action.id && action.click && action.isEnabled(this.activity, this.comment, this.commentTypeExtension));
    },
  },
  created() {
    // Workaround to fix closing menu when clicking outside
    $(document).mousedown(() => {
      if (this.menu) {
        window.setTimeout(() => {
          this.menu = false;
        }, 200);
      }
    });
  },
  methods: {
    clickOnAction(action) {
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
  },
};
</script>