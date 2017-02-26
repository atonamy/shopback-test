package sg.assignment.shopback.moviediscovery.data;

import io.realm.RealmObject;
import sg.assignment.shopback.moviediscovery.data.realm.Movie;

/**
 * Created by archie on 25/2/17.
 */

public class MovieDetails {

    private Movie intro;
    private String synopsis;
    private String[] genres;
    private String[] languages;
    private String backdropPath;
    private Integer duration;

    public Movie getIntro() {
        return intro;
    }

    public void setIntro(Movie intro) {
        this.intro = intro;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
}
