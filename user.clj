(ns user
  (:require [nextjournal.clerk :as clerk]
            [clojure.string]))

(clerk/show! 'nextjournal.clerk.tap)
(clerk/serve! {:browse true})
