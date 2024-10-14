<template>
  <input type="text">
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
    },
    boundGroups: {
      type: Array,
      default: () => []
    },
    secondDrawerSelectedGroups: {
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
      if (typeof sourceProvider === 'function') {
        suggesterOptions.providers[sourceProvider.name] = sourceProvider;
        suggesterOptions.sourceProviders.push(sourceProvider.name);
      } else {
        suggesterOptions.sourceProviders.push(sourceProvider);
      }
    });
    
    const options = Object.assign({}, suggesterOptions, this.options);
    
    //init suggester
    $(this.$el).suggester(options);
    if (options.fillSelectize === true) {
      this.fillSelectize();
    }
  },
  methods: {
    bindSelectedItems() {
      const selectize = $(this.$el)[0].selectize;
      if (this.selectedItems.length === 1 && !this.selectedItems[0].startsWith('/')) {
        this.selectedItems.shift();
      }
      //
      const removeItems = [];
      const  addedItems = [];
      selectize.items.forEach(item => {
        if (!this.selectedItems.includes(item)) {
          removeItems.push(item);
        }
      });
      removeItems.forEach(item => {
        selectize.removeItem(item, true);
      });
      
      // add newly added items
      this.selectedItems.forEach(item => {
        if (item && item.startsWith('/') && !selectize.items.includes(item)) {
          addedItems.push(item);
        }
      });

      if (addedItems && addedItems.length > 0) {
        addedItems.forEach(item => {
          this.addItem(item);
        });
      }
    },
    fillSelectize() {
      for (const group of this.boundGroups) {
        const selectize = $(this.$el)[0].selectize;
        selectize.options[`${group}`] = {
          avatarUrl: null,
          text: group,
          value: group,
          type: 'group'
        };
        selectize.addItem(group);
      }
    },
    addItem(item) {
      if (item) {
        const boundGroups = this.boundGroups.map(binding => binding.group);
        if (!boundGroups.includes(item)) {
          const selectize = $(this.$el)[0].selectize;
          // if selectize options doesn't contain the option of this item add it
          if (!selectize.options[`${item}`]) {
            const group = this.getGroup(item);
            selectize.options[`${item}`] = {
              avatarUrl: null,
              text: group.name,
              value: group.id,
              type: 'group'
            };
          }
          // add item
          selectize.addItem(item);
        }
      }
    },
    getGroup(groupId) {
      return this.secondDrawerSelectedGroups.filter(group => group.id === groupId)[0];
    },
  }
};
</script>
