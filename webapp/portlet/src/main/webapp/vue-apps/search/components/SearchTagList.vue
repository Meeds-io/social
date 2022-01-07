<template>
  <v-menu
    v-model="open"
    :close-on-content-click="false"
    :nudge-width="200"
    content-class="tag-search-content"
    offset-x>
    <template v-slot:activator="{ on, attrs }">
      <v-chip
        :outlined="!open"
        :color="open && 'primary' || ''"
        class="border-color mx-1"
        v-bind="attrs"
        v-on="on">
        #{{ $t('Tag.search.button') }}
      </v-chip>
    </template>
    <v-card>
      <v-text-field
        ref="tagSearchInput"
        v-model="query"
        :placeholder="$t('Tag.search.placeholder')"
        class="px-4" />
      <div class="pa-4">
        <v-chip-group
          v-model="value"
          active-class="primary--text"
          multiple>
          <v-chip
            v-for="tag in tags"
            :key="tag"
            :value="tag">
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
    query: '',
    open: false,
  }),
  watch: {
    query() {
      this.search();
    },
    value() {
      this.$emit('input', this.value);
    },
    open() {
      if (this.open) {
        window.setTimeout(() => {
          if (this.$refs.tagSearchInput) {
            this.$refs.tagSearchInput.$el.focus();
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
  },
  methods: {
    search() {
      return this.$tagService.searchTags(this.query)
        .then(tagNames => {
          this.tags = tagNames.map(tagName => tagName.name) || [];
        });
    },
  },
};
</script>
