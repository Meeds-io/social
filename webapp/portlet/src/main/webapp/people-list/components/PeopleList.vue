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
    class="transparent peopleList"
    flat>
    <people-toolbar
      :keyword="keyword"
      :filter="filter"
      :people-count="peopleCount"
      :skeleton="skeleton"
      @keyword-changed="keyword = $event"
      @filter-changed="filter = $event" />
    <people-card-list
      ref="peopleList"
      :keyword="keyword"
      :filter="filter"
      :loading-people="loadingPeople"
      :skeleton="skeleton"
      :people-count="peopleCount"
      @loaded="peopleLoaded" />
  </v-app>    
</template>

<script>

export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    peopleCount: 0,
    loadingPeople: false,
    skeleton: true,
  }),
  methods: {
    peopleLoaded(peopleCount) {
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      this.peopleCount = peopleCount;
      if (this.skeleton) {
        this.skeleton = false;
      }
    }
  },
};
</script>