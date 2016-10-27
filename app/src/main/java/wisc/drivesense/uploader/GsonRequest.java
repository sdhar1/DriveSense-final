package wisc.drivesense.uploader;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by peter on 10/27/16.
 */

public class GsonRequest extends StringRequest {
    public GsonRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        String httpPostBody="grant_type=password&username=Alice&password=password123";
        // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
        try {
            httpPostBody=httpPostBody+"&randomFieldFilledWithAwkwardCharacters="+ URLEncoder.encode("{{%stuffToBe Escaped/","UTF-8");
        } catch (UnsupportedEncodingException exception) {
            Log.e("GsonRequest", "exception", exception);
            // return null and don't pass any POST string if you encounter encoding error
            return null;
        }
        return httpPostBody.getBytes();
    }
}
