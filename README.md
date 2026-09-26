# Band Lock Toggle

Small root-only Android app for the POCO X5 Pro setup discussed in chat.

## What it does
- B3 (0x4)
- B7 (0x40)
- B28 (0x8000000)
- B3+B7 (0x44)
- B3+B28 (0x8000004)
- ALL bands (`unlock`)
- Quick Settings tile: one tap toggles **B3 <-> ALL**

## Root
Yes. The app runs the existing `qmi_tool` through `su`, so KSU Next/Magisk must grant the app root access.

## qmi_tool path
By default:
`/data/data/com.termux/files/home/bandlock-pro/native/qmi_tool`

The app does not include a second copy of the modem tool.

## Build with GitHub Actions
1. Create a new GitHub repository.
2. Upload the contents of this `BandLockToggle` folder (not the outer folder itself).
3. Push to GitHub.
4. Open **Actions** → **Build APK**.
5. Wait for the workflow to finish.
6. Open the completed run and download the artifact **BandLockToggle-debug**.
7. The artifact contains `app-debug.apk`.

You can also start it manually from **Actions → Build APK → Run workflow**.

## Install/use
Install the APK on the POCO X5 Pro, grant root in KSU Next, verify the qmi_tool path, and add **Bands** to Quick Settings. The tile switches between B3 and ALL.
