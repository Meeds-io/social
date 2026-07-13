/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

export function getCategoryTree(options) {
  const formData = new FormData();
  if (options.objectType) {
    formData.append('objectType', options.objectType);
  }
  if (options.parentId) {
    formData.append('parentId', options.parentId);
  }
  if (options.ownerId) {
    formData.append('ownerId', options.ownerId);
  }
  if (options.spaceId) {
    formData.append('spaceId', options.spaceId);
  }
  if (options.depth) {
    formData.append('depth', options.depth);
  }
  if (options.offset) {
    formData.append('offset', options.offset);
  }
  if (options.limit) {
    formData.append('limit', options.limit);
  }
  if (options.linkPermission) {
    formData.append('linkPermission', options.linkPermission);
  }
  if (options.token) {
    formData.append('token', options.token);
  }
  const urlParams = new URLSearchParams(formData).toString();
  return fetch(`/social/rest/categories?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving category tree');
    }
  });
}

export function getCategory(id) {
  return fetch(`/social/rest/categories/${id}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving the category');
    }
  });
}

export function getCategoryEntries(options) {
  const formData = new FormData();
  formData.append('types', (options.types || []).join(','));
  if (options.offset) {
    formData.append('offset', options.offset);
  }
  if (options.limit) {
    formData.append('limit', options.limit);
  }
  const urlParams = new URLSearchParams(formData).toString();
  return fetch(`/social/rest/categories/${options.categoryId}/entries?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving the category entries');
    }
  });
}

export function getAncestorIds(id) {
  return fetch(`/social/rest/categories/${id}/ancestors`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving ancestors');
    }
  });
}

export function getSubcategoryIds(id, options) {
  const formData = new FormData();
  if (options.depth) {
    formData.append('depth', options.depth);
  }
  if (options.offset) {
    formData.append('offset', options.offset);
  }
  if (options.limit) {
    formData.append('limit', options.limit);
  }
  if (options.token) {
    formData.append('token', options.token);
  }
  const urlParams = new URLSearchParams(formData).toString();
  return fetch(`/social/rest/categories/${id}/subcategories?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving subcategories');
    }
  });
}

export function findCategories(options) {
  const formData = new FormData();
  if (options.query) {
    formData.append('query', options.query);
  }
  if (options.parentId) {
    formData.append('parentId', options.parentId);
  }
  if (options.ownerId) {
    formData.append('ownerId', options.ownerId);
  }
  if (options.offset) {
    formData.append('offset', options.offset);
  }
  if (options.limit) {
    formData.append('limit', options.limit);
  }
  if (options.sortByName) {
    formData.append('sortByName', options.sortByName);
  }
  if (options.linkPermission) {
    formData.append('linkPermission', options.linkPermission);
  }
  const urlParams = new URLSearchParams(formData).toString();
  return fetch(`/social/rest/categories/search?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when searching for categories');
    }
  });
}

export function createCategory(category) {
  return fetch('/social/rest/categories', {
    credentials: 'include',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(category),
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when creating the category');
    }
  });
}

export function updateCategory(category) {
  return fetch(`/social/rest/categories/${category.id}`, {
    credentials: 'include',
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(category),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when updating the category');
    }
  });
}

export function deleteCategory(id) {
  return fetch(`/social/rest/categories/${id}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when deleting the category');
    }
  });
}

export function canEdit(id) {
  return fetch(`/social/rest/categories/canEdit/${id}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      return resp.text().then(text => text === 'true');
    } else {
      throw new Error('Error when requesting server');
    }
  });
}

export function canLink(id) {
  return fetch(`/social/rest/categories/canLink/${id}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      return resp.text().then(text => text === 'true');
    } else {
      throw new Error('Error when requesting server');
    }
  });
}
