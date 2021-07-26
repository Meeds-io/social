<template>
  <v-textarea
    v-model="value"
    :rules="rules"
    :counter-value="counterValue"
    :placeholder="placeholder"
    counter
    auto-grow
    class="extended-textarea" />
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    maxLength: {
      type: Number,
      default: () => 0
    }
  },
  data: () => ({
    rules: [],
  }),
  computed: {
    limitMessageLabel() {
      return this.$t('textarea.limitMessage').replace('{0}', this.maxLength);
    }
  },
  watch: {
    value() {
      this.emitText();
    },
  },
  created() {
    this.rules = [v => !v || v.length <= this.maxLength || this.limitMessageLabel];
  },
  methods: {
    counterValue(value) {
      return `${value && value.length || 0} / ${this.maxLength}`;
    },
    emitText() {
      this.$emit('input', this.value);
    }
  }
};
</script>