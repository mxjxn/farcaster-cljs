(ns farcaster-cljs.core
  "Main entry point for the Farcaster ClojureScript SDK wrapper.

  This namespace provides a convenient API for all Farcaster mini app
  functionality, including:
  - SDK initialization
  - User context and information
  - Actions (compose cast, add mini app, etc.)
  - Wallet integration (Ethereum & Solana)

  Quick start:
    (require '[farcaster-cljs.core :as fc])
    (require '[clojure.core.async :refer [go <!]])

    (go
      (<! (fc/init!))
      (<! (fc/ready!))
      (let [username (<! (fc/get-username))]
        (println \"Hello\" username)))"
  (:require [clojure.core.async :refer [go chan put!]]
            [farcaster-cljs.actions :as actions]
            [farcaster-cljs.context :as context]
            [farcaster-cljs.wallet :as wallet]
            [farcaster-cljs.specs :as specs]
            ["@farcaster/frame-sdk" :as fc-sdk]))

(def ^:private sdk (atom nil))
(def ^:private initialized? (atom false))

;; Initialization

(defn init!
  "Initialize the Farcaster SDK.
  Must be called before using any other SDK functions.

  Returns a channel that completes when initialization is done.

  Example:
    (go
      (<! (init!))
      (println \"SDK initialized!\"))"
  []
  (let [c (chan)]
    (if @initialized?
      (do
        (js/console.warn "SDK already initialized")
        (put! c {:success true :already-initialized true}))
      (try
        (let [sdk-instance (fc-sdk/default)]
          (reset! sdk sdk-instance)
          (reset! initialized? true)
          ;; Set SDK instance for all modules
          (actions/set-sdk! sdk-instance)
          (context/set-sdk! sdk-instance)
          (wallet/set-sdk! sdk-instance)
          (put! c {:success true :sdk sdk-instance}))
        (catch js/Error e
          (js/console.error "Failed to initialize SDK:" e)
          (put! c {:error (.-message e)}))))
    c))

(defn get-sdk
  "Get the raw SDK instance (for advanced usage).
  Returns nil if not initialized."
  []
  @sdk)

(defn initialized?
  "Check if the SDK has been initialized."
  []
  @initialized?)

;; Re-export all actions

(def ready! actions/ready!)
(def close! actions/close!)
(def compose-cast! actions/compose-cast!)
(def add-mini-app! actions/add-mini-app!)
(def view-profile! actions/view-profile!)
(def open-url! actions/open-url!)
(def sign-in! actions/sign-in!)
(def log-available-actions actions/log-available-actions)

;; Re-export all context functions

(def get-context context/get-context)
(def get-user context/get-user)
(def get-fid context/get-fid)
(def get-username context/get-username)
(def get-display-name context/get-display-name)
(def get-profile-image context/get-profile-image)
(def get-bio context/get-bio)
(def get-verified-addresses context/get-verified-addresses)
(def get-custody-address context/get-custody-address)
(def is-logged-in? context/is-logged-in?)
(def log-context context/log-context)
(def log-user context/log-user)

;; Re-export all wallet functions

(def get-ethereum-provider wallet/get-ethereum-provider)
(def get-solana-provider wallet/get-solana-provider)
(def request-ethereum-accounts wallet/request-ethereum-accounts)
(def get-ethereum-chain-id wallet/get-ethereum-chain-id)
(def send-ethereum-transaction wallet/send-ethereum-transaction)
(def sign-ethereum-message wallet/sign-ethereum-message)
(def sign-typed-data wallet/sign-typed-data)
(def request-solana-accounts wallet/request-solana-accounts)
(def sign-solana-message wallet/sign-solana-message)
(def log-ethereum-provider wallet/log-ethereum-provider)
(def log-solana-provider wallet/log-solana-provider)

;; Re-export spec validation helpers

(def validate specs/validate)
(def validate-or-warn specs/validate-or-warn)

;; Convenience helpers

(defn quick-start!
  "Quick start helper that initializes SDK and signals ready.
  Returns a channel that completes when both operations are done.

  Example:
    (go
      (<! (quick-start!))
      ;; Your app code here
      )"
  []
  (go
    (clojure.core.async/<! (init!))
    (clojure.core.async/<! (ready!))))

(defn get-user-summary
  "Get a summary of the current user's information.
  Returns a channel with a map containing common user fields.

  Example:
    (go
      (let [summary (<! (get-user-summary))]
        (println \"User:\" (:username summary) \"FID:\" (:fid summary))))"
  []
  (go
    (let [user (clojure.core.async/<! (get-user))
          addresses (clojure.core.async/<! (get-verified-addresses))]
      {:fid (:fid user)
       :username (:username user)
       :display-name (:displayName user)
       :profile-image (:profileImage user)
       :bio (:bio user)
       :verified-addresses addresses})))

(defn compose-cast-with-app!
  "Convenience function to compose a cast with app information embedded.
  Automatically includes your app's URL in the embeds.

  Options:
  - :text - The cast text
  - :app-url - Your app's URL to embed (required)
  - :additional-embeds - Vector of additional URLs to embed

  Example:
    (go
      (<! (compose-cast-with-app!
           {:text \"Check out this cool mini app!\"
            :app-url \"https://myapp.com\"})))"
  [{:keys [text app-url additional-embeds]}]
  (let [all-embeds (vec (concat [app-url] (or additional-embeds [])))]
    (compose-cast! {:text text
                    :embeds all-embeds})))

;; Debug helper to check everything
(defn debug-info
  "Get debug information about the SDK state.
  Returns a map with initialization status and available functions."
  []
  {:initialized? @initialized?
   :sdk-instance (some? @sdk)
   :available-namespaces [:actions :context :wallet :specs]})
