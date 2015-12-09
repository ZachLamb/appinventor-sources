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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Date;

/**
 * Camera provides access to the phone's camera
 *
 *
 */
@DesignerComponent(version = 2,
   description = "This is version 2 of BlockyTalky.",
   category = ComponentCategory.EXTENSION,
   nonVisible = true,
   iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class BlockyTalky extends AndroidNonvisibleComponent
    implements  Component {
  private static final String CAMERA_OUTPUT = "output";
  private final ComponentContainer container;
  private Uri imageFile;

  /* Used to identify the call to startActivityForResult. Will be passed back
  into the resultReturned() callback method. */
  private int requestCode;

  // whether to open into the front-facing camera
  private boolean useFront;

  /**
   * Creates a Camera component.
   *
   * Camera has a boolean option to request the forward-facing camera via an intent extra.
   *
   * @param container container, component will be placed in
   */
  public BlockyTalky(ComponentContainer container) {
    super(container.$form());
    this.container = container;
  }

  /**
   * Returns true if the front-facing camera is to be used (when available)
   *
   * @return {@code true} indicates front-facing is to be used, {@code false} will open default
   */
  @SimpleProperty(
    category = PropertyCategory.BEHAVIOR)
  public boolean UseFront() {
    return useFront;
  }

  /**
   * Specifies whether the front-facing camera should be used (when available)
   *
   * @param front
   *          {@code true} for front-facing camera, {@code false} for default
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty(description = "Specifies whether the front-facing camera should be used (when available). "
    + "If the device does not have a front-facing camera, this option will be ignored "
    + "and the camera will open normally.")
  public void UseFront(boolean front) {
    useFront = front;
  }

  /**
   * Takes a picture, then raises the AfterPicture event.
   * If useFront is true, adds an extra to the intent that requests the front-facing camera.
   */
  @SimpleFunction
  public void doSomething() {
    Log.i("CameraComponent", "Yasssss");
  }
  // @SimpleEvent
  // public void AfterPicture(String image) {
  //   Log.i("CameraComponent", "Deleted file "
  // }
}
