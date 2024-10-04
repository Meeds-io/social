<template>
  <v-card
    :width="icon && 36 || 'auto'"
    flat>
    <v-tooltip :disabled="extension.title && ($root.isMobile || !icon)" bottom>
      <template #activator="{on, attrs}">
        <v-btn
          v-on="on"
          v-bind="attrs"
          :aria-label="extension.title"
          :icon="icon"
          :color="!icon && 'primary'"
          :outlined="!icon"
          :loading="extension.loading"
          :small="!icon"
          class="absolute-vertical-align z-index-one"
          ripple
          @mousedown.stop="0"
          @mouseup.stop="0"
          @click.stop.prevent="extension.click(space)">
          <div class="d-flex align-center justify-center">
            <v-icon v-if="extension.icon" size="16">{{ extension.icon }}</v-icon>
            <span
              v-if="!icon"
              :class="extension.icon && 'ms-2'"
              class="text-body-font-size">
              {{ extension.title }}
            </span>
          </div>
        </v-btn>
      </template>
      <span>{{ extension.title }}</span>
    </v-tooltip>
  </v-card>
</template>
<script>
export default {
  props: {
    extension: {
      type: Object,
      default: () => ({
        icon: null,
        title: null,
        loading: false,
        click: null,
      }),
    },
    space: {
      type: Object,
      default: null,
    },
    icon: {
      type: Boolean,
      default: false,
    },
  },
};
</script>

