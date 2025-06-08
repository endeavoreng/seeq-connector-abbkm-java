package com.abbkmv4.seeq.link.connector;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.seeq.link.sdk.utilities.Sample;
import com.seeq.link.sdk.utilities.TimeInstant;

import org.slf4j.Logger;

public class SignalQuery {
	private Logger log;
//	private LogDataApi logDataApi;
	private SecureApi logDataApi;
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final long NANOSECONDS_PER_MILLISECOND = 1_000_000;

	
	public SignalQuery(SecureApi api, Logger log) {
		this.logDataApi = api;
		this.log = log;
	}

	private void debug(String msg) {
		log.debug(SecureApi.VERSION +": "+ msg);
	}

	private void error(String msg) {
		log.error(SecureApi.VERSION +": "+ msg);
	}
	
	private void info(String msg) {
		log.info(SecureApi.VERSION +": "+msg);
	}

	public List<TagData> querySignalData(String tagName, ZonedDateTime start, ZonedDateTime finish) {
		List<String> tagNames = new ArrayList<String>(List.of(tagName));
		String response = logDataApi.getTagData(tagNames, start, finish);
		Type listType = new TypeToken<List<TagData>>() {}.getType();
		try {
			List<TagData> result = new Gson().fromJson(response, listType);
			return result;
		} catch (Exception e) {
			debug("ERROR ERROR:  Exception thrown parsing time series data.");
			debug("ERROR ERROR:  Response was: " +response);
			debug("ERROR ERROR: "+e.getMessage());
		}
		return new ArrayList<TagData>();
	}
	
	
	public List<Sample> getSamples(TimeInstant startTime, TimeInstant endTime,
								   String signalId, int sampleLimit, String userTimeZone) {
		
		// Get the time interval for the signal in seconds.
        // Provide streams to do the multiple queries here
        ZonedDateTime ZstartDateTime = TimeInstant.timestampToDateTime(startTime.getTimestamp());
        ZonedDateTime ZendDateTime = TimeInstant.timestampToDateTime(endTime.getTimestamp());
        ZonedDateTime startDateTime = ZstartDateTime.withZoneSameInstant(ZoneId.of(userTimeZone));
        ZonedDateTime endDateTime = ZendDateTime.withZoneSameInstant(ZoneId.of(userTimeZone));
        
        
        List<TagData> tagDataList = this.querySignalData(signalId, startDateTime, endDateTime);
//        if (tagDataList == null) {
//        	error("No data returned.");
//        	List<TagData> empty =  new ArrayList<TagData>();
//        	return empty.stream();
//        }
        TagData tagData = tagDataList.get(0);
        
        info("Signal query: "+ signalId+" from "+startDateTime + " - " + endDateTime + " Number: "+tagData.DataPoints.size() );
        if (tagData.DataPoints.size() > 0) {
        	DataPoint p = tagData.DataPoints.get(0);
        	TimeInstant ti = p.getTimeInstant(userTimeZone);
        	debug("First point format: " + p.StartTime+".  Time instant: "+ti.getTimestamp());        	
        }
        List<Sample> samples = new ArrayList<>();
        int idx = 0;
        while (samples.size() < sampleLimit && idx < tagData.DataPoints.size()) {
        	DataPoint point = tagData.DataPoints.get(idx);
        	Sample sample = new Sample().key(point.getTimeInstant(userTimeZone)).value(point.value);
        	samples.add(sample);
        	idx += 1;
        }
		return samples;
	}

}

class DataQuery {
	List<TagData> tagData = new ArrayList<>();
}

class TagData {
	public String TagName;
	public List<DataPoint> DataPoints = new ArrayList<>();
}

class DataPoint {
	public String StartTime;
	public String EndTime;
	public float value;
	
	public TimeInstant getTimeInstant(String userTimeZone) {
		ZonedDateTime zdt = ZonedDateTime.parse(StartTime);
		return new TimeInstant(ZonedDateTime.parse(StartTime));
	}
}
