(ns riemann.influxdb
  "Forwards events to InfluxDB"
  (:require [capacitor.core :as influx])
  (:use [clojure.string :only [split join]]))

(defn influxdb-metric-name
  "Constructs a metric-name for an event."
  [event]
  (let [service (:service event)
        split-service (if service (split service #" ") [])]
     (join "." split-service)))

(defn influxdb
    "Construct a client to InfluxDB and forward events to it.
     Takes an optional parameter fields which returns additional fields
     other name, host, state and value to send to influx given an event."
  [opts & [fields]]
  (let [opts (merge {:host "127.0.0.1"
                     :port 8086
                     :scheme "http"
                     :username "root"
                     :password "root"
                     :db "riemann"
                     :series "riemann-events" } opts)
        fields (if (not= fields nil) fields (fn [e] {}))
        client (influx/make-client opts)]
    (fn [event]
    (when (:metric event)
      (when (:service event)
        (when (:host event)
          (influx/post-points client (:series opts)
          [(merge (fields event) {:name (influxdb-metric-name event)
                                  :host (:host event)
                                  :state (:state event)
                                  :value (:metric event)})])))))))
