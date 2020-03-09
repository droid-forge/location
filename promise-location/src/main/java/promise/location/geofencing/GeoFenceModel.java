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

package promise.location.geofencing;

import com.google.android.gms.location.Geofence;

public class GeoFenceModel {
  private String requestId;
  private double latitude;
  private double longitude;
  private float radius;
  private long expiration;
  private int transition;
  private int loiteringDelay;

  private GeoFenceModel(
      String id,
      double latitude,
      double longitude,
      float radius,
      long expiration,
      int transition,
      int loiteringDelay) {
    this.requestId = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.radius = radius;
    this.expiration = expiration;
    this.transition = transition;
    this.loiteringDelay = loiteringDelay;
  }

  public String getRequestId() {
    return requestId;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public float getRadius() {
    return radius;
  }

  public long getExpiration() {
    return expiration;
  }

  public int getTransition() {
    return transition;
  }

  public int getLoiteringDelay() {
    return loiteringDelay;
  }

  public Geofence toGeoFence() {
    return new Geofence.Builder()
        .setCircularRegion(latitude, longitude, radius)
        .setExpirationDuration(expiration)
        .setRequestId(requestId)
        .setTransitionTypes(transition)
        .setLoiteringDelay(loiteringDelay)
        .build();
  }

  public static class Builder {
    private String requestId;
    private double latitude;
    private double longitude;
    private float radius;
    private long expiration;
    private int transition;
    private int loiteringDelay;

    public Builder(String id) {
      this.requestId = id;
    }

    public Builder setLatitude(double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder setLongitude(double longitude) {
      this.longitude = longitude;
      return this;
    }

    public Builder setRadius(float radius) {
      this.radius = radius;
      return this;
    }

    public Builder setExpiration(long expiration) {
      this.expiration = expiration;
      return this;
    }

    public Builder setTransition(int transition) {
      this.transition = transition;
      return this;
    }

    public Builder setLoiteringDelay(int loiteringDelay) {
      this.loiteringDelay = loiteringDelay;
      return this;
    }

    public GeoFenceModel build() {
      return new GeoFenceModel(
          requestId, latitude, longitude, radius, expiration, transition, loiteringDelay);
    }
  }
}
