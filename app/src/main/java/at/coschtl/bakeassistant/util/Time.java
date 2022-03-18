package at.coschtl.bakeassistant.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class Time implements Serializable {

    private final DateFormat dateFormat;
    private Date time;

    public Time(Date time) {
        this.time = time;
        dateFormat = new SimpleDateFormat("E HH:mm");
    }

    public static Time of(Date date) {
        return new Time(date);
    }

    public static Time of(Time time) {
        return new Time(time.time);
    }

    public int minute() {
        return cal().get(Calendar.MINUTE);
    }

    public int hour() {
        return cal().get(Calendar.HOUR_OF_DAY);
    }

    public Time setMinute(int minutes) {
        Calendar cal = cal();
        cal.set(Calendar.MINUTE, minutes);
        time = cal.getTime();
        return this;
    }

    public Date date() {
        return time;
    }

    public Time setHour(int hour) {
        Calendar cal = cal();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        time = cal.getTime();
        return this;
    }

    @Override
    public String toString() {
        return dateFormat.format(time);
    }

    private Calendar cal() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        return cal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time1 = (Time) o;
        return time.equals(time1.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }
}
