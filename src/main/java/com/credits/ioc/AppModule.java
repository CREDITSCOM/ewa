package com.credits.ioc;

import com.credits.ApplicationProperties;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.service.LevelDbService;
import com.credits.leveldb.client.service.LevelDbServiceImpl;
import com.credits.service.db.leveldb.LevelDbInteractionService;
import com.credits.service.db.leveldb.LevelDbInteractionServiceThriftImpl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.credits.ioc.Injector.INJECTOR;

@Module
public class AppModule {

    @Inject
    ApplicationProperties properties;

    public AppModule(){
        INJECTOR.component.inject(this);
    }

    @Singleton
    @Provides
    LevelDbInteractionService provideLevelDbInteractionService() {
        return new LevelDbInteractionServiceThriftImpl();
    }


    @Singleton
    @Provides
    LevelDbService provideLevelDbService(ApplicationProperties properties) {
        return LevelDbServiceImpl.getInstance(properties.apiHost, properties.apiPort);
    }

    @Singleton
    @Provides
    ApiClient provideApiClient(ApplicationProperties properties) {
        return ApiClient.getInstance(properties.apiHost, properties.apiPort);
    }

    @Singleton
    @Provides
    ApplicationProperties provideProperties() {
        return new ApplicationProperties();
    }
}
