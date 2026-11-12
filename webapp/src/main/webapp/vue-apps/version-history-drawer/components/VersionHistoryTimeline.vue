<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2026 Meeds Association
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
  <div class="versionHistoryTimeline">
    <v-chip-group
      v-if="sources.length > 1"
      :value="selected"
      class="versionHistoryFilter px-4 pt-3 mb-2"
      mandatory
      @change="selectChip">
      <v-chip
        :value="ALL"
        class="versionHistoryFilter-all"
        active-class="primary--text"
        small
        outlined>
        {{ $t('versionHistory.filter.all') }}
      </v-chip>
      <v-chip
        v-for="source in sources"
        :key="source.id"
        :value="source.id"
        :class="`versionHistoryFilter-${source.id}`"
        active-class="primary--text"
        small
        outlined>
        {{ sourceLabel(source) }}
      </v-chip>
    </v-chip-group>
    <div v-if="failed" class="versionHistoryError text-sub-title px-4 py-4">
      {{ $t('versionHistory.changes.error') }}
    </div>
    <v-list class="versionHistoryList py-0">
      <template v-for="line in lines">
        <v-list-item
          v-if="line.sourceId === VERSIONS_SOURCE"
          :key="`${line.sourceId}-${line.entry.key}`"
          :class="[line.entry.version.current? 'comment-background-color' : '']"
          class="history-line versionHistoryLine py-3 px-4 border-bottom-color d-block text-start"
          @click="openVersion(line.entry.version)">
          <version-card
            :version="line.entry.version"
            :can-manage="canManage"
            :disable-restore-version="disableRestoreVersion"
            :enable-edit-description="enableEditDescription"
            @version-update-description="updateVersionDescription"
            @restore-version="restoreVersion" />
        </v-list-item>
        <component
          :is="sourceComponent(line.sourceId)"
          v-else
          :key="`${line.sourceId}-${line.entry.key}`"
          :entry="line.entry"
          :params="sourceParams"
          class="border-bottom-color" />
      </template>
    </v-list>
    <v-progress-linear v-if="isBusy" indeterminate />
    <div
      v-else-if="!lines.length && !hasMore && !failed"
      class="versionHistoryEmpty text-center text-light-color mt-5">
      {{ $t('versionHistory.label.noVersion') }}
    </div>
    <div ref="end" class="versionHistoryEnd"></div>
  </div>
</template>

<script>
/** The lines given out per scroll, and asked of each source per read. */
const PAGE_SIZE = 20;
/**
 * The type under which an add-on feeds a version history that opts in:
 * extensionRegistry.registerComponent(<the host's sourcesExtension>, 'version-history-source', options),
 * the options being, the last registered per id kept:
 * - id: the source's id, unique, 'versions' being the host's own versions;
 * - labelKey, or label: the source's chip, shown once two sources apply;
 * - rank: its order among sources, lowest first, which also orders their
 *   lines of a same date;
 * - isEnabled(params): optional, a boolean or a promise of one, whether the
 *   source applies to the object; a failure means it does not;
 * - load({...params, before, limit}): a promise of {entries, next}, entries
 *   being at most limit lines older than the cursor before (null for the
 *   first page), newest first, each as {time, key, ...}: time in
 *   milliseconds, key unique within the source and, among its lines of a
 *   same time, highest first; next being the cursor the following page is
 *   read from, given back as before, null once none is left. A page must
 *   neither repeat nor skip a line across reads, even when lines are added
 *   meanwhile: a cursor on (time, key) does that. A rejection drops the
 *   source and tells the reader some changes cannot be read;
 * - refreshEvent: optional, the name of a document event after which the
 *   source may hold new lines, such as a save made beside the list;
 * - isRefreshFor(detail, params): optional, whether such an event, by its
 *   detail, concerns the object shown; without it, every such event does;
 * - vueComponent: renders one line, given the props entry and params.
 * params are the host's sourceParams, such as {noteId}.
 */
