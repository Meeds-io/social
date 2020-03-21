<template>
  <input type="text"/>
</template>

<script>

export default {
  model: {
    prop: 'selectedItems',
    event: 'change'
  },
  props: {
    options: {
      type: Object,
      default: function() { return {}; }
    },
    sourceProviders: {
      type: Array,
      default: () => []
    },
    selectedItems: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    selectedItems() {
      this.$emit('change', this.selectedItems);
      this.bindSelectedItems();
    }
  },
  mounted() {
    const thiss = this;
    const suggesterOptions = {
      type: 'tag',
      sourceProviders: [],
      plugins: ['remove_button'],
      providers: {},
      onChange(items) {
        thiss.selectedItems = items.split(',');
      },
    };
    
    //
    this.sourceProviders.forEach(sourceProvider => {
      if(typeof sourceProvider === 'function') {
        suggesterOptions.providers[sourceProvider.name] = sourceProvider;
        suggesterOptions.sourceProviders.push(sourceProvider.name);
      } else {
        suggesterOptions.sourceProviders.push(sourceProvider);
      }
    });
    
    const options = Object.assign({}, suggesterOptions, this.options);
    
    //init suggester
    $(this.$el).suggester(options);
  },
  methods: {
    bindSelectedItems() {
      const selectize = $(this.$el)[0].selectize;
      //
      const removeItems = [];
      selectize.items.forEach(item => {
        if (!this.selectedItems.includes(item)) {
          removeItems.push(item);
        }
      });
      removeItems.forEach(item => {
        selectize.removeItem(item, true);
      });
    }
  }
};
</script>