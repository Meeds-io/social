<script>
export default {
  render: function (createElement) {
    return createElement(
      this.element || 'div',
      this.$slots.default,
      this.children || this.child && [
        createElement(this.child),
      ] || null,
    );
  },
  props: {
    element: {
      type: String,
      default: () => null,
    },
    children: {
      type: Array,
      default: () => null,
    },
    child: {
      type: Object,
      default: () => null,
    },
  },
};
</script>