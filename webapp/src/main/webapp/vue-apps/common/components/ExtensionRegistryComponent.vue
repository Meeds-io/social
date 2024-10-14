<script>
export default {
  render: function (createElement) {
    return createElement(
      this.element || 'div',
      {
        attrs: {
          id: this.id,
          class: this.elementClass || '',
        },
      },
      [
        createElement(this.component.componentOptions.vueComponent, {
          props: this.params,
        })
      ],
    );
  },
  props: {
    component: {
      type: Object,
      default: null,
    },
    params: {
      type: Object,
      default: null,
    },
    element: {
      type: String,
      default: () => null,
    },
    elementClass: {
      type: String,
      default: () => '',
    },
  },
  data: () => ({
    randomId: String(parseInt(Math.random() * 100000)),
    mounted: false,
  }),
  computed: {
    id() {
      return `Ext${this.component.componentName}-${this.randomId}`;
    },
  },
};
</script>