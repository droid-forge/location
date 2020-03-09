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

import androidx.annotation.VisibleForTesting;
import androidx.collection.ArrayMap;

import promise.commons.pref.Preferences;
import promise.location.Store;


public class GeoFenceStore implements Store<GeoFenceModel> {

  private static final String PREFERENCES_FILE = "promise_location_geofence";
  private static final String PREFIX_ID = GeoFenceStore.class.getCanonicalName() + ".key";
  private static final String LATITUDE_ID = "lat";
  private static final String LONGITUDE_ID = "lon";
  private static final String RADIUS_ID = "rad";
  private static final String TRANSITION_ID = "transition";
  private static final String EXPIRATION_ID = "exp";
  private static final String LOITERING_DELAY_ID = "loitering_delay";

  private Preferences preferences;

  public GeoFenceStore() {
    preferences = new Preferences(PREFERENCES_FILE);
  }

  @VisibleForTesting
  public void setPreferences(Preferences preferences) {
    this.preferences = preferences;
  }

  @Override
  public void put(final String id, final GeoFenceModel geofenceModel) {
    preferences.save(new ArrayMap<String, Object>() {{
      put(getFieldKey(id, LATITUDE_ID), Double.doubleToLongBits(geofenceModel.getLatitude()));
      put(getFieldKey(id, LONGITUDE_ID), Double.doubleToLongBits(geofenceModel.getLongitude()));
      put(getFieldKey(id, RADIUS_ID), geofenceModel.getRadius());
      put(getFieldKey(id, TRANSITION_ID), geofenceModel.getTransition());
      put(getFieldKey(id, EXPIRATION_ID), geofenceModel.getExpiration());
      put(getFieldKey(id, LOITERING_DELAY_ID), geofenceModel.getLoiteringDelay());
    }});
  }

  @Override
  public GeoFenceModel get(String id) {
    if (preferences != null) {
      GeoFenceModel.Builder builder = new GeoFenceModel.Builder(id);
      builder.setLatitude(
          Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LATITUDE_ID))));
      builder.setLongitude(
          Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LONGITUDE_ID))));
      builder.setRadius((float) preferences.getDouble(getFieldKey(id, RADIUS_ID)));
      builder.setTransition(preferences.getInt(getFieldKey(id, TRANSITION_ID)));
      builder.setExpiration(preferences.getLong(getFieldKey(id, EXPIRATION_ID)));
      builder.setLoiteringDelay(preferences.getInt(getFieldKey(id, LOITERING_DELAY_ID)));
      return builder.build();
    } else {
      return null;
    }
  }

  @Override
  public void remove(String id) {
    preferences.clear(getFieldKey(id, LATITUDE_ID));
    preferences.clear(getFieldKey(id, LONGITUDE_ID));
    preferences.clear(getFieldKey(id, RADIUS_ID));
    preferences.clear(getFieldKey(id, TRANSITION_ID));
    preferences.clear(getFieldKey(id, EXPIRATION_ID));
    preferences.clear(getFieldKey(id, LOITERING_DELAY_ID));
  }

  private String getFieldKey(String id, String field) {
    return PREFIX_ID + "_" + id + "_" + field;
  }
}
