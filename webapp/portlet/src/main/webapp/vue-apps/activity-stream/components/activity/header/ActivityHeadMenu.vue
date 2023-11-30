<template>
  <v-list-item-icon v-show="enabledActions && enabledActions.length" class="ma-0">
    <v-menu
      v-model="menu"
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      bottom
      offset-y
      attach>
      <template #activator="{ on, attrs }">
        <v-btn
          :disabled="loading"
          :loading="loading"
          icon
          small
          :aria-label="$t('activity.head.menu.title.open')"
          class="me-2"
          v-bind="attrs"
          v-on="on">
          <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
        </v-btn>
      </template>
      <v-list dense class="pa-0">
        <v-list-item
          v-for="action of enabledActions"
          :key="action.id"
          dense
          @click="clickOnAction(action)">
          <v-icon size="13" class="dark-grey-color">{{ $t(action.icon) }}</v-icon>
          <v-list-item-title class="pl-3">{{ $t(action.labelKey) }}</v-list-item-title>
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
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    menu: false,
    loading: false,
  }),
  computed: {
    enabledActions() {
      return this.activityActions && Object.values(this.activityActions).filter(action => action.isEnabled && action.id && action.click && action.isEnabled(this.activity, this.activityTypeExtension, this.isActivityDetail));
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
      const result = action.click(this.activity, this.activityTypeExtension, this.isActivityDetail);
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