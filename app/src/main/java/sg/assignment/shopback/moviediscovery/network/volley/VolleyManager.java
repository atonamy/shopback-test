package sg.assignment.shopback.moviediscovery.network.volley;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by archie on 25/2/17.
 */

public class VolleyManager {

    private static RequestQueue requestQueue = null;

    public static void init(Context context) {
        if(context != null && requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static boolean isInitialized() {
        return (requestQueue != null) ? true : false;
    }

}
