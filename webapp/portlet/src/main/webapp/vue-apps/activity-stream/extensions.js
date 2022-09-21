const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`;

extensionRegistry.registerComponent('ActivityStream', 'activity-stream-drawers', {
  id: 'share-drawer',
  vueComponent: Vue.options.components['activity-share-drawer'],
  rank: 10,
});

extensionRegistry.registerComponent('ActivityStream', 'activity-stream-drawers', {
  id: 'comments-drawer',
  vueComponent: Vue.options.components['activity-comments-drawer'],
  rank: 20,
});

extensionRegistry.registerComponent('ActivityStream', 'activity-stream-drawers', {
  id: 'reactions-drawer',
  vueComponent: Vue.options.components['activity-reactions-drawer'],
  rank: 25,
});

extensionRegistry.registerComponent('ActivityStream', 'activity-stream-drawers', {
  id: 'composer-drawer',
  vueComponent: Vue.options.components['activity-composer-drawer'],
  rank: 35,
});

extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
  id: 'body',
  vueComponent: Vue.options.components['activity-body'],
  rank: 1,
});

extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
  id: 'link',
  isEnabled: (params) => {
    const activityTypeExtension = params && params.activityTypeExtension;
    const activity = params && params.activity;
    const isActivityDetail = params && params.isActivityDetail;
    return activityTypeExtension.getSourceLink && activityTypeExtension.getSourceLink(activity, isActivityDetail);
  },
  vueComponent: Vue.options.components['activity-link'],
  rank: 5,
});

extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
  id: 'shared-activity',
  isEnabled: (params) => params.activity && params.activity.originalActivity,
  vueComponent: Vue.options.components['activity-share'],
  rank: 1000,
});


const defaultActivityOptions = {
  getEmbeddedHtml: activity => activity && activity.templateParams && activity.templateParams.html,
  getSourceLink: activity => activity && activity.templateParams && !activity.templateParams.html && activity.templateParams.link,
  getTitle: activity => activity && activity.templateParams && (activity.templateParams.title || activity.templateParams.defaultTitle || activity.templateParams.link) || '',
  getWindowTitle: activity => activity
                              && activity.templateParams
                              && activity.templateParams.title
                              || activity.templateParams.defaultTitle
                              || activity.templateParams.link
                              || activity.title
                              || '',
  getSummary: activity => activity && activity.templateParams && activity.templateParams.description || '',
  getThumbnail: activity => activity && activity.templateParams && activity.templateParams.image || '',
  getThumbnailProperties: activity => !(activity  && activity.templateParams && activity.templateParams.image) && {
    height: '90px',
    width: '90px',
    noBorder: true,
  },
  isUseSameViewForMobile: activity => !activity || !activity.templateParams || !activity.templateParams.image,
  supportsThumbnail: true,
  supportsIcon: true,
  defaultIcon: {
    icon: 'fa fa-link',
  },
  getBody: activity => {
    return Vue.prototype.$utils.trim((activity.templateParams && activity.templateParams.comment)
           || (activity && activity.title)
           || (activity && activity.body)
           || '');
  },
  getBodyToEdit: activity => {
    const templateParams = activity.templateParams;
    return Vue.prototype.$utils.trim(window.decodeURIComponent(templateParams
      && templateParams.default_title
      && templateParams.default_title
      || (activity.title && activity.title.replaceAll('%', '%25'))
      || (activity.body && activity.title.replaceAll('%', '%25'))
      || ''));
  },
  canShare: () => true,
};

extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
  id: 'embedded-html',
  isEnabled: (params) => {
    const activityTypeExtension = params && params.activityTypeExtension;
    const activity = params && params.activity;
    const isActivityDetail = params && params.isActivityDetail;
    return (activityTypeExtension.getEmbeddedHtml || defaultActivityOptions.getEmbeddedHtml)(activity, isActivityDetail);
  },
  vueComponent: Vue.options.components['activity-embedded-html'],
  rank: 5,
});
// Register predefined activity types
extensionRegistry.registerExtension('activity', 'type', {
  type: 'default',
  options: defaultActivityOptions,
});

extensionRegistry.registerExtension('activity', 'action', {
  id: 'pin',
  labelKey: 'UIActivity.label.Pin',
  icon: 'mdi-pin',
  isEnabled: (activity, activityTypeExtension) => {
    if (activityTypeExtension.canPin && !activityTypeExtension.canPin(activity)) {
      return false;
    }
    return eXo.env.portal.PinActivityEnabled && activity.canPin && !activity.pinned;
  },
  click: (activity) => {
    return Vue.prototype.$activityService.pinActivity(activity.id)
      .then(() => {
        document.dispatchEvent(new CustomEvent('activity-pinned', {detail: activity}));
      });
  },
});

extensionRegistry.registerExtension('activity', 'action', {
  id: 'unpin',
  labelKey: 'UIActivity.label.UnPin',
  icon: 'mdi-pin-off',
  isEnabled: (activity, activityTypeExtension) => {
    if (activityTypeExtension.canPin && !activityTypeExtension.canPin(activity)) {
      return false;
    }
    return eXo.env.portal.PinActivityEnabled && activity.canPin && activity.pinned;
  },
  click: (activity) => {
    return Vue.prototype.$activityService.unpinActivity(activity.id)
      .then(() => {
        document.dispatchEvent(new CustomEvent('activity-unpinned', {detail: activity}));
      });
  },
});

extensionRegistry.registerExtension('activity', 'action', {
  id: 'edit',
  labelKey: 'UIActivity.label.Edit',
  icon: 'fa-edit',
  isEnabled: (activity, activityTypeExtension) => {
    return activity.canEdit === 'true' && (!activityTypeExtension.canEdit || activityTypeExtension.canEdit(activity));
  },
  click: (activity, activityTypeExtension) => {
    const bodyToEdit = activityTypeExtension.getBodyToEdit && activityTypeExtension.getBodyToEdit(activity) || activityTypeExtension.getBody(activity);
    document.dispatchEvent(new CustomEvent('activity-composer-drawer-open', {detail: {
      activityId: activity.id,
      spaceId: activity && activity.activityStream && activity.activityStream.space && activity.activityStream.space.id || '',
      composerAction: 'update',
      ckEditorType: `editActivity${activity.id}`,
      activityBody: bodyToEdit,
      files: activity.files ? window.JSON.parse(window.JSON.stringify(activity.files)) : null,
      templateParams: window.JSON.parse(window.JSON.stringify(activity.templateParams)),
      activityType: activity.type
    }}));
  },
});

extensionRegistry.registerExtension('activity', 'action', {
  id: 'delete',
  labelKey: 'UIActivity.label.Delete',
  icon: 'fa-trash-alt',
  confirmDialog: true,
  confirmMessageKey: 'UIActivity.msg.Are_You_Sure_To_Delete_This_Activity',
  confirmTitleKey: 'UIActivity.label.Confirmation',
  confirmOkKey: 'UIActivity.label.Confirm_Delete_Activity-Button',
  confirmCancelKey: 'UIActivity.label.Cancel_Delete_Activity-Button',
  isEnabled: (activity, activityTypeExtension) => {
    if (activityTypeExtension.canDelete && !activityTypeExtension.canDelete(activity)) {
      return false;
    }
    return activity.canDelete === 'true';
  },
  click: (activity, activityTypeExtension, isActivityDetail) => {
    document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
    return Vue.prototype.$activityService.deleteActivity(activity.id, activityTypeExtension.hideOnDelete)
      .then(() => {
        document.dispatchEvent(new CustomEvent('activity-deleted', {detail: activity.id}));
        if (isActivityDetail) {
          setTimeout(() => {
            if (activity.activityStream.type === 'space') {
              window.location.href = `${eXo.env.portal.context}/g/${activity.activityStream.space.groupId.replace(/\//g, ':')}`;
            } else {
              window.location.href = eXo.env.portal.context;
            }
          }, 500);
        }
      })
      .finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
  },
});

