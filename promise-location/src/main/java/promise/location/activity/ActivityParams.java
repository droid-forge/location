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

package promise.location.activity;

public class ActivityParams {
  // Defaults
  public static final ActivityParams NORMAL = new Builder().setInterval(500).build();

  private long interval;

  ActivityParams(long interval) {
    this.interval = interval;
  }

  public long getInterval() {
    return interval;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ActivityParams)) return false;
    ActivityParams that = (ActivityParams) o;
    return interval == that.interval;
  }

  @Override
  public int hashCode() {
    return (int) (interval ^ (interval >>> 32));
  }

  public static class Builder {
    private long interval;

    public Builder setInterval(long interval) {
      this.interval = interval;
      return this;
    }

    public ActivityParams build() {
      return new ActivityParams(interval);
    }
  }
}
