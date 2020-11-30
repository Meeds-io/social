function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('space-menu', type);
}

export const spaceMenuActionComponents = getExtensionsByType('action-component');

function getExtensionsByTypeCallButton(type) {
  return extensionRegistry.loadExtensions('space-title-action-components', type);
}

export const spaceMenuCallActionComponents = getExtensionsByTypeCallButton('action-component');
