package com.yuri.notebook.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.yuri.notebook.R;
import com.yuri.notebook.R.string;

import android.content.Context;
import android.text.format.DateUtils;


public class DateFormatUtils {
	private static final String TAG = "DateFormatUtils";
	private int year_now;
	private int month_now;
	private int day_now;
	private int hours_now;
	private long time_now;
	
	private static SimpleDateFormat sfd_year = null;
	private static SimpleDateFormat sfd_month = null;
	private static SimpleDateFormat sfd_hour = null;
	
	private Context mContext;
	/**
	 * @param time current time in mills
	 */
	public DateFormatUtils(Context context,long time){
		time_now = time;
		Calendar calendar_now = Calendar.getInstance();
		calendar_now.setTimeInMillis(time);
		year_now = calendar_now.get(Calendar.YEAR);
		month_now = calendar_now.get(Calendar.MONTH) + 1;
		day_now = calendar_now.get(Calendar.DAY_OF_MONTH);
		hours_now = calendar_now.get(Calendar.HOUR_OF_DAY);//24H
		
		sfd_year = new SimpleDateFormat(context.getResources().getString(R.string.year_sfd));
		sfd_month = new SimpleDateFormat(context.getResources().getString(R.string.month_sfd));
		sfd_hour = new SimpleDateFormat(context.getResources().getString(R.string.hours_sfd));
		
		this.mContext = context;
	}
	
	/**
	 * @param time the time that need format
	 * @return the time that formated like "42minutes ago"
	 */
	public String getDateFormatString(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);//24H
//		LogUtils.d(TAG, "year=" + year + ",month=" + month + ",day=" + day + ",hours=" + hours);
		
		String format_time = "";
		Date date;
		date = new Date(time);
		
		if (year == year_now ) {
			//this year,we do not need show year in UI
			if (month == month_now) {
				//this year & this month
				int day_dur = day - day_now;
				if (day_dur == 0) {
					//today start 
					long duration = time_now - time;
					if (duration < DateUtils.HOUR_IN_MILLIS && duration > 0) {
						//1小时以内
						//the time is in an hour,so we show like "42miniutes ago" 
						format_time = DateUtils.getRelativeTimeSpanString(time,
			                    getCurrentTimeMillis(),
			                    DateUtils.MINUTE_IN_MILLIS,
			                    DateUtils.FORMAT_ABBREV_ALL).toString();
					}else if (duration > DateUtils.HOUR_IN_MILLIS) {
						//超过一小时
						//an hour ago,we just show like "19:20"
						format_time = sfd_hour.format(date);
					}else {
						//in the future
						format_time = DateUtils.getRelativeTimeSpanString(time,
			                    getCurrentTimeMillis(),
			                    DateUtils.MINUTE_IN_MILLIS,
			                    DateUtils.FORMAT_ABBREV_ALL).toString();
					}
					//today end
				}else if (day_dur == -1) {
					//yestoday,we will show like "yestoday 12:11"
					format_time = mContext.getResources().getString(R.string.yestoday, sfd_hour.format(date));
				}else if (day_dur < -1) {
					//one day ago,we just show like "Apirl 12,12:01"
					format_time = sfd_month.format(date);
				}else {
					// int the future
					format_time = DateUtils.getRelativeTimeSpanString(time,
		                    getCurrentTimeMillis(),
		                    DateUtils.MINUTE_IN_MILLIS,
		                    DateUtils.FORMAT_ABBREV_ALL).toString();
				}
			}else {
				//the other month,we show like "Apirl 12 14:03"
				format_time = sfd_month.format(date);
			}
		}else {
			//not this year,we show like "Apirl 12,2012 14:03"
			format_time = sfd_year.format(date);
		}
		
		return format_time;
	}
	
	private Long mCurrentTimeMillisForTest;
	public long getCurrentTimeMillis() {
        if (mCurrentTimeMillisForTest == null) {
            return System.currentTimeMillis();
        } else {
            return mCurrentTimeMillisForTest;
        }
    }	
}
