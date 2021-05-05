<template>
  <v-flex class="space-suggester text-truncate">
    <exo-identity-suggester
      ref="newsSpaceSuggester"
      v-model="sharingSpaces"
      :labels="spaceSuggesterLabels"
      :include-users="false"
      :width="220"
      :search-options="searchOptions"
      name="newsSpaceAutocomplete"
      class="space-suggester newsSpaceAutocomplete"
      include-spaces
      only-redactor
      only-manager />
    <div v-if="displayedSpaces" class="identitySuggester no-border mt-0">
      <share-activity-space-item
        v-for="space in displayedSpaces"
        :key="space.identity.remoteId"
        :space="space"
        @remove-space="removeSpace" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    spaces: {
      type: Array,
      default: () => null,
    },
  },
  data() {
    return {
      displayedSpaces: [],
      currentUser: null,
      sharingSpaces: []
    };
  },
  computed: {
    searchOptions() {
      return {
        spaceURL: '' ,
      };
    },
    spaceSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('news.share.spaces.searchPlaceholder'),
        placeholder: this.$t('news.share.spaces.placeholder'),
        noDataLabel: this.$t('news.share.spaces.noDataLabel'),
      };
    },
  },
  watch: {
    sharingSpaces() {
      if (!this.sharingSpaces) {
        this.$nextTick(this.$refs.newsSpaceSuggester.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      if (!this.spaces) {
        // eslint-disable-next-line vue/no-mutating-props
        this.spaces = [];
      }
      const found = false;
      if (!found) {
        this.displayedSpaces.push({
          identity: this.$suggesterService.convertSuggesterItemToIdentity(this.sharingSpaces),
        });
        // eslint-disable-next-line vue/no-mutating-props
        this.spaces.push(this.sharingSpaces.remoteId);
      }
      this.sharingSpaces = null;
    }
  },
  created() {
    this.$root.$on('news-share-drawer-closed', ()=> {
      this.displayedSpaces = [];
    });
  },
  methods: {
    removeSpace(space) {
      const index = this.displayedSpaces.findIndex(addedSpace => {
        return space.identity.remoteId === addedSpace.identity.remoteId
            && space.identity.providerId === addedSpace.identity.providerId;
      });
      if (index >= 0) {
        this.displayedSpaces.splice(index, 1);
      }
    },
  }
};
</script>
