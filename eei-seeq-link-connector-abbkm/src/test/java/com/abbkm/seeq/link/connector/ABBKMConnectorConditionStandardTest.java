package com.abbkm.seeq.link.connector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.abbkmv4.seeq.link.connector.ABBKMConnection;
import com.abbkmv4.seeq.link.connector.ABBKMConnectionConfigV4;
import com.abbkmv4.seeq.link.connector.ABBKMConnector;
import com.abbkmv4.seeq.link.connector.ABBKMConnectorConfigV4;
import com.seeq.link.sdk.ConfigObject;
import com.seeq.link.sdk.interfaces.ConnectorServiceV2;
import com.seeq.link.sdk.testframework.ConditionPullConnectionTestSuite;

public class BBKMConnectorConditionStandardTest extends ConditionPullConnectionTestSuite<ABBKMConnection, ABBKMConnector,
        ABBKMConnectionConfigV4, ABBKMConnectorConfigV4> {
    private ABBKMConnector ABBKMConnectorMock;
    private ABBKMConnection ABBKMConnection;

    private final Map<StandardTest, String> DATA_IDS_FOR_STANDARD_TESTS = Map.of(
            StandardTest.CapsulesStartingAfterIntervalOnly, "condition-data-id-1",
            StandardTest.CapsulesStartingBeforeIntervalOnly, "condition-data-id-2",
            StandardTest.CapsuleStartsAtEndTime, "condition-data-id-3",
            StandardTest.CapsuleStartsAtStartTime, "condition-data-id-4",
            StandardTest.CapsuleStartsOneNanosecondAfterStart, "condition-data-id-5",
            StandardTest.CapsuleStartsOneNanosecondBeforeEnd, "condition-data-id-6"
    );

    @Override
    public String dataIdForTest(String testName) {
        return DATA_IDS_FOR_STANDARD_TESTS.get(StandardTest.valueOf(testName));
    }

    @Override
    public void conditionPullConnectionOneTimeSetUp() {
    }

    @Override
    public void pullConnectionOneTimeSetUp() {
    }

    @Override
    public void indexingConnectionOneTimeSetUp() {
    }

    @Override
    public ABBKMConnection getConnection() {
        return ABBKMConnection;
    }

    @Override
    public ABBKMConnector getConnector() {
        return ABBKMConnectorMock;
    }

    /*
     * If for some reason, you need to ignore any standard test in the suite, use this method to specify. An example
     * can be seen in the {@link MyConnectorSignalStandardTest#getIgnoredTests()} method.
     */
    @Override
    public List<IgnoredTest> getIgnoredTests() {
        return List.of();
    }

    /*
     * Use this method to configure the connector and connection that should be used for all standard tests in
     * the suite
     */
    @Override
    public void baseConnectionOneTimeSetUp() {
        var connectionConfig = new ABBKMConnectionConfigV4();
        connectionConfig.setSamplePeriod("1s");
        connectionConfig.setTagCount(5);
        connectionConfig.setEnabled(true);

        var connectorConfig = new ABBKMConnectorConfigV4();
        connectorConfig.setConnections(List.of(connectionConfig));

        try {
            var mockConnectorService = mock(ConnectorServiceV2.class);
            when(mockConnectorService.loadConfig(any())).thenReturn(connectorConfig);

            this.ABBKMConnectorMock = mock();
            ABBKMConnection = new ABBKMConnection(this.ABBKMConnectorMock, connectionConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ConfigObject> getConnectorConfigVersions() {
        return List.of(new ABBKMConnectorConfigV4());
    }
}
