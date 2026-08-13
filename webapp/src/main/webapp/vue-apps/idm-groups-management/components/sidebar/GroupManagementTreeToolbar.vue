<template>
  <application-toolbar
    id="groupsManagementToolbar"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('GroupsManagement.filterByGroup'),
      tooltip: $t('GroupsManagement.filterByGroup'),
    }"
    compact
    class="px-1"
    no-text-truncate
    cols-auto
    @filter-text-input-end-typing="$root.$emit('searchGroup', $event)"
    @loading="$emit('loading', $event)">
    <template #left>
      <div class="d-flex align-center">
        <v-btn
          color="primary"
          elevation="0"
          @click="$root.$emit('add-new-group')">
          {{ $t('GroupsManagement.addGroup') }}
        </v-btn>
      </div>
    </template>
  </application-toolbar>
</template>
