function getExtensionsByType(type) {
  return extensionRegistry.loadExtensions('profile-header', type);
}

export const profileHeaderActionComponents = getExtensionsByType('action-component');