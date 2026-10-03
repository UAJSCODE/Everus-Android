# Release Signing Setup

To configure release signing for the Everus Android app, follow these steps:

## 1. Generate a Keystore (if you don't have one)

```bash
keytool -genkey -v -keystore everus-release-key.jks -alias everus_release_key -keyalg RSA -keysize 2048 -validity 10000
```

**Important:** Store this keystore file in a secure location **outside** of this project directory and version control.

## 2. Configure Signing Credentials

You can provide the signing credentials via either:

### Option A: Gradle Properties (recommended for CI/CD)
Create a file `~/.gradle/everus-signing.properties` (or any location outside the project) with:

```
RELEASE_KEYSTORE_FILE=/path/to/your/everus-release-key.jks
RELEASE_KEYSTORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=everus_release_key
RELEASE_KEY_PASSWORD=your_key_password
```

Then add to your `gradle.properties` file in the project:
```
# Do NOT commit this file to version control!
signingPropsFile=~/.gradle/everus-signing.properties
```

And in `app/build.gradle.kts`, the signing configuration will automatically load these properties.

### Option B: Environment Variables
Set these environment variables before building:
```bash
export RELEASE_KEYSTORE_FILE="/path/to/your/everus-release-key.jks"
export RELEASE_KEYSTORE_PASSWORD="your_keystore_password"
export RELEASE_KEY_ALIAS="everus_release_key"
export RELEASE_KEY_PASSWORD="your_key_password"
```

## 3. Build the Release APK/AAB

```bash
./gradlew clean
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

## 4. Verify the Generated Artifacts

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

The signed AAB will be at:
```
app/build/outputs/bundle/release/app-release.aab
```

## Security Notes

- **Never** commit the keystore file (`*.jks`, `*.keystore`) to version control
- **Never** commit passwords or sensitive signing information
- The `local.properties` and `gradle.properties` files are already in `.gitignore`
- Consider using a secrets management service or CI/CD secret stores for production environments