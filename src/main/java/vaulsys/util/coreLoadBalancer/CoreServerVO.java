package vaulsys.util.coreLoadBalancer;


import com.fanap.cms.exception.BusinessException;
import com.ghasemkiani.util.icu.PersianCalendar;

import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreServerVO extends CoreServer {
    private Integer currentCount;

    public CoreServerVO(CoreServer server) {
        super();
        this.setUrl(server.getUrl());
        this.setWeight(server.getWeight());
        this.setReservedURL(server.getReservedURL());
        this.setReservedWeight(server.getReservedWeight());
        this.setEnabled(server.getEnabled());
        this.setUsageType(server.getUsageType());
        this.setDownTimes(server.getDownTimes());
        this.currentCount = 0;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public String getURLForUse() throws BusinessException {
        if (isDown()) {
            if (this.getReservedURL() != null) {
                if (currentCount.compareTo(this.getReservedWeight()) < 0) {
                    currentCount++;
                    return this.getReservedURL();
                } else {
                    currentCount = 0;
                    return null;
                }
            } else {
                currentCount = 0;
                return null;
            }
        } else {
            if (currentCount.compareTo(this.getWeight()) < 0) {
                currentCount++;
                return this.getUrl();
            } else {
                currentCount = 0;
                return null;
            }
        }
    }

    public Boolean isDown() throws BusinessException {
        final String nowTime = getTimeOnly(getCurrentDate());
        for (TimeRange timeRange : (Set<TimeRange>) this.getDownTimes()) {
            if (timeRange.getStartTime() != null) {
                if (timeRange.getEndTime() != null) {
                    if (nowTime.compareTo(timeRange.getStartTime()) >= 0 && nowTime.compareTo(timeRange.getEndTime()) <= 0)
                        return true;
                } else {
                    if (nowTime.compareTo(timeRange.getStartTime()) >= 0)
                        return true;
                }
            } else if (timeRange.getEndTime() != null) {
                if (nowTime.compareTo(timeRange.getEndTime()) <= 0)
                    return true;
            }
        }

        return false;
    }

    private static Date simulationDate = null;

    public static Date getCurrentDate() {
        if (simulationDate == null)
            return new Date();
        else
            return simulationDate;
    }

    public static String getTimeOnly(Date date) throws BusinessException {
        if (date == null)
            return null;

        PersianCalendar pc = new PersianCalendar(date);
        String sTime = getStandardTimeFormatNormal((pc.get(PersianCalendar.HOUR_OF_DAY) >= 10 ? String.valueOf(pc.get(PersianCalendar.HOUR_OF_DAY)) : ("0" + pc.get(PersianCalendar.HOUR_OF_DAY)))
                + ":" + (pc.get(PersianCalendar.MINUTE) >= 10 ? String.valueOf(pc.get(PersianCalendar.MINUTE)) : ("0" + pc.get(PersianCalendar.MINUTE)))
                + ":" + (pc.get(PersianCalendar.SECOND) >= 10 ? String.valueOf(pc.get(PersianCalendar.SECOND)) : ("0" + pc.get(PersianCalendar.SECOND))));
        return new StringBuilder().append(sTime).toString();
    }

    private static String getStandardTimeFormatNormal(String time) throws BusinessException {
        try {
            MillTimeBean tempTime = new MillTimeBean(time);
            StringBuilder sb = new StringBuilder();
            return sb.append((tempTime.getHour() >= 10) ? String.valueOf(tempTime.getHour()) : ("0" + tempTime.getHour()))
                    .append(":")
                    .append((tempTime.getMinute() >= 10) ? String.valueOf(tempTime.getMinute()) : ("0" + tempTime.getMinute()))
                    .append(":")
                    .append((tempTime.getSecond() >= 10) ? String.valueOf(tempTime.getSecond()) : ("0" + tempTime.getSecond())).toString();
        } catch (TimeFormatException e) {
            throw new BusinessException(e);
        }
    }

    public static class MillTimeBean {
        int hour;
        int minute;
        int second;
        int millSec;

        public MillTimeBean(int hour, int minute, int second, int millSec) throws TimeFormatException {
            validate(hour, minute, second, millSec);
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.millSec = millSec;
        }

        public MillTimeBean(String time) throws TimeFormatException {
            Pattern pattern = Pattern.compile("([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]\\.[0-9][0-9][0-9]");
            Matcher matcher = pattern.matcher(time);
            if (time.indexOf(".") != time.lastIndexOf(".")) //Legacy pattern.
                initNonStandard(time);
            else if (matcher.matches())
                initStandard(time);
            else if (isStandardMillTimeMatch(time)) {
                initStandard(time);
            } else {
            }
        }

        public boolean isStandardMillTimeMatch(String time) {
            Pattern pattern = Pattern.compile("([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
            Matcher matcher = pattern.matcher(time);
            return matcher.matches();
        }

        private void initStandard(String time) throws TimeFormatException {
            StringTokenizer timeTokenizer = new StringTokenizer(time, ":");
            String hour;
            String minute;
            String second;
            String millSec;
            if (timeTokenizer.hasMoreTokens())
                hour = timeTokenizer.nextToken();
            else
                throw new TimeFormatException(getTimeErrorMessage(time));
            if (timeTokenizer.hasMoreTokens()) {
                minute = timeTokenizer.nextToken();
            } else
                throw new TimeFormatException(getTimeErrorMessage(time));
            if (timeTokenizer.hasMoreTokens()) {
                second = timeTokenizer.nextToken();
                timeTokenizer = new StringTokenizer(second, "\\.");
                String temp;
                if (timeTokenizer.hasMoreTokens())
                    temp = timeTokenizer.nextToken();
                else
                    throw new TimeFormatException(getTimeErrorMessage(time));
                second = temp;
                if (timeTokenizer.hasMoreTokens())
                    millSec = timeTokenizer.nextToken();
                else
                    millSec = "0";
            } else {
                second = "0";
                millSec = "0";
            }
            try {
                int h = Integer.parseInt(hour);
                int m = Integer.parseInt(minute);
                int s = Integer.parseInt(second);
                int ms = Integer.parseInt(millSec);
                validate(h, m, s, ms);
                setHour(h);
                setMinute(m);
                setSecond(s);
                setMillSec(ms);
            } catch (NumberFormatException e) {
                throw new TimeFormatException("Cannot Parse Strings: Hour=" + hour + ", Min=" + minute + ", Second=" + second + ", millSecond=" + millSec, e);
            }
        }

        private void initNonStandard(String time) throws TimeFormatException {
            StringTokenizer timeTokenizer = new StringTokenizer(time, ":");
            String hour;
            String minute;
            String second;
            String millSec;
            if (timeTokenizer.hasMoreTokens())
                hour = timeTokenizer.nextToken();
            else
                throw new TimeFormatException(getTimeErrorMessage(time));

            if (timeTokenizer.hasMoreTokens()) {
                String temp = timeTokenizer.nextToken();
                timeTokenizer = new StringTokenizer(temp, "\\.");

                minute = timeTokenizer.nextToken();

                if (timeTokenizer.hasMoreTokens())
                    second = timeTokenizer.nextToken();
                else
                    throw new TimeFormatException(getTimeErrorMessage(time));

                if (timeTokenizer.hasMoreTokens())
                    millSec = timeTokenizer.nextToken();
                else
                    millSec = "0";

            } else {
                minute = "0";
                second = "0";
                millSec = "0";
            }
            try {
                int h = Integer.parseInt(hour);
                int m = Integer.parseInt(minute);
                int s = Integer.parseInt(second);
                int ms = Integer.parseInt(millSec);
                validate(h, m, s, ms);
                setHour(h);
                setMinute(m);
                setSecond(s);
                setMillSec(ms);
            } catch (NumberFormatException e) {
                throw new TimeFormatException(e.getMessage(), e);
            }
        }


        String getTimeErrorMessage(String time) {
            return "فرمت زمان ورودي ( " + time + " ) نامعتبر است.";
        }

        void validate(int hour, int minute, int second, int millSec) throws TimeFormatException {
            if (hour < 0 || hour > 23) {
                throw new TimeFormatException("Invalid hour: " + hour);
            }
            if (minute < 0 || minute > 59) {
                throw new TimeFormatException("Invalid minute: " + minute);
            }
            if (second < 0 || second > 59) {
                throw new TimeFormatException("Invalid second: " + second);
            }
            if (millSec < 0 || millSec > 999) {
                throw new TimeFormatException("Invalid millisecond: " + millSec);
            }
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public int getMillSec() {
            return millSec;
        }

        public void setMillSec(int millSec) {
            //We don't need millisecond.
            this.millSec = 0;
//			this.millSec = millSec;
        }
    }

}
