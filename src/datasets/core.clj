(ns datasets.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [datasets
             [server :as server]
             [config :as config]
             [handler :as handler]]
            [datasets.core.initialization-status :as init-status]
            [datasets.util.i18n :refer [set-locale trs]]
            [toucan.db :as db]))

; ----------------------
(defn- detrsoy!
  "General application shutdown function which should be called once at application shuddown."
  []
  (log/info (trs "datasets Shutting Down ..."))
  ;; TODO - it would really be much nicer if we implemented a basic notification system so these things could listen
  ;; to a Shutdown hook of some sort instead of having here
  (server/stop-web-server!)
  (log/info (trs "datasets Shutdown COMPLETE")))


(defn init!
  "General application initialization function which should be run once at application startup."
  []
  (log/info (trs "System timezone is ''{0}'' ..." (System/getProperty "user.timezone")))
  ;; First of all, lets register a shutdown hook that will tidy things up for us on app exit
  (.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable detrsoy!))
  (init-status/set-progress! 1.0)
  )

(defn- start-normally []
  (log/info (trs "Starting datasets in STANDALONE mode"))
  (try
    ;; launch embedded webserver async
    (server/start-web-server! handler/app)
    ;; run our initialization process
    (init!)
    ;; Ok, now block forever while Jetty does its thing
    (when (config/config-bool :mb-jetty-join)
      (.join (server/instance)))
    (catch Throwable e
      (log/error e (trs "datasets Initialization FAILED"))
      (System/exit 1))))


(defn -main
  "Launch datasets in standalone mode"
  []
  (start-normally)
  )