extensionRegistry.registerExtension('activity', 'action', {
  id: 'copyLink',
  labelKey: 'UIActivity.label.CopyLink',
  icon: 'fa-copy',
  isEnabled: activity => activity && activity.id,
  click: (activity) => {
    const activityLink = `${window.location.origin}${activityBaseLink}?id=${activity.id}`;
    if (!$('#copyToClipboard').length) {
      $('body').append(`<input id="copyToClipboard" type="text" value="${activityLink}" style="position:absolute;left: -9999px;">`);
    }
    const clipboardInput = $('#copyToClipboard')[0];
    clipboardInput.value = activityLink;
    clipboardInput.select();
    clipboardInput.setSelectionRange(0, 99999);
    document.execCommand('copy');
  },
});

extensionRegistry.registerExtension('activity', 'comment-action', {
  id: 'delete',
  rank: 20,
  labelKey: 'UIActivity.label.Delete',
  icon: 'fa-trash-alt',
  confirmDialog: true,
  confirmMessageKey: 'UIActivity.msg.Are_You_Sure_To_Delete_This_Comment',
  confirmTitleKey: 'UIActivity.label.Confirmation',
  confirmOkKey: 'UIActivity.label.Confirm_Delete_Activity-Button',
  confirmCancelKey: 'UIActivity.label.Cancel_Delete_Activity-Button',
  isEnabled: (activity, comment) => comment.canDelete === 'true',
  click: (activity, comment) => {
    document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
    return Vue.prototype.$activityService.deleteActivity(comment.id)
      .then(() => {
        document.dispatchEvent(new CustomEvent('activity-comment-deleted', {detail: comment}));
      })
      .finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
  },
});

