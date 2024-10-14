<template>
  <exo-confirm-dialog
    ref="confirmDialog"
    :message="$t(confirmMessageKey)"
    :title="$t(confirmTitleKey)"
    :ok-label="$t(confirmOkKey)"
    :cancel-label="$t(confirmCancelKey)"
    @ok="clickCallback && clickCallback()"
    @dialog-opened="dialogOpened = true"
    @dialog-closed="dialogOpened = false" />
</template>

<script>
export default {
  data: () => ({
    confirmMessageKey: null,
    confirmTitleKey: null,
    confirmOkKey: null,
    confirmCancelKey: null,
    clickCallback: null,
    dialogOpened: false,
  }),
  watch: {
    dialogOpened() {
      if (this.dialogOpened) {
        this.$root.$emit('activity-stream-confirm-opened');
      } else {
        this.$root.$emit('activity-stream-confirm-closed');
      }
    },
  },
  created() {
    this.$root.$on('activity-stream-display-confirm', action => {
      this.confirmTitleKey = action.title;
      this.confirmMessageKey = action.message;
      this.confirmOkKey = action.ok;
      this.confirmCancelKey = action.cancel;
      this.clickCallback = action.callback;
      this.$refs.confirmDialog.open();
    });
  },
};
</script>