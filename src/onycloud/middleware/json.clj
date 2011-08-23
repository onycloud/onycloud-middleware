(ns onycloud.middleware.json
  (:use [clojure.walk :only [prewalk]])
  (:require [clojure.data.json :as json])
  (:import org.bson.types.ObjectId))

(defn- remove-mongo-id [form]
  (if (and (map? form) (= ObjectId (class (:_id form))))
    (dissoc form :_id)
    form))

(defn json-response
  "Construct a JSON HTTP response."
  [status & [body]]
  {:status status
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/json-str (prewalk remove-mongo-id (or body "")))})

(defn wrap-json
  [handler]
  (fn [req]
    (let [json-req-body (if-let [body (:body req)]
                          (let [body-str (cond (string? body) body
                                               :else (slurp body))]
                            (when-not (empty? body-str)
                              (json/read-json body-str))))
          req* (assoc req
                 :json-body json-req-body)
          resp-obj (handler req*)
          resp (let [status (:status resp-obj)]
                 (if (number? status)
                   (json-response status (:body resp-obj))
                   (json-response 200 resp-obj)))]
      resp)))

(defmacro JSON [verb path args handler]
  `(~verb ~path ~args (wrap-json ~handler)))
