// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
// New Extension that I want to add.
package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailList;

import com.google.appinventor.components.annotations.PropertyCategory;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.os.AsyncTask;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import android.content.Context;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.LogRecord;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Date;

import org.apache.http.conn.util.InetAddressUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.json.JSONException;

import java.awt.dnd.DragGestureEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import com.google.appinventor.components.runtime.collect.Lists;


@DesignerComponent(version = 1,
        description = "Component needed to connect to BlockyTalky system",
        category = ComponentCategory.CONNECTIVITY,
        nonVisible = true,
        iconName = "images/bt.png")
@SimpleObject
@UsesPermissions(permissionNames =  "android.permission.INTERNET, " +
                                    "android.permission.CHANGE_WIFI_MULTICAST_STATE, " +
                                    "android.permission.ACCESS_NETWORK_STATE, " +
                                    "android.permission.ACCESS_WIFI_STATE")

@UsesLibraries(libraries = "java_websocket.jar")
public class BlockyTalky extends AndroidNonvisibleComponent implements Component {
    private static String LOG_TAG = "BLOCKYTALKY";
    private ConcurrentHashMap<String, LocalBTData> localBTs;
    private HashMap<String, String > headers;
    private Handler handler;
    private Context mContext;
    private Handler announcerHandler;
    private String receivedMessage = "";
    private String receivedMessageFrom = "";
    private String nodeName = "BlockyTalky";
    private EmptyClient client = null;
    private final String blockyTalkyMessageRouter = "ws://btrouter.getdown.org:8005/dax";
    private DatagramSocket ds;
    private MulticastSocket broadcastSocket;
    final int UNICAST_PORT = 8675;
    final int MULTICAST_PORT = 8676;
    private final int MAX_PACKET_SIZE = 65535;
    private final String MULTICAST_ADDRESS = "224.0.0.1";



    private final Handler androidUIHandler;



    /**
     * Creates a new BlockyTalky component.
     */
    public BlockyTalky(ComponentContainer container) {
        super(container.$form());
        androidUIHandler = new Handler();

        localBTs = new ConcurrentHashMap<String, LocalBTData>();
        headers = new HashMap<String, String>(){{
            put("Sec-WebSocket-Protocol","echo-protocol");
        }};
        handler = new Handler();
        announcerHandler = new Handler();
        Log.d(LOG_TAG, "Done with BlockyTalky constructor.");
        String addr =  getIPAddress(true);
        if(addr.equals("")){
            addr = getIPAddress(false);
        }

        try {
            mContext = container.$context();
            InetAddress ip = InetAddress.getByName(addr); //Android ip
            InetAddress bip = InetAddress.getByName(MULTICAST_ADDRESS);
            ds = new DatagramSocket(UNICAST_PORT);
            //ds.setReuseAddress(true);
            broadcastSocket = new MulticastSocket(MULTICAST_PORT);
            broadcastSocket.joinGroup(bip);
            broadcastSocket.setBroadcast(true);
            MessageListener listener = new MessageListener();
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
            BroadcastListener broadcastListener = new BroadcastListener();
            Thread broadcastThread = new Thread(broadcastListener);
            broadcastThread.start();
            aquireMulticastLock();
            BroadcastAnnouncer announcer = new BroadcastAnnouncer();
            Thread announcerThread = new Thread(announcer);
            announcerThread.start();
        } catch (SocketException e) {
            Log.d(LOG_TAG,"Caught SocketException while trying to initialize BlockyTalky messaging: " + e.getMessage());
            e.printStackTrace();
        } catch(UnknownHostException e) {
            Log.d(LOG_TAG,"Caught UnknownHostException while trying to reach Blockytalky messaging router: " + e.getMessage());
        } catch(Exception e){
            Log.d(LOG_TAG,"Exception while initializing BlockyTalky messaging: " + e);
            e.printStackTrace();
        }
        connectToMessagingRouter();

    }

    private void connectToMessagingRouter(){
        try {
          Log.d(LOG_TAG, "Opening connection to BlockyTalky messaging router");
          client = new EmptyClient(this.nodeName, new URI(blockyTalkyMessageRouter), new Draft_10(), headers, 10000);
          client.connect();
        } catch (Exception e) {
          Log.d(LOG_TAG, "Exception Caught while trying to connect to messasge router: " + e);
        }
    }

    private void aquireMulticastLock(){
        WifiManager wifi = (WifiManager)mContext.getSystemService( Context.WIFI_SERVICE );
        if(wifi != null){
            WifiManager.MulticastLock lock = wifi.createMulticastLock(LOG_TAG);
            lock.acquire();
        }else{
            Log.d(LOG_TAG, "WIFI MANAGER NULL");
        }
    }

