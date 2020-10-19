function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('space-menu', type);
}

export const spaceMenuActionComponents = getExtensionsByType('action-component');