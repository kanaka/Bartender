(ns rend.server
  (:require [compojure.core :refer [GET]]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
;            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.file :refer [wrap-file]]))

(defn start-server [cfg]
  (let [port (-> cfg :web :port)
        dir (-> cfg :web :dir)
        routes (compojure.core/routes
                 (GET "/" [] "Hello World")
                 (route/files "/gen" {:root dir})
                 (route/not-found "Not Found"))
        app (-> routes
;                   (wrap-defaults site-defaults)
                   (wrap-file dir)
                   (wrap-content-type)
                (wrap-not-modified))
        server (jetty/run-jetty app {:port port :join? false})]
    (println (str "Ring server started on port: " port
                  ", serving directory: " dir))
    server))

