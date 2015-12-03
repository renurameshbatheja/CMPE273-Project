package com.redisapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.redisapp.util.ObjectToJsp;
import com.redisapp.util.RedisClient;
import com.redisapp.util.RedisManager;
import com.redisapp.util.RedisServerInfo;


/**
 * Servlet implementation class MainServlet
 * @author renu.batheja
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(MainServlet.class.getName());

	public MainServlet() {
		super();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		log.info("Initialising Servlet!");
		RedisClient.initNodeAndData();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> requestParams = request.getParameterMap();
		takeAction(requestParams, request, response);

	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Map<String, String[]> requestParams = request.getParameterMap();
		takeAction(requestParams, request, response);
	}

	
	/**
	 * @param requestParams
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * Based on the requestParams, determine which 'action' has to be taken.
	 */
	private void takeAction(Map<String, String[]> requestParams, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String hostIP = null;
		String hostPort = null;
		String id = null;
		String actionToBeTaken = null;
		String queryString = null;
		String configuredInstances = null;

		for(String key : requestParams.keySet()) {
			log.info(key + " : " + (requestParams.get(key))[0].toString());
			if(key.equals("hostIP"))
				hostIP = (requestParams.get(key))[0].toString();
			if(key.equals("hostPort"))
				hostPort = (requestParams.get(key))[0].toString();
			if(key.equals("ServerID"))
				id = (requestParams.get(key))[0].toString();
			if(key.equals("actionToBeTaken"))
				actionToBeTaken = (requestParams.get(key))[0].toString();
			if(key.equals("queryString"))
				queryString = (requestParams.get(key))[0].toString();
			if(key.equals("configuredInstances"))
				configuredInstances = (requestParams.get(key))[0].toString();

		}

		if(actionToBeTaken != null) {
			if(actionToBeTaken.equalsIgnoreCase("add")) {
				response.setContentType("text/html");
				PrintWriter out= response.getWriter();

				try {
					if(RedisClient.servers.contains(new RedisServerInfo(hostIP, hostPort, id))) {
						log.info("This Redis Instance is already configured!");
						out.println("<br/><strong>This Redis Instance is already configured and available in the cluster.<br/>");
						out.println("Please enter correct values.</strong><br/>");
					}  else {
						boolean ableToConnect = true;
						Jedis jedis = new Jedis(hostIP, new Integer(hostPort));
						try {							
							jedis.ping();
						} catch(JedisConnectionException jce) {
							ableToConnect = false;
						}

						if (ableToConnect) {

							RedisManager.addNodesDynamic(hostIP, hostPort, id);
							response.setStatus(200);
							log.info("Node added successfully !");

							RedisServerInfo server = new RedisServerInfo(hostIP, hostPort, id);
							RedisClient.getRedisServerInfo(server, jedis);

							//changes for ajax
							out.println("<br/><strong>Redis Instance Added Successfully!</strong><br/><br/>");
							out.println("<strong>New Redis Instance Information</strong><br/>");
							printRedisServerInfo(out, server);
						} else {
							log.info("Not able to connect to this Redis Instance!");
							out.println("<br/><strong>Not able to connect to this Redis Instance!<br/>");
							out.println("Please ensure that this Instance is Running.</strong><br/>");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					response.setStatus(505);
					log.info("Error occurred while adding new node!");
					request.getRequestDispatcher("/errorPage.jsp").forward(request, response);
				}
			} else if(actionToBeTaken.equalsIgnoreCase("remove")) {
				response.setContentType("text/html");
				PrintWriter out= response.getWriter();
				if(!RedisClient.servers.contains(new RedisServerInfo(hostIP, hostPort, id))) {
					log.info("This Redis Instance is not configured!");
					out.println("<br/><strong>This Redis Instance is not configured in this cluster!<br/>");
					out.println("Please enter correct values.</strong><br/>");
				} else {
					boolean ableToConnect = true;
					Jedis jedis = new Jedis(hostIP, new Integer(hostPort));
					try {							
						jedis.ping();
					} catch(JedisConnectionException jce) {
						ableToConnect = false;
					} finally {
						jedis.close();
					}

					if (ableToConnect) {
						try {
							RedisManager.removeNode(hostIP, hostPort, id);
							response.setStatus(200);
							log.info("Redis Instance Removed successfully !");

							//changes for ajax
							out.println("<br/><strong>Redis Instance Removed Successfully!</strong><br/><br/>");
						} catch (Exception e) {
							e.printStackTrace();
							response.setStatus(505);
							log.info("Error occurred while removing new node!");
							request.getRequestDispatcher("/errorPage.jsp").forward(request, response);
						}	

					} else {
						log.info("Not able to connect to this Redis Instance!");
						out.println("<br/><strong>Not able to connect to this Redis Instance!<br/>");
						out.println("Please ensure that this Instance is Running.</strong><br/>");
					}
				}

			}
		} else if(configuredInstances != null) {
			response.setContentType("text/html");
			PrintWriter out= response.getWriter();
			int index = 0;
			for(RedisServerInfo redisServer : RedisClient.servers) {
				out.println("<strong>Redis Instance Information #"+index+"</strong><br/>");

				Jedis jedis = new Jedis(redisServer.getHost(), redisServer.getPort());
				RedisClient.getRedisServerInfo(redisServer, jedis);
				printRedisServerInfo(out, redisServer);		
				index++;
			}
		} else if(queryString != null) {
			try {
				ObjectToJsp data = new ObjectToJsp();
				log.info("Old QueryString : " + queryString);
				String newQueryString = queryString.replaceAll(" ", "+");
				log.info("New QueryString : " + newQueryString);
				data = RedisClient.storeDataInCache(newQueryString);
				response.setContentType("application/json");
				JSONObject json = new JSONObject();
				json.append("HitOrMiss", data.getHitorMiss());
				json.append("Content", data.getContent());
				response.getWriter().write(json.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	

	/**
	 * @param out
	 * @param redisServer
	 * Print the Redis Server information
	 */
	private void printRedisServerInfo(PrintWriter out, RedisServerInfo redisServer) {
		out.println("Hostname:PortNo : " + redisServer.getHost()+":"+redisServer.getPort() + "<br/>");
		out.println("Host ID : " + redisServer.getId() + "<br/>");
		out.println("Redis Version : " + redisServer.getRedisVersion() + "<br/>");
		out.println("Uptime in Seconds : " + redisServer.getUptimeInSeconds() + "<br/>");
		out.println("Connected Clients : " + redisServer.getConnectedClients() + "<br/>");
		out.println("Used Memory : " + redisServer.getUsedMemory() + "<br/>");
		out.println("Keyspace Hits : " + redisServer.getKeyHits() + "<br/>");
		out.println("Keyspace Misses : " + redisServer.getKeyMisses() + "<br/>");
		out.println("Role : " + redisServer.getRole() + "<br/>");
		out.println("No. of Keys : " + redisServer.getNoOfKeys() + "<br/><br/>");
	}
}
