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

package promise.location.activity.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import promise.dev4vin.promiselocation.PromiseLocationRobolectricTestRunner;
import promise.location.activity.ActivityParams;


@RunWith(PromiseLocationRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityParamsTest {

  private static final long INTERVAL = 1000;

  @Test
  public void test_activity_params_builder() {
    ActivityParams activityParams = new ActivityParams.Builder().setInterval(INTERVAL).build();

    Assert.assertEquals(activityParams.getInterval(), INTERVAL);
  }
}
