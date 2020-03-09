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

import promise.commons.data.log.LogUtil;

class LoggerFactory {

  static Logger buildLogger(boolean loggingEnabled) {
    return loggingEnabled ? new Blabber() : new Sssht();
  }

  private static class Sssht implements Logger {

    @Override
    public void v(String message, Object... args) {
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
    }

    @Override
    public void d(String message, Object... args) {
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
    }

    @Override
    public void i(String message, Object... args) {
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
    }

    @Override
    public void w(String message, Object... args) {
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
    }

    @Override
    public void e(String message, Object... args) {
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
    }
  }

  private static class Blabber implements Logger {

    private String getTag() {
      return new Exception().getStackTrace()[3].getMethodName();
    }

    private String formatMessage(String message, Object... args) {
      return args.length == 0 ? message : String.format(message, args);
    }

    @Override
    public void v(String message, Object... args) {

      LogUtil.d(getTag(), formatMessage(message, args));
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
      LogUtil.d(getTag(), formatMessage(message, args), t);
    }

    @Override
    public void d(String message, Object... args) {
      LogUtil.d(getTag(), formatMessage(message, args));
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
      LogUtil.d(getTag(), formatMessage(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
      LogUtil.i(getTag(), formatMessage(message, args));
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
      LogUtil.i(getTag(), formatMessage(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
      LogUtil.w(getTag(), formatMessage(message, args));
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
      LogUtil.w(getTag(), formatMessage(message, args), t);
    }

    @Override
    public void e(String message, Object... args) {
      LogUtil.e(getTag(), formatMessage(message, args));
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
      LogUtil.e(getTag(), formatMessage(message, args), t);
    }
  }
}
