<script>
export default {
  render: function (createElement) {
    if (this.html) {
      const formattedHtml = ExtendedDomPurify.purify(`<div>${this.html}</div>`);
      return createElement(this.element || 'div', {
        domProps: {
          innerHTML: formattedHtml
        }
      });
    }
    return createElement(
      this.element || 'div',
      this.$slots.default,
      this.children || this.child && [
        createElement(this.child),
      ] || null,
    );
  },
  mounted() {
    this.markColors();
  },
  updated() {
    this.markColors();
  },
  methods: {
    // black and white text of a rich content gets the Auto text colour in dark (social's rich text colours)
    markColors() {
      if (this.html) {
        this.$richTextColors.markColors(this.$el);
      }
    },
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
    html: {
      type: String,
      default: ''
    }
  },
};
</script>