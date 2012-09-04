package org.haagensoftware.netty.webserver.spi;

import java.util.List;

import no.haagensoftware.netty.webserver.ServerInfo;

import org.jboss.netty.channel.ChannelHandler;

public abstract class NettyWebserverRouterPlugin {

	public abstract List<String> getRoutes();
	
	public abstract ChannelHandler getHandlerForRoute(String route);
	
	public abstract void setServerInfo(ServerInfo serverInfo);
}
