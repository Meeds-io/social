<template>
  <v-container class="recentDrawer" flat>
    <v-flex class="filterSpaces d-flex align-center">
      <v-list-item-icon class="d-flex d-sm-none backToMenu my-5 mx-2 icon-default-color justify-center" @click="closeMenu()">
        <v-icon class="fas fa-arrow-left" small />
      </v-list-item-icon>
      <v-list-item class="recentSpacesTitle px-2">
        <v-list-item-icon 
          class="me-2 align-self-center " 
          @click="closeMenu()"> 
          <v-icon size="20" class="disabled--text">fas fa-filter </v-icon>
        </v-list-item-icon>
        <v-list-item-content v-if="showFilter" class="recentSpacesTitleLabel">
          <v-text-field
            v-model="keyword"
            :placeholder="$t('menu.spaces.recentSpaces')"
            class="recentSpacesFilter border-bottom-color pt-0 mt-0"
            single-line
            hide-details
            required
            autofocus />
        </v-list-item-content>
        <v-list-item-content
          v-else
          class="recentSpacesTitleLabel pt-1 pb-2px disabled--text border-bottom-color "
          @click="openFilter()">
          {{ $t('menu.spaces.recentSpaces') }}
        </v-list-item-content>
        <v-list-item-action v-if="showFilter" class="recentSpacesTitleIcon position-absolute r-3">
          <v-btn
            text
            icon
            color="blue-grey darken-1"
            size="22"
            @click="closeFilter()">
            <v-icon size="18">mdi-close</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-flex>
    <exo-spaces-navigation-content
      :limit="itemsToShow"
      :page-size="itemsToShow"
      :keyword="keyword"
      show-more-button
      class="recentSpacesWrapper mt-4" />
  </v-container>
</template>
<script>
import {checkCanCreateSpaces} from '../../spaces-administration/spacesAdministrationServices.js';

export default {
  data () {
    return {
      itemsToShow: 15,
      canAddSpaces: false,
      showFilter: false,
      keyword: '',
    };
  },
  created() {
    checkCanCreateSpaces().then(data => {
      this.canAddSpaces = data;
    });
  },
  methods: {
    closeMenu() {
      this.$emit('close-menu');
    },
    closeFilter() {
      this.keyword = '';
      this.showFilter = false;
    },
    getSpacesPage(item) {
      if (this.itemsToShow <= this.spacesList.length) {
        const l = this.spacesList.length - this.itemsToShow;
        if ( l > item ) {
          this.itemsToShow+=item;
        } else {
          this.itemsToShow+=l;
          this.showButton = false;
        }
      }
    },
    leftNavigationActionEvent(clickedItem) {
      document.dispatchEvent(new CustomEvent('space-left-navigation-action', {detail: clickedItem} ));
    },
    openFilter() {
      this.showFilter = true;
      this.leftNavigationActionEvent('filterBySpaces');
    }
  }
};
</script>
