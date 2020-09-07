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
package org.commonjava.o11yphant.metrics.jaxrs;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.commonjava.o11yphant.metrics.conf.MetricsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CodahalePrometheusDeploymentProvider implements PrometheusDeploymentProvider
{
    private static final String PROMETHEUS_REPORTER = "prometheus";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private MetricsConfig config;

    @Inject
    private MetricRegistry codahaleMetricRegistry;

    @Override
    public DeploymentInfo getDeploymentInfo( String contextRoot )
    {
        if ( !config.isEnabled() || !config.getReporter().contains( PROMETHEUS_REPORTER ) )
        {
            return null;
        }

        CollectorRegistry.defaultRegistry.register( new DropwizardExports( codahaleMetricRegistry, new PrometheusSampleBuilder( config.getNodePrefix() ) ) );

        final ServletInfo servlet =
                        Servlets.servlet( "prometheus-metrics", MetricsServlet.class ).addMapping( "/metrics" );

        final DeploymentInfo di = new DeploymentInfo().addListener(
                        Servlets.listener( CodahaleHealthCheckServletContextListener.class ) )
                                                      .setContextPath( contextRoot )
                                                      .addServlet( servlet )
                                                      .setDeploymentName( "Prometheus Metrics Deployment" )
                                                      .setClassLoader( ClassLoader.getSystemClassLoader() );

        logger.info( "Returning deployment info for Prometheus metrics servlet" );
        return di;
    }
}
