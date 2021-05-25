<template>
  <span>{{ relativeDateLabel }}</span>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: () => 0,
    },
    label: {
      type: String,
      default: null,
    },
  },
  computed: {
    date() {
      return this.value && new Date(this.value);
    },
    relativeDateLabelKey() {
      return this.date && this.$dateUtil.getRelativeTimeLabelKey(this.date) || '';
    },
    relativeDateLabelValue() {
      return this.date && this.$dateUtil.getRelativeTimeValue(this.date) || 1;
    },
    relativeDateLabel() {
      const label = this.date && this.$t(this.relativeDateLabelKey, {0: this.relativeDateLabelValue}) || '';
      if (this.label) {
        return this.$t(this.label, {0: label});
      } else {
        return label;
      }
    },
  },
};
</script>