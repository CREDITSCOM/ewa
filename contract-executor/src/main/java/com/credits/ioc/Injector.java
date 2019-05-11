package com.credits.ioc;

public enum Injector {
    INJECTOR;
    public AppComponent component =
        DaggerAppComponent.builder().appModule(new AppModule()).build();

}

