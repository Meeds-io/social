 
extensionRegistry.registerComponent('favorite-activity', 'favorite-drawer-item', {
  id: 'activity',
  vueComponent: Vue.options.components['activity-favorite-item'],
}); 

extensionRegistry.registerComponent('favorite-space', 'favorite-drawer-item', {
  id: 'space',
  vueComponent: Vue.options.components['space-favorite-item'],
}); 

