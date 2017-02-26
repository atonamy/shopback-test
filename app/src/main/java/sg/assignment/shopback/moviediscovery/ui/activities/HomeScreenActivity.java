package sg.assignment.shopback.moviediscovery.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import com.tuyenmonkey.mkloader.MKLoader;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import sg.assignment.shopback.moviediscovery.R;
import sg.assignment.shopback.moviediscovery.data.realm.Movie;
import sg.assignment.shopback.moviediscovery.network.themoviedb.MovieApi;
import sg.assignment.shopback.moviediscovery.ui.adapters.MoviesAdapter;
import sg.assignment.shopback.moviediscovery.utils.FormatterHelper;
import sg.assignment.shopback.moviediscovery.utils.TransitionHelper;

public class HomeScreenActivity extends AppCompatActivity {

    private GridLayoutManager layoutManager;

    @BindView(R.id.toolbar)
    Toolbar activityToolbar;

    @BindView(R.id.movie_list)
    RecyclerView movieList;

    @BindView(R.id.progress)
    MKLoader progressWheal;

    @BindView(R.id.movies_refresh)
    WaveSwipeRefreshLayout moviesRefresh;

    @BindView(R.id.fab_scroll_up)
    FloatingActionButton scrollUpButton;

    private int currentPage;
    private boolean hasNewUpdates;
    private Handler activityHandler;
    private SweetAlertDialog errorDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home_screen);
        ButterKnife.bind(this);
        setSupportActionBar(activityToolbar);
        layoutManager = new GridLayoutManager(this, 2);
        activityHandler = new Handler();
        errorDialog = null;
        initMovies();
        initButtons();
    }

    protected void initButtons() {

        scrollUpButton.setVisibility(View.GONE);
        scrollUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               movieList.scrollToPosition(0);
            }
        });
    }

    protected void initMovies() {
        final LayoutInflater inflater = (LayoutInflater)getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        final MoviesAdapter adapter = new MoviesAdapter(this);
        movieList.setHasFixedSize(true);
        movieList.setLayoutManager(layoutManager);
        movieList.setAdapter(adapter);
        hasNewUpdates = true;
        currentPage = 1;

        populateMovies(currentPage, adapter);

        movieList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    int total_item_count = layoutManager.getItemCount();
                    int last_visible_item = layoutManager.findLastVisibleItemPosition();

                    if (dy > 0 && last_visible_item >= total_item_count - 1 && hasNewUpdates) {
                        hasNewUpdates = false;
                        currentPage++;
                        if (currentPage > 0 && currentPage <= 1000) {
                            activityHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setFooter(inflater.inflate(R.layout.movie_item_footer, null));
                                    populateMovies(currentPage, adapter);
                                }
                            });

                        }
                    }

                    if (last_visible_item > 4 && scrollUpButton.getVisibility() != View.VISIBLE)
                        activityHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showScrollButton(true);
                            }
                        });
                    else if (last_visible_item <= 4 && scrollUpButton.getVisibility() == View.VISIBLE)
                                    activityHandler.post(new Runnable() {
                                @Override
                                public void run () {
                                    showScrollButton(false);
                                }
                            });
            }
        });

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {

                switch(adapter.getItemViewType(position)){
                    case MoviesAdapter.TYPE_ITEM:
                        return 1;
                    case MoviesAdapter.TYPE_FOOTER:
                        return 2;
                    default:
                        return -1;
                }
            }
        });

        moviesRefresh.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                movieList.setVisibility(View.INVISIBLE);
                activityHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentPage = 1;
                        populateMovies(currentPage, adapter);
                    }
                }, 1500);
            }
        });

        adapter.setOnItemMovieClickListener(new MoviesAdapter.OnItemMovieClickListener() {
            @Override
            public void onClick(Movie movie, int position) {
                Intent intent = new Intent(HomeScreenActivity.this, DetailsScreenActivity.class);
                Pair<View, String>[] binding = TransitionHelper.
                        createSafeTransitionParticipants(HomeScreenActivity.this, false);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(HomeScreenActivity.this,
                        binding);
                intent.putExtra("movie_id", movie.getId());
                intent.putExtra("movie_popularity", movie.getPopularity());
                intent.putExtra("movie_poster_path", movie.getPosterPath());
                intent.putExtra("movie_release_date", FormatterHelper.formatDate(movie.getReleaseDate()));
                intent.putExtra("movie_title", movie.getTitle());
                startActivity(intent/*, options.toBundle()*/);
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void populateMovies(int page, final MoviesAdapter adapter) {
        MovieApi api = new MovieApi(this);
        api.fetchMoviesByDescendingReleaseDate(page, new MovieApi.MoviesResult() {

            @Override
            public void onError(String message) {

                if(errorDialog == null) {
                    errorDialog = new SweetAlertDialog(HomeScreenActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.error_title))
                            .setContentText(message);
                    errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            errorDialog = null;
                        }
                    });
                    errorDialog.setCancelable(true);
                    errorDialog.setCanceledOnTouchOutside(true);
                    errorDialog.show();
                }
                complete(true);
                showScrollButton(false);
            }

            @Override
            public void onSuccess(List<Movie> movies) {
                movieList.setVisibility(View.VISIBLE);
                boolean is_animate = (adapter.getItemCount() == 0);
                adapter.addMovies(movies);
                complete(movies.size() > 0);
                if(is_animate) {
                    animate();
                    showScrollButton(false);
                }
                else if(movies.size() > 0)
                    showScrollButton(true);
                else
                    showScrollButton(false);

            }

            private void animate() {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.zoom_in);
                movieList.startAnimation(animation);
            }

            private void complete(boolean new_updates) {
                adapter.setFooter(null);
                moviesRefresh.setRefreshing(false);
                progressWheal.setVisibility(View.GONE);
                hasNewUpdates = new_updates;
            }
        });
    }

    protected void showScrollButton(final boolean show) {
        if((show && scrollUpButton.getVisibility() == View.VISIBLE) ||
                (!show && scrollUpButton.getVisibility() != View.VISIBLE))
            return;
        if(show)
            scrollUpButton.setVisibility(View.VISIBLE);
        Animation animation = (show) ? AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in) :
                AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_out);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                scrollUpButton.clearAnimation();
                if(!show)
                    scrollUpButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        scrollUpButton.startAnimation(animation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityHandler.removeCallbacksAndMessages(null);
    }

}
