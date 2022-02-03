<template>
  <v-menu
    v-model="open"
    :close-on-content-click="false"
    :nudge-width="200"
    :nudge-bottom="30"
    content-class="tag-search-content"
    offset-x>
    <template v-slot:activator="{ on, attrs }">
      <v-chip
        :outlined="!open"
        :color="open && 'primary' || ''"
        class="border-color mx-1"
        v-bind="attrs"
        v-on="on">
        <span class="subtitle-1"><span class="text-sub-title">#</span> {{ $t('Tag.search.button') }}</span>
      </v-chip>
    </template>
    <v-card>
      <v-text-field
        ref="tagSearchInput"
        v-model="query"
        :placeholder="$t('Tag.search.placeholder')"
        class="px-4" />
      <div class="pa-3">
        <span v-if="!searching" class="text-sm-body-2 font-weight-bold pl-1">{{ $t('Tag.last.added') }}</span>
        <v-chip-group
          v-model="value"
          active-class="primary--text"
          class="pt-2"
          multiple>
          <v-chip
            v-for="tag in tags"
            :key="tag"
            :value="tag"
            @click="handleTag(tag)"
            @keyup.enter="handleTag(tag)">
            {{ tag }}
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
        window.setTimeout(() => {
          if (this.$refs.tagSearchInput) {
            this.$refs.tagSearchInput.$el.querySelector('input').focus();
          }
        }, 200);
        this.search();
      }
    },
  },
  created() {
    // Workaround to fix closing menu when clicking outside
    document.onmousedown = event => {
      if (this.open && event && event.target) {
        if (!$('.tag-search-content').find(event.target).length) {
          window.setTimeout(() => {
            this.open = false;
          }, 200);
        }
      }
    };
    this.selectedTags = this.value;
  },
  methods: {
    search() {
      return this.$tagService.searchTags(this.query , this.searchLimit)
        .then(tagNames => {
          this.tags = tagNames.map(tagName => tagName.name) || [];
        });
    },
    handleTag(tag) {
      if (this.selectedTags.includes(tag)) {
        const tagIndex = this.selectedTags.indexOf(tag);
        this.selectedTags.splice(tagIndex , 1);
      }
      else {
        this.selectedTags.push(tag);
        document.dispatchEvent(new CustomEvent('search-tag'));
      }
    }
  },
};
</script>
