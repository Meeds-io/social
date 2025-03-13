 
extensionRegistry.registerComponent('favorite-activity', 'favorite-drawer-item', {
  id: 'activity',
  vueComponent: Vue.component['activity-favorite-item'],
}); 

extensionRegistry.registerComponent('favorite-space', 'favorite-drawer-item', {
  id: 'space',
  vueComponent: Vue.component['space-favorite-item'],
}); 

