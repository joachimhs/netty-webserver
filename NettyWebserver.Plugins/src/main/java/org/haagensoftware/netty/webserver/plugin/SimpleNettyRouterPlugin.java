package org.haagensoftware.netty.webserver.plugin;

import java.util.ArrayList;
import java.util.List;

import no.haagensoftware.netty.webserver.ServerInfo;
import no.haagensoftware.netty.webserver.handler.FileServerHandler;

import org.haagensoftware.netty.webserver.spi.NettyWebserverRouterPlugin;
import org.jboss.netty.channel.ChannelHandler;

/**
 * A simple router plugin to be able to serve the Haagen-Software.no website. 
 * @author joahaa
 *
 */
public class SimpleNettyRouterPlugin extends NettyWebserverRouterPlugin {
	private List<String> routes;
	private ServerInfo serverInfo;
	
	public SimpleNettyRouterPlugin() {
		routes = new ArrayList<String>();
		routes.add("startsWith:/about");
		routes.add("startsWith:/opensource");
		routes.add("startsWith:/blog");
		routes.add("startsWith:/consultancy");
		routes.add("startsWith:/eurekaj");
		routes.add("startsWith:/btrace");
		routes.add("startsWith:/pages/");
		routes.add("startsWith:/cv");
		routes.add("equals:/index.html");
		routes.add("equals:/");
		routes.add("startsWith:/cachedScript");
		//routes.add("endsWith:.json");
		//routes.add("endsWith:.jsons");
	}
	
	@Override
	public List<String> getRoutes() {
		return routes;
	}

	@Override
	public ChannelHandler getHandlerForRoute(String route) {
		//TODO: Expand with logic for handling specific routes.
		if (route.equalsIgnoreCase("startsWith:/cachedScript")) {
			return new CachedScriptHandler(route.substring(11));
		}
		return new CachedIndexHandler(serverInfo.getWebappPath(), 0);
		
		//return new FileServerHandler(serverInfo.getWebappPath());
	}
	
	@Override
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
		
	}

}
