<template>
  <v-menu
    v-model="open"
    :close-on-content-click="false"
    :nudge-width="Width"
    :nudge-left="isMobile && 300"
    :nudge-bottom="30"
    content-class="tag-search-content"
    attach
    offset-x>
    <template #activator="{ on, attrs }">
      <v-chip
        :outlined="!open"
        :color="open && 'primary' || ''"
        :aria-label="$t('search.filter.tag')"
        tabindex="0"
        class="text-body border-color text-header-color me-1"
        v-bind="attrs"
        v-on="on"
        @keydown.enter="on.click">
        <v-icon size="16" class="pe-2">
          fas fa-hashtag
        </v-icon>
        <span> {{ $t('Tag.search.button') }}</span>
      </v-chip>
    </template>
    <v-card>
      <v-text-field
        ref="tagSearchInput"
        v-model="query"
        autofocus
        :placeholder="$t('Tag.search.placeholder')"
        class="px-4" />
      <div class="pa-3">
        <span v-if="!searching" class="font-weight-bold pl-1">{{ $t('Tag.last.added') }}</span>
        <v-chip-group
          v-model="value"
          active-class="primary--text primary-border-color"
          class="pt-2"
          multiple>
          <v-chip
            v-for="t in tagsWithAriaLabel"
            :color="`${isMobile ? 'blue lighten-4' : ''}`"
            :key="t.tag"
            :value="t.tag"
            :aria-label="t.ariaLabel"
            :aria-pressed="isTagSelected(t.tag) ? 'true' : 'false'"
            role="button"
            tabindex="0"
            @click="handleTag(t.tag)"
            @keyup.enter="handleTag(t.tag)">
            <span :class="`${isMobile ? 'primary--text' : ''}`"> {{ t.tag }}</span>
          </v-chip>
        </v-chip-group>
      </div>
    </v-card>
  </v-menu>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    initialized: false,
    tags: [],
    selectedTags: [],
    query: '',
    searching: false,
    searchLimit: 5,
    open: false,
  }),
  computed: {
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    Width() {
      return this.isMobile && '400' || '200';
    },
    tagsWithAriaLabel() {
      return this.tags.map(tag => ({
        tag,
        ariaLabel: this.selectedTags.includes(tag) && this.$t('search.tag.option.active.item.ariaLabel', {0: tag}) || this.$t('search.tag.option.item.ariaLabel', {0: tag})
      }));
    }
  },
  watch: {
    query() {
      this.searching = this.query.length > 0;
      this.search();
    },
    selectedTags() {
      this.$emit('input', this.selectedTags);
    },
    open() {
      if (this.open) {
        this.query = '';
        this.search();
      }
    },
  },
  created() {
    this.selectedTags = this.value;
  },
  methods: {
    search() {
      return this.$tagService.searchTags(this.query , this.searchLimit)
        .then(tagNames => {
          this.tags = tagNames.map(tagName => tagName.name) || [];
          if (this.selectedTags && this.selectedTags.length) {
            this.selectedTags = this.selectedTags.map(tag => {
              const tagsLowerCase = this.tags.map(t => t.toLowerCase());
              const tagLowerCase = tag.toLowerCase();
              const tagIndex = tagsLowerCase.indexOf(tagLowerCase);
              if (tagIndex >= 0) {
                return this.tags[tagIndex];
              } else {
                return tag;
              }
            });
          }
        });
    },
    handleTag(tag) {
      const selectedTagsLowerCase = this.selectedTags.map(t => t.toLowerCase());
      const tagLowerCase = tag.toLowerCase();
      if (selectedTagsLowerCase.includes(tagLowerCase)) {
        const tagIndex = selectedTagsLowerCase.indexOf(tagLowerCase);
        this.selectedTags.splice(tagIndex , 1);
      } else {
        this.selectedTags.push(tag);
        document.dispatchEvent(new CustomEvent('search-tag'));
      }
    },
    isTagSelected(tag) {
      return this.selectedTags.includes(tag);
    }
  },
};
</script>
