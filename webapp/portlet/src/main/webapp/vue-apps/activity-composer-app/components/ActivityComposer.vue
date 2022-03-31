<template>
  <div id="activityComposer" class="activityComposer activityComposerApp pa-0">
    <div class="openLink d-flex flex-row mb-3 text-truncate">
      <span
        v-if="standalone"
        class="flex d-flex text-sub-title text-uppercase pt-3">{{ $t('activity.stream.title') }}</span>
      <a
        v-else
        @click="openComposerDrawer(true)"
        class="primary--text">
        <i class="uiIconEdit"></i>
        {{ composerButtonLabel }}
      </a>
      <select
        v-if="displayStreamFilter"
        id="filterStream"
        v-model="selectedFilter"
        name="status"
        class=" flex d-flex justify-end flex-nowrap activityFilter ignore-vuetify-classes">
        <option value="all">{{ $t('activity.stream.filter.all') }}</option>
        <option value="myPosted"> {{ $t('activity.stream.filter.myposted') }} </option>
      </select>
    </div>
    <activity-composer-drawer ref="activityComposerDrawer" />
  </div>
</template>

<script>
export default {
  props: {
    activityBody: {
      type: String,
      default: ''
    },
    activityId: {
      type: String,
      default: ''
    },
    activityParams: {
      type: Object,
      default: null
    },
    selectedFilter: {
      type: String,
      default: ''
    },
    standalone: {
      type: Boolean,
      default: false
    },
  },
  computed: {
    composerButtonLabel() {
      if (eXo.env.portal.spaceDisplayName){
        return this.$t('activity.composer.link', {0: eXo.env.portal.spaceDisplayName});
      } else {
        return this.$t('activity.composer.post');
      }
    },
    displayStreamFilter() {
      return !eXo.env.portal.spaceId;
    }
  },
  watch: {
    selectedFilter(newValue, oldValue) {
      if (oldValue !== '') {
        localStorage.setItem('ActivityFilter',newValue);
        this.$root.$emit('filter-activities',newValue);
      }
    }
  },
  methods: {
    openComposerDrawer() {
      this.$refs.activityComposerDrawer.open({
        activityId: this.activityId,
        activityBody: this.activityBody,
        activityParams: this.activityParams,
        files: [],
        activityType: null
      });
    },
  },
};
</script>