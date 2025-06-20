package com.abbkmv4.seeq.link.connector;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import com.google.gson.Gson;
import com.seeq.link.sdk.interfaces.DatasourceConnectionServiceV2;
import com.seeq.model.AssetInputV1;
import com.seeq.model.AssetTreeSingleInputV1;
import com.seeq.model.ScalarPropertyV1;
import com.seeq.model.SignalWithIdInputV1;
import com.seeq.utilities.SeeqNames;


public class Indexer {
	private Logger log;
//	private LogDataApi logDataApi;
	private SecureApi logDataApi;
//	private String debugOutputJsonString;

	private static List<String> SIGNAL_TYPES = new ArrayList<>(
			List.of("15M", "30M", "HRS", "SHT", "DAY", "WEK", "MTH", "YER", "PRI"));
	Set<String> assetIds = new HashSet<String>(); 
	
	public Indexer(SecureApi api, Logger log) {  //  String debugJSON) {
		this.logDataApi = api;
		this.log = log;
//		this.debugOutputJsonString = debugJSON;
	}

	private void debug(String msg) {
        log.debug("{}: {}", SecureApi.VERSION, msg);
	}
	
	private void info(String msg) {
        log.info("{}: {}", SecureApi.VERSION, msg);
	}

	private void error(String msg) {
        log.error("{}: {}", SecureApi.VERSION, msg);
	}

	public void CreateSeeqHierarchy(DatasourceConnectionServiceV2 connectionService, String mode, String plantName)  {
		info("Querying tags for all types");
		String queryResult = this.logDataApi.getTagListTypeFilter("not used filter.. maybe ill put back");
		
		
//		
//		byte[] buffer = queryResult.getBytes();
//
//		FileChannel rwChannel = new RandomAccessFile("textfile.txt", "rw").getChannel();
//		ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, buffer.length );
//		wrBuf.put(buffer);
//		rwChannel.close();
		
		
		if (queryResult==null) {
			info("No result returned.   Was it cancelled?");
			return;
		}
		
		String infoMsg = "Received" + queryResult;
		if (queryResult.length() > 1000) {
			infoMsg = infoMsg + " (trunc): " + queryResult.substring(0, 999);
		} else {
			infoMsg = infoMsg + " (raw): " + queryResult;
		}
		info(infoMsg);
		
//		if (this.debugOutputJsonString != null) {
//			try {
//				String filename = this.debugOutputJsonString.replace("\\", "\\\\");
//				DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//		        filename += "."+timeStampPattern.format(java.time.LocalDateTime.now())+".json";
//		        info("Writing Indexer debug to file: "+filename);
//			      FileWriter myWriter = new FileWriter(filename);
//			      myWriter.write(queryResult);
//			      myWriter.close();
//			      info("Successfully wrote to the file: "+this.debugOutputJsonString);
//			    } catch (IOException e) {
//			      error("Writing JSON debug: "+ e.getMessage());
//			      e.printStackTrace();
//			    }
//		}
		
		try {
			TagList tagList = parseTagList(queryResult);
            connectionService.log().info("Retrieved {} tags", Integer.toString(tagList.Tags.size()));
			this.setTags(tagList, connectionService, mode, plantName);
		} catch (Exception e) {
			String errMsg = "Error parsing Index TagList return.  errorClass: '%s', errorMessage: '%s', infoMsg: '%s', ".formatted(e.getClass(), e.getMessage(), infoMsg);
			connectionService.log().error(errMsg);
		}


	}

	public static TagList parseTagList(String data) {
		Gson gson = new Gson();
		TagList tagList = gson.fromJson(data, TagList.class);
		return tagList;
	}

	public void createAssetTree(String childID, String parentID, DatasourceConnectionServiceV2 connectionService) {
		int count = 0;
		int maxCounts = 2;
		while(true) {
			try {
				AssetTreeSingleInputV1 signalNode = new AssetTreeSingleInputV1();
				signalNode.setChildDataId(childID);
				signalNode.setParentDataId(parentID);
				connectionService.putRelationship(signalNode);
				break;
			} catch (Exception e) {
				debug("CreateAssetTree ERROR "+e.getMessage());
				debug("IDs "+childID+", "+parentID);
				debug("Retrying: "+count);
				if (count++ >= maxCounts) {
					break;
				}
			}
		}
	}

	public void createAndPutAsset(String name, String ID, DatasourceConnectionServiceV2 connectionService) {
		
		if (assetIds.contains(ID)) {
			return;
		}
		assetIds.add(ID);
		AssetInputV1 signalHierarchy = new AssetInputV1();
		signalHierarchy.setName(name);
		signalHierarchy.setDataId(ID);
		connectionService.putAsset(signalHierarchy);
	}
	
	
	
