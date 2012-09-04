package no.haagensoftware.netty.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import no.haagensoftware.netty.webserver.classloader.PluginClassLoader;
import no.haagensoftware.netty.webserver.pipeline.NettyWebserverPipelineFactory;
import no.haagensoftware.pluginService.ApplicationRouterPluginService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.haagensoftware.netty.webserver.spi.NettyWebserverRouterPlugin;
import org.haagensoftware.netty.webserver.util.IntegerParser;
import org.haagensoftware.netty.webserver.util.JarUtils;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.google.gson.Gson;

public class Main {
	private static Logger logger = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) throws Exception{
		//new Main().run();
		configure();
		
		Main main = new Main();
		main.run();
	}
	
	private static void configure() throws Exception {
		Properties properties = new Properties();
		File configFile = new File("config.properties");
		if (!configFile.exists()) {
			configFile = new File("../config.properties");
		}
		if (!configFile.exists()) {
			configFile = new File("../../config.properties");
		}
		if (configFile.exists()) {
			FileInputStream configStream = new FileInputStream(configFile);
			properties.load(configStream);
			configStream.close();
			logger.info("Server properties loaded from " + configFile.getAbsolutePath());
			for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
				Object property = (String) e.nextElement();
				logger.info("\t\t* " + property + "=" + properties.get(property));
			}
		}
		
		setProperties(properties);
	}

	private static void setProperties(Properties properties) {
		Enumeration<Object> propEnum = properties.keys();
		while (propEnum.hasMoreElements()) {
			String property = (String) propEnum.nextElement();
			System.setProperty(property, properties.getProperty(property));
		}
		
		if (System.getProperty("netty-webserver.port") == null) {
			System.setProperty("netty-webserver.port", "8080");
			logger.info(" * Property 'netty-webserver.port' is not specified. Using default: 8080. Configure in file config.properties.");
		}
		
		if (System.getProperty("netty-webserver.webappDirectory") == null) {
			System.setProperty("netty-webserver.webappDirectory", "webapp");
			logger.info(" * Property 'netty-webserver.webappDirectory' is not specified. Using default: 'webapp' Configure in file config.properties.");
		}
		
		if (System.getProperty("netty-webserver.pluginDirectory") == null) {
			System.setProperty("netty-webserver.pluginDirectory", "plugins");
			logger.info(" * Property 'netty-webserver.pluginDirectory' is not specified. Using default: 'plugins' Configure in file config.properties.");
		}
	}
	
	private static String downloadJarFromUrl(String url, String toPath) {
		String jarFilename = url.substring(url.lastIndexOf(File.separatorChar));
		String fqJarFileName = toPath + File.separatorChar + jarFilename; 
		File file = new File(fqJarFileName);
		if (file != null && file.exists() && file.isFile()) {
			logger.info("Jar already downloaded, no need to download file again");
		} else {
			logger.info("Downloading Jar from: " + url + " to: " + toPath);
		
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			
			HttpResponse response;
			
			try {
				response = httpclient.execute(httpGet);
				int statusCode = response.getStatusLine().getStatusCode();
			
				HttpEntity entity = response.getEntity();
				if (entity != null && statusCode == 200) {
					FileOutputStream fos = new java.io.FileOutputStream(fqJarFileName);
					entity.writeTo(fos);
					fos.close();
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return file.getAbsolutePath();
	}
	
	public void run() throws Exception {
		String webappDir = System.getProperty("netty-webserver.webappDirectory", "/srv/netty-webserver/webapp");
		System.setProperty("basedir", webappDir);
		
		String pluginDir = System.getProperty("netty-webserver.pluginDirectory", "/srv/netty-webserver/plugins");
		
		PluginClassLoader cl = new PluginClassLoader(Thread.currentThread().getClass().getClassLoader());
		cl.addJarsFromDirectory(pluginDir);
		
		ApplicationRouterPluginService applicationRouterPluginService = new ApplicationRouterPluginService();
		List<NettyWebserverRouterPlugin> routerPlugins = applicationRouterPluginService.getRouterPlugins();
		logger.info("Starting server");
				
		Integer port = IntegerParser.parseIntegerFromString(System.getProperty("netty-webserver.port"), 8080);
		
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        // Set up the event pipeline factory          .
        bootstrap.setPipelineFactory(new NettyWebserverPipelineFactory(routerPlugins));

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        
        logger.info("Started server on port: " + port + " hosting directory: " + webappDir);
    }
}
