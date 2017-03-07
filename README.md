# Programming test for SHOPBACK.SG (Android)

![screenshot](https://cloud.githubusercontent.com/assets/5222190/23640527/50e3ea94-0329-11e7-8943-bb2e247d0578.png) ![screenshot](https://cloud.githubusercontent.com/assets/5222190/23640528/53e09ff8-0329-11e7-9f42-98f2a5a6d22d.png)

## Test Scenario

Cinema operators such as Cathay is one of the most popular merchants we have in ShopBack.

To help our users discover movies easily, create a movie app with the following screens:

1. Home screen with list of available movies

  a. Ordered by release date
  
  b. Pull to refresh
  
  c. Load when scrolled to bottom
  
  d. Each movie to include:
  
      i.   Poster/Backdrop image
    
      ii.  Title
    
      iii. Popularity
    
    
    
2. Detail screen

  a. Movie details with these **additional** details:
  
      i.   Synopsis
      
      ii.  Genres
      
      iii. Language
      
      iv.  Duration
      
  b. Book the movie (simulate opening of [this link](http://www.cathaycineplexes.com.sg/movies/) in a web view)
  
It is entirely up to you to design the UI. It is a bonus if you create awesome looking UI, so don’t be too
hard on yourself with the design. Simple is good (:

Use the [API from TMDb](https://developers.themoviedb.org/3/getting-started)  :

  1. http://api.themoviedb.org/3/discover/movie?api_key=328c283cd27bd1877d9080ccb1604c91&primary_release_date.lte=2016-12-31&sort_by=release_date.desc&page=1
  
  2. http://api.themoviedb.org/3/movie/328111?api_key=328c283cd27bd1877d9080ccb1604c91
  
Use of third-party libraries is OK.

Evaluation Criteria

  1. Coding style, cleanliness, organization

  2. Functional

Bonus Points

    ● Nice UI

    ● Git commits

    ● Unit tests


