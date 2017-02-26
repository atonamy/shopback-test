package sg.assignment.shopback.moviediscovery.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andexert.library.RippleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;
import sg.assignment.shopback.moviediscovery.R;
import sg.assignment.shopback.moviediscovery.data.realm.Movie;
import sg.assignment.shopback.moviediscovery.ui.views.AppTextView;
import sg.assignment.shopback.moviediscovery.utils.FormatterHelper;

/**
 * Created by archie on 25/2/17.
 */

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_movie_poster)
        public ImageView moviePoster;

        @BindView(R.id.item_movie_title)
        public AppTextView movieTitle;

        @BindView(R.id._item_movie_popularity)
        public AppTextView moviePopularity;

        @BindView(R.id.item_movie_release_date)
        public AppTextView movieReleaseDate;

        @BindView(R.id.ripple_effect)
        public RippleView clickView;


        public View rootView;


        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            rootView = itemView;
            clickView.setRippleDuration(125);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }

    }

    public interface OnItemMovieClickListener {
        public void onClick(Movie movie, int position);
    }


    private List<Movie> allMovies;
    private View footerView;
    private Context adapaterContext;
    private OnItemMovieClickListener itemClickListener;

    public MoviesAdapter(Context context) {
        if(context == null)
            throw new NullPointerException("Adapter context cannot be null");
        allMovies = new RealmList<>();
        footerView = null;
        adapaterContext = context;
    }

    public void addMovies(List<Movie> movies) {
        if(movies == null || movies.size() == 0)
            return;
        int position = allMovies.size();
        allMovies.addAll(movies);
        notifyItemRangeInserted(position, movies.size());
    }

    public void setFooter(View footer){
        boolean new_footer = (footerView == null) ? true : false;
        footerView = footer;
        if(new_footer)
            notifyItemInserted(allMovies.size());
        else if(footer == null)
            notifyItemRemoved(allMovies.size());
        else
            notifyItemChanged(allMovies.size());
    }

    public void clear() {
        allMovies.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

     if (position == allMovies.size() && footerView != null)
            return TYPE_FOOTER;

        return TYPE_ITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case TYPE_ITEM:
                View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, null);
                return new MovieViewHolder(layoutView);

            case TYPE_FOOTER:
                return new FooterViewHolder(footerView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof MovieViewHolder)
            bindMovieViewHolder((MovieViewHolder)holder, position);
    }

    protected void bindMovieViewHolder(MovieViewHolder holder, final int position) {
        DecimalFormat precision = new DecimalFormat("0.0000");
        Drawable defaultImage = ContextCompat.getDrawable(adapaterContext, R.drawable.loading_failed);
        final Movie movie = getItem(position);
        if(movie == null)
            throw new RuntimeException("Invalid movie item position");

        holder.moviePopularity.setText(FormatterHelper.formatPopularity(movie.getPopularity()));
        holder.movieReleaseDate.setText(adapaterContext.getString(R.string.release_date_header) + " "
                + FormatterHelper.formatDate(movie.getReleaseDate()));
        holder.movieTitle.setText(movie.getTitle());
        Glide.clear(holder.moviePoster);
        Glide.with(adapaterContext).load(movie.getPosterPath()).error(defaultImage).crossFade()
            .diskCacheStrategy( DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(holder.moviePoster);
        holder.clickView.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                if(itemClickListener != null)
                    itemClickListener.onClick(movie, position);
            }
        });
    }

    public Movie getItem(int position) {
        if(position >= 0 && position < allMovies.size())
            return allMovies.get(position);
        return null;
    }

    public void setOnItemMovieClickListener(OnItemMovieClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return (footerView == null) ? allMovies.size() : allMovies.size()+1;
    }

}
