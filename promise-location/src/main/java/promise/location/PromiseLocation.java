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
import androidx.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import promise.location.activity.ActivityParams;
import promise.location.activity.ActivityProvider;
import promise.location.activity.ActivityUpdatedListener;
import promise.location.activity.GooglePlayServicesActivityProvider;
import promise.location.geocoding.AndroidGeoCodingProvider;
import promise.location.geocoding.GeoCodingListener;
import promise.location.geocoding.GeoCodingProvider;
import promise.location.geocoding.ReverseGeoCodingListener;
import promise.location.geofencing.GeoFenceModel;
import promise.location.geofencing.GeoFenceProvider;
import promise.location.geofencing.GeoFencingTransitionListener;
import promise.location.geofencing.GooglePlayServicesGeoFenceProvider;

public class PromiseLocation {

  private Context context;
  private Logger logger;
  private boolean preInitialize;

  private PromiseLocation(Context context, Logger logger, boolean preInitialize) {
    this.context = context;
    this.logger = logger;
    this.preInitialize = preInitialize;
  }

  public static PromiseLocation with(Context context) {
    return new Builder(context).build();
  }

  public LocationControl location() {
    return location(new GooglePlayServicesWithFallbackLocationProvider(context));
  }

  public LocationControl location(LocationProvider provider) {
    return new LocationControl(this, provider);
  }

  @Deprecated
  public ActivityRecognitionControl activityRecognition() {
    return activity();
  }

  public ActivityRecognitionControl activity() {
    return activity(new GooglePlayServicesActivityProvider());
  }

  public ActivityRecognitionControl activity(ActivityProvider activityProvider) {
    return new ActivityRecognitionControl(this, activityProvider);
  }

  public GeofencingControl geofencing() {
    return geofencing(new GooglePlayServicesGeoFenceProvider());
  }

  public GeofencingControl geofencing(GeoFenceProvider geoFenceProvider) {
    return new GeofencingControl(this, geoFenceProvider);
  }

  public GeocodingControl geocoding() {
    return geocoding(new AndroidGeoCodingProvider());
  }

  public GeocodingControl geocoding(GeoCodingProvider geocodingProvider) {
    return new GeocodingControl(this, geocodingProvider);
  }

  public static class Builder {
    private final Context context;
    private boolean loggingEnabled;
    private boolean preInitialize;

    public Builder(@NonNull Context context) {
      this.context = context;
      this.loggingEnabled = false;
      this.preInitialize = true;
    }

    public Builder logging(boolean enabled) {
      this.loggingEnabled = enabled;
      return this;
    }

    public Builder preInitialize(boolean enabled) {
      this.preInitialize = enabled;
      return this;
    }

    public PromiseLocation build() {
      return new PromiseLocation(context, LoggerFactory.buildLogger(loggingEnabled), preInitialize);
    }
  }

  public static class LocationControl {

    private static final Map<Context, LocationProvider> MAPPING = new WeakHashMap<>();

    private final PromiseLocation promiseLocation;
    private LocationParams params;
    private LocationProvider provider;
    private boolean once;

    public LocationControl(
        @NonNull PromiseLocation promiseLocation, @NonNull LocationProvider locationProvider) {
      this.promiseLocation = promiseLocation;
      params = LocationParams.BEST_EFFORT;
      once = false;

      if (!MAPPING.containsKey(promiseLocation.context))
        MAPPING.put(promiseLocation.context, locationProvider);
      provider = MAPPING.get(promiseLocation.context);

      if (promiseLocation.preInitialize)
        provider.init(promiseLocation.context, promiseLocation.logger);
    }

    public LocationControl config(@NonNull LocationParams params) {
      this.params = params;
      return this;
    }

    public LocationControl once() {
      this.once = true;
      return this;
    }

    public LocationControl continuous() {
      this.once = false;
      return this;
    }

    public LocationState state() {
      return LocationState.with(promiseLocation.context);
    }

    @Nullable
    public Location getLastLocation() {
      return provider.getLastLocation();
    }

    public LocationControl get() {
      return this;
    }

    public void start(LocationUpdatedListener listener) {
      if (provider == null) throw new RuntimeException("A provider must be initialized");
      provider.start(listener, params, once);
    }

    public void stop() {
      provider.stop();
    }
  }

  public static class GeocodingControl {

    private static final Map<Context, GeoCodingProvider> MAPPING = new WeakHashMap<>();

    private final PromiseLocation promiseLocation;
    private GeoCodingProvider provider;
    private boolean directAdded = false;
    private boolean reverseAdded = false;

    public GeocodingControl(
        @NonNull PromiseLocation promiseLocation, @NonNull GeoCodingProvider geocodingProvider) {
      this.promiseLocation = promiseLocation;

      if (!MAPPING.containsKey(promiseLocation.context)) {
        MAPPING.put(promiseLocation.context, geocodingProvider);
      }
      provider = MAPPING.get(promiseLocation.context);

      if (promiseLocation.preInitialize) {
        provider.init(promiseLocation.context, promiseLocation.logger);
      }
    }

