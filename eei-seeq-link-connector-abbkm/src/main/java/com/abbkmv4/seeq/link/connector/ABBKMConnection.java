package com.abbkmv4.seeq.link.connector;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.seeq.link.sdk.DefaultIndexingDatasourceConnectionConfig;
import com.seeq.link.sdk.interfaces.ConditionPullDatasourceConnection;
import com.seeq.link.sdk.interfaces.Connection.ConnectionState;
import com.seeq.link.sdk.interfaces.DatasourceConnectionServiceV2;
import com.seeq.link.sdk.interfaces.GetCapsulesParameters;
import com.seeq.link.sdk.interfaces.GetSamplesParameters;
import com.seeq.link.sdk.interfaces.SignalPullDatasourceConnection;
import com.seeq.link.sdk.interfaces.SyncMode;
import com.seeq.link.sdk.utilities.Capsule;
import com.seeq.link.sdk.utilities.FormulaHelper;
import com.seeq.link.sdk.utilities.Sample;
import com.seeq.link.sdk.utilities.TimeInstant;
import com.seeq.model.AssetInputV1;
import com.seeq.model.AssetTreeSingleInputV1;
import com.seeq.model.ConditionUpdateInputV1;
import com.seeq.model.ScalarInputV1;
import com.seeq.model.ScalarPropertyV1;
import com.seeq.model.SignalWithIdInputV1;
import com.seeq.utilities.SeeqNames;

/**
 * Represents a connection to a unique datasource. A connector can host any number of such connections to
 * datasources.
 *
 * This example implements the {@link SignalPullDatasourceConnection} interface, which means that
 * the connection responds to on-demand requests from Seeq Server for Samples within a Signal and queries
 * its datasource to produce the result.
 *
 * A connection can also implement {@link com.seeq.link.sdk.interfaces.ConditionPullDatasourceConnection}
 * to respond to on-demand requests from Seeq Server for Capsules within a Condition.
 *
 * Alternatively, a connection could choose to implement neither of the above interfaces and instead "push"
 * Samples or Capsules into Seeq using the {@link com.seeq.api.SignalsApi} or {@link com.seeq.api.ConditionsApi}
 * obtained via {@link com.seeq.link.sdk.interfaces.SeeqApiProvider} on
 * {@link com.seeq.link.sdk.interfaces.AgentService} on {@link DatasourceConnectionServiceV2}.
 */
public class ABBKMConnection implements SignalPullDatasourceConnection, ConditionPullDatasourceConnection {
    private final ABBKMConnector connector;
    private final ABBKMConnectionConfigV4 connectionConfig;
    //    private LogDataApi logDataApi;
    private SecureApi logDataApi;
    private DatasourceConnectionServiceV2 connectionService;
    private String serverURL;

    public ABBKMConnection(ABBKMConnector connector, ABBKMConnectionConfigV4 connectionConfig) {
        // You will generally want to accept a configuration object from your connector parent. Do not do any I/O in the
        // constructor -- leave that for the other functions like initialize() or connect(). Generally, you should just
        // be setting private fields in the constructor.
        this.connector = connector;
        this.connectionConfig = connectionConfig;
    }

    @Override
    public String getDatasourceClass() {
        // Return a string that identifies this type of datasource. Example: "ERP System"
        // This value will be seen in the Information panel in Seeq Workbench.
        return "ABBKM-V3";
    }

    @Override
    public String getDatasourceName() {
        // The name will appear in Seeq Workbench and can change (as long as the DatasourceId does not change)
        return this.connectionConfig.getName();
    }

    @Override
    public String getDatasourceId() {
        // This unique identifier usually must come from the configuration file and be unchanging
        return this.connectionConfig.getId();
    }

    @Override
    public DefaultIndexingDatasourceConnectionConfig getConfiguration() {
        // The configuration should extend DefaultIndexingDatasourceConnectionConfig so that concerns like property
        // transforms and index scheduling are taken care of by the SDK.
        return this.connectionConfig;
    }

    @Override
    public Integer getMaxConcurrentRequests() {
        // This parameter can help control the load that Seeq puts on an external datasource. It is typically
        // controlled from the configuration file.
        return this.connectionConfig.getMaxConcurrentRequests();
    }

    @Override
    public Integer getMaxResultsPerRequest() {
        // This parameter can help control the load and memory usage that Seeq puts on an external datasource. It is
        // typically controlled from the configuration file.
        return this.connectionConfig.getMaxResultsPerRequest();
    }

