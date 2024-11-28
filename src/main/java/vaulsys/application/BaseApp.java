package vaulsys.application;

import vaulsys.base.ApplicationInterface;

public abstract class BaseApp implements ApplicationInterface {

    public abstract void run();

    public abstract void shutdown();

    public abstract void startup();

    public BaseApp() {
        Thread sd = new ShutdownInterceptor(this);
        Runtime.getRuntime().addShutdownHook(sd);
    }
}
