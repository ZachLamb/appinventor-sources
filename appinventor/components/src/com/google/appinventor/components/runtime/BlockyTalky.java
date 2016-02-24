// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

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

// import org.java_websocket.client.WebSocketClient;
// import org.java_websocket.drafts.Draft;
// import org.java_websocket.drafts.Draft_10;
// import org.java_websocket.handshake.ServerHandshake;
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
import com.google.appinventor.components.runtime.RandomNameGenerator;
@DesignerComponent(version = 2,
   description = "This is version 2 of BlockyTalky.",
   category = ComponentCategory.EXTENSION,
   nonVisible = true,
   iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, " +
                                    "android.permission.CHANGE_WIFI_MULTICAST_STATE, " +
                                    "android.permission.ACCESS_NETWORK_STATE, " +
                                    "android.permission.ACCESS_WIFI_STATE")
public class BlockyTalky extends AndroidNonvisibleComponent
    implements  Component {
    private static String LOG_TAG = "BLOCKYTALKY";
    //
    private final ComponentContainer container;
    private String nodeName = "null";
	private int itemTextColor;
	private int itemBackgroundColor;
	public final static int DEFAULT_ITEM_TEXT_COLOR = Component.COLOR_GREEN;
	public final static int DEFAULT_ITEM_BACKGROUND_COLOR = Component.COLOR_BLACK;

  /* Used to identify the call to startActivityForResult. Will be passed back
  into the resultReturned() callback method. */


  sendMessage()
  private int requestCode;
  public BlockyTalky(ComponentContainer container) 
  {
    super(container.$form());
    this.container = container;
  }
  	// property 
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty(description = "Name of message sender")
    public void NodeName(String name) 
    {
      if(name == "null")
      {
        RandomNameGenerator random = new RandomNameGenerator();
        name = random.GenerateRandomName();
      }
      else
      {
      	Log.i("BlockyTalky", "There's already a name");
      }
    }
  @SimpleFunction(description = "Sends a message to a BlockyTalky")
    public void SendMessage(String message, String destination) 
    {
        Log.i("BlockyTalky", "I'm sending a message to" + destination);
        //check network connectivity
        if(nearbyBlockyTalkies().contains(destination)){
        	//send message

        }
        else{
        	//what do I do now?
        	return null
        }

    }

   // return nearby BlockyTalky's
  // @SimpleProperty(description = "Will return BlockyTalkys nearby.")
  @SimpleFunction
  public List<String> nearbyBlockyTalkies() 
  {
    List<String> nearbyBTs = new ArrayList<String>();
      nearbyBTs.add("Everything");
    for(String key : localBTs.keySet()){
            nearbyBTs.add(key);
        }
   return nearbyBTs;
  }
}
public class AnnnouncementBroadcaster
{
  
}
