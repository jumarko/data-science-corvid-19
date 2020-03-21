(ns practicalli.design-journal
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]))


;; Accessing the data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; path to data
;; https://github.com/open-covid-19/data/blob/master/output/world.csv
;; https://raw.githubusercontent.com/open-covid-19/data/master/output/world.csv

(def data "https://raw.githubusercontent.com/open-covid-19/data/master/output/world.csv")

(slurp data)


(def data-cache (slurp data))

data-cache

;; Not a good approach for large data sets


;; For larger files then using a buffered reader is recommended.
;; The Clojure java.io API provides a reader function
;; https://clojure.github.io/clojure/clojure.java.io-api.html

;; Add requires to source code namespace
;; (:require [clojure.java.io :as io])

(require '[clojure.java.io :as io])

(io/reader data)
;; => #object[java.io.BufferedReader 0x2f606d07 "java.io.BufferedReader@2f606d07"]

;; Get the data from the reader as a string with slurp

(slurp (io/reader data))


;; Save the data file locally
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; To save hitting GitHub with multiple calls throughout the development,
;; copy the file to `resources` directory.
;; https://raw.githubusercontent.com/open-covid-19/data/master/output/world.csv

(def data-local "resources/world.csv")

(slurp (io/reader data-local))


;; Alternatively, you can use the resource function
;; which returns the location of a file on the classpath
;; the `resources` directory is included in the classpath
;; via the `deps.edn` configuration
;; `resource` is a nice abstraction to use instead of `reader`
;; especially in a web application

(io/resource "world.csv")
;; => #object[java.net.URL 0x3f239a8c "file:/home/practicalli/projects/clojure/data-science/corvid-19/resources/world.csv"]


;; Working with files and directories in Clojure
;; http://clojure-doc.org/articles/cookbooks/files_and_directories.html
;; https://www.tutorialspoint.com/clojure/clojure_file_io.htm


;; Parsing CSV data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; clojure.data.csv is a library to transform data between
;; CSV format and Clojure data structures (hash-maps, vectors, etc)
;; It contains `read-csv` and `write-csv` functions, that's all.

;; Add dependency to parse CSV files (restart REPL)
;; https://github.com/clojure/data.csv
;; org.clojure/data.csv {:mvn/version "1.0.0"}

;; see the section on lazyness for using this library effectively
;; https://github.com/clojure/data.csv#laziness

(require '[clojure.data.csv :as csv])

;; clojure.data.csv/read-csv function will create a lazy sequence
;; of the data from the CSV file, in a Clojure data structure.

(csv/read-csv
  (slurp (io/reader data-local)))

;; as we are calling read-csv in a top level expression,
;; it becomes eager and we get the result.


;; Helper function for loading CSV files by geographical name
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; There are data sets for particular geographical regions,
;; China, USA and the whole world.

;; A helper function will

(defn csv->clj [location]
  (csv/read-csv
    (slurp
      (io/reader
        (format "resources/%s.csv" location)))))


;; format is used to create the respective filename for the location
(format "resources/%s.csv" "world")
;; => "resources/world.csv"

;; NOTE: original article uses io/resource which does not work locally

;; As the data set is not that large, bind it for convenience

(def covid-world (csv->clj "world"))
#_(def covid-china (csv->clj "china"))
#_(def covid-usa (csv->clj "usa "))

;; covid-world


;; Deconstruct the data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Example data
;; (["Date" "CountryCode" "CountryName" "Confirmed" "Deaths" "Latitude" "Longitude"]
;;  ["2019-12-31" "AE" "United Arab Emirates" "0" "0" "23.424076" "53.847818"]
;;  ["2019-12-31" "AF" "Afghanistan" "0" "0" "33.93911" "67.709953"]
;;  ["2019-12-31" "AM" "Armenia" "0" "0" "40.069099" "45.038189"]
;;  ["2019-12-31" "AT" "Austria" "0" "0" "47.516231" "14.550072"]
;;  ["2019-12-31" "AU" "Australia" "0" "0" "-25.274398" "133.775136"]
;;  ["2019-12-31" "AZ" "Azerbaijan" "0" "0" "40.143105" "47.576927"]
;;  ["2019-12-31" "BE" "Belgium" "0" "0" "50.503887" "4.469936"]

;; The heading data is at the start of the file as a vector
;; Each additional vector is an observation taken on a specific date


;; headings

(first covid-world)
;; => ["Date" "CountryCode" "CountryName" "Confirmed" "Deaths" "Latitude" "Longitude"]

;; Observations

(second covid-world)
;; => ["2019-12-31" "AE" "United Arab Emirates" "0" "0" "23.424076" "53.847818"]

;; total number of observations

(count (rest covid-world))
;; => 5609


;; Countries with observations

(distinct
  (map
    (fn [data] (second (rest data)))
    (rest covid-world)))

;; maybe more efficient to do

(distinct
  (map
    (fn [data] (last (take 3 data)))
    (rest covid-world)))

;; syntax sugar

(distinct
  (map
    #(last (take 3 %))
    (rest covid-world)))


;; total countries
;; if we just want the totals, then we can use the abreviated country names

(count
  (distinct
    (map
      second
      (rest covid-world))))
;; => 152



;; How complete is our data?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; One of the biggest challenges of data science is getting good data.
;; You should at least understand the basic quality of data
;; before delving deep into the science.

;; At least the data is in a proper CSV format and not just adhoc tables!

;; divide the total data set by the number of countries
;; if the remainder is zero,
;; we have all data for all countries for all dates

(rem (count (next covid-world))
     (count (distinct (map second (rest covid-world)))))
;; => 137


;; finding out how many data points are missing

(count (next covid-world))
;; => 5609

;; count the number of dates
(count (distinct (map first (rest covid-world))))
;; => 81


;; Given 81 dates and 152 countries, how many data sets should we have in total?

(* (count (distinct (map first (rest covid-world))))
   (count (distinct (map second (rest covid-world)))))
;; => 12312

;; missing observations
(- 12312 5609)
;; => 6703


;; How many observations have zero confirmed cases?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Which column has the number of cases
(first covid-world)
;; => ["Date" "CountryCode" "CountryName" "Confirmed" "Deaths" "Latitude" "Longitude"]

;; filter to show how many observations are zero

(map #(nth % 3) (rest covid-world))

;; The Confirmed data is strings of numbers,
;; ideally the Confirmed and Deaths should be numbers.

;; without changing the data format, filtering is harder

(filter zero? (map #(nth % 3) (rest covid-world)))

;; using an anonymous function works

(filter (fn [value] (= "0" value)) (map #(nth % 3) (rest covid-world)))

;; total

(count (filter (fn [value] (= "0" value)) (map #(nth % 3) (rest covid-world))))


;; Converting the data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def covid-world-data (next covid-world))

;; manual conversion

(def covid-world-data-converted
  (map (fn [[date country-code country-name confirmed death]]
         [date country-code country-name (Long/parseLong confirmed) (Long/parseLong death)])
       covid-world-data))

covid-world-data-converted


(count (filter zero? (map #(nth % 3) covid-world-data-converted)))

;; So in about half the observations there are no confirmed cases

;; Alternative:
;; semantic-csv can convert strings into Clojure types
;; as it parses the file.
;; This library creates maps from the CSV data rather than vectors
;; https://github.com/metasoarous/semantic-csv


;; Understanding the data a little more
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; There are several reasons for a zero recording on any give day
;; - pandemic had not reached a country by a particular date
;; - no tests were recorded on that date, or not published

;; how many observations happened on each day?

(def date-frequencies
  (sort-by first (frequencies (map first covid-world-data-converted))) )


(take 10 date-frequencies)

;; The countries

(take 10 (drop 10 date-frequencies))

(take 20 (drop 60 date-frequencies))

(drop-while #(= 66 (second %)) date-frequencies)

;; 66 countries were reporting up until March,
;; then there was a significant drop (change in reporting, discarding zero recordings)
;; March 11 2020 the pandemic as announced by the World Health Organisation
;; after that the numbers of observations increased markedly.


;; How much data for a particular country
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def country-frequencies
  (sort-by first (frequencies (map second covid-world-data-converted))))

(take 10 country-frequencies)


;; narrowing to a few specific countries

;; create a set of countries to investigate

(def selective-countries
  #{"IT" "FR" "ES" "CN"})


;; filter for just those countries

(filter (fn [[_ code]] (selective-countries code))
        covid-world-data-converted)

(take 10
      (filter (fn [[_ code]] (selective-countries code))
              covid-world-data-converted))



;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn take-countries [data country-set]
  (filter (fn [[_ code]] (country-set code)) data))

(defn date-freqs [data]
  (sort-by first (frequencies (map first data))))

(defn country-freqs [data]
  (sort-by first (frequencies (map second data))))

(def my-countries
  (country-freqs
    (take-countries  covid-world-data-converted
                     #{"IT" "FR" "ES" "CN" "US" "RS" "DE"})))


(take 10 my-countries)




;; Plotting the data - with ascii graphs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ASCII-Data is a small Java library
;; for producing nice looking ASCII line-graphs and tables.
;; https://github.com/MitchTalmadge/ASCII-Data/wiki

;; Include the library in the namespace

(import 'com.mitchtalmadge.asciidata.graph.ASCIIGraph)


;; Get the results from Serbia
;; just the confirmed cases
;; from the first reported case (ignore the initial zero reports)

(def serbia-data
  (drop-while zero?
              (map #(nth % 3)
                   (take-countries covid-world-data-converted #{"RS"}))))

;; first attempt at printing a graph

(println
  (.plot (ASCIIGraph/fromSeries (double-array serbia-data))))

;; The graph was plotted exponentially, so doesn't reflect growth correctly

;; We are interested in growth of confirmed cases,
;; not the absolute numbers.
;; So use the logarithm of this function

;; A logarithm helper function

(defn logarithm ^double [^double x]
  (Math/log x))


(println (.plot (ASCIIGraph/fromSeries
                  (double-array (map logarithm serbia-data)))))

;; Okay, now we can see the growth over time.


;; Make a helper function from this plotting code

(defn log-plot
  ""
  [series-data]
  (.plot (ASCIIGraph/fromSeries
           (double-array (map logarithm series-data)))))


;; Plotting other countries
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn confirmed-cases
  "Extracts sequence of confirmed cases for a specified country.
  Observations before the first case was reported are not included

  Arguments:
  Data source as sequence of vectors with confirmed as integer values,
  Country code as a string

  Returns: Sequence of confirmed cases as integer values"

  [data-source country-code]

  (drop-while zero? (map #(nth % 3)
                         (take-countries data-source #{country-code}))) )

;; Italy

(reverse (map logarithm (confirmed-cases covid-world-data-converted "IT")))

(println (log-plot (confirmed-cases covid-world-data-converted "IT")))



;; China

(println (log-plot (confirmed-cases covid-world-data-converted "CN")))



;; Explicitly plotting the change
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Show the range of changes of confirmed cases

(defn absolute-plot [series-data]
  (.plot (ASCIIGraph/fromSeries
           (double-array series-data))))

(defn abolute-series
  [data-source country-code]
  (map #(/ % 1000)
       (reduce (fn [acc x]
                 (conj acc (- x (peek acc))))
               [0]
               (confirmed-cases data-source country-code))))

(println
  (absolute-plot
    (abolute-series covid-world-data-converted "CN")))
