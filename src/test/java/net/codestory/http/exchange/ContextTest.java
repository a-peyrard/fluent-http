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
package net.codestory.http.exchange;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;

import net.codestory.http.injection.*;
import net.codestory.http.security.*;

import org.junit.*;

public class ContextTest {
  HttpRequest request = mock(HttpRequest.class);
  HttpResponse response = mock(HttpResponse.class);
  IocAdapter iocAdapter = mock(IocAdapter.class);

  Context context = new Context(request, response, iocAdapter);

  @Test
  public void create_bean() {
    Service expectedService = new Service();
    when(iocAdapter.get(Service.class)).thenReturn(expectedService);

    Service actualService = context.getBean(Service.class);

    assertThat(actualService).isSameAs(expectedService);
  }

  @Test
  public void extract() throws IOException {
    byte[] rawContent = "Content".getBytes();
    User user = mock(User.class);
    Cookies cookies = mock(Cookies.class);
    HttpQuery query = mock(HttpQuery.class);
    context.setCurrentUser(user);
    when(request.inputStream()).thenReturn(new ByteArrayInputStream(rawContent));
    when(request.content()).thenReturn("Content");
    when(request.cookies()).thenReturn(cookies);
    when(request.query()).thenReturn(query);

    assertThat(context.extract(Context.class)).isSameAs(context);
    assertThat(context.extract(HttpRequest.class)).isSameAs(request);
    assertThat(context.extract(HttpResponse.class)).isSameAs(response);
    assertThat(context.extract(User.class)).isSameAs(user);
    assertThat(context.extract(byte[].class)).isEqualTo(rawContent);
    assertThat(context.extract(String.class)).isSameAs("Content");
    assertThat(context.extract(Cookies.class)).isSameAs(cookies);
    assertThat(context.extract(HttpQuery.class)).isSameAs(query);
  }

  static class Service {
  }
}