    /*
    * Gets IP Address for android phone
    */
    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    private void sendString(String message, String destination) {
        BlockyTalkyMessage btMessage = new BlockyTalkyMessage(this.nodeName, destination, message);
        Log.d(LOG_TAG,"Sending to: " + destination);
        if (localBTs.get(destination) == null) {
            if (client != null && client.isOpen()) {
                client.send(btMessage.toJson());
            }else{
               //reopen client
                connectToMessagingRouter();
                client.send(btMessage.toJson()); //@fixme: message will likely be dropped on the floor, since registration will not be complete when this statement executes.
            }

        } else {
            new SendMessageTask().execute(btMessage.toJson(), destination);
        }
    }

    @SimpleFunction(description = "Sends a message to a BlockyTalky")
    public void SendMessage(String message, String destination) {
        this.sendString(message, destination);
        // new SendMessageTask().execute(message,destination);
    }


    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "BlockyTalky")
    @SimpleProperty(description = "Name of message sender")
    public void NodeName(String name) {

      //
      // if ( (this.nodeName != null) && (this.nodeName != name) && (this.client.isOpen())) { //name change
      //   this.client.close();
      //   this.client = null;
      // }
      this.nodeName = name;
      if(client == null || !client.isOpen()){
          connectToMessagingRouter();
      }

    }

    // @SimpleProperty(description = "List of Local BlockyTalkies")
    // public List<String> LocalBlockyTalkies(){
    //     List<String> localBTNames;
    //     localBTNames = Lists.newArrayList();
    //     localBTNames.add("TEST");
    //     for(String key : localBTs.keySet()){
    //         localBTNames.add(key);
    //     }
    //     return localBTNames;
    // }

    @SimpleProperty(description = "Contents of message received from WebSocket.")
    public String Message() { return receivedMessage; }
