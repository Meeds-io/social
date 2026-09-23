/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import BackgroundImageAttachment from './components/BackgroundImageAttachment.vue';
import BackgroundInput from './components/BackgroundInput.vue';
import BackgroundMarginInput from './components/BackgroundMarginInput.vue';
import BackgroundRadiusInput from './components/BackgroundRadiusInput.vue';
import BorderInput from './components/BorderInput.vue';
import BorderRadiusInput from './components/BorderRadiusInput.vue';
import BorderRadiusSelector from './components/BorderRadiusSelector.vue';
import ColorPicker from './components/ColorPicker.vue';
import MarginInput from './components/MarginInput.vue';
import SectionMarginInput from './components/SectionMarginInput.vue';
import TextBackgroundInput from './components/TextBackgroundInput.vue';
import TextInput from './components/TextInput.vue';

const components = {
  'styling-color-picker': ColorPicker,
  'styling-border-radius-selector': BorderRadiusSelector,
  'styling-background-image-attachment': BackgroundImageAttachment,
  'styling-background-input': BackgroundInput,
  'styling-background-margin-input': BackgroundMarginInput,
  'styling-background-radius-input': BackgroundRadiusInput,
  'styling-text-input': TextInput,
  'styling-text-background-input': TextBackgroundInput,
  'styling-margin-input': MarginInput,
  'styling-section-margin-input': SectionMarginInput,
  'styling-border-input': BorderInput,
  'styling-border-radius-input': BorderRadiusInput,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
