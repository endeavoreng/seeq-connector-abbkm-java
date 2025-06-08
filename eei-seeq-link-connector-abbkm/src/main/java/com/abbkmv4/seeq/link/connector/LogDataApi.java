package com.abbkmv4.seeq.link.connector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;

import com.google.gson.Gson;


public class LogDataApi {
    private Logger log;
	private String serverHostURL;
	
	public LogDataApi(String serverHostURL, Logger log) {
		this.serverHostURL = serverHostURL;
		this.log = log;
	}
	
	
	public boolean isAvailable() {
		String url = serverHostURL + "/GetAvailability";
		this.log.info("isAvailable using: "+url);
		HttpPost post = new HttpPost(url);
		String result = "";
		
		List<NameValuePair> parameters = new ArrayList<>();
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(post);
			result = EntityUtils.toString(response.getEntity());
			this.log.info("isAvailable return: "+result);
		} catch (IOException e) {
			this.log.error("Cannot connect to WebAPI server!");
			this.log.error(e.getMessage());
			return false;
		}
		return true;
	}
		
	public String getTagListTypeFilter(String filter) {
		String url = serverHostURL + "/GetTagList";
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");
		String result = "";
		
		
		String json = "{ \"Ptypes\": []}";
		StringEntity entity;
		entity = new StringEntity(json, ContentType.APPLICATION_JSON);
		post.setEntity(entity);
		
//		List<String> ptypes = new ArrayList<>();
//		ptypes.add(filter);
//		List<NameValuePair> parameters = new ArrayList<>();
//		parameters.add(new BasicNameValuePair("Ptypes", "[ ]"));
//		parameters.add(new BasicNameValuePair("NameFilter", "CLD*"));
//		parameters.add(new BasicNameValuePair("LocationFilter", ""));
//		parameters.add(new BasicNameValuePair("MaterialFilter", ""));
//		parameters.add(new BasicNameValuePair("MaxRecordsToReturn", Integer.toString(50000)));
		
		
//		try {
//			UrlEncodedFormEntity urlencoded = new UrlEncodedFormEntity(parameters, "utf-8");
//			this.log.info(urlencoded.toString());
//			this.log.info(post.toString());
//			post.setEntity(urlencoded);
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(post);
			result = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			this.log.error("Cannot connect to WebAPI server!");
			this.log.error(e.getMessage());
			return "";
		}		
		return result;
	}

	
	public String getTagList() {
		String url = serverHostURL + "/GetTagList";
		HttpPost post = new HttpPost(url);
		String result = "";
		
		List<NameValuePair> parameters = new ArrayList<>();
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(post);
			result = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			this.log.error("Cannot connect to WebAPI server!");
			this.log.error(e.getMessage());
			return "";
		}		
		return result;
	}
	
	
	public String getTagData(List<String> tagNames, ZonedDateTime zstart, ZonedDateTime zfinish) {
		String url = serverHostURL + "/GetTagData";
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-type", "application/json");
		String result = "";
		
		
		TagDataParameters2 parameters = new TagDataParameters2();
		parameters.TagNames = tagNames;
		parameters.StartDate = zstart.toString();
		parameters.EndDate = zfinish.toString();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		parameters.StartDate = zstart.format(format);
		parameters.EndDate = zfinish.format(format);

		this.log.debug("FOUR:  Query using times: " + zstart.format(format) +" and "+zfinish.format(format));
		
		Gson gson = new Gson();
		String payload = gson.toJson(parameters);
		try {
			post.setEntity(new StringEntity(payload));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			CloseableHttpResponse response = client.execute(post);
			result = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			this.log.error("Cannot connect to WebAPI server!");
			this.log.error(e.getMessage());
			return "";
		}		
		return result;
	}
		
}

class TagDataParameters2 {
	public List<String> TagNames = new ArrayList<>();
	public String StartDate;
	public String EndDate;
	public TagDataParameters2() {}
}
