# API Key Security Setup

## How API Keys Are Secured in This Project

This project follows Android best practices to keep API keys secure and out of version control.

### Setup

1. **API keys are stored in `local.properties`** - This file is automatically ignored by git
2. **Keys are injected at build time** - Gradle reads from local.properties and adds to BuildConfig
3. **Code uses BuildConfig** - Java code accesses keys via `BuildConfig.GROQ_API_KEY`

### File Structure

```
local.properties          ← API keys stored here (gitignored)
    ↓
build.gradle.kts         ← Reads keys and injects to BuildConfig
    ↓
BuildConfig.java         ← Auto-generated at build time
    ↓
AIChatActivity.java      ← Accesses via BuildConfig.GROQ_API_KEY
```

### For New Team Members

If you clone this repo, you need to add your own API key:

1. Open `local.properties` in project root
2. Add this line:
   ```
   GROQ_API_KEY=your_actual_key_here
   ```
3. Get your free key from https://console.groq.com
4. Rebuild the project

### Security Checks

✅ `local.properties` is in `.gitignore`
✅ No API keys hardcoded in source code
✅ BuildConfig is auto-generated (not committed)
✅ Keys only exist in local environment

### NEVER Do This

❌ Hardcode keys in source files
❌ Commit `local.properties` to git
❌ Share keys in chat/email
❌ Push keys to GitHub/GitLab

### For Production

For production releases, consider:
- Using Android Keystore for additional encryption
- Environment-specific keys (dev, staging, prod)
- Backend API proxy to hide keys entirely
- Key rotation policies
