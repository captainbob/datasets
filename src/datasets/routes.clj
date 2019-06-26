(ns datasets.routes
  "Main Compojure routes tables. See https://github.com/weavejester/compojure/wiki/Routes-In-Detail for details about
   how these work. `/api/` routes are in `metabase.api.routes`."
  (:require [compojure
             [core :refer [context defroutes GET]]
             [route :as route]]
            [datasets.core.initialization-status :as init-status]
            [ring.util.response :as resp]))

(defn- redirect-including-query-string
  "Like `resp/redirect`, but passes along query string URL params as well. This is important because the public and
   embedding routes below pass query params (such as template tags) as part of the URL."
  [url]
  (fn [{:keys [query-string]} respond _]
    (respond (resp/redirect (str url "?" query-string)))))



;; Redirect naughty users who try to visit a page other than setup if setup is not yet complete
(defroutes ^{:doc "Top-level ring routes for Metabase."} routes
           ;; ^/$ -> index.html
           (GET "/favicon.ico" [] (resp/resource-response "frontend_client/favicon.ico"))
           ;; ^/api/health -> Health Check Endpoint
           (GET "/api/health" [] (if (init-status/complete?)
                                   {:status 200, :body {:status "ok"}}
                                   {:status 503, :body {:status "initializing", :progress (init-status/progress)}}))
           ;;; ^/api/ -> All other API routes
           ;(context "/api" [] (fn [& args]
           ;                     ;; if Metabase is not finished initializing, return a generic error message rather than
           ;                     ;; something potentially confusing like "DB is not set up"
           ;                     (if-not (init-status/complete?)
           ;                       {:status 503, :body "Metabase is still initializing. Please sit tight..."}
           ;                       (apply api/routes args))))

           )