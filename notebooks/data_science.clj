;; # A map showing PT lines in the Melbourne/Eastern Suburbs area
^{:nextjournal.clerk/visibility {:code :hide}}
(ns data-science
  (:require [clojure.set :refer [join rename-keys project]]
            [clojure.string :as str]
            [dk.ative.docjure.spreadsheet :as ss]
            [kixi.stats.core :as kixi-stats]
            [kixi.stats.protocols :as kixi-p]
            [meta-csv.core :as csv]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]
            [nextjournal.clerk :as clerk]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [clojure.edn :as edn]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def suburb-data (slurp "notebooks/inputs/cm_suburb_boundaries.json"))
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def line-data (slurp "notebooks/inputs/line_freq_clipped_4to7.json"))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def edn-input {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
           :width 700,
           :height 500,
           :view {:stroke "transparent"},
           :title "Melbourne/Clayton PT lines"
           :layer
           [{:data {:values "notebooks/inputs/cm_suburb_boundaries.json"
                    :format {:type "json", :property "features"}},
             :mark {:type "geoshape", :stroke "white", :strokeWidth 2},
             :encoding {:color {:value "#eee"}}}
            {:data {:values "notebooks/inputs/line_freq_clipped_4to7.json"
                    :format {:type "json" :property "features"}}
             :mark {:type "geoshape" :filled false :strokeWidth 0.8}
             :encoding {:color {:value "#111"}}}
            ]})

;; Here are all the transit lines in Melbourne's CBD and Eastern Suburbs (because the Victorian data set was too big)
;; I want to eventually visualise the frequencies of all the lines in the area, by time-slot and by direction
(clerk/vl {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
           :width 700,
           :height 500,
           :view {:stroke "transparent"},
           :title "Melbourne/Clayton PT lines"
           :layer
           [{:data {:values suburb-data
                    :format {:type "json", :property "features"}},
             :mark {:type "geoshape", :stroke "white", :strokeWidth 2},
             :encoding {:color {:value "#eee"}}}
            {:data {:values line-data
                    :format {:type "json" :property "features"}}
             :mark {:type "geoshape" :filled false :strokeWidth 0.8}
             :encoding {:color {:value "#111"}}}
            ]}
          )
