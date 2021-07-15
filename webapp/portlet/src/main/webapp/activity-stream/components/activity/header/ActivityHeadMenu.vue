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
          :disabled="loading"
          :loading="loading"
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
    activityTypeExtension: {
      type: Object,
      default: null,
    },
    activityActions: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    loading: false,
  }),
  computed: {
    enabledActions() {
      return this.activityActions && Object.values(this.activityActions).filter(action => action.isEnabled && action.id && action.click && action.isEnabled(this.activity, this.activityTypeExtension));
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
          callback: () => this.confirmAction(action),
        });
      } else {
        this.confirmAction(action);
      }
    },
    confirmAction(action) {
      const result = action.click(this.activity, this.activityTypeExtension);
      if (result && result.finally && result.then) {
        this.loading = true;
        result.finally(() => {
          window.setTimeout(() => {
            this.loading = false;
          }, 500);
        });
      }
    },
  },
};
</script>