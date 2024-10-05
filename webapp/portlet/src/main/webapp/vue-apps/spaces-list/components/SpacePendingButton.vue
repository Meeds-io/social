<template>
  <v-tooltip
    :disabled="$root.isMobile"
    bottom>
    <template #activator="{on, attrs}">
      <v-btn
        :id="`${filter}SpacesButton`"
        v-on="on"
        v-bind="attrs"
        :aria-label="$t(labelKey, {0: count})"
        :color="isSelected && 'primary'"
        :class="{
          'primary-border-color' : isSelected,
          'border-color-transparent' : !isSelected,
        }"
        class="ms-2"
        height="36"
        width="36"
        icon
        @click="apply">
        <v-icon
          :class="iconClass"
          size="20"
          dark>
          {{ icon }}
        </v-icon>
        <v-card
          :class="badgeColor"
          class="d-flex align-center justify-center aspect-ratio-1 border-radius-circle position-absolute t-0 r-0 mt-n2 me-n2 text-subtitle-font-size line-height-normal"
          height="24"
          width="24"
          dark
          flat>
          {{ count > 9 ? '+9' : count }}
        </v-card>
      </v-btn>
    </template>
    <span>{{ $t(labelKey, {0: count}) }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    count: {
      type: Number,
      default: () => 0,
    },
    filter: {
      type: String,
      default: null,
    },
    icon: {
      type: String,
      default: null,
    },
    iconClass: {
      type: String,
      default: null,
    },
    labelKey: {
      type: String,
      default: null,
    },
    badgeColor: {
      type: String,
      default: null,
    },
  },
  computed: {
    isSelected() {
      return this.$root.filter === this.filter;
    }
  },
  methods: {
    apply() {
      if (this.filter === 'requests') {
        this.$root.$emit('space-list-pending-open', this.count);
      } else {
        this.$root.$emit('spaces-list-filter-update', this.isSelected ? 'all' : this.filter);
      }
    },
  },
};
</script>