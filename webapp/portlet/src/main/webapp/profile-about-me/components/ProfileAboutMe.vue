<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <v-app
    :class="owner && 'profileAboutMe' || 'profileAboutMeOther'"
    class="white">
    <v-toolbar color="white" flat class="border-box-sizing">
      <div
        :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height-thick skeleton-border-radius'"
        class="text-header-title text-sub-title">
        {{ skeleton && '&nbsp;' || $t('profileAboutMe.title') }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner || skeleton"
        :disabled="skeleton"
        :class="skeleton && 'skeleton-background'"
        icon
        outlined
        small
        @click="editAboutMe">
        <i
          v-if="!skeleton"
          class="uiIconEdit uiIconLightBlue pb-2" />
      </v-btn>
    </v-toolbar>
    <v-card
      :class="skeleton && 'pr-7'"
      class="border-box-sizing"
      flat>
      <template v-if="skeleton">
        <div
          v-for="i in 8"
          :key="i"
          :class="(i % 2) === 0 && 'skeleton-text-full-width' || 'skeleton-text-half-width'"
          class="my-3 mx-4 border-box-sizing skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius">
          &nbsp;
        </div>
      </template>
      <p v-else-if="aboutMe || !owner" class="paragraph text-color pt-0 pb-6 px-4" v-text="aboutMe"></p>
      <p v-else class="paragraph text-color pt-0 pb-6 px-4" v-text="$t('profileAboutMe.emptyOwner')"></p>
    </v-card>
    <exo-drawer
      v-if="owner"
      ref="aboutMeDrawer"
      class="aboutMeDrawer"
      right>
      <template slot="title">
        {{ $t('profileAboutMe.title') }}
      </template>
      <template slot="content">
        <v-card flat>
          <v-card-text>
            <extended-textarea
              v-model="modifyingAboutMe"
              :max-length="aboutMeTextLength" />
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
          <v-card-text v-if="error">
            <v-alert type="error">
              {{ error }}
            </v-alert>
          </v-card-text>
        </v-card>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>

export default {
  props: {
    aboutMe: {
      type: String,
      default: () => '',
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    skeleton: true,
    error: null,
    saving: null,
    modifyingAboutMe: null,
    aboutMeTextLength: 2000
  }),
  computed: {
    valid() {
      return !this.modifyingAboutMe || this.modifyingAboutMe.length <= this.aboutMeTextLength;
    }
  },
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    this.skeleton = false;
  },
  methods: {
    refresh(aboutMe) {
      this.aboutMe = aboutMe;
      this.$refs.aboutMeDrawer.close();
    },
    editAboutMe() {
      this.modifyingAboutMe = this.aboutMe;
      this.$refs.aboutMeDrawer.open();
    },
    saveAboutMe() {
      this.error = null;
      this.saving = true;
      this.$refs.aboutMeDrawer.startLoading();
      return this.$userService.updateProfileField(eXo.env.portal.userName, 'aboutMe', this.modifyingAboutMe)
        .then(() => this.refresh(this.modifyingAboutMe))
        .catch(error => {
          console.warn('Error saving about me section', error); // eslint-disable-line no-console
          this.error = this.$t('profileAboutMe.savingError');
        })
        .finally(() => {
          this.saving = false;
          this.$refs.aboutMeDrawer.endLoading();
        });
    },
  },
};
</script>