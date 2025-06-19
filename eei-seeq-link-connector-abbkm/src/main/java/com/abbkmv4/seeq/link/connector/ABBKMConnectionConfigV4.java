package com.abbkmv4.seeq.link.connector;

import com.seeq.link.sdk.DefaultPullDatasourceConnectionConfig;

/**
 * The configuration object should be a Plain Old Java Object (POJO) with little to no logic, just fields.
 */
public class ABBKMConnectionConfigV4 extends DefaultPullDatasourceConnectionConfig {
    private String webServiceIP;
    private String webServiceURL;
    private int webServicePort;
    private int webServiceTimeout;
    private String webServiceMethod;
    private String hierarchyMode;
    private String userTimeZone;
    private String userName;
    private String password;
    private String domain;
    private String debugIndexFile;
    private boolean virtualForgeEnabled;


    public void setVirtualForgeEnabled(boolean enabled) {
        virtualForgeEnabled = enabled;
    }
    public boolean getVirtualForgeEnabled() {
        return this.virtualForgeEnabled;
    }

    public void setWebServiceTimeout(int port) {
        this.webServiceTimeout = port;
    }

    public int getWebServiceTimeout() {
        return this.webServiceTimeout;
    }


    public void setWebServicePort(int port) {
        this.webServicePort = port;
    }

    public int getWebServicePort() {
        return this.webServicePort;
    }

    public void setWebServiceMethod(String method) {
        this.webServiceMethod = method;
    }
    public String getWebServiceMethod() {
        return this.webServiceMethod;
    }

    public void setWebServiceURL(String url) {
        this.webServiceURL = url;
    }
    public String getWebServiceURL() {
        return this.webServiceURL;
    }


    public void setUserName(String name) {
        userName = name;
    }
    public String getUserName() {
        return this.userName;
    }

    public void setPassword(String passwd) {
        password = passwd;
    }
    public String getPassword() {
        return this.password;
    }

    public void setDomain(String dm) {
        domain = dm;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setUserTimeZone(String zone) {
        userTimeZone = zone;
    }

    public String getUserTimeZone() {
        return this.userTimeZone;
    }

    public void setHierarchyMode(String mode) {
        hierarchyMode = mode;
    }

    public String getHierarchyMode() {
        return this.hierarchyMode;
    }

    public String getWebServiceIP() {
        return this.webServiceIP;
    }

    public void setWebServiceIP(String url) {
        this.webServiceIP= url;
    }

    public void setDebugIndexFile(String zone) {
        debugIndexFile = zone;
    }

    public String getDebugIndexFile() {
        return this.debugIndexFile;
    }

}
