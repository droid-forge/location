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

import androidx.annotation.NonNull;

/**
 * A decorator for a {@link ServiceConnectionListener} used to execute the {@link
 * MultiFallBackLocationProvider}'s failover.
 */
class FallbackListenerWrapper implements ServiceConnectionListener {

  private final ServiceConnectionListener listener;
  private final MultiFallBackLocationProvider fallbackProvider;
  private final ServiceLocationProvider childProvider;

  public FallbackListenerWrapper(
      @NonNull MultiFallBackLocationProvider parentProvider, ServiceLocationProvider childProvider) {
    this.fallbackProvider = parentProvider;
    this.childProvider = childProvider;
    this.listener = childProvider.getServiceListener();
  }

  @Override
  public void onConnected() {
    if (listener != null) listener.onConnected();
  }

  @Override
  public void onConnectionSuspended() {
    if (listener != null) listener.onConnectionSuspended();
    runFallback();
  }

  @Override
  public void onConnectionFailed() {
    if (listener != null) listener.onConnectionFailed();
    runFallback();
  }

  private void runFallback() {
    LocationProvider current = fallbackProvider.getCurrentProvider();
    if (current != null && current.equals(childProvider)) fallbackProvider.fallbackProvider();
  }
}
