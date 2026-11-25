<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div class="objectReactions d-inline-flex align-center flex-wrap">
    <reaction-chooser
      :current-reaction-id="userReactionId"
      :disabled="loading"
      :object-type="reactionOptionsType"
      @reaction-select="selectReaction">
      <v-btn
        :id="`objectReactionLike-${objectType}-${objectId}`"
        :loading="loading"
        :class="userReactionId && 'primary--text'"
        :aria-pressed="!!userReactionId"
        class="px-1 width-auto"
        text
        small
        @click="toggleReaction">
        {{ likeLabel }}
      </v-btn>
    </reaction-chooser>
    <v-btn
      v-if="totalCount"
      :title="$t('UIActivity.label.reactions')"
      :aria-label="$t('UIActivity.label.reactions')"
      class="objectReactionCount px-2 mx-1"
      min-width="0"
      text
      @click="openReactors">
      <span
        v-for="reaction in topReactions"
        :key="reaction.id"
        class="objectReactionEmoji text-subtitle-1 me-n1">{{ reaction.emoji }}</span>
      <span class="ms-2 text-body-2 text-color">{{ totalCount }}</span>
    </v-btn>
  </div>
</template>
<script>
/**
 * Reactions of any object type registered in the Reactions framework (a task,
 * a comment...): the same Like button, reaction chooser and reactors summary as
 * the activities ones. The reactors list opens in the object reactions drawer.
 */
export default {
  props: {
    objectType: {
      type: String,
      required: true,
    },
    objectId: {
      type: [String, Number],
      default: null,
    },
  },
  data: () => ({
    counts: {},
    userReactionId: null,
    options: [],
    optionsLoaded: false,
    loading: false,
  }),
  computed: {
    // The options are the ones of the activities, whatever the object type
    reactionOptionsType() {
      return 'activity';
    },
    totalCount() {
      return Object.values(this.counts).reduce((sum, count) => sum + count, 0);
    },
    reactions() {
      return Object.keys(this.counts)
        .filter(id => this.counts[id] > 0)
        .map(id => ({
          id,
          count: this.counts[id],
          emoji: this.options.find(option => option.id === id)?.emoji || id,
        }));
    },
    topReactions() {
      return this.reactions.slice().sort((a, b) => b.count - a.count).slice(0, 3);
    },
    likeLabel() {
      if (!this.userReactionId) {
        return this.$t('UIActivity.msg.LikeActivity');
      }
      if (!this.optionsLoaded && /^[a-z0-9_-]+$/i.test(this.userReactionId)) {
        return ' ';
      }
      const option = this.options.find(registered => registered.id === this.userReactionId);
      return option?.selectedLabelKey && this.$t(option.selectedLabelKey)
        || this.$t('UIActivity.reaction.selected.custom');
    },
  },
  watch: {
    objectId: {
      immediate: true,
      handler() {
        this.refresh();
      },
    },
  },
  created() {
    this.$reactionService.getReactionOptions(this.reactionOptionsType)
      .then(options => this.options = options)
      .catch(() => this.options = [])
      .finally(() => this.optionsLoaded = true);
  },
  methods: {
    openReactors() {
      this.$root.$emit('object-reactions-drawer-open', {
        objectType: this.objectType,
        objectId: this.objectId,
      });
    },
    refresh() {
      this.counts = {};
      this.userReactionId = null;
      if (!this.objectId) {
        return Promise.resolve();
      }
      return this.$reactionService.getReactions(this.objectType, this.objectId, null, 0, 1)
        .then(result => {
          this.counts = result?.counts || {};
          this.userReactionId = result?.userReactionId || null;
        })
        .catch(() => this.counts = {});
    },
    toggleReaction() {
      return this.userReactionId ? this.removeReaction() : this.selectReaction({id: 'like'});
    },
    selectReaction(option) {
      if (this.loading || !option?.id || option.id === this.userReactionId) {
        return Promise.resolve();
      }
      return this.apply(() => this.$reactionService.setReaction(this.objectType, this.objectId, option.id));
    },
    removeReaction() {
      return this.apply(() => this.$reactionService.deleteReaction(this.objectType, this.objectId));
    },
    apply(request) {
      this.loading = true;
      return request()
        .then(() => this.refresh())
        .catch(() => this.$root.$emit('alert-message', this.$t('UIActivity.reaction.error'), 'error'))
        .finally(() => this.loading = false);
    },
  },
};
</script>
