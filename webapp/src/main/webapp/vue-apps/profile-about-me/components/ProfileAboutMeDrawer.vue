<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    class="aboutMeDrawer"
    allow-expand
    right>
    <template #title>
      {{ $t('profileAboutYouself.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card flat>
        <v-card-text>
          <rich-editor
            id="aboutMeRichEditor"
            v-model="aboutMe"
            :placeholder="$t('profileAboutMe.placeholder')"
            :max-length="maxLength"
            :tag-enabled="false"
            ck-editor-type="abountMe" />
        </v-card-text>
        <v-card-actions class="px-4">
          <v-spacer />
          <v-btn
            :loading="saving"
            :disabled="saving || !valid"
            class="btn btn-primary"
            @click="saveAboutMe">
            {{ $t('profileAboutMe.save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    aboutMe: null,
    saving: null,
    maxLength: 1300,
    drawer: false,
  }),
  computed: {
    valid() {
      return !this.aboutMe || this.$utils.htmlToText(this.aboutMe).length <= this.maxLength;
    },
  },
  methods: {
    open() {
      this.aboutMe = this.value;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    saveAboutMe() {
      this.saving = true;
      return this.$userService.updateProfileField(eXo.env.portal.userName, 'aboutMe', this.aboutMe)
        .then(() => {
          this.$emit('input', this.aboutMe);
          this.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('profileAboutMe.savingError'), 'error'))
        .finally(() => this.saving = false);
    },
  },
};
</script>