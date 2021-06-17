import ActivityStream from './components/ActivityStream.vue';
import ActivityStreamFeatureSwitch from './components/ActivityStreamFeatureSwitch.vue';
import ActivityStreamList from './components/list/ActivityStreamList.vue';
import ActivityStreamUpdater from './components/list/ActivityStreamUpdater.vue';
import ActivityStreamConfirm from './components/list/ActivityStreamConfirm.vue';
import ActivityStreamActivity from './components/activity/ActivityStreamActivity.vue';
import ActivityHead from './components/head/ActivityHead.vue';
import ActivityHeadSpace from './components/head/ActivityHeadSpace.vue';
import ActivityHeadUser from './components/head/ActivityHeadUser.vue';
import ActivityHeadTime from './components/head/ActivityHeadTime.vue';
import ActivityHeadMenu from './components/head/ActivityHeadMenu.vue';
import ActivityBody from './components/content/ActivityBody.vue';
import ActivityLink from './components/content/ActivityLink.vue';
import ActivityFooter from './components/footer/ActivityFooter.vue';
import ActivityActions from './components/footer/ActivityActions.vue';
import ActivityCommentAction from './components/footer/actions/ActivityCommentAction.vue';
import ActivityLikeAction from './components/footer/actions/ActivityLikeAction.vue';
import ActivityCommentsPreview from './components/comment/ActivityCommentsPreview.vue';
import ActivityComments from './components/comment/ActivityComments.vue';
import ActivityComment from './components/comment/ActivityComment.vue';

const components = {
  'activity-stream': ActivityStream,
  'activity-stream-feature-switch': ActivityStreamFeatureSwitch,
  'activity-stream-list': ActivityStreamList,
  'activity-stream-updater': ActivityStreamUpdater,
  'activity-stream-confirm-dialog': ActivityStreamConfirm,
  'activity-stream-activity': ActivityStreamActivity,
  'activity-head': ActivityHead,
  'activity-head-space': ActivityHeadSpace,
  'activity-head-user': ActivityHeadUser,
  'activity-head-time': ActivityHeadTime,
  'activity-head-menu': ActivityHeadMenu,
  'activity-body': ActivityBody,
  'activity-link': ActivityLink,
  'activity-footer': ActivityFooter,
  'activity-actions': ActivityActions,
  'activity-comment-action': ActivityCommentAction,
  'activity-like-action': ActivityLikeAction,
  'activity-comments-preview': ActivityCommentsPreview,
  'activity-comments': ActivityComments,
  'activity-comment': ActivityComment,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
