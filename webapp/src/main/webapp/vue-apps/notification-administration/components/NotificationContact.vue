<template>
  <v-card class="d-flex justify-center" flat>
    <div class="d-flex flex-column flex-grow-1 flex-shrink-1">
      <div class="text-title">{{ $t('NotificationAdmin.sender.title') }}</div>
      <div>
        <!-- eslint-disable-next-line -->
        <v-chip small>{{ settings.senderName }} <{{ settings.senderEmail }}> </v-chip>
      </div>
    </div>
    <div class="d-flex flex-grow-0 flex-shrink-0">
      <v-btn icon @click="$refs.drawer.open()">
        <v-icon>
          fa-edit
        </v-icon>
      </v-btn>
    </div>
    <notification-administration-contact-drawer
      ref="drawer"
      :settings="settings" />
  </v-card>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  methods: {
    openDrawer(plugin, group) {
      this.$nextTick(() => this.$refs.drawer.open(plugin, group));
    },
  },
};
</script>