    public GeocodingControl get() {
      return this;
    }

    public void reverse(
        @NonNull Location location, @NonNull ReverseGeoCodingListener reverseGeocodingListener) {
      add(location);
      start(reverseGeocodingListener);
    }

    public void direct(@NonNull String name, @NonNull GeoCodingListener geocodingListener) {
      add(name);
      start(geocodingListener);
    }

    public GeocodingControl add(@NonNull Location location) {
      reverseAdded = true;
      provider.addLocation(location, 1);
      return this;
    }

    public GeocodingControl add(@NonNull Location location, int maxResults) {
      reverseAdded = true;
      provider.addLocation(location, maxResults);
      return this;
    }

    public GeocodingControl add(@NonNull String name) {
      directAdded = true;
      provider.addName(name, 1);
      return this;
    }

    public GeocodingControl add(@NonNull String name, int maxResults) {
      directAdded = true;
      provider.addName(name, maxResults);
      return this;
    }

    public void start(GeoCodingListener geocodingListener) {
      start(geocodingListener, null);
    }

    public void start(ReverseGeoCodingListener reverseGeocodingListener) {
      start(null, reverseGeocodingListener);
    }

    public void start(
        GeoCodingListener geocodingListener,
        ReverseGeoCodingListener reverseGeocodingListener) {
      if (provider == null) {
        throw new RuntimeException("A provider must be initialized");
      }
      if (directAdded && geocodingListener == null) {
        promiseLocation.logger.w(
            "Some places were added for geocoding but the listener was not specified!");
      }
      if (reverseAdded && reverseGeocodingListener == null) {
        promiseLocation.logger.w(
            "Some places were added for reverse geocoding but the listener was not specified!");
      }

      provider.start(geocodingListener, reverseGeocodingListener);
    }

    public void stop() {
      provider.stop();
    }
  }

  public static class ActivityRecognitionControl {
    private static final Map<Context, ActivityProvider> MAPPING = new WeakHashMap<>();

    private final PromiseLocation promiseLocation;
    private ActivityParams params;
    private ActivityProvider provider;

    public ActivityRecognitionControl(
        @NonNull PromiseLocation promiseLocation, @NonNull ActivityProvider activityProvider) {
      this.promiseLocation = promiseLocation;
      params = ActivityParams.NORMAL;

      if (!MAPPING.containsKey(promiseLocation.context))
        MAPPING.put(promiseLocation.context, activityProvider);
      provider = MAPPING.get(promiseLocation.context);

      if (promiseLocation.preInitialize)
        provider.init(promiseLocation.context, promiseLocation.logger);
    }

    public ActivityRecognitionControl config(@NonNull ActivityParams params) {
      this.params = params;
      return this;
    }

    @Nullable
    public DetectedActivity getLastActivity() {
      return provider.getLastActivity();
    }

    public ActivityRecognitionControl get() {
      return this;
    }

    public void start(ActivityUpdatedListener listener) {
      if (provider == null) {
        throw new RuntimeException("A provider must be initialized");
      }
      provider.start(listener, params);
    }

    public void stop() {
      provider.stop();
    }
  }

  public static class GeofencingControl {
    private static final Map<Context, GeoFenceProvider> MAPPING = new WeakHashMap<>();

    private final PromiseLocation promiseLocation;
    private GeoFenceProvider provider;

    public GeofencingControl(
        @NonNull PromiseLocation promiseLocation, @NonNull GeoFenceProvider geoFenceProvider) {
      this.promiseLocation = promiseLocation;

      if (!MAPPING.containsKey(promiseLocation.context)) {
        MAPPING.put(promiseLocation.context, geoFenceProvider);
      }
      provider = MAPPING.get(promiseLocation.context);

      if (promiseLocation.preInitialize) {
        provider.init(promiseLocation.context, promiseLocation.logger);
      }
    }

    public GeofencingControl add(@NonNull GeoFenceModel geofenceModel) {
      provider.addGeoFence(geofenceModel);
      return this;
    }

    public GeofencingControl remove(@NonNull String geofenceId) {
      provider.removeGeoFence(geofenceId);
      return this;
    }

    public GeofencingControl addAll(@NonNull List<GeoFenceModel> geoFenceModelList) {
      provider.addGeoFences(geoFenceModelList);
      return this;
    }

    public GeofencingControl removeAll(@NonNull List<String> geofenceIdsList) {
      provider.removeGeoFences(geofenceIdsList);
      return this;
    }

    public void start(GeoFencingTransitionListener listener) {
      if (provider == null) {
        throw new RuntimeException("A provider must be initialized");
      }
      provider.start(listener);
    }

    public void stop() {
      provider.stop();
    }
  }
}
