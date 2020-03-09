/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Android Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.location.activity;

import androidx.annotation.VisibleForTesting;

import com.google.android.gms.location.DetectedActivity;

import promise.commons.pref.Preferences;
import promise.location.Store;

public class ActivityStore implements Store<DetectedActivity> {

  private static final String PREFERENCES_FILE = "promise_location_activity";
  private static final String PREFIX_ID = ActivityStore.class.getCanonicalName() + ".key";
  private static final String ACTIVITY_ID = "activity";
  private static final String CONFIDENCE_ID = "confidence";

  private Preferences preferences;

  public ActivityStore() {
    preferences = new Preferences(PREFERENCES_FILE);
  }

  @VisibleForTesting
  public void setPreferences(Preferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public void put(String id, DetectedActivity activity) {
    preferences.save(getFieldKey(id, ACTIVITY_ID), activity.getType());
    preferences.save(getFieldKey(id, CONFIDENCE_ID), activity.getConfidence());
  }

  @Override
  public DetectedActivity get(String id) {
    if (preferences != null) {
      int activity = preferences.getInt(getFieldKey(id, ACTIVITY_ID));
      int confidence = preferences.getInt(getFieldKey(id, CONFIDENCE_ID));
      return new DetectedActivity(activity == 0 ? DetectedActivity.UNKNOWN : activity, confidence);
    } else return null;
  }

  @Override
  public void remove(String id) {
    preferences.clear(getFieldKey(id, ACTIVITY_ID));
    preferences.clear(getFieldKey(id, CONFIDENCE_ID));
  }

  private String getFieldKey(String id, String field) {
    return PREFIX_ID + "_" + id + "_" + field;
  }
}
