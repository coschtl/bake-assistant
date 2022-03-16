package at.coschtl.bakeassistant.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Day {
    private final DateFormat dateFormat;
    private final Date date;

    public Day(Date date) {
        this.date = date;
        dateFormat = new SimpleDateFormat("E\td. MMMM");
    }

    public static Day getDayRelativeTo(Date date, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, offset);
        return new Day(cal.getTime());
    }

    public Date getDate() {
        return date;
    }

    public String toString() {
        return dateFormat.format(date);
    }
}
