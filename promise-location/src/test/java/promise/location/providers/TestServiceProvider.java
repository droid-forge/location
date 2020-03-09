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

package promise.location.providers;

import android.content.Context;
import android.location.Location;

import promise.location.LocationUpdatedListener;
import promise.location.ServiceLocationProvider;
import promise.location.LocationParams;
import promise.location.Logger;
import promise.location.ServiceConnectionListener;

/**
 * Test double for a {@link ServiceLocationProvider}.
 *
 */
public class TestServiceProvider implements ServiceLocationProvider {

  private ServiceConnectionListener listener;
  private int initCount;
  private int startCount;
  private int stopCount;
  private int lastLocCount;

  @Override
  public ServiceConnectionListener getServiceListener() {
    return listener;
  }

  @Override
  public void setServiceListener(ServiceConnectionListener listener) {
    this.listener = listener;
  }

  @Override
  public void init(Context context, Logger logger) {
    initCount++;
  }

  @Override
  public void start(
      LocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
    startCount++;
  }

  @Override
  public void stop() {
    stopCount++;
  }

  @Override
  public Location getLastLocation() {
    lastLocCount++;
    return null;
  }

  public int getInitCount() {
    return initCount;
  }

  public int getStartCount() {
    return startCount;
  }

  public int getStopCount() {
    return stopCount;
  }

  public int getLastLocCount() {
    return lastLocCount;
  }

  /**
   * Simulate a service connection failure, and call {@link
   * ServiceConnectionListener#onConnectionFailed()}
   */
  public void simulateFailure() {
    if (listener != null) {
      listener.onConnectionFailed();
    }
  }
}