	public SignalWithIdInputV1 createSignalInput(SignalData signalData, Tag tag) {
		String signalID = signalData.signalId;
		SignalWithIdInputV1 signal = new SignalWithIdInputV1();
		signal.setMaximumInterpolation(signalData.getInterpolation());
		signal.setDataId(signalID);
		signal.setName(signalData.signalName);
		signal.setDescription(signalData.description);
		ArrayList<ScalarPropertyV1> additionalProperties = new ArrayList<>();
		if (tag.GroupNr != null) {
			ScalarPropertyV1 groupNr = new ScalarPropertyV1();
			groupNr.setName("Group Number");
			groupNr.setValue(tag.GroupNr);
		}
		if (tag.ChannelNr != null) {
			ScalarPropertyV1 groupNr = new ScalarPropertyV1();
			groupNr.setName("Channel Number");
			groupNr.setValue(tag.ChannelNr);
		}
		signal.setAdditionalProperties(additionalProperties);
		return signal;
	}

	public void setTags(TagList tagList, DatasourceConnectionServiceV2 connectionService, String mode, String plantName) {

		AssetInputV1 root = new AssetInputV1();
//    	String plantName = "KMLOGS2021-01-06b";
		debug("Setting PlantName from Config: "+plantName+"  FYI: Database plantname "+tagList.PlantName);
		root.setDataId(plantName);
		root.setName(plantName);
		connectionService.putRootAsset(root);
		debug("Processing "+tagList.Tags.size()+" Tags");
		int tagCount = 0;
		assetIds.clear();
		for (Tag tag : tagList.Tags) {
			tagCount += 1;
			SignalData signalData = tag.getSignalData(connectionService.log());
//			String locationID = plantName+"|"+signalData.locationName;
//			String materialID = locationID + "|" + signalData.materialName;
			String signalID = signalData.signalId;
//			String signalHierID = plantName + "|" + signalData.logName;
			String parentID = plantName;
			if (mode.equals("flat")) {
				// Plant -> Signal -> SignalWithPeriod
				AssetInputV1 signalHierarchy = new AssetInputV1();
				signalHierarchy.setName(signalData.logName);
				signalHierarchy.setDataId(signalData.logName);
				connectionService.putAsset(signalHierarchy);
				createAssetTree(signalData.logName, parentID, connectionService);
				parentID = signalData.logName;

				SignalWithIdInputV1 signal = createSignalInput(signalData, tag);
				connectionService.putSignal(signal);
				createAssetTree(signalID, parentID, connectionService);
			} else if (mode.equals("tagid")) {
				List<String> kmpaths = tag.getKmPaths();
				this.createAndPutAsset(signalData.periodType, signalData.periodType, connectionService);
				createAssetTree(signalData.periodType, parentID, connectionService);
				parentID = signalData.periodType;
				parentID = createKmLogPath(tag, kmpaths, parentID, signalData.periodType, connectionService);
				SignalWithIdInputV1 signal = createSignalInput(signalData, tag);
				signal.setDataId(tag.TagId);  // Here's the major change
				connectionService.putSignal(signal);
				createAssetTree(tag.TagId, parentID, connectionService);
				if (tagCount % 10000 == 0) {
					debug("TagID... at tag count: "+Integer.toString(tagCount)+ " ID size: "+Integer.toString(assetIds.size()));
				}
			} else if (mode.equals("kmlogs")) {
				List<String> kmpaths = tag.getKmPaths();
//				debug("KMLOG: "+signalData.signalName+" path: "+tag.KmPath);
				// Plant -> PTYPE -> KMLOG path -> Signals
				// Set demonstration using HashSet 
				this.createAndPutAsset(signalData.periodType, signalData.periodType, connectionService);
				createAssetTree(signalData.periodType, parentID, connectionService);
				parentID = signalData.periodType;
				parentID = createKmLogPath(tag, kmpaths, parentID, signalData.periodType, connectionService);
				SignalWithIdInputV1 signal = createSignalInput(signalData, tag);
				connectionService.putSignal(signal);
				createAssetTree(signalID, parentID, connectionService);
				if (tagCount % 10000 == 0) {
					debug("NOT Flushing Seeq KMLOGS... at tag count: "+Integer.toString(tagCount)+ " ID size: "+Integer.toString(assetIds.size()));
				}

			} else {
				// Mode: Ptypes puts the hierarchy in Plant-> PTYPE -> Signals
				AssetInputV1 signalHierarchy = new AssetInputV1();
				signalHierarchy.setName(signalData.periodType);
				signalHierarchy.setDataId(signalData.periodType);
				connectionService.putAsset(signalHierarchy);
				createAssetTree(signalData.periodType, parentID, connectionService);
				parentID = signalData.periodType;
				SignalWithIdInputV1 signal = createSignalInput(signalData, tag);
				connectionService.putSignal(signal);
				createAssetTree(signalID, parentID, connectionService);
				if (tagCount % 10000 == 0) {
					debug("Flushing Seeq PTYPES... at tag count: "+Integer.toString(tagCount));
					connectionService.flushAssets();
					connectionService.flushSignals();
					connectionService.flushRelationships();
					ScalarPropertyV1 cp = new ScalarPropertyV1();
					cp.setName(SeeqNames.Properties.ExpectDuplicatesDuringIndexing);
					cp.setValue(true);
					connectionService.getDatasource().addAdditionalPropertiesItem(cp);
				}

			}
			
		}
		assetIds.clear();

	}

