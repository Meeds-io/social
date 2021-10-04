<template>
  <span>
    <v-chip
      v-for="(selectedTag, index) in selectedTags"
      :key="selectedTag"
      color="primary"
      class="border-color mx-1"
      close
      @click:close="deleteTag(index)">
      <span class="subtitle-1">#{{ selectedTag }}</span>
    </v-chip>
  </span>
</template>
<script>
export default {
  data: () => ({
    selectedTags: [],
  }),
  watch: {
    selectedTags() {
      this.$emit('tags-changed', this.selectedTags);
    },
  },
  created() {
    if (this.$root.tagName) {
      this.selectedTags = [this.$root.tagName];
    } else {
      const search = window.location.search && window.location.search.substring(1);
      if (search) {
        const parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
        const tags = parameters['tags'];
        if (tags && tags.length) {
          this.selectedTags = tags.split(',');
        }
      }
    }
    document.addEventListener('search-metadata-tag', this.selectTag);
  },
  methods: {
    deleteTag(index) {
      this.selectedTags.splice(index, 1);
    },
    selectTag(event) {
      if (event && event.detail) {
        this.selectedTags = [event.detail];
      }
    }
  },
};
</script>
