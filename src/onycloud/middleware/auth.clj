(ns onycloud.middleware.auth)

(def ^:dynamic *authenticated-user*)

(defn- fail-auth [req]
  {:status 401, :body "Authentication failed."})

(defn wrap-auth
  "A middleware that adds authentication. 'auth' is an authentication
   function that takes a request and returns a user object (success)
   or nil (failure). The returned user object is bound to
   *authenticated-user* when the wrapped handler is called. If 'auth'
   returns nil, 'failure' will be called with the request as argument,
   and the handler will not be called."
  [handler & {:keys [auth failure] :or {failure fail-auth}}]
  {:pre [auth]}
  (fn [req]
    (if-let [user (auth req)]
      (binding [*authenticated-user* user] ;; TODO: Remove
        (handler req))
      (failure req))))
