<template>
  <v-app>
    <template v-if="displayed">
      <mobile-settings-window
        v-if="displayDetails"
        @back="closeDetail" />
      <v-card
        v-else
        class="ma-4 border-radius"
        flat>
        <v-list>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title text-color">
                {{ $t('MobileSettings.title') }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-list-item>
            <v-list-item-content class="px-0 pb-0 pt-2 mt-auto mb-2">
              <v-list-item-title class="text-color text-wrap">
                {{ $t('MobileSettings.connect') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                small
                icon
                @click="openDetail">
                <v-icon size="24" class="text-sub-title">
                  {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                </v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    id: `Mobile${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    displayed: true,
    displayDetails: false,
  }),
  created() {
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    openDetail() {
      document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: this.id}));
      this.displayDetails = true;
    },
    closeDetail() {
      document.dispatchEvent(new CustomEvent('showSettingsApps'));
      this.displayDetails = false;
    },
  },
};
</script>