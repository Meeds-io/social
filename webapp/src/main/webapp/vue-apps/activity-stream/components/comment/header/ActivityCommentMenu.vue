<template>
  <v-list-item-icon
    v-show="enabledActions && enabledActions.length"
    class="mx-0 mb-0 mt-n1 flex-shrink-0">
    <v-menu
      v-model="menu"
      attach
      bottom
      content-class="white"
      :left="!$vuetify.rtl"
      :nudge-left="!$vuetify.rtl && '12'"
      :nudge-right="!$vuetify.rtl && '12'"
      offset-y
      :right="$vuetify.rtl">
      <template #activator="{ on, attrs }">
        <v-btn
          icon
          small
          v-bind="attrs"
          v-on="on">
          <v-icon
            class="primary--text"
            size="18">
            mdi-dots-vertical
          </v-icon>
        </v-btn>
      </template>
      <v-list
        class="pa-0"
        dense>
        <v-list-item
          v-for="action of enabledActions"
          :key="action.id"
          dense
          @click="clickOnAction(action)">
          <v-icon
            class="icon-default-color"
            size="16">
            {{ $t(action.icon) }}
          </v-icon>
          <v-list-item-title class="pl-3">
            {{ $t(action.labelKey) }}
          </v-list-item-title>
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
      enabledActions () {
        return this.actions && Object.values(this.actions).filter(action => action.isEnabled && action.id && action.click && action.isEnabled(this.activity, this.comment, this.commentTypeExtension))
          .sort((ext1, ext2) => (ext1.rank || 0) - (ext2.rank || 0));
      },
    },
    created () {
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
      clickOnAction (action) {
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