<template>
  <v-list-item
    :key="name"
    class="px-4 mx-n4 rounded-lg"
    dense
    @click="$emit('select', false)">
    <v-list-item-icon class="me-2 my-auto align-center justify-center">
      <v-icon size="18" class="icon-default-color">
        {{ icon }}
      </v-icon>
    </v-list-item-icon>
    <v-list-item-content>
      <v-list-item-title class="subtitle-2">
        {{ label }}
      </v-list-item-title>
    </v-list-item-content>
    <v-scale-transition>
      <v-list-item-icon v-if="badge" class="me-2 full-height align-center justify-center position-relative">
        <v-tooltip bottom>
          <template #activator="{on, bind}">
            <v-btn
              :value="badge > 0"
              :outlined="unreadOnly"
              :dark="!unreadOnly"
              :class="unreadOnly && 'btn'"
              max-width="30"
              max-height="30"
              color="red darken-4"
              class="pa-1"
              elevation="0"
              fab
              v-on="on"
              v-bind="bind"
              @click.stop.prevent="$emit('select', true)">
              <span class="caption">
                {{ badge > 99 && '99+' || badge }}
              </span>
            </v-btn>
          </template>
          <span>{{ $t('Notification.label.types.unread', {0: badge}) }}</span>
        </v-tooltip>
      </v-list-item-icon>
    </v-scale-transition>
  </v-list-item>
</template>
<script>
export default {
  props: {
    group: {
      type: Object,
      default: null,
    },
    selected: {
      type: Boolean,
      default: false,
    },
    unreadOnly: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    name() {
      return this.group.name;
    },
    badge() {
      return this.group.badge || 0;
    },
    label() {
      return this.group.label;
    },
    icon() {
      return this.group.icon;
    },
  },
  watch: {
    badge(newVal, oldVal) {
      if (this.selected && this.unreadOnly && oldVal && !newVal) {
        window.setTimeout(() => this.$emit('select', false), 50);
      }
    },
  },
  created() {
    if (this.selected && this.unreadOnly && !this.badge) {
      this.$emit('select', false);
    }
  },
};
</script>