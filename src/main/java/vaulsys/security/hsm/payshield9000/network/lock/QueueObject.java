package vaulsys.security.hsm.payshield9000.network.lock;

import vaulsys.security.hsm.base.HSMNetworkManager;
import vaulsys.security.hsm.payshield9000.network.exception.NotAvailableHSMChannelFoundException;

public class QueueObject {
    private boolean isNotified = false;
    private boolean shouldBeTransferred=false;
    public long threadId;



    public synchronized void doWait() throws InterruptedException {
        while(!isNotified){
            this.wait();
        }
        this.isNotified = false;
    }

    public synchronized void doNotify() {
        this.isNotified = true;
        this.notify();
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public boolean isShouldBeTransferred() {
        return shouldBeTransferred;
    }

    public void setShouldBeTransferred(boolean shouldBeTransferred) {
        this.shouldBeTransferred = shouldBeTransferred;
    }
}


