(ns farcaster-cljs.specs
  "Flexible clojure.spec definitions for Farcaster SDK data structures.
  These specs validate required fields without being overly restrictive."
  (:require [clojure.spec.alpha :as s]))

;; Basic types
(s/def ::non-empty-string (s/and string? #(not (empty? %))))
(s/def ::url (s/and string? #(re-matches #"^https?://.*" %)))

;; Context specs (flexible - only validates what we need)
(s/def ::fid int?)
(s/def ::username string?)
(s/def ::display-name string?)
(s/def ::profile-image string?)
(s/def ::bio string?)

(s/def ::user-context
  (s/keys :opt-un [::fid ::username ::display-name ::profile-image ::bio]))

;; Compose cast options
(s/def ::text string?)
(s/def ::embeds (s/coll-of ::url))

(s/def ::compose-cast-opts
  (s/keys :opt-un [::text ::embeds]))

;; Add mini app options
(s/def ::name ::non-empty-string)
(s/def ::icon-url ::url)
(s/def ::description string?)

(s/def ::add-mini-app-opts
  (s/keys :opt-un [::name ::icon-url ::description]))

;; Open URL options
(s/def ::open-url-opts
  (s/keys :req-un [::url]))

;; Sign-in options
(s/def ::nonce ::non-empty-string)
(s/def ::request-id string?)
(s/def ::siwe-uri ::url)
(s/def ::domain ::non-empty-string)

(s/def ::sign-in-opts
  (s/keys :opt-un [::nonce ::request-id ::siwe-uri ::domain]))

;; Wallet transaction options
(s/def ::to ::non-empty-string)
(s/def ::value string?)
(s/def ::data string?)
(s/def ::chain-id int?)

(s/def ::transaction
  (s/keys :req-un [::to]
          :opt-un [::value ::data ::chain-id]))

;; Sign message options
(s/def ::message ::non-empty-string)

(s/def ::sign-message-opts
  (s/keys :req-un [::message]))

;; Sign typed data options (flexible - EIP-712 can vary)
(s/def ::typed-data map?)

(s/def ::sign-typed-data-opts
  (s/keys :req-un [::typed-data]))

;; Helper functions
(defn validate
  "Validate data against a spec. Returns data if valid, throws if invalid."
  [spec data]
  (if (s/valid? spec data)
    data
    (throw (ex-info "Spec validation failed"
                    {:spec spec
                     :data data
                     :explain (s/explain-str spec data)}))))

(defn validate-or-warn
  "Validate data against spec, but only warn if invalid (don't throw).
  Useful for non-critical validations."
  [spec data]
  (when-not (s/valid? spec data)
    (js/console.warn "Spec validation warning:"
                     (s/explain-str spec data)))
  data)
