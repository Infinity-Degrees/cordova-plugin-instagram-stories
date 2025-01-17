/**
 */
package com.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.pm.PackageManager;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

import android.util.Log;
import android.util.Base64;

import java.io.FileOutputStream;
import android.net.Uri;
import android.content.Intent;
import android.app.Activity;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import androidx.core.content.FileProvider;
import java.io.InputStream;

import android.os.Build;

public class IGStory extends CordovaPlugin {
  private static final String TAG = "IGStory";
  private CallbackContext callback = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing IGStory");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    PackageManager pm = this.cordova.getActivity().getBaseContext().getPackageManager();

    if (isPackageInstalled("com.instagram.android", pm)) {
      if (action.equals("shareToStory")) {
        String backgroundImageUrl = args.getString(0);
        String stickerAssetUrl = args.getString(1);
        String attributionLinkUrl = args.getString(2);
        String backgroundTopColor = args.getString(3);
        String backgroundBottomColor = args.getString(4);

        shareToStory(backgroundImageUrl, stickerAssetUrl, attributionLinkUrl, backgroundTopColor, backgroundBottomColor, callbackContext);
      } else if (action.equals("shareImageToStory")) {
        String backgroundImageData = args.getString(0);

        shareImageToStory(backgroundImageData, callbackContext);
      } else if (action.equals("shareVideoToStory")) {
        String backgroundVideoUrl = args.getString(0);

        String stickerAssetUrl = args.getString(1);
        if (stickerAssetUrl.equals("") || stickerAssetUrl.length() == 0) {
          stickerAssetUrl = null;
        }

        String attributionLinkUrl = args.getString(2);
        if (attributionLinkUrl.equals("") || attributionLinkUrl.length() == 0) {
          attributionLinkUrl = null;
        }

        String backgroundTopColor = args.getString(3);
        if (backgroundTopColor.equals("") || backgroundTopColor.length() == 0) {
          backgroundTopColor = null;
        }

        String backgroundBottomColor = args.getString(4);
        if (backgroundBottomColor.equals("") || backgroundBottomColor.length() == 0) {
          backgroundBottomColor = null;
        }

        shareVideoToStory(backgroundVideoUrl, stickerAssetUrl, attributionLinkUrl, backgroundTopColor, backgroundBottomColor, callbackContext);
      } else {
        callbackContext.error("ig not installed");
      }

    }