/*
    @SimpleProperty(description = "Sender of message received from WebSocket.")
    public String Sender() { return receivedMessageFrom; }
*/

    @SimpleEvent(description = "Message was received from WebSocket.")
    public void OnMessageReceived() {
        Log.d(LOG_TAG, "inside BlockyTalky.OnMessageReceived");
        androidUIHandler.post(new Runnable(){
            public void run(){
                EventDispatcher.dispatchEvent(BlockyTalky.this, "OnMessageReceived");
            }
        });
    }

    public class LocalBTData {
        private InetAddress ip;
        private int port;
        private long lastReceived;

        public LocalBTData(InetAddress ip, int port, Date lastReceived){
            this.ip = ip;
            this.lastReceived = lastReceived.getTime();
            this.port = port;
        }

        public InetAddress getIP(){
            return this.ip;
        }

        public int getPort(){
            return this.port;
        }

        public long getLastReceived(){
            return this.lastReceived;
        }

        public void setIP(InetAddress ip){
            this.ip = ip;
        }

        public void setPort(int port){
            this.port = port;
        }

        public void setLastReceived(Date newDate){
            this.lastReceived = newDate.getTime();
        }

    }

    class SendMessageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String msg = strings[0];
                String dest = strings[1];
                InetAddress ip = localBTs.get(dest).getIP();
                int port = localBTs.get(dest).getPort();
                byte[] data = msg.getBytes();
                int len = data.length;
                DatagramPacket dp = new DatagramPacket(data, len, ip, port);
                ds.send(dp);
            } catch (IOException e) {
                Log.d(LOG_TAG,"Caught IO Exception: " + e.getMessage());
            }
            return "Success";
        }
    }

    class MessageListener implements Runnable {
        @Override
        public void run() {
            byte[] data = new byte[MAX_PACKET_SIZE];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            while(true){
                try{
                    Log.d(LOG_TAG,"BlockyTalky MessageListener about to block on receive()");
                    ds.receive(dp);
                    String json = new String(dp.getData(), 0, dp.getLength());
                    BlockyTalkyMessage btMessage = new BlockyTalkyMessage(json);
                    receivedMessage = btMessage.content;
                    receivedMessageFrom = btMessage.source;
                    Log.d(LOG_TAG,"Received unicast UDP message: " + receivedMessage);
                    handler.post(new Runnable() {
                        public void run() {
                            OnMessageReceived();
                        }
                    });
                }catch(IOException e){
                    System.err.println("Caught IO Exception: " + e.getMessage());
                }
            }
        }
    }

    class BroadcastListener implements Runnable {

        @Override
        public void run() {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data,1024);
            while(true){
                try{
                    broadcastSocket.receive(dp);
                    String message = new String(dp.getData(), 0, dp.getLength());
                    BlockyTalkyMessage btMessage = new BlockyTalkyMessage(message);
                    String btName = btMessage.source;
                    String btContent = btMessage.content;
                    String btDest = btMessage.destination;
                    if(btDest.equals("announce")){
                        Log.d(LOG_TAG,"RECIEVED ANNOUNCEMENT");
                        Date timeStamp = new Date();
                        LocalBTData btData = new LocalBTData(dp.getAddress(),dp.getPort(),timeStamp);
                        localBTs.put(btName,btData);
                        Log.d(LOG_TAG,"**** Mapping " + btName + " -> " + dp.getAddress());
                    }
                    // if(localBTs.containsKey(btName){
                    // }else{
                    // }
                    System.out.println(message);
                }catch(IOException e){
                    Log.d(LOG_TAG,"IO EXCEPTION IN BROADCAST LISTENER");
                    System.err.println("Caught IO Exception: " + e.getMessage());
                }catch(Exception e){
                    Log.d(LOG_TAG,"EXCEPTION IN BROADCAST LISTENER " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private void filterLocalBTs(){
        Date now = new Date();
        long curMilSeconds = now.getTime();
        for(String key : localBTs.keySet()){
            long lastMilSeconds = localBTs.get(key).getLastReceived();
            if(curMilSeconds - lastMilSeconds > 60000){
                Log.d(LOG_TAG, "REMOVING " + key);
                localBTs.remove(key);
            }
        }
    }

    private class BroadcastAnnouncer implements Runnable{
        @Override
        public void run(){
            //Testing print out of localBTs
            Log.d(LOG_TAG,"BTUNITS: ");
            for(String key : localBTs.keySet()){
                Date now = new Date();
                long curMilSeconds = now.getTime();
                long lastMilSeconds = localBTs.get(key).getLastReceived();
                Log.d(LOG_TAG, key);
                Log.d(LOG_TAG, "AGE : " + (curMilSeconds - lastMilSeconds)/1000);
            }
            filterLocalBTs();
            //Send Announcement
            try{
                BlockyTalkyMessage announcement = new BlockyTalkyMessage(nodeName, "announce", "announce");
                String msg = announcement.toJson();
                byte[] data = msg.getBytes();
                int len = data.length;
                DatagramPacket dp = new DatagramPacket(data, len, InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT);
                ds.send(dp);
            }catch(Exception e){
                Log.d(LOG_TAG, "Exception sending broadcast announcement :" + e);
                e.printStackTrace();
            }

            announcerHandler.postDelayed(this,3000);
        }
    }

    public class BlockyTalkyMessage {
        private String source;
        private String destination;
        private String content;
        private String jsonFormatString =
                "{\"py/object\": \"__main__.Message\", \"channel\": \"Message\", \"content\": \"%s\", \"destination\": \"%s\", \"source\": \"%s\"}";

        public BlockyTalkyMessage(String source, String destination, String content) {
            this.source = source;
            this.destination = destination;
            this.content = content;
        }

        public BlockyTalkyMessage(String json) {
            JSONObject message;
            try {
                message = new JSONObject(json);
                this.source = message.getString("source");
                this.destination = message.getString("destination");
                this.content = message.getString("content");
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception while parsing json");
                e.printStackTrace();
            }
        }

        public String toJson() {
            return String.format(
                    this.jsonFormatString,
                    this.content,
                    this.destination,
                    this.source);
        }
    }

    private class EmptyClient extends WebSocketClient {

      private boolean isReadyForUse = false;
      private String nodeName = null;

      public EmptyClient(String nodeName, URI serverUri, Draft draft, HashMap<String, String > protocol, int timeout) {
          super(serverUri, draft, protocol, timeout);
          this.nodeName = nodeName;
      }
      public EmptyClient(String nodeName, URI serverUri, Draft draft) {
          super(serverUri, draft);
          this.nodeName = nodeName;
      }
      public EmptyClient(String nodeName, URI serverURI) {
          super(serverURI);
          this.nodeName = nodeName;
      }

      public boolean readyToCommunicate(){
        boolean ret = false;
        synchronized(this){
          ret = this.isReadyForUse;
        }
        return ret;
      }

      @Override
      public void onOpen(ServerHandshake handshakedata) {
          Log.d(LOG_TAG, "BlockyTalky message router connection opened... attempting registration");
          BlockyTalkyMessage registerMessage = new BlockyTalkyMessage(this.nodeName, "dax", "");
          super.send(registerMessage.toJson());
          Log.d(LOG_TAG, "Registered with BlockyTalky message router as " + this.nodeName);
          synchronized(this){
            isReadyForUse = true;
          }
      }

      @Override
      public void send(String message){
        if (readyToCommunicate()){
          super.send(message);
        }else{
          Log.d(LOG_TAG, "Could not send message via messaging router because it is not connected and ready: " + message);
        }
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
          Log.d(LOG_TAG, "closed with exit code " + code + " additional info: " + reason);
          client = null;
      }

      @Override
      public void onMessage(String json) {
          BlockyTalkyMessage message = new BlockyTalkyMessage(json);
          receivedMessage = message.content;
          receivedMessageFrom = message.source;
          Log.d(LOG_TAG, "*****received message: " + message.toJson());
          handler.post(new Runnable() {
              public void run() {
                  OnMessageReceived();
              }
          });
      }

      @Override
      public void onError(Exception ex) {
          Log.d(LOG_TAG, "an error occured:" + ex);
      }
    }
}
