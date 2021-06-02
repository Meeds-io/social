<template>
  <v-list-item-icon v-show="enabledActions && enabledActions.length" class="ma-0">
    <v-menu
      v-model="menu"
      bottom
      left
      offset-y>
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
  }),
  computed: {
    enabledActions() {
      return this.activityActions && Object.values(this.activityActions).filter(action => action.isEnabled && action.id && action.click && action.isEnabled(this.activity, this.activityTypeExtension));
    },
  },
  created() {
    $(document).mouseup(() => this.menu = false);
  },
  methods: {
    clickOnAction(action) {
      if (action.confirmDialog) {
        this.$root.$emit('activity-stream-display-confirm', {
          title: action.confirmTitleKey,
          message: action.confirmMessageKey,
          ok: action.confirmOkKey,
          cancel: action.confirmCancelKey,
          callback: () => action.click(this.activity, this.activityTypeExtension),
        });
      } else {
        action.click(this.activity, this.activityTypeExtension);
      }
    },
  },
};
</script>