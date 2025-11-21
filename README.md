# farcaster-cljs

A production-ready ClojureScript wrapper for the [Farcaster Mini App SDK](https://miniapps.farcaster.xyz/), providing idiomatic CLJS access to all Farcaster mini app functionality.

## Features

✅ **Complete SDK Coverage** - All Farcaster SDK actions wrapped
✅ **Idiomatic CLJS** - core.async channels for all async operations
✅ **Flexible Specs** - clojure.spec validation without being overly restrictive
✅ **Wallet Integration** - Full Ethereum (EIP-1193) and Solana support
✅ **User Context** - Easy access to FID, username, verified addresses
✅ **Type Safe** - Proper JS interop with keyword maps
✅ **Debug Helpers** - Built-in debugging utilities

## Installation

Add to your `shadow-cljs.edn` or `deps.edn`:

```clojure
;; shadow-cljs.edn
{:dependencies [[farcaster-cljs "0.1.0"]]}
```

And install the npm dependency:

```bash
npm install @farcaster/miniapp-sdk
```

## Quick Start

```clojure
(ns my-app.core
  (:require [farcaster-cljs.core :as fc]
            [clojure.core.async :refer [go <!]]))

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
```

## API Reference

### Initialization

#### `(init!)`
Initialize the Farcaster SDK. Must be called before any other SDK functions.

```clojure
(go
  (let [result (<! (fc/init!))]
    (if (:success result)
      (println "SDK ready!")
      (println "Error:" (:error result)))))
```

#### `(ready!)`
Signal that your app is ready and hide the splash screen.

```clojure
(go
  (<! (fc/ready!))
  (println "Splash screen hidden"))
```

#### `(quick-start!)`
Convenience function that calls both `init!` and `ready!`.

```clojure
(go
  (<! (fc/quick-start!))
  ;; Your app code here
  )
```

### User Context

#### `(get-user)`
Get the full user object.

```clojure
(go
  (let [user (<! (fc/get-user))]
    (println user)))
```

#### `(get-fid)`, `(get-username)`, `(get-display-name)`, etc.
Get specific user properties.

```clojure
(go
  (let [fid (<! (fc/get-fid))
        username (<! (fc/get-username))
        bio (<! (fc/get-bio))]
    (println fid username bio)))
```

#### `(get-verified-addresses)`
Get the user's verified blockchain addresses.

```clojure
(go
  (let [addresses (<! (fc/get-verified-addresses))]
    (println "ETH:" (:eth-addresses addresses))
    (println "SOL:" (:sol-addresses addresses))))
```

#### `(is-logged-in?)`
Check if a user is currently logged in.

```clojure
(go
  (when (<! (fc/is-logged-in?))
    (println "User is logged in!")))
```

### Actions

#### `(compose-cast! opts)`
Open the cast composer with optional prefilled content.

```clojure
;; Simple cast
(go
  (<! (fc/compose-cast! {:text "Hello Farcaster!"})))

;; Cast with embeds
(go
  (<! (fc/compose-cast!
       {:text "Check this out!"
        :embeds ["https://example.com"
                 "https://github.com/myproject"]})))
```

#### `(add-mini-app! opts)`
Prompt the user to bookmark your mini app.

```clojure
(go
  (<! (fc/add-mini-app!
       {:name "My Cool App"
        :icon-url "https://myapp.com/icon.png"
        :description "An amazing mini app"})))
```

#### `(view-profile! fid)`
Open a Farcaster user's profile.

```clojure
;; View @dwr's profile (FID 3)
(go
  (<! (fc/view-profile! 3)))
```

#### `(open-url! url)`
Open an external URL.

```clojure
(go
  (<! (fc/open-url! "https://docs.farcaster.xyz")))
```

#### `(close!)`
Close the mini app.

```clojure
(go
  (<! (fc/close!)))
```

#### `(sign-in! opts)`
Sign in with Farcaster (SIWE).

```clojure
(go
  (let [result (<! (fc/sign-in!
                    {:nonce "abc123"
                     :domain "myapp.com"}))]
    (println "Signature:" (:signature result))))
```

### Wallet - Ethereum

#### `(get-ethereum-provider)`
Get the EIP-1193 Ethereum provider (synchronous).

```clojure
(let [provider (fc/get-ethereum-provider)]
  ;; Use with viem, ethers, etc.
  )
```

#### `(request-ethereum-accounts)`
Request access to the user's Ethereum accounts.

```clojure
(go
  (let [accounts (<! (fc/request-ethereum-accounts))]
    (println "Connected:" (first accounts))))
```

#### `(get-ethereum-chain-id)`
Get the current chain ID.

```clojure
(go
  (let [chain-id (<! (fc/get-ethereum-chain-id))]
    (println "Chain ID:" chain-id)))
```

#### `(send-ethereum-transaction tx)`
Send an Ethereum transaction.

```clojure
(go
  (let [result (<! (fc/send-ethereum-transaction
                    {:to "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
                     :value "0x38D7EA4C68000"  ;; 0.001 ETH
                     :from "0x..."}))]
    (println "TX Hash:" (:hash result))))
```

#### `(sign-ethereum-message opts)`
Sign a message with the user's account.

```clojure
(go
  (let [sig (<! (fc/sign-ethereum-message
                 {:message "Hello Farcaster!"
                  :address "0x..."}))]
    (println "Signature:" (:signature sig))))
```

#### `(sign-typed-data opts)`
Sign EIP-712 typed data.

```clojure
(go
  (let [sig (<! (fc/sign-typed-data
                 {:address "0x..."
                  :typed-data {...}}))]
    (println "Signature:" (:signature sig))))
```

### Wallet - Solana

#### `(get-solana-provider)`
Get the Solana provider (synchronous, experimental).

```clojure
(let [provider (fc/get-solana-provider)]
  ;; Use with @solana/web3.js
  )
```

#### `(request-solana-accounts)`
Connect to Solana wallet.

```clojure
(go
  (let [account (<! (fc/request-solana-accounts))]
    (println "Connected:" account)))
```

#### `(sign-solana-message message)`
Sign a message with Solana wallet.

```clojure
(go
  (let [sig (<! (fc/sign-solana-message "Hello Solana!"))]
    (println "Signature:" sig)))
```

## Project Structure

```
farcaster-cljs/
├── src/farcaster_cljs/
│   ├── core.cljs        # Main API (re-exports everything)
│   ├── actions.cljs     # SDK actions (cast, bookmark, etc.)
│   ├── context.cljs     # User info access
│   ├── wallet.cljs      # Ethereum & Solana integration
│   ├── specs.cljs       # Flexible clojure.spec validation
│   └── example.cljs     # Comprehensive usage examples
├── README.md            # This file
├── OVERVIEW.md          # Quick reference
├── shadow-cljs.edn      # Shadow CLJS configuration
└── package.json         # NPM dependencies
```

## Architecture Decisions

### Why core.async?
All async operations return core.async channels rather than JS promises. This provides:
- Composability with other CLJS async code
- Ability to use `go` blocks, `alts!`, timeouts
- More idiomatic ClojureScript patterns

### Why flexible specs?
The specs validate required fields but aren't overly restrictive about JS object shapes. This prevents unnecessary errors from over-specification while still catching common mistakes.

### Why separate namespaces?
The library is split into logical modules (`actions`, `context`, `wallet`) for:
- Clear separation of concerns
- Easy code navigation
- Ability to require only what you need

All functions are re-exported from `core.cljs` for convenience.

## Examples

See [`example.cljs`](./src/farcaster_cljs/example.cljs) for comprehensive examples of every SDK feature, including:

- Initialization & lifecycle
- User context & information
- Cast composition
- Mini app bookmarking
- Profile viewing
- URL opening
- Authentication (SIWE)
- Ethereum wallet operations
- Solana wallet operations
- Complete application flows
- Error handling patterns

## Development

```bash
# Install dependencies
npm install

# Watch for changes (library build)
npm run watch

# Compile library
npm run compile

# Release build
npm run release
```

## Farcaster Mini App Setup

To use this library in a Farcaster mini app, you'll need:

1. A `.well-known/farcaster.json` manifest file
2. Proper splash screen configuration
3. HTTPS hosting
4. Node.js 22.11.0 or higher

See the [Farcaster Mini Apps documentation](https://miniapps.farcaster.xyz/docs/getting-started) for complete setup instructions.

## License

MIT

## Contributing

Issues and PRs welcome! This library aims to provide complete, idiomatic CLJS coverage of the Farcaster Mini App SDK.

## Resources

- [Farcaster Mini Apps Documentation](https://miniapps.farcaster.xyz/)
- [Farcaster Mini App SDK](https://github.com/farcasterxyz/miniapps)
- [@farcaster/miniapp-sdk on npm](https://www.npmjs.com/package/@farcaster/miniapp-sdk)
- [ClojureScript](https://clojurescript.org/)
- [Shadow CLJS](https://shadow-cljs.github.io/docs/UsersGuide.html)
