<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

-->
<template>
  <form
    class="px-4 py-2"
    autocomplete="off"
    @submit.prevent="$emit('next')">
    <div class="d-flex align-center justify-space-between">
      <label for="userCreationCustomizeUsername" class="text-color">
        {{ $t('UsersManagement.creation.customizeUsername') }}
      </label>
      <v-switch
        id="userCreationCustomizeUsername"
        :input-value="customizeUsername"
        :disabled="saving"
        class="mt-0 pt-0 me-n2"
        hide-details
        dense
        inset
        @change="$emit('update:customize-username', !!$event)" />
    </div>
    <v-text-field
      v-if="customizeUsername"
      :value="user.userName"
      :placeholder="$t('UsersManagement.creation.enterUsername')"
      :disabled="saving"
      :error-messages="fieldErrors.userName"
      class="pt-2"
      name="userName"
      autocomplete="off"
      maxlength="2000"
      prepend-inner-icon="far fa-id-card"
      outlined
      dense
      required
      @input="$emit('update-field', 'userName', $event)" />
    <v-text-field
      :value="user.firstName"
      :placeholder="$t('UsersManagement.creation.enterFirstName')"
      :disabled="saving"
      :error-messages="fieldErrors.firstName"
      class="pt-2"
      name="firstName"
      autocomplete="off"
      maxlength="2000"
      prepend-inner-icon="fa-user"
      outlined
      dense
      required
      @input="$emit('update-field', 'firstName', $event)" />
    <v-text-field
      :value="user.lastName"
      :placeholder="$t('UsersManagement.creation.enterLastName')"
      :disabled="saving"
      :error-messages="fieldErrors.lastName"
      class="pt-2"
      name="lastName"
      autocomplete="off"
      maxlength="2000"
      prepend-inner-icon="fa-user-alt"
      outlined
      dense
      required
      @input="$emit('update-field', 'lastName', $event)" />
    <v-text-field
      :value="user.email"
      :placeholder="$t('UsersManagement.creation.enterEmail')"
      :disabled="saving"
      :error-messages="fieldErrors.email"
      class="pt-2"
      name="email"
      type="email"
      autocomplete="off"
      maxlength="2000"
      prepend-inner-icon="far fa-envelope"
      outlined
      dense
      required
      @input="$emit('update-field', 'email', $event)" />
    <v-select
      :value="user.language"
      :items="languages"
      :disabled="saving || !languages.length"
      :error-messages="fieldErrors.language"
      :menu-props="{ offsetY: true }"
      class="pt-2"
      item-text="text"
      item-value="value"
      prepend-inner-icon="far fa-closed-captioning"
      outlined
      dense
      @change="$emit('update-field', 'language', $event)">
      <template #selection="{ item }">
        <span class="text-truncate">
          {{ $t('UsersManagement.creation.preferredLanguage') }}: {{ item.text }}
        </span>
      </template>
    </v-select>
    <div class="d-flex align-center justify-space-between pt-2">
      <label for="userCreationCustomizePassword" class="text-color">
        {{ $t('UsersManagement.creation.customizePassword') }}
      </label>
      <v-switch
        id="userCreationCustomizePassword"
        :input-value="customizePassword"
        :disabled="saving"
        class="mt-0 pt-0 me-n2"
        hide-details
        dense
        inset
        @change="$emit('update:customize-password', !!$event)" />
    </div>
    <template v-if="customizePassword">
      <v-text-field
        :value="user.password"
        :placeholder="$t('UsersManagement.creation.enterPassword')"
        :disabled="saving"
        :error-messages="fieldErrors.password"
        class="pt-2"
        name="password"
        type="password"
        autocomplete="new-password"
        prepend-inner-icon="fa-lock"
        outlined
        dense
        required
        @input="$emit('update-field', 'password', $event)" />
      <v-text-field
        :value="confirmPassword"
        :placeholder="$t('UsersManagement.creation.confirmPassword')"
        :disabled="saving"
        :error-messages="fieldErrors.confirmPassword"
        class="pt-2"
        name="confirmPassword"
        type="password"
        autocomplete="new-password"
        prepend-inner-icon="fa-lock"
        outlined
        dense
        required
        @input="$emit('update:confirm-password', $event)" />
    </template>
  </form>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => ({}),
    },
    customizeUsername: {
      type: Boolean,
      default: false,
    },
    customizePassword: {
      type: Boolean,
      default: false,
    },
    confirmPassword: {
      type: String,
      default: '',
    },
    fieldErrors: {
      type: Object,
      default: () => ({}),
    },
    languages: {
      type: Array,
      default: () => [],
    },
    saving: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