	public String createKmLogPath(Tag tag, List<String> kmlogs, String parentID, String period,
			DatasourceConnectionServiceV2 connectionService) {
		int level = 0;
		String path = "";
		for (String km : kmlogs) {
			path += km;
			String dataId = path+" P:" + period + " lvl" + Integer.toString(level);
			this.createAndPutAsset(km,  dataId, connectionService);
			createAssetTree(dataId, parentID, connectionService);
			parentID = dataId;
			path += "|";
			level += 1;
		}
		return parentID;
	}
}

class SignalData {
	public boolean valid;
	public String logName;
	public String materialName;
	public String locationName;
	public String periodType;
	public String signalId;
	public String signalName;
	public String description;
	public String tagId;

	public SignalData() {
		this.valid = true;
	}

	public String getInterpolation() {
		switch (periodType) {
		case "15M":
			return "5h";
		case "30M":
			return "10h";
		case "HRS":
			return "10h";
		case "SHT":
			return "5h";
		case "DAY":
			return "10d";
		case "WEK":
			return "5wk";
		case "MTH":
			return "4mo";
		case "YER":
			return "4 years";
		default:
			return "10h";
		}

	}

	public long getSamplePeriod() {
		switch (periodType) {
		case "15M":
			return 900000l;
		case "30M":
			return 60 * 30 * 1000;
		case "HRS":
			return 3600 * 1000;
		case "SHT":
			return 3600 * 1000;
		case "DAY":
			return 86400 * 1000;
		case "WEK":
			return 7 * 86400 * 1000;
		case "MTH":
			return 30 * 86400 * 1000;
		case "YER":
			return 365 * 86400 * 1000;
		default:
			return 100;
		}

	}
};

class TagList {
	public String TimeStamp;
	public String PlantName;
	public ArrayList<Tag> Tags = new ArrayList<>();
	public String notUsed;

	public Set<String> getUniqueLocations(Logger log) {
		Set<String> locations = new HashSet<>();
		for (Tag tag : Tags) {
			locations.add(tag.getSignalData(log).locationName);
		}
		log.info("Found {} unique locations in the tags", locations.size());
		return locations;
	}
}

class Tag {
	public String Name;
	public String GroupNr = null;
	public String ChannelNr = null;
	public String KmPath = null;
	public String Description = null;
	public String TagId = null;
	private SignalData data = null;

	public SignalData getSignalData(Logger log) {
		if (this.data == null) {
			this.data = parseSignalData(this.Name, log);
		}
		this.data.description = this.Description;
		if (this.Description == null) 
			this.data.description = "";
		return this.data;
	}

	public List<String> getKmPaths() {
		String[] tokens = KmPath.split("\\\\");
		List<String> output = new ArrayList<String>();
		for (String t : tokens) {
			if (t.length() > 0 && !t.contentEquals("Logs")) {
				output.add(t);
			}
		}
		return output;
	}

	public static SignalData parseSignalData(String input, Logger log) {
		SignalData data = new SignalData();
		data.signalId = input;
		String[] tokens = input.split("\\|");
		if (tokens.length != 4) {
			log.error("Signal data in Indexer not formated correctly: " + input);
			data.valid = false;
			return data;
		}
		if (tokens[0].length() == 0) {
			log.error("Signal data name cannot be empty: {}", input);
			data.valid = false;
			return data;
		}

		boolean locationUnassigned = (tokens[2].length() == 0) || "-".equals(tokens[2]) || "*".equals(tokens[2]);
		data.locationName = locationUnassigned ? "*" : tokens[2];
		data.logName = tokens[0];
		boolean materialUnassigned = (tokens[1].length() == 0) || "-".equals(tokens[1]) || "*".equals(tokens[1]);
		data.materialName = materialUnassigned ? "*" : tokens[1];
		data.periodType = tokens[3];
		data.signalName = input;
//		if (data.locationName.length() > 1) {
//			data.signalName += "|" + data.locationName;
//		}
//		if (data.materialName.length() > 1) {
//			data.signalName += "|"+ data.materialName; 
//		}
//		data.signalName += "|" + tokens[3];
		data.valid = true;

		data.locationName = data.locationName.replace("/", "-");
		data.materialName = data.materialName.replace("/", "-");
		data.logName = data.logName.replace("/", "-");
		data.signalId = data.signalId.replace("/", "-");
		return data;
	}

}
