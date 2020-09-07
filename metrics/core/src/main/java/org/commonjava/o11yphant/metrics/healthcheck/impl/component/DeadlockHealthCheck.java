/**
 * Copyright (C) 2020 Red Hat, Inc. (nos-devel@redhat.com)
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
 * limitations under the License.
 */
package org.commonjava.o11yphant.metrics.healthcheck.impl.component;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import org.commonjava.o11yphant.metrics.healthcheck.impl.HealthCheckResult;

import javax.inject.Named;

@Named
public class DeadlockHealthCheck
                extends ComponentHealthCheck
{
    private ThreadDeadlockHealthCheck deadlock = new ThreadDeadlockHealthCheck();

    @Override
    public Result check() throws Exception
    {
        HealthCheck.Result ret = deadlock.execute();
        return new HealthCheckResult( ret.isHealthy() ).withMessage( ret.getMessage() )
                                                       .withThrowable( ret.getError() )
                                                       .withDetails( ret.getDetails() );
    }
}
