package com.oudmon.qc_utils.date;

import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
public class DateUtil {

    public static long Hour_S_Min = 60 * 60;

    /**
     * 获取当天零点时间戳
     * 返回秒
     * 10位
     *
     * @return
     */
    public long getZeroTime() {

        DateUtil dateUtil = new DateUtil(getYear(), getMonth(), getDay());

        return dateUtil.getUnixTimestamp();
    }

    /**
     * 获取当天零点的 时间格式 字符串
     *
     * @return
     */
    public String getZeroTimeYyyyMMdd_HHmmssDate() {

        DateUtil dateUtil = new DateUtil(getYear(), getMonth(), getDay());

        return dateUtil.getYyyyMMdd_HHmmssDate();
    }

    /**
     * 13位
     *
     * @return
     */
    public long getZeroTime1() {
        DateUtil dateUtil = new DateUtil(getYear(), getMonth(), getDay());

        return dateUtil.getTimestamp();
    }

    public static long getFirstDayMonth(Date date) {
        //获取当前月第一天：
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        return c.getTimeInMillis();
    }

    public static DateUtil getFirstDayOfMonth(DateUtil dateUtil) {
        return new DateUtil(getFirstDayMonth(dateUtil.toDate()), false);
    }

    public static DateUtil getLastDayOfMonth(DateUtil dateUtil) {
        return new DateUtil(getLastDayMonth(dateUtil.toDate()), false);
    }

//
//    public static DateUtil getFirstDayOfMonthSport(DateUtil dateUtil){
//        TimeZone timeZone= TimeZone.getTimeZone("Asia/Shanghai");
//        return new DateUtil(getFirstDayMonth(dateUtil.toDate()),false,timeZone);
//    }
//
//    public static DateUtil getLastDayOfMonthSport(DateUtil dateUtil){
//        TimeZone timeZone= TimeZone.getTimeZone("Asia/Shanghai");
//        return new DateUtil(getLastDayMonth(dateUtil.toDate()),false,timeZone);
//    }

