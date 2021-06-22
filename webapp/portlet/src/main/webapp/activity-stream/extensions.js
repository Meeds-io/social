const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`;

if (extensionRegistry) {

  extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
    id: 'body',
    isEnabled: () => true,
    vueComponent: Vue.options.components['activity-body'],
    rank: -1,
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
    rank: 0,
  });

  const defaultActivityOptions = {
    getSourceLink: activity => activity && activity.templateParams && activity.templateParams.link,
    getTitle: activity => activity && activity.templateParams && activity.templateParams.title || activity.templateParams.defaultTitle || activity.templateParams.link || '',
    getSummary: activity => activity && activity.templateParams && activity.templateParams.description || '',
    getThumbnail: activity => activity && activity.templateParams && activity.templateParams.image || '',
    supportsThumbnail: true,
    getBody: activity => (activity.templateParams && activity.templateParams.comment) || (activity && activity.title) || '',
    getBodyToEdit: activity => {
      const body = activity && activity.templateParams && activity.templateParams.default_title && activity.templateParams.default_title.split('<oembed>')[0] || '';
      const link = activity && activity.templateParams && activity.templateParams.link || '';
      if (link) {
        return `${body}<p><a id='editActivityLinkPreview' href='${link}'>${link}</a></p>`;
      } else if (body) {
        return body;
      } else {
        return activity && activity.title;
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
      if (activityTypeExtension.canEdit && !activityTypeExtension.canEdit(activity)) {
        return false;
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
          document.dispatchEvent(new CustomEvent('activity-stream-activity-deleted', {detail: activity.id}));
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
    labelKey: 'UIActivity.label.Delete',
    confirmDialog: true,
    confirmMessageKey: 'UIActivity.msg.Are_You_Sure_To_Delete_This_Comment',
    confirmTitleKey: 'UIActivity.label.Confirmation',
    confirmOkKey: 'UIActivity.label.Confirm_Delete_Activity-Button',
    confirmCancelKey: 'UIActivity.label.Cancel_Delete_Activity-Button',
    isEnabled: comment => comment.canDelete === 'true',
    click: comment => {
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      return Vue.prototype.$activityService.deleteActivity(comment.id)
        .then(() => {
          document.dispatchEvent(new CustomEvent('activity-stream-comment-deleted', {detail: {
            activityId: comment.activityId,
            commentId: comment.id,
          }}));
        })
        .finally(() => document.dispatchEvent(new CustomEvent('hideTopBarLoading')));
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
    id: 'reply',
    vueComponent: Vue.options.components['activity-comment-reply-action'],
    rank: 20,
  });
}