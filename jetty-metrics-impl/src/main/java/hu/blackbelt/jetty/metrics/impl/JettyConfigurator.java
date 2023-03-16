package hu.blackbelt.jetty.metrics.impl;

/*-
 * #%L
 * Jetty metrics :: Karaf :: Implementation
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import org.eclipse.jetty.server.Handler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true)
public class JettyConfigurator {

    private MetricRegistry registry;
    private ServiceRegistration<Handler> registerService;

    private Handler handler;

    @Activate
    public void activate(final BundleContext bundleContext) {
        Handler handler = new InstrumentedHandler(registry, "JettyMetrics");
        registerService = bundleContext.registerService(Handler.class, handler, null);
    }

    @Deactivate
    public void deactivate() {
        if (registerService != null) {
            registerService.unregister();
        }
    }

    @Reference(target = "(name=sling)")
    public void setMetricsService(final MetricRegistry registry) {
        this.registry = registry;
    }
}
