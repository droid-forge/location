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

package promise.dev4vin.promiselocation;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import promise.location.activity.ActivityUpdatedListener;
import promise.location.PromiseLocation;
import promise.location.activity.ActivityParams;
import promise.dev4vin.promiselocation.util.MockActivityRecognitionProvider;
import promise.location.Logger;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityRecognitionControlTest {

  private static final ActivityParams DEFAULT_PARAMS = ActivityParams.NORMAL;

  private MockActivityRecognitionProvider mockProvider;
  private ActivityUpdatedListener activityUpdatedListener;

  @Before
  public void setup() {
    mockProvider = mock(MockActivityRecognitionProvider.class);
    activityUpdatedListener = mock(ActivityUpdatedListener.class);
  }

  @Test
  public void test_activity_recognition_control_init() {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    PromiseLocation promiseLocation =
        new PromiseLocation.Builder(context).preInitialize(false).build();
    PromiseLocation.ActivityRecognitionControl activityRecognitionControl =
        promiseLocation.activity(mockProvider);

    verifyZeroInteractions(mockProvider);

    promiseLocation = new PromiseLocation.Builder(context).build();
    activityRecognitionControl = promiseLocation.activity(mockProvider);
    verify(mockProvider).init(eq(context), any(Logger.class));
  }

  @Test
  public void test_activity_recognition_control_start_defaults() {
    PromiseLocation.ActivityRecognitionControl activityRecognitionControl =
        createActivityRecognitionControl();

    activityRecognitionControl.start(activityUpdatedListener);
    verify(mockProvider).start(activityUpdatedListener, DEFAULT_PARAMS);
  }

  @Test
  public void test_activity_recognition_get_last_location() {
    PromiseLocation.ActivityRecognitionControl activityRecognitionControl =
        createActivityRecognitionControl();
    activityRecognitionControl.getLastActivity();

    verify(mockProvider).getLastActivity();
  }

  @Test
  public void test_activity_recognition_stop() {
    PromiseLocation.ActivityRecognitionControl activityRecognitionControl =
        createActivityRecognitionControl();
    activityRecognitionControl.stop();

    verify(mockProvider).stop();
  }

  private PromiseLocation.ActivityRecognitionControl createActivityRecognitionControl() {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    PromiseLocation promiseLocation =
        new PromiseLocation.Builder(context).preInitialize(false).build();
    PromiseLocation.ActivityRecognitionControl activityRecognitionControl =
        promiseLocation.activity(mockProvider);
    return activityRecognitionControl;
  }
}
