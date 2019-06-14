package com.credits.ioc;

public enum Injector {
    INJECTOR;
    public final AppComponent component =
        DaggerAppComponent.builder().appModule(new AppModule()).build();

}

