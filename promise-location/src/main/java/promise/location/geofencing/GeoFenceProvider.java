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

import android.content.Context;

import java.util.List;

import promise.location.Logger;

public interface GeoFenceProvider {
  void init(Context context, Logger logger);

  void start(GeoFencingTransitionListener listener);

  void addGeoFence(GeoFenceModel geofence);

  void addGeoFences(List<GeoFenceModel> geofenceList);

  void removeGeoFence(String geofenceId);

  void removeGeoFences(List<String> geofenceIds);

  void stop();
}
