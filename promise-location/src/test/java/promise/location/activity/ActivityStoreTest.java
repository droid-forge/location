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

import com.google.android.gms.location.DetectedActivity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import promise.commons.Promise;
import promise.commons.pref.Preferences;
import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;

/**
 * Tests {@link ActivityStore}
 */
@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityStoreTest {

  private static final DetectedActivity TEST_ACTIVITY =
      new DetectedActivity(DetectedActivity.UNKNOWN, 100);
  private static final String TEST_ACTIVITY_ID = "test_activity_1";

  @Test
  public void test_activity_store_full_cycle() {
    Promise.init(RuntimeEnvironment.application);
    ActivityStore store = new ActivityStore();
    store.setPreferences(getPreferences());

    Assert.assertNull(store.get(TEST_ACTIVITY_ID));

    store.put(TEST_ACTIVITY_ID, TEST_ACTIVITY);
    DetectedActivity storedActivity = store.get(TEST_ACTIVITY_ID);
    Assert.assertEquals(storedActivity.getConfidence(), TEST_ACTIVITY.getConfidence());
    Assert.assertEquals(storedActivity.getType(), TEST_ACTIVITY.getType());

    store.remove(TEST_ACTIVITY_ID);
    Assert.assertNull(store.get(TEST_ACTIVITY_ID));
  }

  private Preferences getPreferences() {
    return new Preferences("test_prefs");
  }
}
