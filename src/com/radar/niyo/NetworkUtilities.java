/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.radar.niyo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_UPDATED = "timestamp";
    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms
    public static final String BASE_URL = "http://niyoapi.appspot.com";
//    public static final String BASE_URL = "http://10.20.5.126:6366";
//    public static final String BASE_URL = "http://192.168.1.130:6366";
//    public static final String BASE_URL = "http://192.168.0.110:6366";
//    public static final String BASE_URL = "http://192.168.1.129:6366";
    public static final String AUTH_URI = BASE_URL + "/auth";
    public static final String FETCH_FRIEND_UPDATES_URI =
        BASE_URL + "/fetch_friend_updates";
    public static final String FETCH_STATUS_URI = BASE_URL + "/fetch_status";
    private static HttpClient mHttpClient;

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Connects to the Voiper server, authenticates the provided username and
     * password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
//    public static boolean authenticate(String username, String password,
//        Handler handler, final Context context) {
//        final HttpResponse resp;
//
//        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair(PARAM_USERNAME, username));
//        params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
//        HttpEntity entity = null;
//        try {
//            entity = new UrlEncodedFormEntity(params);
//        } catch (final UnsupportedEncodingException e) {
//            // this should never happen.
//            throw new AssertionError(e);
//        }
//        final HttpPost post = new HttpPost(AUTH_URI);
//        post.addHeader(entity.getContentType());
//        post.setEntity(entity);
//        maybeCreateHttpClient();
//        
//        sendResult(true, handler, context);
//        return true;

//        try {
//            resp = mHttpClient.execute(post);
//            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                    Log.v(TAG, "Successful authentication");
//                }
//                sendResult(true, handler, context);
//                return true;
//            } else {
//                if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
//                }
//                sendResult(false, handler, context);
//                return false;
//            }
//        } catch (final IOException e) {
//            if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                Log.v(TAG, "IOException when getting authtoken", e);
//            }
//            sendResult(false, handler, context);
//            return false;
//        } finally {
//            if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                Log.v(TAG, "getAuthtoken completing");
//            }
//        }
//    }

    /**
     * Sends the authentication response from server back to the caller main UI
     * thread through its handler.
     * 
     * @param result The boolean holding authentication result
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     */
//    private static void sendResult(final Boolean result, final Handler handler,
//        final Context context) {
//        if (handler == null || context == null) {
//            return;
//        }
//        handler.post(new Runnable() {
//            public void run() {
//                ((AuthenticatorActivity) context).onAuthenticationResult(result);
//            }
//        });
//    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
//    public static Thread attemptAuth(final String username,
//        final String password, final Handler handler, final Context context) {
//        final Runnable runnable = new Runnable() {
//            public void run() {
//                authenticate(username, password, handler, context);
//            }
//        };
//        // run on background thread.
//        return NetworkUtilities.performOnBackgroundThread(runnable);
//    }

}
