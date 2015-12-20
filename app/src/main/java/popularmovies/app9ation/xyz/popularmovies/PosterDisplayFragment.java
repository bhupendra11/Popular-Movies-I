package popularmovies.app9ation.xyz.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class PosterDisplayFragment extends Fragment {
    private MovieAdapter movieAdapter;
    private Movie movie;
    private String SORT_PARAM ="popularity.desc";
    private String VOTE_COUNT_MIN ="200";


    public PosterDisplayFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        movieAdapter = new MovieAdapter(getActivity() , new ArrayList<Movie>());

        GridView gridView  = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 movie = movieAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("MovieParcel", movie);


                startActivity(intent);


                //Toast.makeText(getContext(), movie.title,Toast.LENGTH_SHORT).show();

            }
        } );

        return  rootView;
    }

    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_fragment_main, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_sort_popularity){

            SORT_PARAM = "popularity.desc";
            updateMovies(SORT_PARAM);

        }

        if(id== R.id.action_sort_rating){

            SORT_PARAM = "vote_average.desc";
            updateMovies(SORT_PARAM);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movieParcel",movie);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onStart() {
        super.onStart();
        updateMovies(SORT_PARAM);
    }

    public void updateMovies(String sortType){
        FetchMovieDataTask movieDataTask = new FetchMovieDataTask();

        SORT_PARAM = sortType;
        movieDataTask.execute(SORT_PARAM);
    }










    public class FetchMovieDataTask extends AsyncTask<String, Void, ArrayList<Movie>>{

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        private ArrayList<Movie> getMoviesPosterDataFromJson(String moviesJsonStr)
                throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS= "results";
            final String TMDB_POSTER_PATH= "poster_path";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_TITLE = "title";
            final String TMDB_BACKDROP_PATH = "backdrop_path";
            final String TMDB_POPULARITY = "popularity";
            final String TMDB_VOTE_AVG = "vote_average";
            final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
            final String POSTER_SIZE = "w185";
            final String SMALL_POSTER_SIZE ="w92";
            final String BACKDROP_SIZE = "w500";
            final String RELEASE_DATE ="release_date";


            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);
            // TMDB returns json movie objects

            ArrayList<Movie> movieList = new ArrayList<Movie>();

          //  List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();

            for(int i = 0; i < moviesArray.length(); i++) {

                //Create a Movie  object
                Movie movie ;

                String poster_path;
                String overview;
                String title;
                String backdrop_path;
                String popularity;
                String vote_avg;
                String release_year;
                // Get the JSON object representing the movie
                JSONObject movieObject = moviesArray.getJSONObject(i);


                poster_path = BASE_IMAGE_URL + POSTER_SIZE +movieObject.getString(TMDB_POSTER_PATH) ;
                overview = movieObject.getString(TMDB_OVERVIEW);
                title = movieObject.getString(TMDB_TITLE);
                backdrop_path = BASE_IMAGE_URL + BACKDROP_SIZE +movieObject.getString(TMDB_BACKDROP_PATH);
                popularity = movieObject.getString(TMDB_POPULARITY);
                vote_avg = movieObject.getString(TMDB_VOTE_AVG);


                release_year =movieObject.getString(RELEASE_DATE)
                       // .substring(0,4)
                ;    // Get the movie Year from movie release_date string

                // create a movie object from above parameters
                movie = new Movie(poster_path,overview,title,backdrop_path,popularity,vote_avg, release_year);

                movieList.add(movie);


            }
               /* for (String s : resultStrs) {
                    Log.v(LOG_TAG, "Forecast entry: " + s);
                }
                */
            return movieList;
        }


        @Override
        protected ArrayList<Movie> doInBackground(String... params) {


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;



            try {
                // Construct the URL for the TMDB query
                // Possible parameters are available at TMDB's  API page, at


                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String APPID_PARAM = "api_key";
                final String VOTE_COUNT_GREATER ="vote_count.gte";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM,params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.TMDB_API_KEY)
                        .appendQueryParameter(VOTE_COUNT_GREATER,VOTE_COUNT_MIN)
                        .build();




                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI : "+builtUri.toString());

                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
                  Log.d(LOG_TAG,"Movies Json String: " +moviesJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return  null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesPosterDataFromJson(moviesJsonStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;

        }


        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {
            super.onPostExecute(movieList);

            if(movieList !=null) {
                movieAdapter.clear();

               Movie curMovie;
                for (int i = 0; i < movieList.size(); i++) {
                    curMovie = movieList.get(i);
                    movieAdapter.add(curMovie);
                }

            }


        }
    }
}
