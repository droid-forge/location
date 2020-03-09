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

public class LocationParams {
  // Defaults
  public static final LocationParams NAVIGATION =
      new Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(0).setInterval(500).build();
  public static final LocationParams BEST_EFFORT =
      new Builder().setAccuracy(LocationAccuracy.MEDIUM).setDistance(150).setInterval(2500).build();
  public static final LocationParams LAZY =
      new Builder().setAccuracy(LocationAccuracy.LOW).setDistance(500).setInterval(5000).build();

  private long interval;
  private float distance;
  private LocationAccuracy accuracy;

  LocationParams(LocationAccuracy accuracy, long interval, float distance) {
    this.interval = interval;
    this.distance = distance;
    this.accuracy = accuracy;
  }

  public long getInterval() {
    return interval;
  }

  public float getDistance() {
    return distance;
  }

  public LocationAccuracy getAccuracy() {
    return accuracy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LocationParams)) return false;

    LocationParams that = (LocationParams) o;

    return Float.compare(that.distance, distance) == 0
        && interval == that.interval
        && accuracy == that.accuracy;
  }

  @Override
  public int hashCode() {
    int result = (int) (interval ^ (interval >>> 32));
    result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
    result = 31 * result + accuracy.hashCode();
    return result;
  }

  public static class Builder {
    private LocationAccuracy accuracy;
    private long interval;
    private float distance;

    public Builder setAccuracy(LocationAccuracy accuracy) {
      this.accuracy = accuracy;
      return this;
    }

    public Builder setInterval(long interval) {
      this.interval = interval;
      return this;
    }

    public Builder setDistance(float distance) {
      this.distance = distance;
      return this;
    }

    public LocationParams build() {
      return new LocationParams(accuracy, interval, distance);
    }
  }
}