    return true;
  }

  private void shareToStory(String backgroundImageUrl, String stickerImageUrl, String attributionLinkUrl, String backgroundTopColor, String backgroundBottomColor, CallbackContext callbackContext) {

    if (!backgroundTopColor.isEmpty() && !backgroundBottomColor.isEmpty()) {
      try {
        File parentDir = this.webView.getContext().getExternalFilesDir(null);
        File stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);
        Uri stickerUri = null;

        URL u = new URL(stickerImageUrl);
        saveImage(u, stickerImageFile);

        String type = "image/*";

        Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
        
        String sourceApplication = "1936818296682390"; // This is your application's FB ID
        intent.putExtra("source_application", sourceApplication);
        
        intent.setType(type);
        intent.putExtra("content_url", attributionLinkUrl);
        intent.putExtra("top_background_color", backgroundTopColor);
        intent.putExtra("bottom_background_color", backgroundBottomColor);


        FileProvider FileProvider = new FileProvider();
        stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);
        intent.putExtra("interactive_asset_uri", stickerUri);

        // Instantiate activity and verify it will resolve implicit intent
        Activity activity = this.cordova.getActivity();
        activity.grantUriPermission("com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.startActivityForResult(intent, 0);
        callbackContext.success("shared");
      } catch (Exception e) {
        Log.e(TAG, "We have an exception!");
        Log.e(TAG, e.getMessage());
        callbackContext.error(e.getMessage());
      }

    } else {
      try {
        File parentDir = this.webView.getContext().getExternalFilesDir(null);
        File backgroundImageFile = File.createTempFile("instagramBackground", ".png", parentDir);
        File stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);
        Uri stickerUri = null;
        Uri backgroundUri = null;

        URL stickerURL = new URL(stickerImageUrl);
        saveImage(stickerURL, stickerImageFile);

        URL backgroundURL = new URL(backgroundImageUrl);
        saveImage(backgroundURL, backgroundImageFile);

        // Instantiate implicit intent with ADD_TO_STORY action,
        // background asset, sticker asset, and attribution link
        Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
        
        String sourceApplication = "1936818296682390"; // This is your application's FB ID
        intent.putExtra("source_application", sourceApplication);
        
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        FileProvider FileProvider = new FileProvider();
        stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);
        backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,backgroundImageFile);

        intent.setDataAndType(backgroundUri, "image/*");
        intent.putExtra("interactive_asset_uri", stickerUri);

        intent.putExtra("content_url", attributionLinkUrl);

        // Instantiate activity and verify it will resolve implicit intent
        Activity activity = this.cordova.getActivity();
        activity.grantUriPermission("com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.startActivityForResult(intent, 0);
        callbackContext.success("shared");
      } catch (Exception e) {
        callbackContext.error(e.getMessage());
      }
    }
  }

  private void shareImageToStory(String backgroundImageData, CallbackContext callbackContext) {

    try {
      File parentDir = this.webView.getContext().getExternalFilesDir(null);
      File backgroundImageFile = File.createTempFile("instagramBackground", ".jpg", parentDir);
      Log.i(TAG, "made it here");

      saveImage(backgroundImageData, backgroundImageFile);

      Log.i(TAG, "savedImage");

      Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");

      String sourceApplication = "1936818296682390"; // This is your application's FB ID
      intent.putExtra("source_application", sourceApplication);
      
      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      
      FileProvider FileProvider = new FileProvider();
      Uri backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider", backgroundImageFile);

      Log.i(TAG, "got backgroundUri: " + backgroundUri);

      intent.setDataAndType(backgroundUri, "image/jpeg");

      Log.i(TAG, "instantiating activity");
      // Instantiate activity and verify it will resolve implicit intent
      Activity activity = this.cordova.getActivity();
      activity.grantUriPermission("com.instagram.android", backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

      activity.startActivityForResult(intent, 0);
      callbackContext.success("shared");
    } catch (Exception e) {
      Log.e(TAG, "error in shareImageToStory");
      Log.e(TAG, e.getMessage());
      callbackContext.error(e.getMessage());
    }

  }

  private void shareVideoToStory(String backgroundVideoUrl, String stickerImageUrl, String attributionLinkUrl, String backgroundTopColor, String backgroundBottomColor, CallbackContext callbackContext) {

    try {
      File parentDir = this.webView.getContext().getExternalFilesDir(null);
      File backgroundVideoFile = File.createTempFile("instagramBackground", ".mp4", parentDir);
      File stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);
      Uri stickerUri = null;
      Uri backgroundUri = null;

      Log.i(TAG, "made it here");

      if (stickerImageUrl != null) {
        URL stickerURL = new URL(stickerImageUrl);
        saveImage(stickerURL, stickerImageFile);
      }

      URL backgroundURL = new URL(backgroundVideoUrl);
      saveImage(backgroundURL, backgroundVideoFile);

      Log.i(TAG, "savedVideo");

      Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
      
      String sourceApplication = "1936818296682390"; // This is your application's FB ID
      intent.putExtra("source_application", sourceApplication);
      
      intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      
      FileProvider FileProvider = new FileProvider();
      if (stickerImageUrl != null) {
        stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);
        Log.i(TAG, "got stickerUri: " + stickerUri);
      }
      
      backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,backgroundVideoFile);

      Log.i(TAG, "got backgroundUri: " + backgroundUri);

      intent.setDataAndType(backgroundUri, "video/mp4");
      
      if (stickerImageUrl != null) {
        intent.putExtra("interactive_asset_uri", stickerUri);
      }

      if (attributionLinkUrl != null) {
        intent.putExtra("content_url", attributionLinkUrl);
      }

      if (backgroundTopColor != null && backgroundBottomColor != null) {
        intent.putExtra("top_background_color", backgroundTopColor);
        intent.putExtra("bottom_background_color", backgroundBottomColor);
      }

      Log.i(TAG, "instantiating activity");
      // Instantiate activity and verify it will resolve implicit intent
      Activity activity = this.cordova.getActivity();

      if (stickerImageUrl != null) {
        activity.grantUriPermission("com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }

      activity.grantUriPermission("com.instagram.android", backgroundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

      activity.startActivityForResult(intent, 0);
      callbackContext.success("shared");
    } catch (Exception e) {
      Log.e(TAG, "error in shareVideoToStory");
      Log.e(TAG, e.getMessage());
      callbackContext.error(e.getMessage());
    }

  }

  private boolean isPackageInstalled(String packageName, PackageManager packageManager) {

    boolean found = true;

    try {

      packageManager.getPackageInfo(packageName, 0);
    } catch (PackageManager.NameNotFoundException e) {

      found = false;
    }

    return found;
  }

  private byte[] downloadUrl(URL toDownload) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      byte[] chunk = new byte[4096];
      int bytesRead;
      InputStream stream = toDownload.openStream();

      while ((bytesRead = stream.read(chunk)) > 0) {
        outputStream.write(chunk, 0, bytesRead);
      }

    } catch (IOException e) {
      Log.e(TAG, "SAVE ERROR (IO): " + e.getMessage());
      return null;
    } catch (Exception e) {
      Log.e(TAG, "SAVE ERROR (REG): " + e.getMessage());
      return null;
    }

    return outputStream.toByteArray();
  }

  private void saveImage(String imageData, File file) {
    FileOutputStream os = null;

    try {
      os = new FileOutputStream(file, true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      String encodedImg = imageData.substring(imageData.indexOf(";base64,") + 8);
      byte[] imgBytesData = Base64.decode(encodedImg, Base64.DEFAULT);
      
      os.write(imgBytesData);
      os.flush();
      os.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      Log.e(TAG, "SAVE ERROR: " + e.getMessage());
    }
  }

  private void saveImage(URL pathUrl, File file) {
    FileOutputStream os = null;

    try {
      os = new FileOutputStream(file, true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      os.write(downloadUrl(pathUrl));
      os.flush();
      os.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      Log.e(TAG, "SAVE ERROR: " + e.getMessage());
    }
  }
}
