package com.phicomm.box.tvremote.httpserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.phicomm.box.tvremote.Configs;
import com.phicomm.box.tvremote.util.Utils;
import com.phicomm.box.tvremote.beans.ApplicationInfo;
import com.phicomm.box.tvremote.beans.ApplicationList;
import com.phicomm.box.tvremote.beans.Status;
import com.phicomm.box.tvremote.impl.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author:xufeng02.zhou
 * Date  : 2017-05-17
 * last modified: 2017-05-17
 */
public class RemoterServer extends NanoHTTPD {
    private final static String TAG = "Remoter";
    private final static int PORT = Configs.DEFAULT_SERVICE_PORT;;
    private final static String MIME_PNG = "image/png";
    private final static String MIME_JPG = "image/jpg";

    private IRemoterService mService;
    private Context mContext;

    public RemoterServer(Context context, IRemoterService server) {
        super(PORT);
        mService = server;
        mContext = context;
    }


    public RemoterServer(Context context, int port, IRemoterService server) {
        super(port);
        mService = server;
        mContext = context;
    }

    @Override
    public void start() throws IOException {
        Log.d(TAG, "RemoterServer start()");
        super.start();
    }

    protected Response onGetCommand(String path, Map<String, String> map) {
        Log.d(TAG, "onGetCommand path=" + path + ", map=" + map.toString());
        if (path.equals("status")) {
            String name = mService.getName();
            String sn = mService.getSN();
            int strength = mService.getWifiStrength();
            long online = mService.getOnlineTime();
            Status status = new Status(name, sn, strength, online);
            Gson gson = new Gson();
            return new Response(Response.Status.OK, MIME_PLAINTEXT, gson.toJson(status));
        } else if (path.equals("appicon")) {
            String app = map.get("appid");
            Drawable icon = mService.getApplicationIcon(app);
            Bitmap bitmap = Utils.drawable2Bitmap(icon);
            if (bitmap != null) {
                ByteArrayOutputStream outs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outs);
                bitmap.recycle();
                ByteArrayInputStream isBm = new ByteArrayInputStream(outs.toByteArray());
                try {
                    outs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new NanoHTTPD.Response(Response.Status.OK, MIME_PNG, isBm);
            } else {
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "internal error");
            }
        } else if (path.equals("ping")) {
            Gson gson = new Gson();
            return new Response(Response.Status.OK, MIME_PLAINTEXT, gson.toJson("OK"));
        } else if (path.equals("applist")) {
            ArrayList<ApplicationInfo> infos = mService.getAppList();
            ApplicationList list = new ApplicationList(infos.size(), infos);
            Gson gson = new Gson();
            return new Response(Response.Status.OK, MIME_PLAINTEXT, gson.toJson(list));
        } else if (path.equals("screenshot")) {
            Log.d(TAG, "screenshot start");
            Bitmap bitmap = mService.onScreenShot();
            Log.d(TAG, "finish screenshot");
            try {
                ByteArrayOutputStream outs = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outs);
                bitmap.recycle();
                Log.d(TAG, "compress ok");
                ByteArrayInputStream isBm = new ByteArrayInputStream(outs.toByteArray());
                try {
                    outs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new NanoHTTPD.Response(Response.Status.OK, MIME_JPG, isBm);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (path.equals("history")) {
            //todo
        }
        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "unkown command");
    }

    protected Response onPostCommand(String path, IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        String data = session.getQueryParameterString();
        Log.d(TAG, "onPostCommand path=" + path + ", body=" + data);
        if (path.equals("keyevent")) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                int keyCode = jsonObject.getInt("keycode");
                boolean longClick = jsonObject.getBoolean("longclick");
                mService.onKeyEvent(keyCode, longClick);
                return new Response(Response.Status.OK, MIME_PLAINTEXT, "OK");
            } catch (JSONException e) {
                Log.d(TAG, "parse json failed");
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Parse json failed");
            }
        } else if (path.equals("action")) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String command = jsonObject.getString("action");
                if (doAction(command)) {
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "OK");
                }
                return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "unkown action");
            } catch (JSONException e) {
                Log.d(TAG, "parse json failed");
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Parse json failed");
            }
        } else if (path.equals("application")) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String packageName = jsonObject.getString("package");
                String className = jsonObject.getString("activity");
                mService.openApplication(packageName, className);
                return new Response(Response.Status.OK, MIME_PLAINTEXT, "OK");
            } catch (JSONException e) {
                Log.d(TAG, "parse json failed");
                return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Parse json failed");
            }
        } else if (path.equals("voice")) {
            InputStream is = session.getInputStream();
            File cacheFile = new File(mContext.getCacheDir(), "voice");


        } else if (path.equals("history")) {
            //todo
        }

        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "unkown command");
    }

    boolean doAction(String action) {
        if (action.equals("shutdown")) {
            mService.onShutDown();
            return true;
        } else if (action.equals("pre")) {
            mService.onPre();
            return true;
        } else if (action.equals("next")) {
            mService.onNext();
            return true;
        }

        return false;
    }

    protected Response onPutCommand(String cmd, String body) {

        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "unkown command");
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();
        Method method = session.getMethod();
        Map<String, String> parms = session.getParms();
        Log.d(TAG, "serve:uri=" + uri + " ,method=" + method);
        String[] paths = uri.split("\\/");

        if (paths.length >= 3) {
            if (paths[1].equals("v1")) {

                switch (method) {
                    case GET:
                        return onGetCommand(paths[2], parms);
                    case POST:
                        return onPostCommand(paths[2], session);
                    case PUT:
                        Map<String, String> files = new HashMap<String, String>();
                        try {
                            session.parseBody(files);
                            String body = session.getQueryParameterString();
                            return onPutCommand(paths[2], body);
                        } catch (ResponseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }

            } else {

            }
        }
        return super.serve(session);
    }
}
