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

package promise.location;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;

import promise.location.activity.ActivityParams;
import promise.location.activity.ActivityUpdatedListener;
import promise.location.activity.GooglePlayServicesActivityProvider;

public class ActivityLocationProvider
    implements LocationProvider, ActivityUpdatedListener {
  private final GooglePlayServicesActivityProvider activityProvider;
  private final GooglePlayServicesLocationProvider locationProvider;
  private final LocationBasedOnActivityListener locationBasedOnActivityListener;
  private LocationUpdatedListener locationUpdatedListener;
  private LocationParams locationParams;

  public ActivityLocationProvider(
      @NonNull LocationBasedOnActivityListener locationBasedOnActivityListener) {
    activityProvider = new GooglePlayServicesActivityProvider();
    locationProvider = new GooglePlayServicesLocationProvider();
    this.locationBasedOnActivityListener = locationBasedOnActivityListener;
  }

  @Override
  public void init(Context context, Logger logger) {
    locationProvider.init(context, logger);
    activityProvider.init(context, logger);
  }

  @Override
  public void start(
      LocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
    if (singleUpdate) throw new IllegalArgumentException("singleUpdate cannot be set to true");
    locationProvider.start(listener, params, false);
    activityProvider.start(this, ActivityParams.NORMAL);
    this.locationParams = params;
    this.locationUpdatedListener = listener;
  }

  @Override
  public void stop() {
    locationProvider.stop();
    activityProvider.stop();
  }

  @Override
  public Location getLastLocation() {
    return locationProvider.getLastLocation();
  }

  @Override
  public void onActivityUpdated(DetectedActivity detectedActivity) {
    LocationParams params =
        locationBasedOnActivityListener.locationParamsForActivity(detectedActivity);
    if (params != null && locationParams != null && !locationParams.equals(params))
      start(locationUpdatedListener, params, false);
  }

  public interface LocationBasedOnActivityListener {
    LocationParams locationParamsForActivity(DetectedActivity detectedActivity);
  }
}
