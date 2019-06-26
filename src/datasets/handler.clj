(ns datasets.handler
  "Top-level datasets Ring handler."
  (:require [datasets
             [config :as config]
             [routes :as routes]]
            [datasets.middleware
             [json :as mw.json]
             ]
            [ring.middleware
             [cookies :refer [wrap-cookies]]
             [keyword-params :refer [wrap-keyword-params]]
             [params :refer [wrap-params]]]))



;; required here because this namespace is not actually used anywhere but we need it to be loaded because it adds
;; impls for handling `core.async` channels as web server responses


(def app
  "The primary entry point to the Ring HTTP server."
  ;; ▼▼▼ POST-PROCESSING ▼▼▼ happens from TOP-TO-BOTTOM
  (->
    ;; when running TESTS use the var so we can redefine routes as needed. No need to waste time with repetitive var
    ;; lookups when running normally
    (if config/is-test?
      #'routes/routes
      routes/routes)
    mw.json/wrap-json-body                                  ; extracts json POST body and makes it avaliable on request
    mw.json/wrap-streamed-json-response     ; middleware to automatically serialize suitable objects as JSON in responses
    wrap-keyword-params                                     ; converts string keys in :params to keyword keys
    wrap-params                                             ; parses GET and POST params as :query-params/:form-params and both as :params
    wrap-cookies                                            ; Parses cookies in the request map and assocs as :cookies
    ))                                                      ; GZIP response if client can handle it
;; ▲▲▲ PRE-PROCESSING ▲▲▲ happens from BOTTOM-TO-TOP
