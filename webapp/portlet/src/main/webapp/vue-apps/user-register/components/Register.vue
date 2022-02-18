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
  <v-app>
    <v-main>
      <v-container fluid>
        <div class="loginBGLight">
          <span></span>
        </div>
        <div class="uiLogin">
          <div class="loginContainer">
            <div class="loginHeader introBox">
              <div class="userLoginIcon">
                {{ $t('portal.login.Connectlabel') }}
              </div>
            </div>
            <div class="loginContent">
              <p class="brandingComanyClass mt-5 mb-0">{{ companyName }}</p>
              <div class="center white--text my-3">
                {{ $t('UILoginForm.label.alreadyHaveAccount') }}
                <a
                  :title="$t('UILoginForm.button.registerNewAccount')"
                  href="/portal/login"
                  class="text-decoration-underline white--text">
                  {{ $t('UILoginForm.label.Signin') }}
                </a>
              </div>
              <div class="titleLogin">
                <extension-registry-components
                  :params="{params}"
                  name="Register"
                  type="register-extension"
                  parent-element="div"
                  element="div">
                  <template #separator>
                    <div class="d-flex my-5">
                      <v-divider class="my-auto white" />
                      <span class="mx-3 white--text text-uppercase">
                        {{ $t('UILoginForm.label.or') }}
                      </span>
                      <v-divider class="my-auto white" />
                    </div>
                  </template>
                </extension-registry-components>
              </div>
            </div>
          </div>
        </div>
        <div class="brandingImageContent">
          <img
            :src="brandingLogo"
            class="brandingImage"
            role="presentation">
        </div>
      </v-container>
    </v-main>
  </v-app>
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
    companyName() {
      return this.params && this.params.companyName;
    },
    brandingLogo() {
      return this.params && this.params.brandingLogo;
    },
  },
  created() {
    document.title = this.$t('UILoginForm.label.ForAccount');
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>