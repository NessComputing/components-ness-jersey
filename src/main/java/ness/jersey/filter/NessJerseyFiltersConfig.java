package ness.jersey.filter;

import org.skife.config.Config;
import org.skife.config.Default;

public interface NessJerseyFiltersConfig {
    /**
     * Default maximum body size.  May be overridden by the {@link BodySizeLimit} annotation.
     */
    @Config("ness.jersey.filter.max-body-size")
    @Default("1048576") // 1MB
    long getMaxBodySize();
}
