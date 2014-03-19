/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.gateway.hive;

import org.apache.hadoop.gateway.dispatch.HttpClientDispatch;
import org.apache.hadoop.gateway.security.PrimaryPrincipal;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.auth.BasicScheme;
import javax.security.auth.Subject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.security.AccessController;
import java.security.Principal;

/**
 * This specialized dispatch provides Hive specific features to the
 * default HttpClientDispatch.
 */
public class HiveHttpClientDispatch extends HttpClientDispatch {
  private static final String BASIC_AUTH_PREEMPTIVE_PARAM = "basicAuthPreemptive";
  private static final String PASSWORD_PLACEHOLDER = "*";
  private boolean basicAuthPreemptive = false;

  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {
    super.init( filterConfig );
    String basicAuthPreemptiveString = filterConfig.getInitParameter( BASIC_AUTH_PREEMPTIVE_PARAM );
    if( basicAuthPreemptiveString != null ) {
      setBasicAuthPreemptive( Boolean.parseBoolean( basicAuthPreemptiveString ) );
    }
  }

  protected Principal getPrimaryPrincipal() {
    Principal principal = null;
    Subject subject = Subject.getSubject( AccessController.getContext());
    if( subject != null ) {
      principal = (Principal)subject.getPrincipals(PrimaryPrincipal.class).toArray()[0];
    }
    return principal;
  }

  protected void addCredentialsToRequest(HttpUriRequest request) {
    if( isBasicAuthPreemptive() ) {
      Principal principal = getPrimaryPrincipal();
      if( principal != null ) {

        UsernamePasswordCredentials credentials =
            new UsernamePasswordCredentials( principal.getName(), PASSWORD_PLACEHOLDER );
        
        request.addHeader(BasicScheme.authenticate(credentials,"US-ASCII",false));
      }
    }
  }

  public void setBasicAuthPreemptive( boolean basicAuthPreemptive ) {
    this.basicAuthPreemptive = basicAuthPreemptive;
  }

  public boolean isBasicAuthPreemptive() {
    return basicAuthPreemptive;
  }
}

