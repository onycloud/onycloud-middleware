(ns onycloud.middleware.auth-test
  (:use clojure.test
        onycloud.middleware.auth))

(deftest test-wrap-auth-failure
  (let [handler (wrap-auth (fn [_] :success)
                           :auth (fn [_] nil)
                           :failure (fn [_] :failure))]
    (is (= :failure (handler {})))))

(deftest test-wrap-auth-success
  (let [handler (wrap-auth (fn [_]
                             (is {:name "user1"} *authenticated-user*)
                             :success)
                           :auth (fn [_] {:name "user1"})
                           :failure (fn [_] :failure))]
    (is (= :success (handler {})))))
