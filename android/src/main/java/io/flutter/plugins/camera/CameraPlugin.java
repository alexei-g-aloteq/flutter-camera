// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.camera;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugins.camera.CameraPermissions.PermissionsRegistry;
import io.flutter.view.TextureRegistry;

public final class CameraPlugin implements FlutterPlugin, ActivityAware {

  private final CameraPermissions cameraPermissions = new CameraPermissions();
  private final Registrar registrar;
  private Camera camera;
  private static final String TAG = "CameraPlugin";
  private @Nullable FlutterPluginBinding flutterPluginBinding;
  private @Nullable MethodCallHandlerImpl methodCallHandler;

  public CameraPlugin() {}

  public static void registerWith(Registrar registrar) {
    CameraPlugin plugin = new CameraPlugin();

    plugin.maybeStartListening(
        registrar.activity(),
        registrar.messenger(),
        registrar::addRequestPermissionsResultListener,
        registrar.view()
    );
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    this.flutterPluginBinding = binding;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    this.flutterPluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    maybeStartListening(
        binding.getActivity(),
        flutterPluginBinding.getFlutterEngine().getDartExecutor(),
        binding::addRequestPermissionsResultListener,
        flutterPluginBinding.getFlutterEngine().getRenderer());
  }

  @Override
  public void onDetachedFromActivity() {
    if (methodCallHandler == null) {
      // Could be on too low of an SDK to have started listening originally.
      return;
    }

    methodCallHandler.stopListening();
    methodCallHandler = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  private void maybeStartListening(
      Activity activity,
      BinaryMessenger messenger,
      PermissionsRegistry permissionsRegistry,
      TextureRegistry textureRegistry) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      // If the sdk is less than 21 (min sdk for Camera2) we don't register the plugin.
      return;
    }
    methodCallHandler =
        new MethodCallHandlerImpl(
            activity, messenger, new CameraPermissions(), permissionsRegistry, textureRegistry);
  }
}
