<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-card
    v-if="confirmed"
    width="600px"
    max-width="100%"
    class="mx-auto px-4"
    flat>
    <div class="d-flex flex-column align-center justify-center pt-10 pb-5">
      <v-icon size="80" color="primary">
        fa-envelope
      </v-icon>
      <span class="subtitle-1 mt-8 text-color">
        {{ $t('UILoginForm.label.confirmationEmailSentPart1') }}
      </span>
      <span class="subtitle-1 mt-3 text-color">
        {{ $t('UILoginForm.label.confirmationEmailSentPart2') }}
      </span>
    </div>
  </v-card>
  <extension-registry-components
    v-else
    :params="{params}"
    name="Register"
    type="register-extension"
    parent-element="div"
    element="div">
    <template #separator>
      <portal-register-separator class="my-5" />
    </template>
  </extension-registry-components>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
  },
  computed: {
    confirmed() {
      return !!this.params?.success?.length;
    },
  },
};
</script>