const SOURCE_TYPE = 'version-history-source';
/** The id of the host's own versions, always a source. */
const VERSIONS_SOURCE = 'versions';

/**
 * @param {Object} version a version, as the host lists it
 * @returns {number} the date its card shows, its last update else its
 *          creation, in milliseconds
 */
function versionTime(version) {
  return Number(version?.updatedDate?.time || version?.createdDate?.time) || 0;
}

export default {
  props: {
    versions: {
      type: Array,
      default: null,
    },
    isLoading: {
      type: Boolean,
      default: false,
    },
    canManage: {
      type: Boolean,
      default: false,
    },
    enableEditDescription: {
      type: Boolean,
      default: false,
    },
    disableRestoreVersion: {
      type: Boolean,
      default: false,
    },
    sourcesExtension: {
      type: String,
      default: null,
    },
    sourceParams: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    ALL: 'all',
    VERSIONS_SOURCE,
    sources: [],
    selected: 'all',
    lines: [],
    ready: false,
    loading: false,
    hasMore: false,
    failed: false,
    timeline: null,
    generation: 0,
    sourcesGeneration: 0,
    observer: null,
    refreshListeners: [],
  }),
  computed: {
    /**
     * Tells whether a read is pending: a source's check or page, or the
     * host's versions.
     * @returns {boolean} true while the progress bar shows
     */
    isBusy() {
      return !this.ready || this.loading || this.isLoading;
    },
    /**
     * Names the object whose history shows: the host passes a new params
     * object at each of its renders, so only a change of its content counts.
     * @returns {string} the params, serialized
     */
    paramsKey() {
      return JSON.stringify(this.sourceParams || {});
    },
    /**
     * The registry event telling that a source was registered for the host.
     * @returns {string} the event's name
     */
    registryEvent() {
      return `component-${this.sourcesExtension}-${SOURCE_TYPE}-updated`;
    },
  },
  watch: {
    /**
     * Reads the sources again when the host shows another object.
     * @returns {void}
     */
    paramsKey() {
      this.loadSources();
    },
  },
  /**
   * Reads the sources, and again when an add-on registers one late; shows
   * the lines again when the host's versions change and it is not loading
   * them. The flag and the list are watched as one pair, so that a host
   * clearing the flag with the list, or after it, rebuilds once.
   * @returns {void}
   */
  created() {
    document.addEventListener(this.registryEvent, this.refreshSources);
    this.$watch(() => ({
      loading: this.isLoading,
      versions: this.versions,
      ids: (this.versions || []).map(version => version?.id).join(),
    }), this.versionsChanged);
    this.loadSources();
  },
  /**
   * Loads the following lines whenever the end of the list comes into view.
   * @returns {void}
   */
  mounted() {
    this.observer = new IntersectionObserver(observed => {
      if (observed.some(entry => entry.isIntersecting)) {
        this.loadMore();
      }
    });
    this.observer.observe(this.$refs.end);
  },
  /**
   * Drops every answer still to come and stops watching the list, the
   * sources' events and the registry.
   * @returns {void}
   */
  beforeDestroy() {
    document.removeEventListener(this.registryEvent, this.refreshSources);
    this.listenToRefreshes([]);
    this.generation++;
    this.sourcesGeneration++;
    if (this.observer) {
      this.observer.disconnect();
    }
  },
  methods: {
    /**
     * Collects the host's versions and the sources add-ons registered for
     * the host, the last one registered per id, that apply to the object,
     * ordered by rank, then shows the lines of the chosen one, at once or,
     * while the host loads its versions, once they are in.
     * @returns {Promise} settled once the first lines are shown
     */
    loadSources() {
      const generation = ++this.sourcesGeneration;
      // the lines of the previous sources, still being read, are dropped, and none is read until the sources are checked
      this.generation++;
      this.timeline = null;
      const params = {...this.sourceParams};
      const byId = new Map();
      (extensionRegistry.loadComponents(this.sourcesExtension) || [])
        .filter(component => component.componentName === SOURCE_TYPE && component.componentOptions.id !== VERSIONS_SOURCE)
        .forEach(component => byId.set(component.componentOptions.id, component.componentOptions));
      const registered = [...byId.values()];
      this.lines = [];
      this.ready = false;
      return Promise.all(registered.map(source => this.isSourceEnabled(source, params)))
        .then(enabled => {
          if (generation === this.sourcesGeneration) {
            this.sources = [this.versionsSource(), ...registered.filter((source, index) => enabled[index])]
              .sort((source1, source2) => Number(source1.rank || 0) - Number(source2.rank || 0));
            this.listenToRefreshes(this.sources);
            this.ready = true;
            // while the host loads its versions, versionsChanged shows the lines once they are in
            return !this.isLoading && this.rebuild() || Promise.resolve();
          }
        });
    },
    /**
     * Reads the sources again when one is registered after the list opened,
     * while it shows no line yet, so that no line read is lost.
     * @returns {Promise} settled once the first lines are shown, or at once
     *          when lines already show
     */
    refreshSources() {
      return this.lines.length && Promise.resolve() || this.loadSources();
    },
    /**
     * Shows the lines again from the newest once the host's versions are
     * loaded, or changed while not loading: when the flag turns false, or
     * the list is another one or holds other versions while it is false.
     * The flag raised and lowered before the watcher runs, the list
     * unchanged, rebuilds nothing.
     * @param {Object} state the flag, the list and its versions' ids now
     * @param {Object} previous the same when the watcher last ran
     * @returns {void}
     */
    versionsChanged(state, previous) {
      if (state.loading || !this.ready) {
        return;
      }
      if (previous.loading || previous.versions !== state.versions || previous.ids !== state.ids) {
        this.rebuild();
      }
    },
    /**
     * Listens to the events after which the sources shown may hold new
     * lines, and to none of the sources shown before.
     * @param {Array<Object>} sources the sources shown
     * @returns {void}
     */
    listenToRefreshes(sources) {
      this.refreshListeners.forEach(({event, listener}) => document.removeEventListener(event, listener));
      this.refreshListeners = sources.filter(source => source.refreshEvent).map(source => {
        const listener = event => this.refreshFor(source, event);
        document.addEventListener(source.refreshEvent, listener);
        return {event: source.refreshEvent, listener};
      });
    },
    /**
     * Shows the lines again, from the newest, with the same chip, when an
     * event a source listens to concerns the object shown; while the host
     * loads its versions, versionsChanged shows them once they are in.
     * @param {Object} source the source the event is for
     * @param {CustomEvent} event the event
     * @returns {Promise} settled once the first lines are shown again
     */
    refreshFor(source, event) {
      let concerned = true;
      try {
        concerned = !source.isRefreshFor || !!source.isRefreshFor(event?.detail, {...this.sourceParams});
      } catch (error) {
        concerned = false;
      }
      return concerned && this.ready && !this.isLoading && this.rebuild() || Promise.resolve();
    },
    /**
     * @param {Object} source the options a source was registered with
     * @param {Object} params the host's params
     * @returns {Promise<boolean>} whether the source applies to the object;
     *          not when its check fails
     */
    isSourceEnabled(source, params) {
      if (!source.isEnabled) {
        return Promise.resolve(true);
      }
      return Promise.resolve()
        .then(() => source.isEnabled(params))
        .then(enabled => !!enabled)
        .catch(() => false);
    },
    /**
     * The host's own versions, read from its list at each first page: the
     * host gives them all, so the first page holds every one and no cursor
     * follows.
     * @returns {Object} the source of the host's versions, each keyed by its
     *          number and dated as its card shows it
     */
    versionsSource() {
      return {
        id: VERSIONS_SOURCE,
        labelKey: 'versionHistory.filter.versions',
        rank: 0,
        load: ({before}) => Promise.resolve({
          entries: before ? [] : (this.versions || [])
            .map(version => ({time: versionTime(version), key: version.versionNumber, version}))
            .sort((entry1, entry2) => entry2.time - entry1.time || entry2.key - entry1.key),
          next: null,
        }),
      };
    },
    /**
     * Shows the lines again, from the newest, with the chip selected, or
     * « All » when its source no longer applies.
     * @returns {Promise} settled once the first lines are shown
     */
    rebuild() {
      const kept = this.sources.some(source => source.id === this.selected);
      return this.select(kept && this.selected || this.ALL);
    },
    /**
     * Keeps the chip the user selected and shows its lines, or, while the
     * sources are checked or the host loads its versions, lets loadSources
     * or versionsChanged show them once they are in.
     * @param {string} sourceId the id of the chosen source, or all
     * @returns {Promise} settled once the first lines are shown, or at once
     *          while the sources are checked or the host loads its versions
     */
    selectChip(sourceId) {
      this.selected = sourceId || this.ALL;
      return this.ready && !this.isLoading && this.select(this.selected) || Promise.resolve();
    },
    /**
     * Shows, from the newest, the lines of the chosen source, of all of
     * them for « All ».
     * @param {string} sourceId the id of the chosen source, or all
     * @returns {Promise} settled once the first lines are shown
     */
    select(sourceId) {
      this.selected = sourceId || this.ALL;
      const chosen = this.selected === this.ALL && this.sources
        || this.sources.filter(source => source.id === this.selected);
      const params = {...this.sourceParams};
      this.generation++;
      this.timeline = this.$changesTimeline.createTimeline(chosen.map(source => ({
        id: source.id,
        rank: Number(source.rank || 0),
        load: ({before, limit}) => source.load({...params, before, limit}),
      })), PAGE_SIZE);
      this.lines = [];
      this.hasMore = true;
      this.failed = false;
      this.loading = false;
      return this.loadMore();
    },
    /**
     * Shows the following lines, then more while the end of the list stays
     * in view, since scrolling cannot reveal it again.
     * @returns {Promise} settled once they are shown, or dropped when the
     *          chip or the object changed meanwhile
     */
    async loadMore() {
      if (this.loading || !this.hasMore || !this.timeline) {
        return;
      }
      const generation = this.generation;
      const timeline = this.timeline;
      this.loading = true;
      const lines = await timeline.next(PAGE_SIZE);
      if (generation !== this.generation) {
        return;
      }
      this.lines.push(...lines);
      this.hasMore = timeline.hasMore();
      this.failed = timeline.hasFailed();
      this.loading = false;
      // the end marker is measured once the new lines are rendered
      await this.$nextTick();
      if (this.isEndInView()) {
        await this.loadMore();
      }
    },
    /**
     * @returns {boolean} whether the end of the list shows in the window
     */
    isEndInView() {
      const end = this.$refs.end;
      if (!end) {
        return false;
      }
      const bounds = end.getBoundingClientRect();
      return bounds.top < window.innerHeight && bounds.bottom >= 0;
    },
    /**
     * @param {Object} source a source
     * @returns {string} its label, translated when it gives a key
     */
    sourceLabel(source) {
      return source.labelKey && this.$te(source.labelKey) && this.$t(source.labelKey) || source.label || source.id;
    },
    /**
     * @param {string} sourceId a source's id
     * @returns {Object} the component showing one of its lines
     */
    sourceComponent(sourceId) {
      const source = this.sources.find(candidate => candidate.id === sourceId);
      return source && source.vueComponent;
    },
    /**
     * Asks the host to open a version.
     * @param {Object} version the version to open
     * @returns {void}
     */
    openVersion(version) {
      this.$emit('open-version', version);
    },
    /**
     * Asks the host to save a version's summary.
     * @param {Object} version the version
     * @param {string} newDescription its new summary
     * @returns {void}
     */
    updateVersionDescription(version, newDescription) {
      this.$emit('version-update-description', version, newDescription);
    },
    /**
     * Asks the host to restore a version the card has confirmed.
     * @param {Object} version the version to restore
     * @returns {void}
     */
    restoreVersion(version) {
      this.$emit('restore-version', version);
    },
  },
};
</script>
