# corvid-19 - ARCHIVED see https://github.com/jumarko/clojure-experiments/blob/master/src/clojure_experiments/stats/covid19.clj#L1

A simple data science project, working with data from [Johns Hopkins University](https://github.com/CSSEGISandData/COVID-19).

Based on the [Baby steps with Covid-19 data for Clojure programmers](https://dragan.rocks/articles/20/Corona-1-Baby-steps-with-Covid-19-for-programmers)  by [dragon.rocks](https://dragan.rocks/articles/20/Corona-1-Baby-steps-with-Covid-19-for-programmers).  Please consider sponsoring the excellent work that Dragan Djuric produces.

## Usage
Open the `src/practicalli/design_journal.clj` file from the project in your favorite Clojure editor and start the REPL.  Starting at the top of the page, evaluate the individual expressions and experiment.

## Deployment
Build a deployable jar of this library:

    $ clojure -A:jar

Install it locally:

    $ clojure -A:install

Deploy to Clojars

    $ clojure -A:deploy

> Configure your clojars.org account detail in the environment variables:
> `CLOJARS_USERNAME` and `CLOJARS_PASSWORD`

## License

Copyright © 2020 Practicalli

Distributed under the Creative Commons Attribution Share-Alike 4.0 International
