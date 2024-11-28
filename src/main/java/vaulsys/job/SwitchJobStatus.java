package vaulsys.job;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SwitchJobStatus implements IEnum {
    private static final byte NOT_STARTED_VALUE = 1;
    private static final byte RUNNING_VALUE = 2;
    private static final byte PAUSING_VALUE = 3;
    private static final byte PAUSED_VALUE = 4;
    private static final byte PAUSE_FAILED_VALUE = 5;
    private static final byte FINISHED_VALUE = 6;
    private static final byte FAILED_VALUE = 7;
    private static final byte UNKNOWN_VALUE = 8;
    private static final byte RESUMING_VALUE = 9;
    private static final byte RESUME_FAILED_VALUE = 10;
    private static final byte DELETING_VALUE = 11;

    public static final SwitchJobStatus NOT_STARTED = new SwitchJobStatus(NOT_STARTED_VALUE);
    public static final SwitchJobStatus RUNNING = new SwitchJobStatus(RUNNING_VALUE);
    public static final SwitchJobStatus PAUSING = new SwitchJobStatus(PAUSING_VALUE);
    public static final SwitchJobStatus PAUSED = new SwitchJobStatus(PAUSED_VALUE);
    public static final SwitchJobStatus PAUSE_FAILED = new SwitchJobStatus(PAUSE_FAILED_VALUE);
    public static final SwitchJobStatus FINISHED = new SwitchJobStatus(FINISHED_VALUE);
    public static final SwitchJobStatus FAILED = new SwitchJobStatus(FAILED_VALUE);
    public static final SwitchJobStatus UNKNOWN = new SwitchJobStatus(UNKNOWN_VALUE);
    public static final SwitchJobStatus RESUMING = new SwitchJobStatus(RESUMING_VALUE);
    public static final SwitchJobStatus RESUME_FAILED = new SwitchJobStatus(RESUME_FAILED_VALUE);
    public static final SwitchJobStatus DELETING = new SwitchJobStatus(DELETING_VALUE);

    private byte status;

    public SwitchJobStatus() {
    }

    public SwitchJobStatus(byte status) {
        this.status = status;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SwitchJobStatus that = (SwitchJobStatus) o;

        if (status != that.status) return false;

        return true;
    }

    public int hashCode() {
        return (int) status;
    }
}
