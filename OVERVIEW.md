# farcaster-cljs Quick Reference

One-page overview of the entire API.

## Installation

```bash
npm install @farcaster/frame-sdk
```

```clojure
;; shadow-cljs.edn
{:dependencies [[farcaster-cljs "0.1.0"]]}
```

## Basic Usage Pattern

```clojure
(ns my-app
  (:require [farcaster-cljs.core :as fc]
            [clojure.core.async :refer [go <!]]))

(go
  (<! (fc/init!))
  (<! (fc/ready!))
  ;; Use SDK functions here
  )
```

## All Functions at a Glance

### 🚀 Initialization

| Function | Description |
|----------|-------------|
| `(init!)` | Initialize SDK |
| `(ready!)` | Hide splash screen |
| `(quick-start!)` | Init + ready combined |
| `(close!)` | Close the mini app |

### 👤 User Context

| Function | Returns |
|----------|---------|
| `(get-context)` | Full context object |
| `(get-user)` | User object |
| `(get-fid)` | User's FID |
| `(get-username)` | Username string |
| `(get-display-name)` | Display name |
| `(get-profile-image)` | Profile image URL |
| `(get-bio)` | Bio text |
| `(get-verified-addresses)` | {:eth-addresses [...] :sol-addresses [...]} |
| `(get-custody-address)` | Custody address |
| `(is-logged-in?)` | Boolean |
| `(get-user-summary)` | Convenient user summary map |

### 📝 Actions

| Function | Parameters | Description |
|----------|-----------|-------------|
| `(compose-cast! opts)` | `{:text "..." :embeds [...]}` | Open cast composer |
| `(add-mini-app! opts)` | `{:name "..." :icon-url "..." :description "..."}` | Prompt to bookmark |
| `(view-profile! fid)` | FID number | Open user profile |
| `(open-url! url)` | URL string | Open external URL |
| `(sign-in! opts)` | `{:nonce "..." :domain "..."}` | SIWE sign-in |

### 💼 Ethereum Wallet

| Function | Parameters | Returns |
|----------|-----------|---------|
| `(get-ethereum-provider)` | - | Provider object (sync) |
| `(request-ethereum-accounts)` | - | Channel with [accounts] |
| `(get-ethereum-chain-id)` | - | Channel with chain ID |
| `(send-ethereum-transaction tx)` | `{:to "0x..." :value "0x..." :from "0x..."}` | Channel with {:hash "..."} |
| `(sign-ethereum-message opts)` | `{:message "..." :address "0x..."}` | Channel with {:signature "..."} |
| `(sign-typed-data opts)` | `{:address "0x..." :typed-data {...}}` | Channel with {:signature "..."} |

### 🔮 Solana Wallet (Experimental)

| Function | Parameters | Returns |
|----------|-----------|---------|
| `(get-solana-provider)` | - | Provider object (sync) |
| `(request-solana-accounts)` | - | Channel with account info |
| `(sign-solana-message msg)` | Message string | Channel with {:signature "..."} |

### 🐛 Debug Helpers

| Function | Description |
|----------|-------------|
| `(log-context)` | Log full context to console |
| `(log-user)` | Log user info to console |
| `(log-available-actions)` | Log available SDK actions |
| `(log-ethereum-provider)` | Log Ethereum provider |
| `(log-solana-provider)` | Log Solana provider |
| `(debug-info)` | Get SDK state info |

## Common Patterns

### Get User Info and Compose Cast

```clojure
(go
  (let [username (<! (fc/get-username))]
    (<! (fc/compose-cast!
         {:text (str "Hello from @" username "!")}))))
```

### Connect Wallet and Send Transaction

```clojure
(go
  (let [accounts (<! (fc/request-ethereum-accounts))
        account (first accounts)]
    (let [result (<! (fc/send-ethereum-transaction
                      {:to "0x..."
                       :value "0x38D7EA4C68000"
                       :from account}))]
      (println "TX:" (:hash result)))))
```

### Complete App Flow

```clojure
(go
  ;; Init
  (<! (fc/quick-start!))

  ;; Get user
  (let [summary (<! (fc/get-user-summary))]
    (println "Welcome" (:username summary)))

  ;; Prompt to bookmark
  (<! (fc/add-mini-app!))

  ;; Compose cast
  (<! (fc/compose-cast-with-app!
       {:text "Check out this app!"
        :app-url "https://myapp.com"})))
```

### Error Handling

```clojure
(go
  (let [result (<! (fc/request-ethereum-accounts))]
    (if (:error result)
      (println "Error:" (:error result))
      (println "Connected:" result))))
```

### Using with Reagent

```clojure
(ns my-app.core
  (:require [reagent.core :as r]
            [farcaster-cljs.core :as fc]
            [clojure.core.async :refer [go <!]]))

(defonce user-state (r/atom nil))

(defn load-user! []
  (go
    (let [summary (<! (fc/get-user-summary))]
      (reset! user-state summary))))

(defn app []
  [:div
   [:h1 "Hello " (:username @user-state)]
   [:button {:on-click #(go (<! (fc/compose-cast!)))}
    "Compose Cast"]])

(defn init! []
  (go
    (<! (fc/quick-start!))
    (load-user!)
    (r/render [app] (.getElementById js/document "app"))))
```

## Return Values

All async functions return **core.async channels**:

```clojure
;; Use in go blocks with <!
(go
  (let [result (<! (fc/get-username))]
    (println result)))

;; Or with callbacks
(let [c (fc/get-username)]
  (async/take! c (fn [result]
                  (println result))))
```

## Data Formats

### User Object
```clojure
{:fid 123
 :username "alice"
 :displayName "Alice"
 :profileImage "https://..."
 :bio "Builder"
 :custodyAddress "0x..."
 :ethAddresses ["0x..."]
 :solAddresses ["..."]}
```

### Verified Addresses
```clojure
{:eth-addresses ["0x..." "0x..."]
 :sol-addresses ["..." "..."]}
```

### Transaction
```clojure
{:to "0x..."           ;; required
 :value "0x..."        ;; optional (hex wei)
 :data "0x..."         ;; optional
 :from "0x..."}        ;; optional
```

## Namespace Organization

```
farcaster-cljs.core      -- Main API (use this!)
  ├── actions            -- SDK actions
  ├── context            -- User context
  ├── wallet             -- Wallet operations
  └── specs              -- Validation
```

For most use cases, just require `farcaster-cljs.core`:

```clojure
(ns my-app
  (:require [farcaster-cljs.core :as fc]))
```

## Next Steps

- Read [README.md](./README.md) for detailed documentation
- Check [example.cljs](./src/farcaster_cljs/example.cljs) for comprehensive examples
- Review [Farcaster Frames docs](https://docs.farcaster.xyz/reference/frames/)

## Tips

1. **Always init first**: Call `init!` or `quick-start!` before using SDK
2. **Use go blocks**: All async functions return channels
3. **Check for errors**: Results may contain `:error` key
4. **Debug helpers**: Use `log-*` functions to inspect state
5. **Flexible specs**: Validation won't block edge cases
6. **Provider access**: `get-*-provider` returns immediately (not a channel)
