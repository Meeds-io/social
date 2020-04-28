<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
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

<style scoped>

</style>