<template>
  <v-app v-if="isShown">
    <widget-wrapper :title="$t('externalSpacesList.title.yourSpaces')">
      <v-list dense class="py-0 external-spaces-list">
        <template>
          <external-space-item
            v-for="space in spacesList"
            :key="space.id"
            :space="space" />
        </template>
      </v-list>
      <v-btn
        :loading="loading"
        :disabled="!hasMore"
        class="btn mx-auto mt-4 flex-grow-0 flex-shrink-0"
        outlined
        @click="loadMore()">
        {{ $t('button.loadMore') }}
      </v-btn> 
    </widget-wrapper>
  </v-app>
</template>
<script>
import * as externalSpacesListService from '../externalSpacesListService.js';

export default {
  data () {
    return {
      spacesList: [],
      hasMore: false,
      loading: false,
      pageSize: 10,
      limit: 10,
      offset: 0,
    };
  },
  computed: {
    isShown() {
      return this.spacesList && this.spacesList.length > 0 || this.spacesRequestsSize > 0;
    }
  },
  created() {
    this.getExternalSpacesList();
  },
  methods: {
    getExternalSpacesList() {
      externalSpacesListService.getExternalSpacesList(this.offset,this.limit).then(data => {
        this.spacesList = this.spacesList.concat(data.spaces);
        this.hasMore = data.spaces.length===this.pageSize;
        this.loading=false;
      });
    },
    loadMore() {
      this.loading=true;
      this.limit += this.pageSize;
      this.offset += this.pageSize;
      this.getExternalSpacesList();
    },
  }
};
</script>
