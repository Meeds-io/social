<template>
  <span>{{ relativeDateLabel }}</span>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    label: {
      type: String,
      default: null,
    },
    short: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    date() {
      return this.value && new Date(this.value);
    },
    relativeDateLabelKey() {
      if (this.short) {
        return this.date && this.$dateUtil.getShortRelativeTimeLabelKey(this.date) || '';
      } else {
        return this.date && this.$dateUtil.getRelativeTimeLabelKey(this.date) || '';
      }
    },
    relativeDateLabelValue() {
      if (this.short) {
        return this.date && this.$dateUtil.getShortRelativeTimeValue(this.date) || 1;
      } else {
        return this.date && this.$dateUtil.getRelativeTimeValue(this.date) || 1;
      }
    },
    relativeDateLabel() {
      const label = this.date && this.$t(this.relativeDateLabelKey, {0: this.relativeDateLabelValue}) || '';
      if (this.label && !this.short) {
        return this.$t(this.label, {0: label});
      } else {
        return label;
      }
    },
  },
};
</script>