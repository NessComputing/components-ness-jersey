package ness.jersey.exceptions;


import com.google.inject.AbstractModule;

public class NessJerseyExceptionMapperModule extends AbstractModule
{
    @Override
    public void configure()
    {
        bind(JsonMessageReaderMapper.class).asEagerSingleton();
        bind(GuiceProvisionExceptionMapper.class).asEagerSingleton();
        bind(ExcessivelySizedHttpBodyExceptionMapper.class).asEagerSingleton();
    }
}