    public static long getLastDayMonth(Date date) {

        //获取当前月最后一天
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.MONTH, 0);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));

        return ca.getTimeInMillis();
    }

    public static int getDaysOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public int getTodayMin() {
        long zeroTime = getZeroTime1();
        int i = Math.round((c.getTimeInMillis() - zeroTime) / (1000 * 60)) + 1;
        return i;
    }

    public int getTodayMinNoPlus() {
        long zeroTime = getZeroTime1();
        int i = Math.round((c.getTimeInMillis() - zeroTime) / (1000 * 60));
        return i;
    }

    public static boolean isSameDay(Date date1, Date date2) {

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2
                .get(Calendar.DAY_OF_MONTH);

        return isSameDate;


    }

    public boolean isSameDay(long compare_time, boolean isUnix) {

        DateUtil compare_dt = new DateUtil(compare_time, isUnix);

        if (compare_dt.getYear() == this.getYear() && compare_dt.getMonth() == this.getMonth()
                && compare_dt.getDay() == this.getDay()) {
            return true;
        }

        return false;
    }

    public static boolean isSameMonth(Date date1, Date date2) {

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
                .get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);

        return isSameMonth;


    }


    /**
     * 距离今天marginSize天
     * 获取对应的时间
     *
     * @param marignSize
     */
    public static long getPreOrNextTimeByDay(long marignSize) {
        long lastTime = System.currentTimeMillis();

        //减去前几天
        return lastTime - marignSize * (1000 * 60 * 60 * 24);
    }

    public static long getGMTDate(long record_date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String time = sdf.format(new Date((record_date * 1000L)));

            String ti_year = time.substring(0, 4);
            String ti_month = time.substring(5, 7);
            String ti_date = time.substring(8, 10);
            String ti_hour = time.substring(11, 13);
            String ti_min = time.substring(14, 16);
            return new DateUtil(Integer.parseInt(ti_year), Integer.parseInt(ti_month), Integer.parseInt(ti_date),
                    Integer.parseInt(ti_hour), Integer.parseInt(ti_min), 0).getUnixTimestamp();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            DateUtil dateUtil = new DateUtil(record_date, true);
            dateUtil.setHour(0);
            dateUtil.setMinute(0);
            dateUtil.setSecond(0);
            return dateUtil.getUnixTimestamp();
        }
    }

    public static String dayMinToStrShow(int mins){
        String strH = "",strM="";
        int h=mins/60;
        int min=mins%60;
        if(h>0){
            if(h<10){
                strH="0"+h;
            }else {
                strH=h+"";
            }
            if(min<10){
                strM="0"+min;
            }else {
                strM=min+"";
            }
            return strH+"h"+strM+"min";
        }else {
            if(min<10){
                strM="0"+min;
            }else {
                strM=min+"";
            }
            return strM+"min";
        }
    }

    public static final Locale localeObject = new Locale("en");

    public enum DateFormater {
        MMdd, MMdd_HHmm, yyyyMM, yyyyMMdd, yyyyMMdd_HHmm, yyyyMMdd_HHmmss, HHmm, HHmmss, yyyyMMddHHmm, SyyyyMMdd, dFyyyyMMdd, dFHHmm, dfddMMyyy
    }

    private final static ThreadLocal<SimpleDateFormat> dFMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM/dd", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFMMdd_HHmm = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MM-dd HH:mm", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFyyyyMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFyyyyMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dfddMMyyy = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd-MM-yyyy", localeObject);
        }
    };


    private final static ThreadLocal<SimpleDateFormat> dFyyyyMMdd_HHmm = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFyyyyMMdd_HHmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFHHmm = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFHHmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss", localeObject);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dFSyyyyMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd", localeObject);
        }
    };

    public static DateUtil valueOf(String sdate) {
        String MMddFmt = "[0-9]{2}-[0-9]{2}"; // MM-dd
        String MMdd_HHmmFmt = "[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}"; // MM-dd
        // HH:mm
        String yyyyMMFmt = "[0-9]{4}-[0-9]{2}"; // yyyy-MM
        String yyyyMMddFmt = "[0-9]{4}-[0-9]{2}-[0-9]{2}"; // yyyy-MM-dd
        String yyyyMMdd_HHmmFmt = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}"; // yyyy-MM-dd
        // HH:mm
        String yyyyMMdd_HHmmssFmt = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"; // yyyy-MM-dd
        // HH:mm:ss
        String HHmmFmt = "[0-9]{2}:[0-9]{2}"; // HH:mm
        String HHmmssFmt = "[0-9]{2}:[0-9]{2}:[0-9]{2}"; // HH:mm:ss
        Pattern p = Pattern.compile(yyyyMMdd_HHmmssFmt);
        try {
            p = Pattern.compile(MMddFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.MMdd);
            }
            p = Pattern.compile(MMdd_HHmmFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.MMdd_HHmm);
            }
            p = Pattern.compile(yyyyMMFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.yyyyMM);
            }
            p = Pattern.compile(yyyyMMddFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.yyyyMMdd);
            }
            p = Pattern.compile(yyyyMMdd_HHmmFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.yyyyMMdd_HHmm);
            }
            p = Pattern.compile(yyyyMMdd_HHmmssFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.yyyyMMdd_HHmmss);
            }
            p = Pattern.compile(HHmmFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.HHmm);
            }
            p = Pattern.compile(HHmmssFmt);
            if (p.matcher(sdate).matches()) {
                return parse(sdate, DateFormater.HHmmss);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DateUtil parse(String sdate, DateFormater formater)
            throws ParseException {
        Date date = null;
        switch (formater) {
            case MMdd:
                date = dFMMdd.get().parse(sdate);
                break;
            case MMdd_HHmm:
                date = dFMMdd_HHmm.get().parse(sdate);
                break;
            case yyyyMM:
                date = dFyyyyMM.get().parse(sdate);
                break;
            case yyyyMMdd:
                date = dFSyyyyMMdd.get().parse(sdate);
                break;
            case dFyyyyMMdd:
                date = dFyyyyMMdd.get().parse(sdate);
                break;
            case dfddMMyyy:
                date = dfddMMyyy.get().parse(sdate);
                break;
            case yyyyMMdd_HHmm:
                date = dFyyyyMMdd_HHmm.get().parse(sdate);
                break;
            case yyyyMMdd_HHmmss:
                date = dFyyyyMMdd_HHmmss.get().parse(sdate);
                break;
            case HHmm:
                date = dFHHmm.get().parse(sdate);
                break;
            case HHmmss:
                date = dFHHmmss.get().parse(sdate);
                break;
        }
        return new DateUtil(date);
    }

    private Calendar c;

    public DateUtil() {
        c = Calendar.getInstance();
    }

    public DateUtil(TimeZone timeZone) {
        c = Calendar.getInstance();
        c.setTimeZone(timeZone);
    }

    public DateUtil(long timestamp, boolean isUnix) {
        c = Calendar.getInstance();
        if (isUnix) {
            c.setTimeInMillis(timestamp * 1000L);
        } else {
            c.setTimeInMillis(timestamp);
        }
    }

    public DateUtil(long timestamp, boolean isUnix, TimeZone timeZone) {
        c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        if (isUnix) {
            c.setTimeInMillis(timestamp * 1000L);
        } else {
            c.setTimeInMillis(timestamp);
        }
    }

    public DateUtil(Date date) {
        c = Calendar.getInstance();
        c.setTime(date);
    }

    public DateUtil(int year, int month, int day) {
        this(year, month, day, 0, 0, 0);
    }

    public boolean futureDate(){
        DateUtil d = new DateUtil();
        if(this.getUnixTimestamp()>d.getUnixTimestamp()){
            return true;
        }else {
            return false;
        }
    }
    public DateUtil(int year, int month, int day, int hour, int minute) {
        this(year, month, day, hour, minute, 0);
    }

    public DateUtil(int year, int month, int day, int hour, int minute, int second) {
        c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1); // 系统从0开始算
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
    }

    public DateUtil(int hour, int minute) {
        c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
    }

    public boolean isToday() {
        DateUtil d = new DateUtil();
        return this.getYear() == d.getYear() && this.getMonth() == d.getMonth()
                && this.getDay() == d.getDay();
    }


    public boolean isYesterday() {
        DateUtil d = new DateUtil();
        d.addDay(-1);
        return this.getYear() == d.getYear() && this.getMonth() == d.getMonth()
                && this.getDay() == d.getDay();
    }

    public boolean isSameWeek(int number) {
        DateUtil date = new DateUtil(new Date());
        int index = date.getWeekOfYear();
        return number == index;
    }

    public boolean isSameMonth(int month, int year) {
        int index = getMonth();
        int nYear = getYear();
        return month == index && nYear == year;
    }

    public int daysBetweenMe(DateUtil dateUtil) {
        return (int) (Math.abs(getZeroTime() - dateUtil.getZeroTime()) / (24 * 60 * 60));
    }

    public static String dayMinToStr(int mins) {
        String strH = "", strM = "";
        int h = mins / 60;
        int min = mins % 60;
        if (h < 10) {
            strH = "0" + h;
        } else {
            strH = h + "";
        }
        if (min < 10) {
            strM = "0" + min;
        } else {
            strM = min + "";
        }
        return strH + ":" + strM;
    }

    public static String dayMinToStrChina(int mins) {
        String strH = "", strM = "";
        int h = mins / 60;
        int min = mins % 60;
        if (h == 0) {
            if (min < 10) {
                strM = "0" + min;
            } else {
                strM = min + "";
            }
            return strM + "分";
        } else {
            if (h < 10) {
                strH = "0" + h;
            } else {
                strH = h + "";
            }
            if (min < 10) {
                strM = "0" + min;
            } else {
                strM = min + "";
            }
            return strH + "时" + strM + "分";
        }
    }

    public static String formatMillis(long millis) {
        long m = millis / 60000;
        long s = (millis % 60000) / 1000;
        return (m < 10 ? "0" : "") + m +
                (s < 10 ? ":0" : ":") + s;
    }

    public static String formatMillisToMinutesSecondsTenths(long millis) {
        long m = millis / 60000;
        long s = (millis % 60000) / 1000;
        long t = (millis % 1000) / 100;
//        return (m < 10 ? "0" : "") + m +
//                (s < 10 ? ":0" : ":") + s
//                + "." + t;
        return (m < 10 ? "0" : "") + m +
                (s < 10 ? ":0" : ":") + s;
    }

    public static String secondToStr(int seconds) {
        String strH = "", strM = "", strS;
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h < 10) {
            strH = "0" + h;
        } else {
            strH = h + "";
        }
        if (m < 10) {
            strM = "0" + m;
        } else {
            strM = m + "";
        }
        if (s < 10) {
            strS = "0" + s;
        } else {
            strS = s + "";
        }
        if(h==0){
            return strM + ":" + strS;
        }else {
            return strH + ":" + strM + ":" + strS;
        }
    }

    public static String dayMinToStrSymbol(int mins) {
        String strH = "", strM = "";
        int h = mins / 60;
        int min = mins % 60;
        if (h < 10) {
            strH = "0" + h;
        } else {
            strH = h + "";
        }
        if (min < 10) {
            strM = "0" + min;
        } else {
            strM = min + "";
        }
        return strH + "'" + strM + "''";
    }

    public static String minsToHHmmdd(int seconds) {
        int temp = 0;
        StringBuffer sb = new StringBuffer();
        temp = seconds / 3600;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 / 60;
        sb.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 % 60;
        sb.append((temp < 10) ? "0" + temp : "" + temp);
        return sb.toString();
    }

    /**
     * 转换为java.util.Date对象
     *
     * @return
     */
    public Date toDate() {
        return c.getTime();
    }

    public String toFormatString(DateFormater formater) {
        Date date = toDate();
        String sdate = "Unknown";
        switch (formater) {
            case MMdd:
                sdate = dFMMdd.get().format(date);
                break;
            case MMdd_HHmm:
                sdate = dFMMdd_HHmm.get().format(date);
                break;
            case yyyyMM:
                sdate = dFyyyyMM.get().format(date);
                break;
            case yyyyMMdd:
            case dFyyyyMMdd:
                sdate = dFyyyyMMdd.get().format(date);
                break;
            case yyyyMMdd_HHmm:
                sdate = dFyyyyMMdd_HHmm.get().format(date);
                break;
            case yyyyMMdd_HHmmss:
                sdate = dFyyyyMMdd_HHmmss.get().format(date);
                break;
            case HHmm:
                sdate = dFHHmm.get().format(date);
                break;
            case HHmmss:
                sdate = dFHHmmss.get().format(date);
                break;
            case SyyyyMMdd:
                sdate = dFSyyyyMMdd.get().format(date);
                break;
            case dfddMMyyy:
                sdate = dfddMMyyy.get().format(date);
                break;
        }
        return sdate;
    }


    public static final String yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMdd_HHmm = "yyyy-MM-dd HH:mm";
    public static final String dFyyyyMMdd1 = "yyyy-MM-dd";
    public static SimpleDateFormat yyyyMMdd_HHmmssF = new SimpleDateFormat(yyyyMMdd_HHmmss);
    public static SimpleDateFormat dFyyyyMMddF = new SimpleDateFormat(dFyyyyMMdd1);
    public static SimpleDateFormat dFyyyyMMddmmF = new SimpleDateFormat(yyyyMMdd_HHmm);


    public static Date String2Date(String formater, String dateString) {
        Date date = null;
        try {
            switch (formater) {
                case yyyyMMdd_HHmmss:
                    date = yyyyMMdd_HHmmssF.parse(dateString);
                    break;
                case yyyyMMdd_HHmm:
                    date = dFyyyyMMddmmF.parse(dateString);
                    break;

                case dFyyyyMMdd1:
                    date = dFyyyyMMddF.parse(dateString);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;

    }


    public String getMMddDate() {
        return toFormatString(DateFormater.MMdd);
    }

    public String getMMdd_HHmmDate() {
        return toFormatString(DateFormater.MMdd_HHmm);
    }


    public String getY_M_D() {
        return toFormatString(DateFormater.dFyyyyMMdd);
    }

    public String getD_M_Y() {
        return toFormatString(DateFormater.dfddMMyyy);
    }

    public String getY_M_D_H_M_S() {
        return toFormatString(DateFormater.yyyyMMdd_HHmmss);
    }


    public String getY_M_D_H_M() {
        return toFormatString(DateFormater.yyyyMMdd_HHmm);
    }

    public String getYyyyMMDate() {
        return toFormatString(DateFormater.yyyyMM);
    }

    public String getYyyyMMddDate() {
        return toFormatString(DateFormater.yyyyMMdd);
    }

    public String getYyyyMMdd_HHmmDate() {
        return toFormatString(DateFormater.yyyyMMdd_HHmm);
    }

    public String getYyyyMMdd_HHmmssDate() {
        return toFormatString(DateFormater.yyyyMMdd_HHmmss);
    }

    public String getHHmmDate() {
        return toFormatString(DateFormater.HHmm);
    }

    public String getHHmmssDate() {
        return toFormatString(DateFormater.HHmmss);
    }

    public String getSyyyyMMddDate() {
        return toFormatString(DateFormater.SyyyyMMdd);
    }

    public String getyyyyMMddDate() {
        return toFormatString(DateFormater.yyyyMMdd);
    }

    public int getYear() {
        return c.get(Calendar.YEAR);
    }

    public void setYear(int year) {
        c.set(Calendar.YEAR, year);
    }

    public int getMonth() {
        return c.get(Calendar.MONTH) + 1;
    }

    public void setMonth(int month) {
        c.set(Calendar.MONTH, month - 1);
    }

    public int getDay() {
        return c.get(Calendar.DAY_OF_MONTH);
    }


    public int getDaysOfThisMonth() {
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public void setDay(int day) {
        c.set(Calendar.DAY_OF_MONTH, day);
    }

    public void addDay(int day) {
        c.add(Calendar.DAY_OF_MONTH, day);
    }

    public void addMonth(int month) {
        c.add(Calendar.MONTH, month);
    }

    public int getHour() {
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public void setHour(int hour) {
        c.set(Calendar.HOUR_OF_DAY, hour);
    }

    public int getMinute() {
        return c.get(Calendar.MINUTE);
    }

    public void setMinute(int minute) {
        c.set(Calendar.MINUTE, minute);
    }

    public int getSecond() {
        return c.get(Calendar.SECOND);
    }

    public void setSecond(int second) {
        c.set(Calendar.SECOND, second);
    }

    public long getTimestamp() {
        return c.getTimeInMillis();
    }

    public void setTimestamp(long timestamp) {
        c.setTimeInMillis(timestamp);
    }

    public long getUnixTimestamp() {
        return c.getTimeInMillis() / 1000l;
    }

    public void setUnixTimestamp(long unix_timestamp) {
        c.setTimeInMillis(unix_timestamp * 1000);
    }

    public int getDayOfWeek() {
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public int getWeekOfYear() {
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    public int getWeekOfMonth() {
        return c.get(Calendar.WEEK_OF_MONTH);
    }

    public String getMonDate() {
//		int dayWeek = c.get(Calendar.DAY_OF_WEEK); // 获得当前日期是一个星期的第几天 
        int day = getDayOfWeek();
        c.add(Calendar.DATE, c.getFirstDayOfWeek() - day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(c.getTime());
    }


    @Override
    public String toString() {
        return this.getYyyyMMdd_HHmmssDate();
    }

    /**
     * 如果是今天就返回具体时间,否则返回日期
     *
     * @param time
     * @return
     */
    public static String getTime(long time) {
        long now = System.currentTimeMillis();
        long dTime = (now - time) / (1000 * 60 * 60 * 24);
        DateUtil dateUtil = new DateUtil(time, false);
        if (dTime > 0) {
            return dateUtil.getYyyyMMddDate();
        } else {
            return dateUtil.getHHmmDate();
        }
    }

    public static long getSunDayTimeFromWeek() {

        Calendar cal = Calendar.getInstance();

        int i = cal.get(Calendar.DAY_OF_WEEK) - 1;
        long l = cal.getTime().getTime() - i * (1000 * 60 * 60 * 24);
//        KLog.e("周末 -> 周六" + DataTimeUtils.getyyyyMMddHHmmss(l) + "  > " + DataTimeUtils.getyyyyMMddHHmmss(l + 6 * 1000 * 60 * 60 * 24));

        return l;
    }

    public static DateUtil firstDayOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setMinimalDaysInFirstWeek(7);
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.WEEK_OF_MONTH, 0);
        cal.set(Calendar.DAY_OF_WEEK, 1);
        Date time = cal.getTime();
        return new DateUtil(time);
    }

    public static DateUtil firstDayOfWeekRing(DateUtil dateUtil) {
//        Calendar cal = Calendar.getInstance();
//        cal.setFirstDayOfWeek(Calendar.SUNDAY);
//        cal.setMinimalDaysInFirstWeek(7);
//        cal.setTimeInMillis(dateUtil.getTimestamp());
//        cal.add(Calendar.WEEK_OF_MONTH, 0);
//        cal.set(Calendar.DAY_OF_WEEK, 1);
//        Date time = cal.getTime();
//        DateUtil d=new DateUtil(time);
//        d.addDay(1);

        // 将时间戳转换为 Date 对象
        Date date = new Date(dateUtil.getTimestamp());

        // 创建 Calendar 实例并设置时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // 设置一周的第一天为周一（Calendar 默认一周第一天是周日）
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // 获取当前日期是这一周的第几天
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // 如果今天是周日，调整 dayOfWeek 的值
        if (dayOfWeek == Calendar.SUNDAY) {
            dayOfWeek = 8; // 将周日视为下一周的第八天
        }

        // 计算该周周一的日期：从当前日期减去 offset 使其回到周一
        int offset = dayOfWeek - Calendar.MONDAY;
        calendar.add(Calendar.DATE, -offset);

        return new DateUtil(calendar.getTime());
    }

    public static LocalDate getWeekStartDateFromTimestamp(long timestamp) {
        // 将时间戳转换为 LocalDate
        LocalDate date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // 使用 TemporalAdjusters 找到最接近的周一
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        return null;
    }


    public static int getDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return day - 1;
    }

    /**
     * 周日->周六
     * 0->6
     *
     * @param size
     * @return
     */
    public static Date getDateByWeekMagin(int size) {
        long sunDayTimeFromWeek = getSunDayTimeFromWeek();
        return new Date(sunDayTimeFromWeek + (size * (1000 * 60 * 60 * 24)));
    }

    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /**
     * 字符串转毫秒数
     */
    public static long dateStr2Stamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return Long.parseLong(String.valueOf(sdf.parse(date).getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 字符串yyyy-MM-dd转毫秒数
     */
    public static long dateY_M_D2Stamp(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return Long.parseLong(String.valueOf(sdf.parse(date).getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getMarginMin(long start, long startTime) {

        return (start - startTime) / 60 + "";

    }

    public static int getWhatDay(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp * 1000L);
        int whatDay = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            whatDay = 6;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            whatDay = 0;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            whatDay = 1;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            whatDay = 2;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            whatDay = 3;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            whatDay = 4;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            whatDay = 5;
        }
        return whatDay;
    }

    public static int getWhatDayRing(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp * 1000L);
        int whatDay = 0;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            whatDay = 5;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            whatDay = 6;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            whatDay = 0;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            whatDay = 1;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            whatDay = 2;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            whatDay = 3;
        }
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            whatDay = 4;
        }
        return whatDay;
    }

    /**
     * 根据用户生日Date数据计算年龄
     */
    public static int getAgeByBirthday(Date birthday) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthday)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthday);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                // monthNow==monthBirth
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                // monthNow>monthBirth
                age--;
            }
        }
        return age;
    }

    public String fetchToDay(int day){
        String strDay="";
        if(day<10){
            strDay="0"+day;
        }else {
            strDay=day+"";
        }
        return strDay;
    }

    /**
     * 将本地时间, 转换成目标时区的时间
     *
     * @param sourceDate
     * @param targetZoneId
     * @return
     */
    public static Date convertTimezone(Date sourceDate, String targetZoneId) {
        return convertTimezone(sourceDate, TimeZone.getTimeZone(targetZoneId));
    }

    public static Date convertTimezone(Date sourceDate, String sourceZoneId, String targetZoneId) {
        TimeZone sourceTimeZone = TimeZone.getTimeZone(sourceZoneId);
        TimeZone targetTimeZone = TimeZone.getTimeZone(targetZoneId);

        return convertTimezone(sourceDate, sourceTimeZone, targetTimeZone);
    }

    /**
     * 将本地时间,转换成对应时区的时间
     *
     * @param localDate
     * @param targetTimezone 转换成目标时区所在的时间
     * @return
     */
    public static Date convertTimezone(Date localDate, TimeZone targetTimezone) {
        return convertTimezone(localDate, TimeZone.getDefault(), targetTimezone);
    }


    /**
     * 将sourceDate转换成指定时区的时间
     *
     * @param sourceDate
     * @param sourceTimezone sourceDate所在的时区
     * @param targetTimezone 转化成目标时间所在的时区
     * @return
     */
    public static Date convertTimezone(Date sourceDate, TimeZone sourceTimezone, TimeZone targetTimezone) {


        // targetDate - sourceDate=targetTimezone-sourceTimezone
        // --->
        // targetDate=sourceDate + (targetTimezone-sourceTimezone)


        Calendar calendar = Calendar.getInstance();
        // date.getTime() 为时间戳, 为格林尼治到系统现在的时间差,世界各个地方获取的时间戳是一样的,
        // 格式化输出时,因为设置了不同的时区,所以输出不一样
        long sourceTime = sourceDate.getTime();


        calendar.setTimeZone(sourceTimezone);
        calendar.setTimeInMillis(sourceTime);// 设置之后,calendar会计算各种filed对应的值,并保存

        //获取源时区的到UTC的时区差
        int sourceZoneOffset = calendar.get(Calendar.ZONE_OFFSET);


        calendar.setTimeZone(targetTimezone);
        calendar.setTimeInMillis(sourceTime);

        int targetZoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        int targetDaylightOffset = calendar.get(Calendar.DST_OFFSET); // 夏令时


        long targetTime = sourceTime + (targetZoneOffset + targetDaylightOffset) - sourceZoneOffset;

        return new Date(targetTime);

    }


}
