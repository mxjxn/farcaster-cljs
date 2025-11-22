# Publishing Guide

## Option 1: Local Install (Quickest - For Testing)

This installs the library locally so you can use it in your fc-idlegame without publishing.

### Steps:

1. **Install Clojure CLI tools** (if you don't have them):
   ```bash
   # macOS
   brew install clojure/tools/clojure

   # Linux
   curl -O https://download.clojure.org/install/linux-install-1.11.1.1429.sh
   chmod +x linux-install-1.11.1.1429.sh
   sudo ./linux-install-1.11.1.1429.sh
   ```

2. **Build and install locally**:
   ```bash
   # In the farcaster-cljs directory
   clj -T:build jar
   clj -X:install
   ```

3. **Use in your fc-idlegame** - Add to your `shadow-cljs.edn` or `deps.edn`:
   ```clojure
   :dependencies [[io.github.mxjxn/farcaster-cljs "0.1.0"]]
   ```

## Option 2: Use as Local Path Dependency

Even simpler - no building needed!

In your **fc-idlegame's deps.edn**, add:

```clojure
{:deps {io.github.mxjxn/farcaster-cljs {:local/root "../farcaster-cljs"}}}
```

Just make sure the path points to where this repo is cloned.

## Option 3: Publish to Clojars (Public Release)

This makes it available to everyone, just like any other CLJS library.

### One-time Setup:

1. **Create Clojars account**: https://clojars.org/register

2. **Create a deploy token**:
   - Log in to Clojars
   - Go to https://clojars.org/tokens
   - Create a new token
   - Save it somewhere safe

3. **Set environment variables**:
   ```bash
   export CLOJARS_USERNAME=your-username
   export CLOJARS_PASSWORD=your-deploy-token
   ```

### Publish:

1. **Build the jar**:
   ```bash
   clj -T:build jar
   ```

2. **Deploy to Clojars**:
   ```bash
   clj -X:deploy
   ```

3. **Use anywhere** - After publishing, anyone can use it:
   ```clojure
   :dependencies [[io.github.mxjxn/farcaster-cljs "0.1.0"]]
   ```

## Option 4: Git Dependency (Backup Option)

If the above don't work, you can use Git directly:

In your **fc-idlegame's deps.edn**:

```clojure
{:deps {io.github.mxjxn/farcaster-cljs
        {:git/url "https://github.com/mxjxn/farcaster-cljs"
         :git/sha "4b89802"}}}  ;; Use the actual commit SHA
```

**Note**: Shadow CLJS sometimes has issues with git deps, so local/root or Clojars are more reliable.

## Updating Version

When you make changes:

1. Update version in `pom.xml` and `deps.edn`
2. Update version in `package.json`
3. Rebuild and republish

## Troubleshooting

### "Could not find artifact"
- Make sure you ran `:install` first for local use
- Check that the path is correct for `:local/root`
- Verify the group-id/artifact-id matches

### "No such var"
- Make sure your fc-idlegame's `shadow-cljs.edn` has the dependency
- Try running `shadow-cljs clean-modules` and rebuilding

### npm dependency not found
- Remember to run `npm install @farcaster/miniapp-sdk` in your game project
- Shadow CLJS needs both the CLJS lib AND the npm package
