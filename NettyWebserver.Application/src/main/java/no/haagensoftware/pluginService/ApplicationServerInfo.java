package no.haagensoftware.pluginService;

import no.haagensoftware.netty.webserver.ServerInfo;

public class ApplicationServerInfo implements ServerInfo {
	private String webappdir;
	
	public ApplicationServerInfo() {
		webappdir = System.getProperty("netty-webserver.webappDirectory");
	}
	
	@Override
	public String getWebappPath() {
		return webappdir;
	}
}
