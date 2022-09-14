<template>
  <v-container class="recentDrawer" flat>
    <v-flex class="filterSpaces d-flex align-center">
      <v-list-item-icon class="d-flex d-sm-none backToMenu me-2 my-5 icon-default-color" @click="closeMenu()">
        <i class="fas fa-arrow-left"></i>
      </v-list-item-icon>
      <a  
        v-if="canAddSpaces"
        :href="allSpacesLink" 
        class="addNewSpaceIcon px-2 primary rounded py-1">
        <v-icon 
          class="fas fa-plus white--text" 
          size="16" />
      </a>
      <v-list-item class="recentSpacesTitle">
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
          @click="showFilter = true">
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
      allSpacesLink: `${eXo.env.portal.context}/${ eXo.env.portal.portalName }/all-spaces?createSpace=true`,
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
    }
  }
};
</script>
