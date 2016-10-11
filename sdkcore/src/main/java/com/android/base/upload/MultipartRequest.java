package com.android.base.upload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.mopub.volley.AuthFailureError;
import com.mopub.volley.NetworkResponse;
import com.mopub.volley.Request;
import com.mopub.volley.Response;
import com.mopub.volley.Response.ErrorListener;
import com.mopub.volley.Response.Listener;
import com.mopub.volley.toolbox.HttpHeaderParser;

public class MultipartRequest extends Request<String> {

    MultipartEntity mMultiPartEntity = new MultipartEntity();

    Map<String, String> mHeaders = new HashMap<String, String>();

    private final Listener<String> mListener;

    /**
     * Creates a new request with the given url.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     */
    public MultipartRequest(String url, Listener<String> listener) {
        this(url, listener, null);
    }

    /**
     * Creates a new POST request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public MultipartRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
    }

    /**
     * @return
     */
    public MultipartEntity getMultiPartEntity() {
        return mMultiPartEntity;
    }

    @Override
    public String getBodyContentType() {
        return mMultiPartEntity.getContentType().getValue();
    }

    public void addHeader(String key, String value) {
        mHeaders.put(key, value);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    public byte[] getBody() {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // multipart body
            mMultiPartEntity.writeTo(bos);
        } catch (IOException e) {
            Log.e("", "IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed = "";
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
	@Override
	public Priority getPriority() {
		return Priority.HIGH;
	}
//	@Override
//	public RetryPolicy getRetryPolicy() {
//		return new DefaultRetryPolicy(30000, 0, 1.0f);
//	}
}