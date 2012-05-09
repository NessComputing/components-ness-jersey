package ness.jersey.filter;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import ness.jersey.util.MaxSizeInputStream;

import org.apache.commons.lang3.ObjectUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nesscomputing.config.Config;
import com.nesscomputing.logging.Log;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

/**
 * A {@link ResourceFilterFactory} which discovers all resource methods.  If they take
 * an input stream, they will wrap it in {@link MaxSizeInputStream} with the configured
 * size.  Configuration keys, with the highest priority last:
 * <table border=1>
 * <tr><th>Key</th><th>Default Value</th><th>Purpose</th></tr>
 * <tr><td>ness.filter.max-body-size</td><td>1MB</td><td>Global restriction / default</td></tr>
 * <tr><td>ness.filter.max-body-size.<i>full-class-name</i></td><td>-</td><td>Restricts all resource methods in the class</td></tr>
 * <tr><td>ness.filter.max-body-size.<i>full-class-name</i>.<i>method-name</i></td><td>Restricts a particular method</td></tr>
 * </table>
 */
@Singleton
public class BodySizeLimitResourceFilterFactory implements ResourceFilterFactory {

    private static final Log LOG = Log.findLog();

    private final Config config;
    private final long defaultSizeLimit;

    @Inject
    BodySizeLimitResourceFilterFactory(Config config) {
        this.config = config;
        NessJerseyFiltersConfig filterConfig = config.getBean(NessJerseyFiltersConfig.class);

        defaultSizeLimit = filterConfig.getMaxBodySize();

    }

    private final BodySizeLimit defaultLimit = new BodySizeLimit() {
        @Override
        public long value() {
            return defaultSizeLimit;
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return BodySizeLimit.class;
        }
    };

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        BodySizeLimit annotation = ObjectUtils.firstNonNull(
                am.getAnnotation(BodySizeLimit.class),
                am.getResource().getAnnotation(BodySizeLimit.class),
                defaultLimit);

        long value = annotation.value();
        if (annotation != defaultLimit) {
            LOG.debug("Found annotation for size %d on %s", value, am);
        } else {
            LOG.debug("No annotation found, default size %d used on %s", value, am);
        }

        String configKey = "ness.filter.max-body-size." + am.getResource().getResourceClass().getName();
        Integer configOverride = config.getConfiguration().getInteger(configKey, null);

        if (configOverride != null) {
            LOG.debug("  ...But it was overridden to %d on the class!", configOverride);
            value = configOverride;
        }

        configKey = configKey + "." + am.getMethod().getName();
        configOverride = config.getConfiguration().getInteger(configKey, null);

        if (configOverride != null) {
            LOG.debug("  ...But it was overridden to %d on the method!", configOverride);
            value = configOverride;
        }

        if (value < 0) {
            return Collections.emptyList();
        }

        return Collections.<ResourceFilter>singletonList(new Filter(value));
    }

    private static class Filter implements ResourceFilter, ContainerRequestFilter {
        private final long maxSize;

        public Filter(long maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null;
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            InputStream entityInputStream = request.getEntityInputStream();
            if (entityInputStream != null) {
                request.setEntityInputStream(new MaxSizeInputStream(entityInputStream, maxSize));
            }
            return request;
        }
    }
}
