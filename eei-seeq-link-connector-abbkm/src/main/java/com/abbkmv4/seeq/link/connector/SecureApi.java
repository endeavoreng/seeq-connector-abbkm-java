package com.abbkmv4.seeq.link.connector;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;

import com.google.gson.Gson;


public class SecureApi {
    private Logger log;
	private String serverHost;
	private String serverURL = "/ExtDataAccess/api";
	private CloseableHttpClient httpclient;
	private HttpClientContext context;
	private HttpHost target ;
	private int availability_calls;
	private int timeout;
	private ABBKMConnectionConfigV4 connection;
	private String debugFile;
	private String dataSource;

	public static int version = 4;
	public static String VERSION = "Version 4.0";

	
	public SecureApi(ABBKMConnectionConfigV4 connection, Logger log, String debugFile, String dataSource) {
		this.availability_calls = 0;
		this.serverHost = connection.getWebServiceIP();
		this.serverURL = connection.getWebServiceMethod()+"://"+connection.getWebServiceIP()+"/"+connection.getWebServiceURL();
		this.timeout= connection.getWebServiceTimeout();
		this.log = log;		
		this.connection = connection;
		this.CreateConnection();
		this.debugFile = debugFile;
		this.dataSource = dataSource;
		
	}
	
	private void CreateConnection() {
		try {

	    	RequestConfig config = RequestConfig.custom()
	  			  .setSocketTimeout(timeout)
				  .setConnectTimeout(timeout)
				  .setConnectionRequestTimeout(timeout)
			        .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.KERBEROS, AuthSchemes.SPNEGO))
			        .build();
			
