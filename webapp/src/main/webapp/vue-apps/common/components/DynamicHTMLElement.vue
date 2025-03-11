<script>
  export default {
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
    render (createElement) {
      return createElement(
        this.element || 'div',
        this.$slots.default,
        this.children || this.child && [
          createElement(this.child),
        ] || null,
      );
    },
  };
</script>