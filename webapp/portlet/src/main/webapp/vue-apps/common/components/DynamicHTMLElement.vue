<script>
export default {
  render: function (createElement) {
    return createElement(
      this.element || 'div',
      this.$slots.default
    );
  },
  props: {
    element: {
      type: String,
      default: () => null,
    },
  },
};
</script>