package hu.blackbelt.jetty.metrics.impl;

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
