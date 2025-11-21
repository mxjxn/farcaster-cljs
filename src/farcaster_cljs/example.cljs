(ns farcaster-cljs.example
  "Comprehensive examples demonstrating all Farcaster SDK features.

  This namespace shows how to use every SDK action, similar to the
  standard Neynar Farcaster starter examples."
  (:require [farcaster-cljs.core :as fc]
            [clojure.core.async :refer [go <!]]))

;; ============================================================================
;; Initialization & Lifecycle
;; ============================================================================

(defn example-init
  "Example: Initialize the SDK and signal ready."
  []
  (go
    ;; Initialize SDK
    (let [init-result (<! (fc/init!))]
      (if (:success init-result)
        (do
          (println "✓ SDK initialized successfully")
          ;; Signal that app is ready (hides splash screen)
          (<! (fc/ready!))
          (println "✓ App ready, splash screen hidden"))
        (println "✗ Failed to initialize:" (:error init-result))))))

(defn example-quick-start
  "Example: Use the convenience quick-start function."
  []
  (go
    (<! (fc/quick-start!))
    (println "✓ SDK initialized and ready!")))

;; ============================================================================
;; User Context & Information
;; ============================================================================

(defn example-get-user-info
  "Example: Get various pieces of user information."
  []
  (go
    ;; Get full user object
    (let [user (<! (fc/get-user))]
      (println "Full user object:" user))

    ;; Get specific fields
    (let [fid (<! (fc/get-fid))
          username (<! (fc/get-username))
          display-name (<! (fc/get-display-name))
          bio (<! (fc/get-bio))]
      (println "FID:" fid)
      (println "Username:" username)
      (println "Display name:" display-name)
      (println "Bio:" bio))

    ;; Get verified addresses
    (let [addresses (<! (fc/get-verified-addresses))]
      (println "Ethereum addresses:" (:eth-addresses addresses))
      (println "Solana addresses:" (:sol-addresses addresses)))

    ;; Check if user is logged in
    (let [logged-in? (<! (fc/is-logged-in?))]
      (println "User logged in?" logged-in?))))

(defn example-user-summary
  "Example: Get a convenient user summary."
  []
  (go
    (let [summary (<! (fc/get-user-summary))]
      (println "User Summary:")
      (println "  FID:" (:fid summary))
      (println "  Username:" (:username summary))
      (println "  Display Name:" (:display-name summary))
      (println "  Verified Addresses:" (:verified-addresses summary)))))

;; ============================================================================
;; Cast Actions
;; ============================================================================

(defn example-compose-cast
  "Example: Open the cast composer with prefilled content."
  []
  (go
    ;; Simple cast with text
    (<! (fc/compose-cast! {:text "Hello from my Farcaster mini app!"}))

    ;; Cast with text and embeds
    (<! (fc/compose-cast!
         {:text "Check out this cool project!"
          :embeds ["https://github.com/yourproject"
                   "https://yourproject.com"]}))

    ;; Empty cast (just open composer)
    (<! (fc/compose-cast!))))

(defn example-compose-cast-with-user
  "Example: Compose a cast that mentions the current user."
  []
  (go
    (let [username (<! (fc/get-username))]
      (<! (fc/compose-cast!
           {:text (str "Built by @" username " using farcaster-cljs!")})))))

(defn example-compose-cast-with-app
  "Example: Use the convenience function to compose with app URL."
  []
  (go
    (<! (fc/compose-cast-with-app!
         {:text "Try out this awesome mini app!"
          :app-url "https://myapp.com"
          :additional-embeds ["https://docs.myapp.com"]}))))

;; ============================================================================
;; Mini App Actions
;; ============================================================================

(defn example-add-mini-app
  "Example: Prompt user to bookmark the mini app."
  []
  (go
    ;; Basic add
    (let [result (<! (fc/add-mini-app!))]
      (println "Add mini app result:" result))

    ;; Add with custom details
    (<! (fc/add-mini-app!
         {:name "My Awesome App"
          :icon-url "https://myapp.com/icon.png"
          :description "An amazing Farcaster mini app"}))))

;; ============================================================================
;; Navigation Actions
;; ============================================================================

(defn example-view-profile
  "Example: Open a Farcaster user profile."
  []
  (go
    ;; View a specific user's profile (FID 3 is @dwr)
    (<! (fc/view-profile! 3))

    ;; View the current user's profile
    (let [my-fid (<! (fc/get-fid))]
      (<! (fc/view-profile! my-fid)))))

(defn example-open-url
  "Example: Open external URLs."
  []
  (go
    ;; Open Warpcast
    (<! (fc/open-url! "https://warpcast.com"))

    ;; Open documentation
    (<! (fc/open-url! "https://docs.farcaster.xyz"))

    ;; Open GitHub repo
    (<! (fc/open-url! "https://github.com/yourproject"))))

(defn example-close-app
  "Example: Close the mini app."
  []
  (go
    ;; Close after showing a thank you message
    (println "Thanks for using the app!")
    (<! (fc/close!))))

;; ============================================================================
;; Authentication
;; ============================================================================

