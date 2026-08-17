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
    <div v-if="filterChips.length > 2" class="d-flex flex-wrap px-3 pb-2">
      <v-chip
        v-for="chip in filterChips"
        :key="chip.id"
        :outlined="selectedReactionId !== chip.id"
        :aria-label="chip.label"
        :title="chip.label"
        class="me-1 mb-1"
        small
        @click="selectReaction(chip.id)">
        <span v-if="chip.emoji" class="reaction-emoji me-1">{{ chip.emoji }}</span>
        <span v-else>{{ chip.label }}</span>
        <span class="ms-1">{{ chip.count }}</span>
      </v-chip>
    </div>
    <div v-if="reactorsToDisplay.length" class="likers-list">
      <div
        v-for="reactor in reactorsToDisplay"
        :key="reactor.id"
        class="d-flex align-center justify-space-between">
        <activity-liker-item :liker="reactor" />
        <div
          v-if="reactor.reactionOption"
          class="d-flex align-center me-4 flex-shrink-0">
          <span class="reaction-emoji me-1">{{ reactor.reactionOption.emoji }}</span>
          <span class="text-caption text-sub-title">{{ $t(reactor.reactionOption.activeLabelKey) }}</span>
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
      return this.likers
        .map(liker => Object.assign({}, liker, {
          reactionId: this.reactionIdsByReactor[liker.id] || 'like',
          reactionOption: this.reactionOptions.find(option => option.id === (this.reactionIdsByReactor[liker.id] || 'like')),
        }))
        .filter(reactor => !this.selectedReactionId || reactor.reactionId === this.selectedReactionId);
    },
    filterChips() {
      const totalCount = Object.values(this.counts).reduce((sum, count) => sum + count, 0);
      const chips = [{
        id: null,
        label: this.$t('UIActivity.label.Show_All_Likers'),
        count: totalCount,
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
      return chips;
    },
    hasMoreReactors() {
      return this.likersSize > this.limit;
    }
  },
  created() {
    this.$root.$on('activity-liked', this.handleActivityLikesUpdate);
    this.$reactionService.getReactionOptions('activity')
      .then(options => this.reactionOptions = options);
    this.retrieveReactors();
    document.addEventListener('check-reactions', event => {
      if (event?.detail && event.detail === this.activityId) {
        this.updateReactionsTabCount();
      }
    });
  },
  beforeDestroy() {
    this.$root.$off('activity-liked', this.handleActivityLikesUpdate);
  },
  watch: {
    activityId() {
      this.selectedReactionId = null;
      this.retrieveReactors();
    }
  },
  methods: {
    selectReaction(reactionId) {
      this.selectedReactionId = this.selectedReactionId === reactionId ? null : reactionId;
    },
    handleActivityLikesUpdate(activityId) {
      if (activityId === this.activityId) {
        this.retrieveReactors();
      }
    },
    retrieveReactors() {
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
    updateReactionsTabCount() {
      document.dispatchEvent(new CustomEvent('update-reaction-extension', {
        detail: {
          numberOfReactions: this.likersSize,
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
