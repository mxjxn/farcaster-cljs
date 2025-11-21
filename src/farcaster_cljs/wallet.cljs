(ns farcaster-cljs.wallet
  "Wallet integration for Ethereum and Solana.

  Provides access to EIP-1193 Ethereum provider and Solana provider,
  along with helpers for common wallet operations like sending transactions,
  signing messages, and more."
  (:require [clojure.core.async :refer [go chan put!]]
            [farcaster-cljs.specs :as specs]))

(def ^:private sdk (atom nil))

(defn set-sdk!
  "Internal: Set the SDK instance for wallet operations."
  [sdk-instance]
  (reset! sdk sdk-instance))

;; Provider access

(defn get-ethereum-provider
  "Get the EIP-1193 Ethereum provider.
  Returns the provider object directly (not a channel).

  The provider can be used with libraries like ethers.js or viem.

  Example:
    (let [provider (get-ethereum-provider)]
      ;; Use with ethers or viem
      )"
  []
  (when-let [s @sdk]
    (.. s -wallet (getEthereumProvider))))

(defn get-solana-provider
  "Get the Solana provider (experimental).
  Returns the provider object directly (not a channel).

  Example:
    (let [provider (get-solana-provider)]
      ;; Use with Solana web3.js
      )"
  []
  (when-let [s @sdk]
    (.. s -wallet (getSolanaProvider))))

;; Ethereum operations

(defn request-ethereum-accounts
  "Request access to the user's Ethereum accounts.
  Returns a channel with a vector of account addresses.

  Example:
    (go
      (let [accounts (<! (request-ethereum-accounts))]
        (println \"Connected account:\" (first accounts))))"
  []
  (let [c (chan)
        provider (get-ethereum-provider)]
    (if provider
      (-> (.request provider #js {:method "eth_requestAccounts"})
          (.then #(put! c (js->clj % :keywordize-keys true)))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Ethereum provider not available"}))
    c))

(defn get-ethereum-chain-id
  "Get the current Ethereum chain ID.
  Returns a channel with the chain ID as a hex string.

  Example:
    (go
      (let [chain-id (<! (get-ethereum-chain-id))]
        (println \"Chain ID:\" chain-id)))"
  []
  (let [c (chan)
        provider (get-ethereum-provider)]
    (if provider
      (-> (.request provider #js {:method "eth_chainId"})
          (.then #(put! c %))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Ethereum provider not available"}))
    c))

(defn send-ethereum-transaction
  "Send an Ethereum transaction.

  transaction - Map with:
    - :to (required) - Recipient address
    - :value (optional) - Amount in wei (hex string)
    - :data (optional) - Transaction data (hex string)
    - :from (optional) - Sender address

  Returns a channel with the transaction hash.

  Example:
    (go
      (let [tx-hash (<! (send-ethereum-transaction
                         {:to \"0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb\"
                          :value \"0x38D7EA4C68000\"}))]
        (println \"Transaction:\" tx-hash)))"
  [transaction]
  (let [c (chan)
        validated (specs/validate ::specs/transaction transaction)
        provider (get-ethereum-provider)]
    (if provider
      (-> (.request provider #js {:method "eth_sendTransaction"
                                  :params #js [(clj->js validated)]})
          (.then #(put! c {:hash %}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Ethereum provider not available"}))
    c))

(defn sign-ethereum-message
  "Sign a message with the user's Ethereum account.

  Options:
  - :message (required) - The message to sign
  - :address (required) - The address to sign with

  Returns a channel with the signature.

  Example:
    (go
      (let [sig (<! (sign-ethereum-message
                     {:message \"Hello Farcaster!\"
                      :address \"0x...\"}))])))"
  [{:keys [message address]}]
  (let [c (chan)
        provider (get-ethereum-provider)]
    (if provider
      (-> (.request provider #js {:method "personal_sign"
                                  :params #js [message address]})
          (.then #(put! c {:signature %}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Ethereum provider not available"}))
    c))

(defn sign-typed-data
  "Sign typed data (EIP-712) with the user's Ethereum account.

  Options:
  - :address (required) - The address to sign with
  - :typed-data (required) - The EIP-712 typed data object

  Returns a channel with the signature.

  Example:
    (go
      (let [sig (<! (sign-typed-data
                     {:address \"0x...\"
                      :typed-data {...}}))]))"
  [{:keys [address typed-data]}]
  (let [c (chan)
        validated (specs/validate ::specs/sign-typed-data-opts {:typed-data typed-data})
        provider (get-ethereum-provider)]
    (if provider
      (-> (.request provider #js {:method "eth_signTypedData_v4"
                                  :params #js [address (js/JSON.stringify (clj->js typed-data))]})
          (.then #(put! c {:signature %}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Ethereum provider not available"}))
    c))

;; Solana operations

(defn request-solana-accounts
  "Request access to the user's Solana accounts.
  Returns a channel with account information.

  Example:
    (go
      (let [accounts (<! (request-solana-accounts))]
        (println \"Connected Solana account:\" accounts)))"
  []
  (let [c (chan)
        provider (get-solana-provider)]
    (if provider
      (-> (.connect provider)
          (.then #(put! c (js->clj % :keywordize-keys true)))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Solana provider not available"}))
    c))

(defn sign-solana-message
  "Sign a message with the user's Solana account.

  message - The message to sign (string or Uint8Array)

  Returns a channel with the signature.

  Example:
    (go
      (let [sig (<! (sign-solana-message \"Hello Solana!\"))]
        (println \"Signature:\" sig)))"
  [message]
  (let [c (chan)
        provider (get-solana-provider)]
    (if provider
      (-> (.signMessage provider (if (string? message)
                                   (.encode (js/TextEncoder.) message)
                                   message))
          (.then #(put! c {:signature %}))
          (.catch #(put! c {:error (.-message %)})))
      (put! c {:error "Solana provider not available"}))
    c))

;; Debug helpers

(defn log-ethereum-provider
  "Debug helper: Log the Ethereum provider to console."
  []
  (when-let [provider (get-ethereum-provider)]
    (js/console.log "Ethereum Provider:" provider)
    provider))

(defn log-solana-provider
  "Debug helper: Log the Solana provider to console."
  []
  (when-let [provider (get-solana-provider)]
    (js/console.log "Solana Provider:" provider)
    provider))