	    	SSLContextBuilder builder = new SSLContextBuilder();
	        builder.loadTrustMaterial(null, new TrustStrategy() {
	            @Override
	            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	                return true;
	            }
	        });
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
	        this.httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(config)
			        .setRedirectStrategy(new LaxRedirectStrategy())
			        .build();
	        
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY,
			        new NTCredentials(connection.getUserName(), connection.getPassword(), "localhost", connection.getDomain()));
			this.target = new HttpHost(serverHost, connection.getWebServicePort(), connection.getWebServiceMethod());

			// Make sure the same context is used to execute logically related requests
			this.context = HttpClientContext.create();
			this.context.setCredentialsProvider(credsProvider);
			this.info("New credential provider configured.");

	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private void debug(String msg) {
		log.debug(SecureApi.VERSION +": "+ msg);
	}
	
	private void info(String msg) {
		log.info(SecureApi.VERSION +": "+msg);
	}
	
	private void error(String msg) {
		log.error(SecureApi.VERSION +": "+msg);
	}

	
	public boolean isAvailable() {
//		CreateConnection();
		String url = serverURL + "/GetAvailability";
		if (this.availability_calls < 10) {
			info("isAvailable using: "+url);
			this.availability_calls += 1;
		}
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");

		List<NameValuePair> parameters = new ArrayList<>();
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			CloseableHttpResponse response2 = this.httpclient.execute(this.target, post, this.context);
			String result = EntityUtils.toString(response2.getEntity());
			response2.close();
			debug("isAvailable return: "+result);
		} catch (Exception e) {
			error("Cannot connect to WebAPI server!  " + e.getMessage());
			return false;
		} 
		return true;
	}
		
	public String getTagListTypeFilter(String filter) {
		String url = serverURL + "/GetTagList";
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");
		String result = "";
		
		
		String json = "{ \"Ptypes\": []}";
		StringEntity entity;
		entity = new StringEntity(json, ContentType.APPLICATION_JSON);
		post.setEntity(entity);
		
		try {
			CloseableHttpResponse response = this.httpclient.execute(this.target, post, this.context);
			result = EntityUtils.toString(response.getEntity());
			response.close();
		} catch (Exception e) {
			error("Cannot connect to WebAPI server! (getTagListTypeFilter)");
			error(e.getMessage());
			return "";
		}		
		return result;
	}

	
	public String getTagList() {
		String url = serverURL + "/GetTagList";
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");
		String result = "";
		
		List<NameValuePair> parameters = new ArrayList<>();
		try {
			post.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			CloseableHttpResponse response2 = this.httpclient.execute(this.target, post, this.context);
			result = EntityUtils.toString(response2.getEntity());
			response2.close();
		} catch (Exception e) {
			error("Cannot connect to WebAPI server!  (getTagList)");
			error(e.getMessage());
			return "";
		}		
		return result;
	}

	private static String encodeValue(String value) {
	    try {
	        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
	    } catch (UnsupportedEncodingException ex) {
	        throw new RuntimeException(ex.getCause());
	    }
	}

	private void writeDebugInformation(String message) {
		if (this.debugFile != null) {
			try {
				Instant now = Instant.now();
				Date dateNow = Date.from(now);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
				String fileDate = formatter.format(dateNow);
				String filename = this.debugFile.replace("\\", "\\\\");
				filename += "-" + fileDate + ".csv";
				File myObj = new File(filename);
				if (myObj.createNewFile()) {
					FileWriter myWriter = new FileWriter(filename, true);
					myWriter.write("LogTime,DataSource,QueryTime,Signal,QueryStart,NumberOfDays,StatusCode,QueryTime, Misc");
					myWriter.write("\r\n");
					myWriter.close();
				}
				FileWriter myWriter = new FileWriter(filename, true);
				DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				String debugMessage = timeStampPattern.format(java.time.LocalDateTime.now()) +", " + message;
				myWriter.write(debugMessage);
				myWriter.write("\r\n");
				myWriter.close();

				// clean up old files
				Date daysAgo = Date.from(now.minus(Duration.ofDays(10)));
				String oldfileDate = new SimpleDateFormat("yyyyMMdd").format(daysAgo);
				String oldfileName = this.debugFile.replace("\\", "\\\\");
				oldfileName += "-" + oldfileDate + ".csv";
				File delFile = new File(oldfileName);
				if (delFile.delete()) {
					info("Deleted DEBUG log file: "+oldfileName);
				}
			} catch (Exception e) {
				error("Writing JSON debug: "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public String getTagData(List<String> tagNames, ZonedDateTime zstart, ZonedDateTime zfinish) {
		String baseMessage = "";
		String url = this.serverURL + "/GetTagData";
		HttpPost post = new HttpPost(url);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-type", "application/json");

		TagDataParameters parameters = new TagDataParameters();
		
//		parameters.TagNames = tagNames;   // REQUIRED for V1,2
		parameters.TagIds = tagNames;
		parameters.StartDate = zstart.toString();
		parameters.EndDate = zfinish.toString();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		parameters.StartDate = zstart.format(format);
		parameters.EndDate = zfinish.format(format);

		debug("Data query using times: " + zstart.format(format) +" and "+zfinish.format(format));

		Gson gson = new Gson();
		String payload = gson.toJson(parameters);
		debug("Request parameters (JSON): "+payload);

		try {
			post.setEntity(new ByteArrayEntity(payload.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		int max_retries = 3;
		int count = 0;
		CloseableHttpResponse response;

		if (this.debugFile != null) {
			DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			baseMessage = this.dataSource;
			baseMessage += ", " + timeStampPattern.format(java.time.LocalDateTime.now());
			baseMessage += ", " + parameters.TagIds.get(0);
			baseMessage += ", " + parameters.StartDate;
			baseMessage += ", " + ChronoUnit.DAYS.between(zstart, zfinish);
		}


		while (true) {
			long startTime = System.currentTimeMillis();
			String misc = "";
			String debugMessage = baseMessage;
			try {
				info("HTTP Client executing...");
				response = this.httpclient.execute(this.target, post, this.context);
			} catch (Exception e) {
				error("Cannot connect to WebAPI server! (getTagData)");
				error(e.getMessage());
				long elapsedTime = System.currentTimeMillis() - startTime;
				debugMessage +=  ", -999" + ", " + Long.toString(elapsedTime) +", Exception: "+e.getMessage();
				this.writeDebugInformation(debugMessage);
				throw new RuntimeException("  --  Database WebAPI Error.   Please try query again.  -- ");
			}
			long elapsedTime = System.currentTimeMillis() - startTime;
			StatusLine sl = response.getStatusLine();
			int errorCode = sl.getStatusCode();
			debugMessage += ", "+Integer.toString(errorCode) + ", " + Long.toString(elapsedTime);
			if (errorCode == 400 || errorCode == 401 || errorCode == 500) {
				error("STATUS ERROR CODE " + Integer.toString(sl.getStatusCode()));
			} else {
				info("Status CODE: " + Integer.toString(sl.getStatusCode()));
			}
			if (errorCode == 401 || errorCode == 503) {
				count += 1;
				String t_message = debugMessage + ", Retry "+ Integer.toString(count);
				this.writeDebugInformation(t_message);
				if (count < max_retries) {
					error("Retrying query " + Integer.toString(count) + " of " + Integer.toString(max_retries));
				} else {
					throw new RuntimeException("  --   Received 401/403 error status from server.   Please try query again.  --  ");
				}
			} else {
				this.writeDebugInformation(debugMessage);
				break;
			}
		}

		String result = "";
		try {
			result = EntityUtils.toString(response.getEntity());
			response.close();
		} catch (IOException e) {
			error("Error processing response (getTagData)");
			error(e.getMessage());
			throw new RuntimeException("  --  Database WebAPI Error.   Please try query again.  --  ");
		}

		if (result.length() > 1000) {
			info("Received data (trunc): " + result.substring(0, 999));
		} else {
			info("Received data: " + result);
		}
		return result;
	}
		
}

class TagDataParameters {
//	public List<String> TagNames = new ArrayList<>(); 
	public List<String> TagIds = new ArrayList<>();
	public String StartDate;
	public String EndDate;
	public TagDataParameters() {}
//	public List<NameValuePair> getParameterArray() {
//		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
//		parameters.add(new BasicNameValuePair("StartDate", this.StartDate));
//		parameters.add(new BasicNameValuePair("EndDate", this.EndDate));
//		parameters.add(new BasicNameValuePair("TagIds",  "[\""+TagIds.get(0)+"\"]"));
//		
////		String result = TagNames.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
////		parameters.add(new BasicNameValuePair("TagNames", result));
//		return parameters;		
//	}
}
