const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`;

if (extensionRegistry) {

  extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
    id: 'body',
    isEnabled: () => true,
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

  const defaultActivityOptions = {
    getSourceLink: activity => activity && activity.templateParams && activity.templateParams.link,
    getTitle: activity => activity && activity.templateParams && activity.templateParams.title || activity.templateParams.defaultTitle || activity.templateParams.link || '',
    getSummary: activity => activity && activity.templateParams && activity.templateParams.description || '',
    getThumbnail: activity => activity && activity.templateParams && activity.templateParams.image || '',
    supportsThumbnail: true,
    getBody: activity => {
      return (activity.templateParams && activity.templateParams.comment)
             || (activity && activity.title)
             || (activity && activity.body)
             || '';
    },
    getBodyToEdit: activity => {
      const body = activity && activity.templateParams && activity.templateParams.default_title && activity.templateParams.default_title.split('<oembed>')[0] || '';
      const link = activity && activity.templateParams && activity.templateParams.link || '';
      if (link) {
        return `${body}<p><a id='editActivityLinkPreview' href='${link}'>${link}</a></p>`;
      } else if (body) {
        return body;
      } else {
        return activity && (activity.title || activity.body);
      }
    },
    canShare: () => true,
  };

  // Register predefined activity types
  extensionRegistry.registerExtension('activity', 'type', {
    type: 'default',
    options: defaultActivityOptions,
  });

  extensionRegistry.registerExtension('activity', 'action', {
    id: 'edit',
    labelKey: 'UIActivity.label.Edit',
    isEnabled: (activity, activityTypeExtension) => {
      if (activityTypeExtension.canEdit) {
        if (activityTypeExtension.forceCanEditOverwrite) {
          return activityTypeExtension.canEdit(activity);
        } else if (!activityTypeExtension.canEdit(activity)) {
          return false;
        }
      }
      return activity.canEdit === 'true';
    },
    click: (activity, activityTypeExtension) => {
      const bodyToEdit = activityTypeExtension.getBodyToEdit && activityTypeExtension.getBodyToEdit(activity) || activityTypeExtension.getBody(activity);
      activityComposer.init({
        activityId: activity.id,
        composerAction: 'update',
        ckEditorType: `editActivity${activity.id}`,
        activityBody: bodyToEdit,
      });
    },
  });

  extensionRegistry.registerExtension('activity', 'action', {
    id: 'delete',
    labelKey: 'UIActivity.label.Delete',
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
    click: activity => {
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      return Vue.prototype.$activityService.deleteActivity(activity.id)
        .then(() => {
          document.dispatchEvent(new CustomEvent('activity-deleted', {detail: activity.id}));
        })
        .finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
    },
  });

  extensionRegistry.registerExtension('activity', 'action', {
    id: 'copyLink',
    labelKey: 'UIActivity.label.CopyLink',
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
}