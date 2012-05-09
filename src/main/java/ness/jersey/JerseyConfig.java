package ness.jersey;

import org.skife.config.Config;
import org.skife.config.Default;

/**
 * Enable and/or disable various features of the Jersey servlet.
 */
public abstract class JerseyConfig
{
    @Config("ness.jersey.trace.enabled")
    @Default("false")
    public boolean isTraceEnabled()
    {
        return false;
    }

    @Config("ness.jersey.trace-per-request.enabled")
    @Default("false")
    public boolean isTracePerRequestEnabled()
    {
        return false;
    }

    @Config("ness.jersey.generate-wadl.enabled")
    @Default("true")
    public boolean isGenerateWadlEnabled()
    {
        return true;
    }

    /**
     * Enables gzip. This is usually false, so that the container
     * can handle that.
     */
    @Config("ness.jersey.gzip.enabled")
    @Default("false")
    public boolean isGzipEnabled()
    {
        return false;
    }

    @Config("ness.jersey.logging.enabled")
    @Default("false")
    public boolean isLoggingEnabled()
    {
        return false;
    }
}
