const activityBaseLink = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity`;

if (extensionRegistry) {
  // Register predefined activity types
  extensionRegistry.registerExtension('activity', 'type', {
    type: 'default',
    options: {
      getBody: activity => activity && activity.title || '',
      canShare: () => true,
    },
  });

  extensionRegistry.registerExtension('activity', 'type', {
    type: 'LINK_ACTIVITY',
    options: {
      init: null,
      getActivityLink: null,
      getBody: activity => activity && activity.templateParams && activity.templateParams.comment || '',
      getBodyToEdit: activity => {
        const body = activity && activity.templateParams && activity.templateParams.default_title && activity.templateParams.default_title.split('<oembed>')[0] || '';
        const link = activity && activity.templateParams && activity.templateParams.link || '';
        if (link) {
          return `${body}<p><a id='editActivityLinkPreview' href='${link}'>${link}</a></p>`;
        } else {
          return body;
        }
      },
      getTitle: activity => activity && activity.templateParams && activity.templateParams.title || activity.templateParams.defaultTitle || activity.templateParams.link || '',
      getSummary: activity => activity && activity.templateParams && activity.templateParams.description || '',
      getThumbnail: activity => activity && activity.templateParams && activity.templateParams.image || '',
      supportsThumbnail: true,
      getSourceLink: activity => activity && activity.templateParams && activity.templateParams.link,
      canShare: () => true,
    },
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

  extensionRegistry.registerComponent('ActivityFooter', 'activity-footer-action', {
    id: 'share',
    vueComponent: Vue.options.components['activity-share-action'],
    rank: 100,
  });
}