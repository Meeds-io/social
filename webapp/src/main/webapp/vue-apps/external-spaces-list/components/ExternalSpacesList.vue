<template>
  <v-app>
    <widget-wrapper
      v-if="isShown"
      extra-class="application-body"
      :title="$t('externalSpacesList.title.yourSpaces')">
      <v-list
        class="py-0 external-spaces-list"
        dense>
        <template>
          <external-space-item
            v-for="space in spacesList"
            :key="space.id"
            :space="space" />
        </template>
      </v-list>
      <v-btn
        v-if="hasMore"
        class="btn mx-auto mt-4 flex-grow-0 flex-shrink-0"
        :loading="loading"
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
      isShown () {
        return this.spacesList && this.spacesList.length > 0 || this.spacesRequestsSize > 0;
      },
    },
    watch: {
      isShown: {
        immediate: true,
        handler () {
          this.$root.$updateApplicationVisibility(this.isShown, this.$el);
        },
      },
    },
    created () {
      this.getExternalSpacesList();
    },
    methods: {
      getExternalSpacesList () {
        this.loading = true;
        return this.$spaceService.getSpacesByFilter({
          offset: this.offset,
          limit: this.limit,
          filter: 'member',
        })
          .then(data => {
            this.spacesList = this.spacesList.concat(data.spaces);
            this.hasMore = data.size > this.spacesList.length;
          })
          .finally(() => this.loading = false);
      },
      loadMore () {
        this.offset += this.pageSize;
        this.getExternalSpacesList();
      },
    },
  };
</script>
