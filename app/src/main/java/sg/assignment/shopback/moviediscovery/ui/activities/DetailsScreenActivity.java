package sg.assignment.shopback.moviediscovery.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;


import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tuyenmonkey.mkloader.MKLoader;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import sg.assignment.shopback.moviediscovery.R;
import sg.assignment.shopback.moviediscovery.data.MovieDetails;
import sg.assignment.shopback.moviediscovery.data.realm.Movie;
import sg.assignment.shopback.moviediscovery.network.themoviedb.MovieApi;
import sg.assignment.shopback.moviediscovery.ui.views.AppTextView;
import sg.assignment.shopback.moviediscovery.utils.FormatterHelper;

public class DetailsScreenActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar activityToolbar;

    @BindView(R.id.app_bar)
    AppBarLayout appToolbar;

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;

    @BindView(R.id.movie_title)
    AppTextView movieTitle;

    @BindView(R.id.release_date)
    AppTextView releaseDate;

    @BindView(R.id.movie_popularity)
    AppTextView moviePopularity;

    @BindView(R.id.linear_synopsis)
    View synopsisSection;

    @BindView(R.id.linear_synopsis1)
    View synopsisWrapped;

    @BindView(R.id.linear_synopsis2)
    View synopsisUnwrapped;

    @BindView(R.id.synopsis)
    AppTextView movieSynopsis;

    @BindView(R.id.linear_genres)
    View genresSection;

    @BindView(R.id.linear_genres1)
    View genresWrapped;

    @BindView(R.id.linear_genres2)
    View genresUnwrapped;

    @BindView(R.id.genres)
    AppTextView movieGenres;

    @BindView(R.id.linear_languages)
    View languagesSection;

    @BindView(R.id.linear_languages1)
    View languagesWrapped;

    @BindView(R.id.linear_languages2)
    View languagesUnwrapped;

    @BindView(R.id.languages)
    AppTextView movieLanguages;

    @BindView(R.id.linear_duration)
    View durationSection;

    @BindView(R.id.linear_duration1)
    View durationWrapped;

    @BindView(R.id.linear_duration2)
    View durationUnwrapped;

    @BindView(R.id.duration)
    AppTextView movieDuration;

    @BindView(R.id.book)
    AppTextView bookMovie;

    @BindView(R.id.movie_details)
    View movieDetails;

    @BindView(R.id.progress)
    MKLoader progressWheal;

    @BindView(R.id.backdrop)
    ImageView backdropImage;

    @BindView(R.id.ripple_effect)
    RippleView rippleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_screen);
        setupWindowAnimations();
        ButterKnife.bind(this);
        initToolbar();
        fetchMovieDetails();
        applyLogic();
    }

    protected Movie restoreMovie() {
        Intent intent = getIntent();
        Movie movie = new Movie();
        movie.setId(intent.getLongExtra("movie_id", 0));
        movie.setTitle(intent.getStringExtra("movie_title"));
        movie.setReleaseDate(FormatterHelper.parseDate(intent.getStringExtra("movie_release_date")));
        movie.setPopularity(intent.getDoubleExtra("movie_popularity", 0));
        movie.setPosterPath(intent.getStringExtra("movie_poster_path"));
        return movie;
    }

    protected void applyLogic() {

        Map<View[], View[]> ui_views = new HashMap<>();
        ui_views.put(new View[]{
                synopsisWrapped, synopsisUnwrapped
        } , new View[] {
                synopsisSection, synopsisWrapped, synopsisUnwrapped, movieSynopsis
        });

        ui_views.put(new View[]{
                genresWrapped, genresUnwrapped
        } , new View[] {
                genresSection, genresWrapped, genresUnwrapped, movieGenres
        });

        ui_views.put(new View[]{
                languagesWrapped, languagesUnwrapped
        } , new View[] {
                languagesSection, languagesWrapped, languagesUnwrapped, movieLanguages
        });

        ui_views.put(new View[]{
                durationWrapped, durationUnwrapped
        } , new View[] {
                durationSection, durationWrapped, durationUnwrapped, movieDuration
        });

        for(View[] key : ui_views.keySet()) {
            final View[] value = ui_views.get(key);

            key[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSection(true, value[0], value[1], value[2], value[3]);
                }
            });

            key[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSection(false, value[0], value[1], value[2], value[3]);
                }
            });
        }

        rippleButton.setRippleDuration(125);
        rippleButton.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                startActivity(new Intent(DetailsScreenActivity.this, WebViewActivity.class));
            }
        });
    }

    protected void fetchMovieDetails() {
        backdropImage.setImageResource(0);
        showContent(false);
        Movie movie = restoreMovie();
        MovieApi api = new MovieApi(this);

        api.fetchMovieDetails(movie, new MovieApi.MovieDetailsResult() {
            @Override
            public void onSuccess(MovieDetails movieDetails) {
                showContent(true);
                populateMovieDetails(movieDetails);
            }

            @Override
            public void onError(String message) {
                SweetAlertDialog errorDialog = new SweetAlertDialog(DetailsScreenActivity.this,
                        SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error_title))
                        .setContentText(message);
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        onBackPressed();
                    }
                });
                errorDialog.setCancelable(true);
                errorDialog.setCanceledOnTouchOutside(true);
                errorDialog.show();
            }
        });
    }

    protected void populateMovieDetails(MovieDetails movieDetails) {
        Drawable defaultImage = ContextCompat.getDrawable(this, R.drawable.loading_failed);
        Glide.with(this).load(movieDetails.getBackdropPath()).error(defaultImage).crossFade()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(backdropImage);


        movieTitle.setText(movieDetails.getIntro().getTitle());
        releaseDate.setText(getString(R.string.release_date_header) + " " +
                FormatterHelper.formatDate(movieDetails.getIntro().getReleaseDate()));
        moviePopularity.setText(FormatterHelper.formatPopularity(movieDetails.getIntro().getPopularity()));

        if(movieDetails.getSynopsis() != null && movieDetails.getSynopsis().length() > 0 &&
                !movieDetails.getSynopsis().contentEquals("null")) {
            movieSynopsis.setText(movieDetails.getSynopsis());
            showSection(true, synopsisSection, synopsisWrapped, synopsisUnwrapped, movieSynopsis);
        }
        else
            showSection(false, synopsisSection, synopsisWrapped, synopsisUnwrapped, movieSynopsis);

        if(movieDetails.getGenres() != null && movieDetails.getGenres().length > 0) {
            movieGenres.setText(arrayToString(movieDetails.getGenres()));
            showSection(true, genresSection, genresWrapped, genresUnwrapped, movieGenres);
        } else
            showSection(false, genresSection, genresWrapped, genresUnwrapped, movieGenres);

        if(movieDetails.getLanguages() != null && movieDetails.getLanguages().length > 0) {
            movieLanguages.setText(arrayToString(movieDetails.getLanguages()));
            showSection(true, languagesSection, languagesWrapped, languagesUnwrapped, movieLanguages);
        } else
            showSection(false, languagesSection, languagesWrapped, languagesUnwrapped, movieLanguages);

        if(movieDetails.getDuration() != null && movieDetails.getDuration() > 0) {
            movieDuration.setText(movieDetails.getDuration() + " " + getString(R.string.duration_unit));
            showSection(true, durationSection, durationWrapped, durationUnwrapped, movieDuration);
        } else
            showSection(false, durationSection, durationWrapped, durationUnwrapped, movieDuration);

    }

    protected String arrayToString(String[] array) {
        if (array != null && array.length > 0) {
            StringBuilder itemsBuilder = new StringBuilder();

            for (String item : array)
                itemsBuilder.append(item).append(", ");

            itemsBuilder.deleteCharAt(itemsBuilder.length() - 1);
            itemsBuilder.deleteCharAt(itemsBuilder.length() - 1);
            return itemsBuilder.toString();
        }

        return "";
    }

    protected void initToolbar() {

        setSupportActionBar(activityToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarLayout.setTitle(" ");

        appToolbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    toolbarLayout.setTitle(getString(R.string.title_activity_detail_screen));
                    isShow = true;
                } else if(isShow) {
                    toolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    protected void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Explode transition = new Explode();
            transition.setDuration(250);
            getWindow().setEnterTransition(transition);
            getWindow().setReenterTransition(transition);
            getWindow().setExitTransition(transition);
        }
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            finishAfterTransition();
        else
            finish();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }

        return true;
    }

    protected void showSection(boolean show, View... views) {
        if(views == null || views.length != 4)
            return;
        views[0].setVisibility(View.VISIBLE);
        views[1].setVisibility((show) ? View.GONE : View.VISIBLE);
        views[2].setVisibility((show) ? View.VISIBLE : View.GONE);
        views[3].setVisibility((show) ? View.VISIBLE : View.GONE);
    }

    protected void showContent(boolean show) {
        appToolbar.setVisibility((show) ? View.VISIBLE : View.INVISIBLE);
        movieDetails.setVisibility((show) ? View.VISIBLE : View.INVISIBLE);
        bookMovie.setVisibility((show) ? View.VISIBLE : View.INVISIBLE);
        progressWheal.setVisibility((show) ? View.GONE : View.VISIBLE);
    }
}
