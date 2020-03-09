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

/**
 * An extension of the {@link LocationProvider} interface for location providers that utilize 3rd
 * party services. Implementations must invoke the appropriate {@link ServiceConnectionListener}
 * events when the connection to the 3rd party service succeeds, fails, or is suspended.
 */
public interface ServiceLocationProvider extends LocationProvider {

  /**
   * Gets the {@link ServiceConnectionListener} callback for this location provider.
   */
  ServiceConnectionListener getServiceListener();

  /**
   * Set the {@link ServiceConnectionListener} used for callbacks from the 3rd party service.
   *
   * @param listener a <code>ServiceConnectionListener</code> to respond to connection events from
   *                 the underlying 3rd party location service.
   */
  void setServiceListener(ServiceConnectionListener listener);
}
