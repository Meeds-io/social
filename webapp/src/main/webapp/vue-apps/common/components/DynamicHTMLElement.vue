<script>
export default {
  mixins: [EmojiAccessibility.readyMixin],
  render: function (createElement) {
    if (this.html) {
      const purifiedHtml = ExtendedDomPurify.purify(`<div>${this.html}</div>`);
      const formattedHtml = this.emojiBankReady && EmojiAccessibility.addAccessibleNameToEmojis(purifiedHtml) || purifiedHtml;
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