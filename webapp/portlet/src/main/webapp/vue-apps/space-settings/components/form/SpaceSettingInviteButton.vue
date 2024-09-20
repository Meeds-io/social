<template>
  <v-menu
    v-if="$root.isExternalFeatureEnabled"
    content-class="application-menu z-index-modal"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        :title="$t('SpaceSettings.users.inviteMembers')"
        color="primary"
        elevation="0"
        v-bind="attrs"
        v-on="on">
        <v-icon
          color="white"
          class="me-2"
          size="18">
          fa-plus
        </v-icon>
        {{ $t('SpaceSettings.users.invite') }}
      </v-btn>
    </template>
    <v-list max-width="300">
      <v-list-item
        link
        @click="$root.$emit('space-settings-invite-member', true)">
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteInternalMembers') }}</v-list-item-title>
          <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteInternalMembers.description') }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item
        link
        @click="$root.$emit('space-settings-invite-email', true)">
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteByEmail') }}</v-list-item-title>
          <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteByEmail.description') }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-btn
    v-else
    :title="$t('SpaceSettings.users.invite')"
    color="primary"
    elevation="0"
    @click="$root.$emit('space-settings-invite-member', true)">
    <v-icon
      color="while"
      class="me-2"
      size="18">
      fa-plus
    </v-icon>
    {{ $t('SpaceSettings.users.invite') }}
  </v-btn>
</template>
<script></script>