/**
 * Copyright (C) 2013 all@code-story.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package net.codestory.http.routes;

import static java.time.ZonedDateTime.*;
import static java.time.format.DateTimeFormatter.*;
import static net.codestory.http.constants.Headers.*;
import static net.codestory.http.constants.Methods.*;
import static net.codestory.http.io.Strings.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import net.codestory.http.exchange.*;
import net.codestory.http.io.*;
import net.codestory.http.misc.*;
import net.codestory.http.payload.*;
import net.codestory.http.types.*;

import org.slf4j.*;

class WebJarsRoute implements Route {
  private final static Logger LOG = LoggerFactory.getLogger(WebJarsRoute.class);

  private final boolean prodMode;
  private final WebJarUrlFinder webJarUrlFinder;

  public WebJarsRoute(boolean prodMode) {
    this.prodMode = prodMode;
    this.webJarUrlFinder = new WebJarUrlFinder(prodMode);
  }

  @Override
  public boolean matchUri(String uri) {
    if (uri.startsWith("/webjars/") && !uri.endsWith("/") && (getResource(uri) != null)) {
      return true;
    }

    if ((!prodMode) && uri.contains("webjar") && uri.endsWith("/")) {
      printKnownWebjars();
    }

    return false;
  }

  private void printKnownWebjars() {
    LOG.info("Found these webjars files:");
    for (String uri : new ClasspathScanner().getResources("META-INF/resources/webjars/")) {
      LOG.info(" + " + substringAfter(uri, "META-INF/resources"));
    }
  }

  @Override
  public boolean matchMethod(String method) {
    return GET.equalsIgnoreCase(method) || HEAD.equalsIgnoreCase(method);
  }

  @Override
  public Object body(Context context) throws IOException {
    String uri = context.uri();

    URL classpathUrl = webJarUrlFinder.url(uri);

    try (InputStream stream = classpathUrl.openStream()) {
      String contentType = ContentTypes.get(Paths.get(uri));
      byte[] data = InputStreams.readBytes(stream);

      return new Payload(contentType, data)
          .withHeader(CACHE_CONTROL, "public, max-age=31536000")
          .withHeader(LAST_MODIFIED, RFC_1123_DATE_TIME.format(now().minusMonths(1L)))
          .withHeader(EXPIRES, RFC_1123_DATE_TIME.format(now().plusWeeks(1L)));
    }
  }

  private static URL getResource(String uri) {
    return Resources.getResource("META-INF/resources" + uri);
  }
}
