package sg.assignment.shopback.moviediscovery.network.themoviedb;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sg.assignment.shopback.moviediscovery.R;
import sg.assignment.shopback.moviediscovery.data.MovieDetails;
import sg.assignment.shopback.moviediscovery.data.realm.Movie;
import sg.assignment.shopback.moviediscovery.network.volley.VolleyManager;
import sg.assignment.shopback.moviediscovery.utils.FormatterHelper;

/**
 * Created by archie on 25/2/17.
 */

public class MovieApi {

    protected interface Error {
        public void onError(String message);
    }

    public interface MoviesResult extends Error  {
        public void onSuccess(List<Movie> movies);
    }

    public interface MovieDetailsResult extends Error {
        public void onSuccess(MovieDetails movieDetails);
    }

    public interface MovieImageResult extends Error {
        public void onSuccess(Bitmap image);
    }

    private final String TAG = getClass().getSimpleName().toString();
    private Context apiContext;

    public MovieApi(Context context) {
        if(context == null)
            throw new NullPointerException("Context cannot be null");
        if(!VolleyManager.isInitialized())
            VolleyManager.init(context);
        apiContext = context;
    }

    public void fetchMoviesByDescendingReleaseDate(int page, final MoviesResult result) {
        fetchMovies(page, "release_date.desc", result);
    }

    public void fetchMovies(int page, String sorting, final MoviesResult result) {
        if(result == null)
            return;

        if(isNoInternetConnection(result))
            return;

        if(page < 1)
            throw new RuntimeException("Page number must be greater than 0");
        if(page > 1000) {
            result.onSuccess(new ArrayList<Movie>());
            return;
        }

        String url = apiContext.getString(R.string.themoviedb_api_catalog_url);
        String key = apiContext.getString(R.string.themoviedb_api_key);

        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                String.format(url + "?api_key=%s" +
                                "&primary_release_date.lte=%s&sort_by=%s&page=%s",
                        key, FormatterHelper.formatDate(new Date()), sorting, page),
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                List<Movie> movies_result = new ArrayList<Movie>();

                try {
                    if(isError(response, result))
                        return;

                    JSONArray movies = response.getJSONArray("results");

                    for(int i = 0; i < movies.length(); i++){
                        JSONObject movie = movies.getJSONObject(i);
                        Movie movie_result = new Movie();
                        movie_result.setPosterPath(
                                fixUrl(apiContext.getString(R.string.themoviedb_images_poster_url))
                                        + cleanUri(movie.getString("poster_path"))
                        );
                        movie_result.setPopularity(movie.getDouble("popularity"));
                        movie_result.setReleaseDate(FormatterHelper.parseDate(movie.getString("release_date")));
                        movie_result.setTitle(movie.getString("title"));
                        movie_result.setId(movie.getLong("id"));
                        movies_result.add(movie_result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    result.onError(e.getMessage());
                    return;
                }

                result.onSuccess(movies_result);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                result.onError(error.getMessage());
            }
        });

        VolleyManager.getRequestQueue().add(jsonReq);
    }


    public void fetchMovieDetails(final Movie movie, final MovieDetailsResult result) {

        if(result == null)
            return;

        if(isNoInternetConnection(result))
            return;

        if(movie == null)
            throw new NullPointerException("Movie cannot be null");

        String url = apiContext.getString(R.string.themoviedb_api_details_url);
        String key = apiContext.getString(R.string.themoviedb_api_key);

        url = fixUrl(url);

        JsonObjectRequest jsonReq = new JsonObjectRequest(Request.Method.GET,
                String.format(url + "%s?api_key=%s",
                        movie.getId(), key),
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                MovieDetails movie_result = new MovieDetails();

                try {
                   if(isError(response, result))
                       return;

                    movie_result.setIntro(movie);
                    movie_result.setBackdropPath(
                            fixUrl(apiContext.getString(R.string.themoviedb_images_backdrop_url))
                                    + cleanUri(response.getString("backdrop_path"))
                    );
                    movie_result.setIntro(movie);
                    movie_result.setLanguages(getStringArray(response.getJSONArray("spoken_languages")));
                    movie_result.setGenres(getStringArray(response.getJSONArray("genres")));
                    movie_result.setSynopsis(response.getString("overview").trim());
                    if(!response.isNull("runtime"))
                        movie_result.setDuration(response.getInt("runtime"));
                    else
                        movie_result.setDuration(null);

                } catch (JSONException e) {
                    e.printStackTrace();
                    result.onError(e.getMessage());
                    return;
                }

                result.onSuccess(movie_result);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                result.onError(error.getMessage());
            }
        });

        VolleyManager.getRequestQueue().add(jsonReq);
    }

    protected String[] getStringArray(JSONArray string_array) throws JSONException {
        String[] result = new String[string_array.length()];
        for(int i = 0; i < string_array.length(); i++)
            result[i] = string_array.getJSONObject(i).getString("name");
        return result;
    }


    protected String fixUrl(String url) {
        if(!url.endsWith("/"))
            url += "/";
        return url;
    }

    protected String cleanUri(String uri) {
        if(uri.startsWith("\\/"))
            uri = uri.substring(2);
        if(uri.startsWith("/"))
            uri = uri.substring(1);
        return uri;
    }

    protected boolean isError(JSONObject response, Error result) throws JSONException {

        JSONArray errors = null;
        String error = null;
        if(response.has("errors"))
            errors = response.getJSONArray("errors");
        if(response.has("status_message"))
            error  = response.getString("status_message");
        if(errors != null && errors.length() > 0){
            result.onError(errors.getString(0));
            return true;
        }
        if(error != null) {
            result.onError(error);
            return true;
        }

        return false;
    }

    protected boolean isNoInternetConnection(Error result) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) apiContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean is_no_internet = !(activeNetworkInfo != null && activeNetworkInfo.isConnected());
        if(is_no_internet && result != null)
            result.onError(apiContext.getString(R.string.error_no_internet));
        return is_no_internet;
    }


}
