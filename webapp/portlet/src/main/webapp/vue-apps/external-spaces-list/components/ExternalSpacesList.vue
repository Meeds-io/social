<template>
  <v-app>
    <widget-wrapper
      v-if="isShown"
      :title="$t('externalSpacesList.title.yourSpaces')"
      extra-class="application-body">
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
    },
  },
  watch: {
    isShown: {
      immediate: true,
      handler() {
        this.$root.$updateApplicationVisibility(this.isShown, this.$el);
      },
    },
  },
  created() {
    this.getExternalSpacesList();
  },
  methods: {
    getExternalSpacesList() {
      this.$externalSpacesListService.getExternalSpacesList(this.offset,this.limit).then(data => {
        this.spacesList = this.spacesList.concat(data.spaces);
        this.hasMore = data.spaces.length===this.pageSize;
        this.loading=false;
      });
    },
    loadMore() {
      this.loading=true;
      this.offset += this.pageSize;
      this.getExternalSpacesList();
    }
  }
};
</script>
