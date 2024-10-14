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
  <extension-registry-components
    :params="extensionParams"
    name="LoginFooter"
    type="login-footer"
    parent-element="div"
    element="div">
    <template #separator>
      <portal-login-separator class="my-5" />
    </template>
    <template #header>
      <portal-login-separator class="my-5" />
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
    rememberme: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    extensionParams() {
      return {
        params: this.params,
        rememberme: this.rememberme,
      };
    },
  },
};
</script>