extensionRegistry.registerExtension('activity', 'comment-action', {
  id: 'edit',
  labelKey: 'UIActivity.label.Edit',
  icon: 'fa-edit',
  rank: 10,
  isEnabled: (activity, comment, activityTypeExtension) => {
    if (activityTypeExtension.canEdit) {
      if (activityTypeExtension.forceCanEditOverwrite) {
        return activityTypeExtension.canEdit(comment);
      } else if (!activityTypeExtension.canEdit(comment)) {
        return false;
      }
    }
    return comment.canEdit === 'true';
  },
  click: (activity, comment, activityTypeExtension) => {
    const bodyToEdit = activityTypeExtension.getBodyToEdit && activityTypeExtension.getBodyToEdit(comment) || activityTypeExtension.getBody(comment);
    document.dispatchEvent(new CustomEvent('activity-comment-edit', {detail: {
      activity,
      comment,
      activityBody: bodyToEdit,
    }}));
  },
});

extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
  id: 'like',
  vueComponent: Vue.options.components['activity-like-action'],
  rank: 10,
});

extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
  id: 'comment',
  vueComponent: Vue.options.components['activity-comment-action'],
  rank: 20,
});

extensionRegistry.registerComponent('ActivityHeader', 'activity-header-action', {
  id: 'favorite',
  vueComponent: Vue.options.components['activity-favorite-action'],
  rank: 30,
});

extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
  id: 'share',
  isEnabled: (params) => params.activity && !params.activity.originalActivity,
  vueComponent: Vue.options.components['activity-share-action'],
  rank: 100,
});

extensionRegistry.registerComponent('ActivityCommentFooter', 'activity-comment-footer-action', {
  id: 'like',
  vueComponent: Vue.options.components['activity-comment-like-action'],
  rank: 10,
});

extensionRegistry.registerComponent('ActivityCommentFooter', 'activity-comment-footer-action', {
  id: 'reply',
  vueComponent: Vue.options.components['activity-comment-reply-action'],
  rank: 20,
});