(defn example-sign-in
  "Example: Sign in with Farcaster (SIWE)."
  []
  (go
    ;; Basic sign-in
    (let [result (<! (fc/sign-in!))]
      (println "Sign-in result:" result)
      (println "Signature:" (:signature result)))

    ;; Sign-in with custom options
    (let [nonce (str (random-uuid))
          result (<! (fc/sign-in!
                      {:nonce nonce
                       :domain "myapp.com"
                       :siwe-uri "https://myapp.com/login"}))]
      (println "Authenticated with nonce:" nonce)
      (println "Signature:" (:signature result)))))

;; ============================================================================
;; Ethereum Wallet Operations
;; ============================================================================

(defn example-ethereum-wallet
  "Example: Ethereum wallet operations."
  []
  (go
    ;; Get accounts
    (let [accounts (<! (fc/request-ethereum-accounts))]
      (println "Connected Ethereum accounts:" accounts)
      (let [account (first accounts)]

        ;; Get chain ID
        (let [chain-id (<! (fc/get-ethereum-chain-id))]
          (println "Connected to chain:" chain-id))

        ;; Sign a message
        (let [signature (<! (fc/sign-ethereum-message
                             {:message "Hello Farcaster!"
                              :address account}))]
          (println "Message signature:" (:signature signature)))

        ;; Send a transaction
        (let [tx-result (<! (fc/send-ethereum-transaction
                             {:to "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
                              :value "0x38D7EA4C68000"  ;; 0.001 ETH in wei
                              :from account}))]
          (println "Transaction hash:" (:hash tx-result)))))))

(defn example-sign-typed-data
  "Example: Sign EIP-712 typed data."
  []
  (go
    (let [accounts (<! (fc/request-ethereum-accounts))
          account (first accounts)
          typed-data {:domain {:name "My App"
                               :version "1"
                               :chainId 1
                               :verifyingContract "0x..."}
                      :types {:Person [{:name "name" :type "string"}
                                       {:name "wallet" :type "address"}]}
                      :primaryType "Person"
                      :message {:name "Alice"
                                :wallet "0x..."}}
          signature (<! (fc/sign-typed-data
                         {:address account
                          :typed-data typed-data}))]
      (println "Typed data signature:" (:signature signature)))))

;; ============================================================================
;; Solana Wallet Operations
;; ============================================================================

(defn example-solana-wallet
  "Example: Solana wallet operations (experimental)."
  []
  (go
    ;; Connect to Solana wallet
    (let [account (<! (fc/request-solana-accounts))]
      (println "Connected Solana account:" account)

      ;; Sign a message
      (let [signature (<! (fc/sign-solana-message "Hello Solana!"))]
        (println "Solana message signature:" signature)))))

;; ============================================================================
;; Complete Application Flow
;; ============================================================================

(defn example-complete-flow
  "Example: A complete mini app flow demonstrating multiple features."
  []
  (go
    ;; 1. Initialize
    (<! (fc/quick-start!))
    (println "=== Mini App Started ===")

    ;; 2. Get user info
    (let [username (<! (fc/get-username))
          fid (<! (fc/get-fid))]
      (println (str "Welcome, " username " (FID: " fid ")!"))

      ;; 3. Show user's verified addresses
      (let [addresses (<! (fc/get-verified-addresses))]
        (println "Your verified Ethereum addresses:")
        (doseq [addr (:eth-addresses addresses)]
          (println "  -" addr)))

      ;; 4. Prompt to bookmark the app
      (println "Please bookmark this app!")
      (<! (fc/add-mini-app! {:name "My Cool App"}))

      ;; 5. Connect wallet and get info
      (let [eth-accounts (<! (fc/request-ethereum-accounts))
            chain-id (<! (fc/get-ethereum-chain-id))]
        (println "Connected wallet:" (first eth-accounts))
        (println "On chain:" chain-id))

      ;; 6. Offer to compose a cast
      (<! (fc/compose-cast!
           {:text (str "Just tried out this awesome mini app by @" username "!")
            :embeds ["https://myapp.com"]}))

      (println "=== Thanks for trying the app! ==="))))

;; ============================================================================
;; Debug Examples
;; ============================================================================

(defn example-debug
  "Example: Debug helpers to inspect SDK state."
  []
  (go
    ;; Log full context
    (<! (fc/log-context))

    ;; Log user info
    (<! (fc/log-user))

    ;; Log available actions
    (fc/log-available-actions)

    ;; Log providers
    (fc/log-ethereum-provider)
    (fc/log-solana-provider)

    ;; Get debug info
    (println "Debug info:" (fc/debug-info))))

;; ============================================================================
;; Error Handling Example
;; ============================================================================

(defn example-error-handling
  "Example: Proper error handling for SDK operations."
  []
  (go
    (let [result (<! (fc/init!))]
      (if (:error result)
        (do
          (println "Error initializing SDK:" (:error result))
          ;; Handle error appropriately
          )
        (do
          (println "SDK initialized successfully")
          ;; Continue with app logic

          ;; Example: Handle wallet connection errors
          (let [accounts (<! (fc/request-ethereum-accounts))]
            (if (:error accounts)
              (println "Failed to connect wallet:" (:error accounts))
              (println "Wallet connected:" (first accounts)))))))))
