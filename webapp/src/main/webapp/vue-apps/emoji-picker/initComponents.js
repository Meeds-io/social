/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import EmojiPicker from './components/EmojiPicker.vue';
import EmojiPickerButton from './components/EmojiPickerButton.vue';
import EmojiPickerQuickEmojis from './components/view/EmojiPickerQuickEmojis.vue';
import EmojiPickerList from './components/view/EmojiPickerList.vue';
import EmojiPickerListCategory from './components/view/EmojiPickerListCategory.vue';
import EmojiSuggester from './components/suggester/EmojiSuggester.vue';
import EmojiSuggestionList from './components/suggester/EmojiSuggestionList.vue';

const components = {
  'emoji-picker': EmojiPicker,
  'emoji-picker-button': EmojiPickerButton,
  'emoji-picker-quick-emojis': EmojiPickerQuickEmojis,
  'emoji-picker-list': EmojiPickerList,
  'emoji-picker-list-category': EmojiPickerListCategory,
  'emoji-suggester': EmojiSuggester,
  'emoji-suggester-list': EmojiSuggestionList
};

for (const key in components) {
  Vue.component(key, components[key]);
}
