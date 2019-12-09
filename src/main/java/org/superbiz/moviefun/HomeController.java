package org.superbiz.moviefun;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private PlatformTransactionManager managerAlbums;
    private PlatformTransactionManager managerMovies;
    private final TransactionTemplate transactionTemplateMovies;
    private final TransactionTemplate transactionTemplateAlbums;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures, PlatformTransactionManager managerAlbums,
                          PlatformTransactionManager managerMovies) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.managerAlbums = managerAlbums;
        this.managerMovies = managerMovies;
        this.transactionTemplateMovies = new TransactionTemplate(managerMovies);
        this.transactionTemplateAlbums = new TransactionTemplate(managerAlbums);;

    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

            try {
                transactionTemplateMovies.execute(new TransactionCallbackWithoutResult() {
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        for (Movie movie : movieFixtures.load()) {
                            moviesBean.addMovie(movie);
                        }
                    }


            } );
        } catch (TransactionException e) {
                e.printStackTrace();
            }


        try {
            transactionTemplateAlbums.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    for (Album album : albumFixtures.load()) {
                        albumsBean.addAlbum(album);
                    }
                }


            } );
        } catch (TransactionException e) {
            e.printStackTrace();
        }

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }


}
