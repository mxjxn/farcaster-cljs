(ns promo-examples
  "Short, screenshot-friendly examples for promotional posts"
  (:require [farcaster-cljs.core :as fc]
            [clojure.core.async :refer [go <!]]))

;; ============================================================================
;; Example 1: Quick Start
;; ============================================================================

(defn init-app []
  (go
    ;; Initialize SDK and signal ready
    (<! (fc/quick-start!))

    ;; Get user info
    (let [username (<! (fc/get-username))
          fid (<! (fc/get-fid))]
      (println "Hello" username "!"))

    ;; Compose a cast
    (<! (fc/compose-cast!
         {:text "Built with farcaster-cljs!"
          :embeds ["https://myapp.com"]}))))

;; ============================================================================
;; Example 2: User Context
;; ============================================================================

(defn show-user-profile []
  (go
    (let [username (<! (fc/get-username))
          fid (<! (fc/get-fid))
          addresses (<! (fc/get-verified-addresses))]

      {:username username
       :fid fid
       :eth-addresses (:eth-addresses addresses)
       :sol-addresses (:sol-addresses addresses)})))

;; ============================================================================
;; Example 3: Ethereum Wallet
;; ============================================================================

(defn send-tip [to-address]
  (go
    ;; Connect wallet
    (let [accounts (<! (fc/request-ethereum-accounts))
          from (first accounts)]

      ;; Send 0.001 ETH tip
      (let [result (<! (fc/send-ethereum-transaction
                        {:to to-address
                         :value "0x38D7EA4C68000"
                         :from from}))]

        ;; Share on Farcaster
        (<! (fc/compose-cast!
             {:text (str "Just sent a tip! 🎉\nTx: "
                        (:hash result))}))))))

;; ============================================================================
;; Example 4: Complete Mini App Flow
;; ============================================================================

(defn mini-app-flow []
  (go
    ;; Initialize
    (<! (fc/quick-start!))

    ;; Get user
    (let [username (<! (fc/get-username))]
      (println "Welcome" username "!")

      ;; Connect wallet
      (let [accounts (<! (fc/request-ethereum-accounts))
            chain-id (<! (fc/get-ethereum-chain-id))]

        ;; Sign message
        (<! (fc/sign-ethereum-message
             {:message "I love farcaster-cljs!"
              :address (first accounts)}))

        ;; Bookmark app
        (<! (fc/add-mini-app!
             {:name "My Cool App"
              :description "Built with ClojureScript"}))))))
