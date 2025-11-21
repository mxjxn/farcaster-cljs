(ns farcaster-cljs.context
  "Access Farcaster user context and client information.

  The context provides information about the current user viewing the mini app,
  including their FID, username, verified addresses, and more."
  (:require [clojure.core.async :refer [go chan put!]]
            [farcaster-cljs.specs :as specs]))

(def ^:private sdk (atom nil))

(defn set-sdk!
  "Internal: Set the SDK instance for context operations."
  [sdk-instance]
  (reset! sdk sdk-instance))

(defn get-context
  "Get the full Farcaster context object.
  Returns a channel that will receive the context map.

  Context includes:
  - :user - User information (FID, username, profile, etc.)
  - :client - Client information
  - :location - Location/environment info

  Example:
    (go
      (let [ctx (<! (get-context))]
        (println \"User FID:\" (-> ctx :user :fid))))"
  []
  (let [c (chan)]
    (if-let [s @sdk]
      (-> (.-context s)
          (.then #(put! c (js->clj % :keywordize-keys true)))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "SDK not initialized"}))
    c))

(defn get-user
  "Get user information from context.
  Returns a channel with user map containing :fid, :username, etc."
  []
  (go
    (let [ctx (:user (clojure.core.async/<! (get-context)))]
      ctx)))

(defn get-fid
  "Get the user's FID (Farcaster ID).
  Returns a channel with the FID as an integer."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:fid user))))

(defn get-username
  "Get the user's Farcaster username.
  Returns a channel with the username string."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:username user))))

(defn get-display-name
  "Get the user's display name.
  Returns a channel with the display name string."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:displayName user))))

(defn get-profile-image
  "Get the user's profile image URL.
  Returns a channel with the image URL string."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:profileImage user))))

(defn get-bio
  "Get the user's bio text.
  Returns a channel with the bio string."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:bio user))))

(defn get-verified-addresses
  "Get the user's verified addresses.
  Returns a channel with a map containing:
  - :eth-addresses - Vector of verified Ethereum addresses
  - :sol-addresses - Vector of verified Solana addresses"
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      {:eth-addresses (:ethAddresses user)
       :sol-addresses (:solAddresses user)})))

(defn get-custody-address
  "Get the user's custody address.
  Returns a channel with the custody address string."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (:custodyAddress user))))

(defn is-logged-in?
  "Check if a user is currently logged in.
  Returns a channel with a boolean."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (some? (:fid user)))))

;; Debug helpers
(defn log-context
  "Debug helper: Log the full context to console.
  Returns a channel that completes when logging is done."
  []
  (go
    (let [ctx (clojure.core.async/<! (get-context))]
      (js/console.log "Farcaster Context:" (clj->js ctx))
      ctx)))

(defn log-user
  "Debug helper: Log user info to console.
  Returns a channel that completes when logging is done."
  []
  (go
    (let [user (clojure.core.async/<! (get-user))]
      (js/console.log "Farcaster User:" (clj->js user))
      user)))
