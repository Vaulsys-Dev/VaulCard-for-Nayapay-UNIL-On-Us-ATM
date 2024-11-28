package vaulsys.application;

import vaulsys.base.ApplicationInterface;

public class ShutdownInterceptor extends Thread {
    private ApplicationInterface app;

    public ShutdownInterceptor(ApplicationInterface app) {
        this.app = app;
    }

    public void run() {
        app.shutdown();
    }
}
