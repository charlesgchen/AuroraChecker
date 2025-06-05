package com.example.aurora_checker;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

class AuroraBackend {
    private static final String url = "https://services.swpc.noaa.gov/text/3-day-forecast.txt";
    // private static final String url = "3-day-forecast.txt";
    private String info;
    private String tonight;
    
    AuroraBackend () {
        info = null;
        tonight = null;
    }

    String getinfo () {
        // System.out.format("%s", info);
        return info;
    }

    String gettonight () {
        // System.out.format("%s", tonight);
        return tonight;
    }

    void updateinfo () {
        // downloads the information
        
        try {
            InputStream in = new URI(url).toURL().openStream();
            // InputStream in = Files.newInputStream(Path.of(url).toAbsolutePath());
            byte[] bytes = in.readAllBytes();
            String result = new String(bytes);
            // System.out.format("%s%n" + "End of text%n", result);
            info = result;
        }
        catch (Exception exception) {
            System.out.format("%s\n", exception);
        }
        return;
    }

    ZonedDateTime getDate () {
        ZonedDateTime now = ZonedDateTime.now();
        return now;
    }

    void parseinfo () {
        // turns downloaded info into something useful for string manipulation later
        
        if (info == null) {
            return;
        }
        String str_start = "NOAA Kp index breakdown";
        // String str_end = "B. NOAA Solar Radiation Activity Observation and Forecast";
        String str_end = "Rationale";
        if (info.contains(str_start) == false) {
            return;
        }
        info = info.substring(info.indexOf(str_start));
        info = info.substring(0, info.indexOf(str_end));
        while (info.indexOf("             ") != 0) {
            // System.out.format("the substring is '%s'\n", info.substring(0, 2));
            // System.out.format("%s" + "index of next '  ' is %d%n", info, info.indexOf("  "));
            info = info.substring(1);
        }
        info = info.toUpperCase();
    }

    private int getRow(final ZonedDateTime datetime) {
        // Assume the hour can be checked from first column

        if (info == null) {
            return -1;
        }
        final ZonedDateTime zdt = datetime.withZoneSameInstant(ZoneId.of("Z"));
        final int hour = zdt.getHour();
        final int end = info.length();
        int start = 0;
        String tmp = info.trim();
        int compare = 0;
        int row = -1;
        while (start != -1 && start < end && !(tmp.isEmpty())) {
            tmp = tmp.substring(start + 1);
            // System.out.format("tmp: '%s'\n", tmp);
            row++;
            // System.out.format("row: %d\n", row);
            try {
                compare = Integer.parseInt(tmp.substring(0, 2));
            }
            catch (Exception exception) {
                // do nothing
            }
            if (hour < compare) {
                row--;
                break;
            }
            start = tmp.indexOf('\n');
            // System.out.format("start: %d\n", start);
        }
        return row;
    }

    private int getCol(final ZonedDateTime datetime) {
        // Returns the column in info using the given datetime
        // Assume the info_goal can be found in the first row of info
        
        if (info == null) {
            return -1;
        }
        final ZonedDateTime zdt = datetime.withZoneSameInstant(ZoneId.of("Z"));
        String str_goal = zdt.getMonth().getDisplayName(TextStyle.valueOf("SHORT"), Locale.of("ENGLISH")).substring(0,3) + " ";
        str_goal = str_goal.toUpperCase();
        if (zdt.getDayOfMonth() < 10) {
            str_goal = str_goal + "0";
        }
        str_goal = str_goal + Integer.valueOf(zdt.getDayOfMonth()).toString();
        if (info.indexOf(str_goal) == -1) {
            return -1;
        }
        String header = info.substring(0, info.indexOf('\n')).trim();
        // System.out.format("original header: %s\n", header);
        final String goal = header.substring(header.indexOf(str_goal));
        // System.out.format("goal: %s\n", goal);
        final int end = header.length();
        int col = -1;
        int start = 0;
        while (start != -1 && start < end) {
            header = header.substring(start).trim();
            // System.out.format("header: %s\n", header);
            col++;
            if (header.equals(goal)) {
                // System.out.format("column found!\n");
                col++;
                break;
            }
            start = header.indexOf("  ");
        }
        return col;
    }

    double getKp(final ZonedDateTime datetime) {
        // Returns the Kp value in info using the given datetime

        if (info == null) {
            return -1;
        }
        final int col = getCol(datetime);
        // System.out.format("column: %d%n", col);
        final int row = getRow(datetime);
        // System.out.format("row: %d%n", row);
        if (row < 1 || col < 1) {
            return -1;
        }
        if (info.isEmpty()) {
            return -1;
        }
        String tmp = info;
        int rowcount = 0;
        while (rowcount < row) {
            tmp = tmp.substring(tmp.indexOf('\n') + 1);
            // System.out.format("now tmp is: '%s'\n", tmp);
            rowcount++;
        }
        int colcount = 0;
        while (colcount < col) {
            tmp = tmp.substring(tmp.indexOf("  ")).stripLeading();
            colcount++;
        }
        tmp = tmp.substring(0, tmp.indexOf(" "));
        return Double.valueOf(tmp);
    }
    
    int getGScale(final double kp) {
        // Returns the G Scale using the given Kp value

        if (kp < 0) {
            return -1;
        }
        else if (kp < 5) {
            return 0;
        }
        else if (kp < 6) {
            return 1;
        }
        else if (kp < 7) {
            return 2;
        }
        else if (kp < 8) {
            return 3;
        }
        else if (kp < 9) {
            return 4;
        }
        else {
            return 5;
        }
    }
    
    String activeOn(final ZonedDateTime datetime) {
        // Returns a String that tells us the likelihood of aurora activity at the specified datetime

        if (info == null) {
            return "no";
        }
        int gscale = getGScale(getKp(datetime));
        switch (gscale) {
            case 0:
                return "low to no";
            case 1:
                return "moderate";
            case 2:
                return "high";
            case 3:
                return "very high";
            case 4:
                return "extremely high";
            case 5:
                return "almost surely";
        }
        return "no";
    }

    void activeTonight(final ZonedDateTime datetime) {
        // Updates tonight so that it tells us aurora activity tonight with respect to the timezone of the given datetime
        // Assumes night starts at 1800hrs and ends at 0600hrs

        if (info == null) {
            // return "no";
            return;
        }
        final int night_start = 18;
        final int night_end = 6;
        // ZoneId localzone = datetime.getZone();
        ZoneId localzone = ZoneId.systemDefault();
        ZonedDateTime localdt = datetime.withZoneSameInstant(localzone).withMinute(0).withSecond(0).withNano(0);
        // return localzone.toString();
        int hour = night_start;
        tonight = "";
        String tmp = "";
        while (hour >= night_start || hour <= night_end) {
            if (hour > 23) {
                hour = 0;
                localdt = localdt.plusDays(1);
            }
            localdt = localdt.withHour(hour);
            // System.out.format("iterated localtime: %s\n", localdt.toString());
            // System.out.format("iterated ztime: %s\n", localdt.withZoneSameInstant(ZoneId.of("Z")).toString());
            tmp = activeOn(localdt);
            if (tmp != "no" && tmp != "low to no") {
                if (hour < 10) {
                    tonight += "0";
                }
                tonight += hour + "00hrs: aurora activity is " + tmp + "\n";
                // System.out.format("%s", tonight);
            }
            hour++;
        }
        // return tonight;
    }
}
