<template>
  <v-textarea
    v-model="value"
    :rules="rules"
    :counter-value="counterValue"
    :placeholder="placeholder"
    :rows="rows"
    :row-height="rowHeight"
    class="extended-textarea"
    counter
    auto-grow />
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
    },
    rows: {
      type: Number,
      default: () => 5
    },
    rowHeight: {
      type: Number,
      default: () => 24
    },
    extraClass: {
      type: String,
      default: () => '',
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
  mounted() {
    if (this.extraClass?.length) {
      const textareaElement = document.querySelector('.extended-textarea textarea');
      textareaElement.classList.add(this.extraClass);
    }
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