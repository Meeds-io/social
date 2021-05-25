import ActivityStream from './components/ActivityStream.vue';
import ActivityStreamFeatureSwitch from './components/ActivityStreamFeatureSwitch.vue';
import ActivityStreamList from './components/list/ActivityStreamList.vue';
import ActivityStreamActivity from './components/activity/ActivityStreamActivity.vue';
import ActivityHead from './components/head/ActivityHead.vue';
import ActivityHeadSpace from './components/head/ActivityHeadSpace.vue';
import ActivityHeadUser from './components/head/ActivityHeadUser.vue';
import ActivityHeadTime from './components/head/ActivityHeadTime.vue';

const components = {
  'activity-stream': ActivityStream,
  'activity-stream-feature-switch': ActivityStreamFeatureSwitch,
  'activity-stream-list': ActivityStreamList,
  'activity-stream-activity': ActivityStreamActivity,
  'activity-head': ActivityHead,
  'activity-head-space': ActivityHeadSpace,
  'activity-head-user': ActivityHeadUser,
  'activity-head-time': ActivityHeadTime,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