    @Override
    public void initialize(DatasourceConnectionServiceV2 connectionService) {
        // You probably won't do much in the initialize() function. But if you have to do some I/O that is separate
        // from the act of connecting, you could do it here.

        this.connectionService = connectionService;

        // It's your job to inspect your configuration to see if the user has enabled this connection.
        if (this.connectionConfig.isEnabled()) {
            // This will cause the connect/monitor thread to be spawned and connect() to be called
            this.connectionService.enable();
        }
    }

    @Override
    public void connect() {
        // First, notify the connection service that you're attempting to connect. You must go through this CONNECTING
        // state before you go to CONNECTED, otherwise the CONNECTED state will be ignored.
        this.connectionService.setConnectionState(ConnectionState.CONNECTING);

        this.serverURL = this.connectionConfig.getWebServiceIP() + this.connectionConfig.getWebServiceURL();
        String userName = this.connectionConfig.getUserName();
        this.connectionService.log().info("Connecting to WebAPI at '{}';  USER: '{}'  On domain: '{}'",
                this.serverURL, userName, connectionConfig.getDomain());
        String debugFile = this.connectionConfig.getDebugIndexFile();
        this.logDataApi = new SecureApi(this.connectionConfig, this.connectionService.log(),
                debugFile, this.getDatasourceName());
        if (this.logDataApi.isAvailable()) {
            this.connectionService.setConnectionState(ConnectionState.CONNECTED);
        } else {
            this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
        }
    }

    @Override
    public boolean monitor() {
        // This function will be called periodically to ensure the connection is "live". Do whatever makes sense for
        // your datasource.
        // If the connection is dead, return false. This will cause disconnect() to be called so you can clean
        // up resources and transition to DISCONNECTED.
        if (!this.logDataApi.isAvailable()) {
            this.connectionService.log().error("Not connected to Web API");
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        // Transition to the disconnected state.
        this.connectionService.setConnectionState(ConnectionState.DISCONNECTED);
    }

    @Override
    public void index(SyncMode syncMode) {
        ScalarPropertyV1 cp = new ScalarPropertyV1();
        cp.setName(SeeqNames.Properties.ExpectDuplicatesDuringIndexing);
        cp.setValue(true);
        connectionService.getDatasource().addAdditionalPropertiesItem(cp);


        String mode = this.connectionConfig.getHierarchyMode();
        this.connectionService.log().info("Getting INDEX using mode: "+mode+" options: (flat, ptypes)");
//        String debugFile = this.connectionConfig.getDebugIndexFile();
        Indexer indexer = new Indexer(this.logDataApi, this.connectionService.log());
        if (mode == null) {
            mode = "tagid";
        }
        indexer.CreateSeeqHierarchy(connectionService, mode, this.connectionConfig.getName());
    }

    @Override
    public Stream<Sample> getSamples(GetSamplesParameters parameters) {
        List<Sample> samples = new ArrayList<>();

        // Return a stream to iterate through all of the samples in the time range.
        // LogDataApi logDataApi = new LogDataApi(this.serverURL, this.connectionService.log());
        SignalQuery query = new SignalQuery(this.logDataApi, this.connectionService.log());
        String userZone = this.connectionConfig.getUserTimeZone();
        long start = parameters.getStartTime().getTimestamp();
        long finish= parameters.getEndTime().getTimestamp();
        long delta = 24L*60*60*1000000000L;
        delta *= isPrimarySignal(parameters.getDataId()) ? 30L : 10000L;
        for (long t = start; t<=finish; t+=delta+1) {
            TimeInstant s = new TimeInstant(t);
            long e = t + delta;
            if (e > finish) e = finish;
            TimeInstant f = new TimeInstant(e);
            samples.addAll( query.getSamples(s, f,
                    parameters.getDataId(), parameters.getSampleLimit(), userZone));
        }

//		samples.addAll( query.getSamples(parameters.getStartTime(),  parameters.getEndTime(),
//				parameters.getDataId(), parameters.getSampleLimit(), userZone));
        return samples.stream();
    }

    @Override
    public Stream<Capsule> getCapsules(GetCapsulesParameters parameters) throws Exception {
        // Keeping this capsules method in place in case there is a need to use it in the future.
        // To implement, see the Seeq Connector SDK for default simulator content.
        return Stream.empty();
    }

    @Override
    public void saveConfig() {
        // Configuration persistence is typically managed by the connector, which stores a list of all connection
        // configurations.
        this.connector.saveConfig();
    }

    private boolean isPrimarySignal(String dataid) {
        String[] tokens = dataid.split("\\|");
        if (tokens.length != 4) {
            this.connectionService.log().error("Data ID not formated correctly on query: " + dataid);
            return false;
        }
        this.connectionService.log().debug(tokens[3]+ " Data ID "+dataid + " is primary: "+Boolean.toString(tokens[3].equals("PRI")));
        return tokens[3].equals("PRI");
    }
}
