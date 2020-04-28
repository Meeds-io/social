<template>
  <v-card class="ma-4 border-radius" flat>
    <v-list @click="openNotificationSettingDetail">
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title text-color">
            {{ $t('UserSettings.security') }}
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn icon>
            <v-icon size="24" class="text-sub-title">
              fa-caret-right
            </v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  methods: {
    openSecurityDetail() {
      // TODO
    },
  },
};
</script>

