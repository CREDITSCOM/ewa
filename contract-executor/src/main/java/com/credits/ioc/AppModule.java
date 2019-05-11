package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.secure.PermissionsManager;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AppModule {

    @Singleton
    @Provides
    public ApplicationProperties provideProperties() {
        return new ApplicationProperties();
    }


    @Singleton
    @Provides
    public PermissionsManager providesPermissionsManager() {
        return new PermissionsManager();
    }
}
