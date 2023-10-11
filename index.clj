^{:nextjournal.clerk/visibility :hide}
(ns index
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

;; # Viz A2


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def suburb-data (slurp "inputs/cm_suburb_boundaries.json"))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def line-data (slurp "inputs/line_freq_clipped_4to7.json"))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def edn-input {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
           :width 700,
           :height 500,
           :view {:stroke "transparent"},
           :title "Melbourne/Clayton PT lines"
           :layer
           [{:data {:values "inputs/cm_suburb_boundaries.json"
                    :format {:type "json", :property "features"}},
             :mark {:type "geoshape", :stroke "white", :strokeWidth 2},
             :encoding {:color {:value "#eee"}}}
            {:data {:values "inputs/line_freq_clipped_4to7.json"
                    :format {:type "json" :property "features"}}
             :mark {:type "geoshape" :filled false :strokeWidth 0.8}
             :encoding {:color {:value "#111"}}}
            ]})

;; Here are all the transit lines in Melbourne's CBD and South-Eastern Suburbs (because the Victorian data set was too big)
;; I want to eventually visualise the frequencies of all the lines in the area, by time-slot and by direction
(merge
 {:nextjournal/width :wide}
 (clerk/vl {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
            :width 700,
            :height 500,
            :view {:stroke "transparent"},
            :title "Melbourne/SE Suburbs PT lines"
            :layer
            [{:data {:values suburb-data
                     :format {:type "json", :property "features"}},
              :mark {:type "geoshape", :stroke "white", :strokeWidth 2},
              :encoding {:color {:value "#eee"}
                         :tooltip {:field "properties.vic_loca_2"
                                   :type "nominal"
                                   :title "Name"}}}
             {:data {:values line-data
                     :format {:type "json" :property "features"}}
              :mark {:type "geoshape" :filled false :strokeWidth 0.8 :opacity 1}
              ;; :encoding {:color {:value "#111"}}}
              :encoding {:color {:bin {:binned true :maxstep 3}
                                 :field "properties.ntrips"
                                 :type "quantitative"
                                 :scale {:scheme "purples"
                                         :count 4}}
                         :tooltip [{:field "properties.ntrips"
                                    :type "quantitative"
                                    :title "Number of trips"}
                                   {:field "properties.route_name"
                                    :type "nominal"
                                    :title "Route ID"}]}
             }
             ]}
           ))

;; **Planned features**:
;; + Select timeslot in day (5-7am, 7-9am etc.)
;; + Show suburb names on map
;; + Select PT stop from map
;; + When PT stop is selected:
;;   + show zoomed map view (separate)
;;   + Show histogram of 'number of stops' per time-bin through the day
;;
;; **Planned fixes**:
;; + fix `nan` values in a lot of route names (mostly bus routes)
;; + fix issue such as there only being 2 trips between 4-7pm for the Pakenham-city train route
;; + work out why there are overlapping routes with the same name in the same timeslot
;; + add mode of transport to tooltip
