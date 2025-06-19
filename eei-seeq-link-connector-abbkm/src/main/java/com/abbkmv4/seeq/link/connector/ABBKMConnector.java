package com.abbkmv4.seeq.link.connector;

import java.util.UUID;

import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.interfaces.ConnectorServiceV2;
import com.seeq.link.sdk.interfaces.ConnectorV2;

/**
 * Implements the {@link ConnectorV2} interface for facilitating dataflow from external systems with Seeq Server.
 */
public class ABBKMConnector implements ConnectorV2 {
    private ConnectorServiceV2 connectorService;
    private ABBKMConnectorConfigV4 connectorConfig;

    @Override
    public String getName() {
        // This name will be used for the configuration file that is found in the data/configuration/link folder.
        return "ABB KM Connector V3";
    }

    @Override
    public void initialize(ConnectorServiceV2 connectorService) throws Exception {
        this.connectorService = connectorService;

        // First, load your configuration using the connector service. If the configuration file is not found, the first
        // object in the passed-in array is returned.
        ConfigObject configObj = this.connectorService.loadConfig(new ConfigObject[] { new ABBKMConnectorConfigV4() });
        this.connectorConfig = (ABBKMConnectorConfigV4) configObj;

        // Check to see if there are any connections configured yet
        if (this.connectorConfig.getConnections().size() == 0) {
            // Create a default connection configuration
            ABBKMConnectionConfigV4 connectionConfig = new ABBKMConnectionConfigV4();

            // The user will likely change this. It's what will appear in the list of datasources in Seeq Workbench.
            connectionConfig.setName("ABBKM-Default Connection");

            // The identifier must be unique. It need not be a UUID, but that's recommended in lieu of anything else.
            connectionConfig.setId(UUID.randomUUID().toString());

            // Normally you would probably leave the default connection disabled to start, but for this example we
            // want to start up in a functioning state.
            connectionConfig.setEnabled(true);

            // ABBKM specific settings
            connectionConfig.setHierarchyMode("tagid");
            connectionConfig.setWebServiceMethod("http");
            connectionConfig.setWebServiceIP("10.138.207.92");
            connectionConfig.setWebServiceURL("/ExtDataAccess/api");
            connectionConfig.setWebServiceTimeout(180000);
            connectionConfig.setUserTimeZone("GMT+02:00");

            connectionConfig.setUserName("test");
            connectionConfig.setPassword("test@endeavoreng.com");
            connectionConfig.setDomain(null);
            connectionConfig.setWebServicePort(80);
            connectionConfig.setDebugIndexFile(null);
            connectionConfig.setVirtualForgeEnabled(false);

            // Add the new connection configuration to its parent connector
            this.connectorConfig.getConnections().add(connectionConfig);
        }

        // Now instantiate your connections based on the configuration.
        // Iterate through the configurations to create connection objects.
        for (ABBKMConnectionConfigV4 connectionConfig : this.connectorConfig.getConnections()) {
            if (connectionConfig.getId() == null) {
                // If the ID is null, then the user likely copy/pasted an existing connection configuration and
                // removed the ID so that a new one would be generated. Generate the new one!
                connectionConfig.setId(UUID.randomUUID().toString());
            }

            if (!connectionConfig.isEnabled()) {
                // If the connection is not enabled, then do not add it to the list of connections
                continue;
            }

            this.connectorService.addConnection(new ABBKMConnection(this, connectionConfig));
        }

        // Finally, save the connector configuration in a file for the user to view and modify as needed
        this.connectorService.saveConfig(this.connectorConfig);
    }

    @Override
    public void destroy() {
        // Perform any connector-wide cleanup as necessary here
    }

    public void saveConfig() {
        // This may be called after indexing activity to save the next scheduled indexing date/time
        this.connectorService.saveConfig(this.connectorConfig);
    }
}
