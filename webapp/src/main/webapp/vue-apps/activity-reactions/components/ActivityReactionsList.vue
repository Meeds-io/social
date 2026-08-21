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
        <span :class="!chip.emoji && 'text-subtitle-2'" class="ms-1">{{ chip.count > 9 ? '9+' : chip.count }}</span>
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
      pageSize: 20,
      likersSize: 0,
      loading: false
    };
  },
  computed: {
    reactorsToDisplay() {
      return this.likers.map(liker => {
        const reactionId = this.reactionIdsByReactor[liker.id] || 'like';
        return {
          ...liker,
          reactionId,
          reactionOption: this.resolveReactionOption(reactionId),
        };
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
      return selectedCount > this.likers.length;
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
      this.likers = [];
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
      this.likers = [];
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
    retrieveReactors(append) {
      return this.selectedReactionId ? this.retrieveFilteredReactors(append) : this.retrieveAllReactors(append);
    },
    fetchReactionPages(reactionId, offset, limit) {
      const requests = [];
      for (let pageOffset = offset; pageOffset < offset + limit; pageOffset += 100) {
        requests.push(this.$reactionService.getReactions('activity',
          this.activityId,
          reactionId,
          pageOffset,
          Math.min(100, offset + limit - pageOffset)));
      }
      return Promise.all(requests).then(pages => ({
        counts: pages[0]?.counts || {},
        reactions: pages.flatMap(page => page.reactions || []),
        reactorProfiles: pages.flatMap(page => page.reactorProfiles || []),
      }));
    },
    retrieveAllReactors(append) {
      this.loading = true;
      const offset = append ? this.likers.length : 0;
      const limit = append ? this.pageSize : Math.max(this.likers.length, this.pageSize);
      return Promise.all([
        this.$activityService.getActivityLikers(this.activityId, offset, limit),
        this.fetchReactionPages(null, offset, limit),
      ]).then(([likersData, reactionsData]) => {
        const likers = likersData.likes || [];
        this.likers = append ? this.likers.concat(likers) : likers;
        this.likersSize = likersData.size || 0;
        this.counts = reactionsData.counts;
        const reactionIdsByReactor = append ? {...this.reactionIdsByReactor} : {};
        reactionsData.reactions.forEach(reaction => reactionIdsByReactor[`${reaction.reactorIdentityId}`] = reaction.reactionId);
        this.reactionIdsByReactor = reactionIdsByReactor;
        this.updateReactionsTabCount();
      }).catch(e => {
        console.error('error retrieving activity reactions', e);
      }).finally(() => this.loading = false);
    },
    retrieveFilteredReactors(append) {
      this.loading = true;
      const offset = append ? this.likers.length : 0;
      const limit = append ? this.pageSize : Math.max(this.likers.length, this.pageSize);
      return this.fetchReactionPages(this.selectedReactionId, offset, limit)
        .then(reactionsData => {
          this.counts = reactionsData.counts;
          const reactions = reactionsData.reactions;
          const reactionIdsByReactor = append ? {...this.reactionIdsByReactor} : {};
          reactions.forEach(reaction => reactionIdsByReactor[`${reaction.reactorIdentityId}`] = reaction.reactionId);
          this.reactionIdsByReactor = reactionIdsByReactor;
          const profilesById = {};
          reactionsData.reactorProfiles.forEach(profile => profilesById[`${profile.id}`] = profile);
          const reactors = reactions
            .map(reaction => profilesById[`${reaction.reactorIdentityId}`])
            .filter(profile => !!profile)
            .map(profile => ({...profile, id: `${profile.id}`}));
          this.likers = append ? this.likers.concat(reactors) : reactors;
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
      this.retrieveReactors(true);
    }
  },
};
</script>
