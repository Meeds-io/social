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
  <div class="reactions-list">
    <div v-if="filterChips.length > 2" class="d-flex flex-nowrap overflow-x-auto px-3 pb-2">
      <v-chip
        v-for="chip in filterChips"
        :key="chip.id"
        :outlined="selectedReactionId !== chip.id"
        :aria-label="chip.label"
        :title="chip.label"
        class="me-1 mb-1 flex-shrink-0"
        small
        @click="selectReaction(chip.id)">
        <span v-if="chip.emoji" class="reaction-emoji me-1">{{ chip.emoji }}</span>
        <span v-else class="text-subtitle-2">{{ chip.label }}</span>
        <span class="ms-1">{{ chip.count > 9 ? '9+' : chip.count }}</span>
      </v-chip>
    </div>
    <div v-if="reactorsToDisplay.length" class="likers-list">
      <div
        v-for="reactor in reactorsToDisplay"
        :key="reactor.id"
        class="d-flex align-center justify-space-between">
        <activity-liker-item :liker="reactor" />
        <span
          v-if="reactor.reactionOption"
          :title="reactor.reactionOption.activeLabelKey && $t(reactor.reactionOption.activeLabelKey) || reactor.reactionOption.emoji"
          :aria-label="reactor.reactionOption.activeLabelKey && $t(reactor.reactionOption.activeLabelKey) || reactor.reactionOption.emoji"
          class="reaction-emoji me-4 flex-shrink-0">{{ reactor.reactionOption.emoji }}</span>
      </div>
    </div>
    <v-btn
      v-if="hasMoreReactors"
      :loading="loading"
      :disabled="loading"
      block
      class="btn pa-0 mt-2"
      @click="loadMore">
      {{ $t('Search.button.loadMore') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: () => ''
    },
    parentId: {
      type: String,
      default: () => ''
    }
  },
  data () {
    return {
      likers: [],
      reactionIdsByReactor: {},
      counts: {},
      reactionOptions: [],
      selectedReactionId: null,
      limit: 20,
      likersSize: 0,
      loading: false
    };
  },
  computed: {
    reactorsToDisplay() {
      return this.likers.map(liker => {
        const reactionId = this.reactionIdsByReactor[liker.id] || 'like';
        return Object.assign({}, liker, {
          reactionId,
          reactionOption: this.resolveReactionOption(reactionId),
        });
      });
    },
    totalCount() {
      return Object.values(this.counts).reduce((sum, count) => sum + count, 0) || this.likersSize;
    },
    filterChips() {
      const chips = [{
        id: null,
        label: this.$t('UIActivity.label.Show_All_Likers'),
        count: this.totalCount,
      }];
      this.reactionOptions.forEach(option => {
        if (this.counts[option.id]) {
          chips.push({
            id: option.id,
            label: this.$t(option.labelKey),
            emoji: option.emoji,
            count: this.counts[option.id],
          });
        }
      });
      Object.keys(this.counts)
        .filter(reactionId => reactionId !== 'like' && !this.reactionOptions.some(option => option.id === reactionId))
        .forEach(reactionId => {
          const customOption = this.resolveReactionOption(reactionId);
          if (customOption) {
            chips.push({
              id: reactionId,
              label: customOption.emoji,
              emoji: customOption.emoji,
              count: this.counts[reactionId],
            });
          }
        });
      return chips;
    },
    hasMoreReactors() {
      const selectedCount = this.selectedReactionId ? (this.counts[this.selectedReactionId] || 0) : this.totalCount;
      return selectedCount > this.limit;
    }
  },
  created() {
    this.$root.$on('activity-liked', this.handleActivityLikesUpdate);
    this.$reactionService.getReactionOptions('activity')
      .then(options => this.reactionOptions = options);
    this.retrieveReactors();
    document.addEventListener('check-reactions', this.handleCheckReactions);
  },
  beforeDestroy() {
    this.$root.$off('activity-liked', this.handleActivityLikesUpdate);
    document.removeEventListener('check-reactions', this.handleCheckReactions);
  },
  watch: {
    activityId() {
      this.selectedReactionId = null;
      this.limit = 20;
      this.retrieveReactors();
    }
  },
  methods: {
    resolveReactionOption(reactionId) {
      const option = this.reactionOptions.find(registered => registered.id === reactionId);
      if (option) {
        return option;
      }
      return /^[a-z0-9_-]+$/i.test(reactionId) ? null : {id: reactionId, emoji: reactionId, activeLabelKey: null};
    },
    selectReaction(reactionId) {
      this.selectedReactionId = this.selectedReactionId === reactionId ? null : reactionId;
      this.limit = 20;
      this.retrieveReactors();
    },
    handleCheckReactions(event) {
      if (event?.detail && event.detail === this.activityId) {
        this.updateReactionsTabCount();
      }
    },
    handleActivityLikesUpdate(activityId) {
      if (activityId === this.activityId) {
        this.retrieveReactors();
      }
    },
    retrieveReactors() {
      return this.selectedReactionId ? this.retrieveFilteredReactors() : this.retrieveAllReactors();
    },
    retrieveAllReactors() {
      this.loading = true;
      return Promise.all([
        this.$activityService.getActivityLikers(this.activityId, 0, this.limit),
        this.$reactionService.getReactions('activity', this.activityId, null, 0, this.limit),
      ]).then(([likersData, reactionsData]) => {
        this.likers = likersData.likes || [];
        this.likersSize = likersData.size || 0;
        this.counts = reactionsData.counts || {};
        const reactionIdsByReactor = {};
        (reactionsData.reactions || []).forEach(reaction => reactionIdsByReactor[`${reaction.reactorIdentityId}`] = reaction.reactionId);
        this.reactionIdsByReactor = reactionIdsByReactor;
        this.updateReactionsTabCount();
      }).catch(e => {
        console.error('error retrieving activity reactions', e);
      }).finally(() => this.loading = false);
    },
    retrieveFilteredReactors() {
      // the filter pages server-side over the selected option's reactors, so
      // a chip with count > 0 always lists its reactors whatever page of
      // likers is loaded
      this.loading = true;
      return this.$reactionService.getReactions('activity', this.activityId, this.selectedReactionId, 0, this.limit)
        .then(reactionsData => {
          this.counts = reactionsData.counts || {};
          const reactions = reactionsData.reactions || [];
          const reactionIdsByReactor = {};
          reactions.forEach(reaction => reactionIdsByReactor[`${reaction.reactorIdentityId}`] = reaction.reactionId);
          this.reactionIdsByReactor = reactionIdsByReactor;
          return Promise.all(reactions.map(reaction =>
            this.$identityService.getIdentityById(reaction.reactorIdentityId)
              .then(identity => Object.assign({}, identity?.profile, {id: `${reaction.reactorIdentityId}`}))
              .catch(() => null)));
        }).then(reactors => {
          this.likers = reactors.filter(reactor => !!reactor);
          this.updateReactionsTabCount();
        }).catch(e => {
          console.error('error retrieving filtered activity reactions', e);
        }).finally(() => this.loading = false);
    },
    updateReactionsTabCount() {
      document.dispatchEvent(new CustomEvent('update-reaction-extension', {
        detail: {
          numberOfReactions: this.totalCount,
          type: 'like'
        }
      }));
    },
    loadMore() {
      this.limit = this.limit + 20;
      this.retrieveReactors();
    }
  },
};
</script>
