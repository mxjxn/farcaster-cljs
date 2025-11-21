(ns farcaster-cljs.actions
  "Farcaster SDK actions for mini app interactions.

  This namespace provides all the core SDK actions like composing casts,
  bookmarking the app, viewing profiles, and more."
  (:require [clojure.core.async :refer [go chan put!]]
            [farcaster-cljs.specs :as specs]))

(def ^:private sdk (atom nil))

(defn set-sdk!
  "Internal: Set the SDK instance for action operations."
  [sdk-instance]
  (reset! sdk sdk-instance))

;; Core lifecycle actions

(defn ready!
  "Signal that the mini app is ready and hide the splash screen.
  Should be called once your app has finished loading.

  Returns a channel that completes when the action is done.

  Example:
    (go
      (<! (ready!))
      (println \"App is ready!\"))"
  []
  (let [c (chan)]
    (if-let [s @sdk]
      (-> (.. s -actions (ready))
          (.then #(put! c {:success true}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "SDK not initialized"}))
    c))

(defn close!
  "Close the mini app.
  Returns a channel that completes when the action is done.

  Example:
    (go
      (<! (close!)))"
  []
  (let [c (chan)]
    (if-let [s @sdk]
      (-> (.. s -actions (close))
          (.then #(put! c {:success true}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "SDK not initialized"}))
    c))

;; Cast and content actions

(defn compose-cast!
  "Open the cast composer with optional prefilled text and embeds.

  Options map can include:
  - :text - Prefilled text for the cast
  - :embeds - Vector of URLs to embed

  Returns a channel that completes when the composer is opened.

  Example:
    (go
      (<! (compose-cast! {:text \"Check out this cool app!\"
                          :embeds [\"https://example.com\"]})))"
  ([]
   (compose-cast! {}))
  ([opts]
   (let [c (chan)
         validated-opts (specs/validate-or-warn ::specs/compose-cast-opts opts)
         js-opts (clj->js (when (seq validated-opts)
                           validated-opts))]
     (if-let [s @sdk]
       (-> (.. s -actions (composeCast js-opts))
           (.then #(put! c {:success true}))
           (.catch #(put! c {:error (.-message %)})))
       (put! c {:error "SDK not initialized"}))
     c)))

;; Mini app actions

(defn add-mini-app!
  "Prompt the user to add/bookmark this mini app.

  Options map can include:
  - :name - Name of the mini app (optional)
  - :icon-url - URL to the app icon (optional)
  - :description - App description (optional)

  Returns a channel with the result.

  Example:
    (go
      (<! (add-mini-app! {:name \"My Cool App\"})))"
  ([]
   (add-mini-app! {}))
  ([opts]
   (let [c (chan)
         validated-opts (specs/validate-or-warn ::specs/add-mini-app-opts opts)
         js-opts (clj->js (when (seq validated-opts)
                           validated-opts))]
     (if-let [s @sdk]
       (-> (.. s -actions (addMiniApp js-opts))
           (.then #(put! c (js->clj % :keywordize-keys true)))
           (.catch #(put! c {:error (.-message %)})))
       (put! c {:error "SDK not initialized"}))
     c)))

;; Navigation actions

(defn view-profile!
  "Open a Farcaster user's profile.

  fid - The Farcaster ID of the user to view

  Returns a channel that completes when the action is done.

  Example:
    (go
      (<! (view-profile! 3)))"
  [fid]
  (let [c (chan)]
    (if-let [s @sdk]
      (-> (.. s -actions (viewProfile fid))
          (.then #(put! c {:success true}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "SDK not initialized"}))
    c))

(defn open-url!
  "Open an external URL.

  url - The URL to open (must be a valid http/https URL)

  Returns a channel that completes when the action is done.

  Example:
    (go
      (<! (open-url! \"https://warpcast.com\")))"
  [url]
  (let [c (chan)
        validated (specs/validate ::specs/url url)]
    (if-let [s @sdk]
      (-> (.. s -actions (openUrl validated))
          (.then #(put! c {:success true}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "SDK not initialized"}))
    c))

;; Authentication actions

(defn sign-in!
  "Sign in with Farcaster (SIWE - Sign In With Ethereum).

  Options map can include:
  - :nonce - Cryptographic nonce for the signature
  - :request-id - Optional request identifier
  - :siwe-uri - The URI for SIWE
  - :domain - The domain requesting sign-in

  Returns a channel with the signature result.

  Example:
    (go
      (let [result (<! (sign-in! {:nonce \"abc123\"
                                  :domain \"myapp.com\"}))]
        (println \"Signature:\" (:signature result))))"
  ([]
   (sign-in! {}))
  ([opts]
   (let [c (chan)
         validated-opts (specs/validate-or-warn ::specs/sign-in-opts opts)
         js-opts (clj->js (when (seq validated-opts)
                           validated-opts))]
     (if-let [s @sdk]
       (-> (.. s -actions (signIn js-opts))
           (.then #(put! c (js->clj % :keywordize-keys true)))
           (.catch #(put! c {:error (.-message %)})))
       (put! c {:error "SDK not initialized"}))
     c)))

;; Debug helpers

(defn log-available-actions
  "Debug helper: Log all available SDK actions to console."
  []
  (when-let [s @sdk]
    (js/console.log "Available SDK actions:" (.-actions s))
    (js/console.log "Action methods:" (.keys js/Object (.-actions s)))))
