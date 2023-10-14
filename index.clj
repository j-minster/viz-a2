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
            [clojure.edn :as edn]
            [cheshire.core :refer [generate-string parse-string]]))

;; # PT Trip frequency by suburb and time of day


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def all-data (-> "inputs/boundaries_stops_and_lines.json"
                  slurp
                  json/read-str))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn get-feature-vic-loca-2 [feature]
  (get-in feature ["properties" "name"]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def vic-loca-vec (as-> (all-data "features") _
                    (filter #(or (= (get-in % ["geometry" "type"]) "Polygon")
                                 (= (get-in % ["geometry" "type"]) "MultiPolygon")) _)
                    (map get-feature-vic-loca-2 _)
                    (set _)
                    (vec _)
                    (sort _)))


;; Select a **suburb** from the dropdown menu and see the distribution of PT trips in the area by the time of day.
(merge
 {:nextjournal/width :wide}
 (clerk/vl {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
            :view {:stroke "transparent"},
            :padding 20
            :title "Melbourne: SE Suburbs"
            :data {:values all-data
                   :format {:type "json" :property "features"}}
            :params [{:name "selected_suburb"
                      :value "CLAYTON"
                      :title "Selected Suburb: "
                      :bind {:input "select" :options vic-loca-vec}}]
            :vconcat [{:hconcat [{
                                    :transform [{:filter "datum.properties.window == '0:00-6:00'"}]
                                    :mark {:type "geoshape", :stroke "white", :strokeWidth 2},
                                    :projection {:type "mercator"}
                                    :width 500
                                    :height 500
                                    :encoding {:color
                                               {:condition
                                                {:test "datum.properties.name == selected_suburb",
                                                 :value "#CBC3E3"},
                                                :value "#ddd"},
                                               :tooltip
                                               {:field "properties.name", :type "nominal", :title "Name"}}
                                    }
                                   {:transform
                                    [{:filter {:or ["datum.properties.suburb_name == selected_suburb"
                                                    "datum.properties.name == selected_suburb"]}}]
                                    :projection {:type "mercator"}
                                    :title {:expr "selected_suburb"}
                                    :layer [
                                            {:mark {:type "geoshape" :stroke "white" :strokeWidth 2}
                                             :transform [{:filter {:or [{:field "geometry.type" :equal "Polygon"}
                                                                        {:field "geometry.type" :equal "MultiPolygon"}]}}]
                                             :encoding {:color {:value "#CBC3E3"}}}

                                            {
                                             :mark {:type "geoshape" :color "purple" :fill "purple" :opacity 1 :stroke "purple"}
                                             :pointRadius {:value 122}
                                             :transform [{:filter {:field "geometry.type" :equal "Point"}}]
                                             :encoding {:size {:value 12}
                                                        :color {:value "blue"}}}

                                            {:mark {:type "geoshape" :filled false :strokeWidth 1 :opacity 0.8}
                                             :transform
                                             [{:filter {:or [{:field "geometry.type" :equal "LineString"}
                                                             {:field "geometry.type" :equal "Line"}
                                                             {:field "geometry.type" :equal "MultiLineString"}]}}]
                                             :encoding
                                             {:color {:value "red"}}}]}]}

                      {
                       :width 700
                       :transform
                       [{:filter {:and [{:or ["datum.properties.suburb_name == selected_suburb"
                                              "datum.properties.name == selected_suburb"]}
                                        {:field "geometry.type" :equal "Point"}]}}
                        {:window [{:op "sum" :field "properties.ntrips" :as "sum_ntrips"}]
                         ;; :sort [{:field "properties.window" :order "ascending"}]
                         }]
                       :mark "bar"
                       :encoding {:x {:field "properties.win_map" :type "ordinal" :axis {:field "properties.window"} :title "Time"}
                                  :y {:field "sum_ntrips" :type "quantitative" :title "Number of trips"}
                                  :color {:value "purple"}}
                       }
                      ]}
           ))
;; The key for the x-axis is:
;; + "0:00-6:00": 1
;; + "6:00-10:00": 2
;; + "10:00-12:00": 3
;; + "12:00-16:00": 4
;; + "16:00-19:00": 5
;; + "19:00-24:00": 6
