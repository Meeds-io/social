function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('space-menu', type);
}

export const spaceMenuActionComponents = getExtensionsByType('action-component');

function getSpaceTitleExtensionsByType(type) {
  return extensionRegistry.loadExtensions('space-title-action-components', type);
}

export const spaceTitleActionComponents = getSpaceTitleExtensionsByType('action-component');
