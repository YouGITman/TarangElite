# TARANG ELITE — Native Android

Your personal training system, rebuilt as a native Android app in Kotlin + Jetpack Compose. Built for the Pixel 10 Pro XL, with automatic Garmin sync via Health Connect, a daily Recovery Score, mental health check-ins, and a 4-7-8 breathing coach.

---

## 1. Get the APK built (one-time setup, ~10 minutes)

The repo ships with a GitHub Actions pipeline that builds and signs a release APK automatically on every push.

1. Create a **private** repository on GitHub called `TarangElite`.
   ⚠️ It must be private — the signing keystore is committed for convenience.
2. Push this folder:
   ```bash
   cd TarangElite
   git init
   git add .
   git commit -m "TARANG ELITE v3.0.0"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/TarangElite.git
   git push -u origin main
   ```
3. Open the repo's **Actions** tab. The `Build APK` workflow starts automatically. The first run takes 5–10 minutes (Gradle downloads everything); later runs are faster thanks to caching.
4. When it finishes, go to **Releases** → the rolling **latest** release → download **`TarangElite.apk`**.

## 2. Install on your Pixel

1. Open the downloaded `TarangElite.apk` on the phone.
2. If prompted, allow your browser/Files app to install unknown apps (Settings → Apps → Special app access → Install unknown apps).
3. Install. Every future release replaces the app in place — your data is kept (it lives in a local Room database on the device).

## 3. Connect Garmin (one-time, ~2 minutes)

Garmin syncs to the app **automatically** via Android Health Connect:

1. On the phone, open **Garmin Connect** → More → Settings → **Health Connect** → turn it on and grant all categories (steps, heart rate, sleep, exercise, calories).
2. Open **TARANG ELITE** → Today tab → **Connect Garmin data** → grant the requested permissions.
3. Done. The app syncs when you open it and every ~6 hours in the background.

What syncs: steps, resting & continuous heart rate, HRV, sleep (with deep/REM stages), workouts, active calories.
What can't sync (Garmin keeps these proprietary): Body Battery, Garmin stress score, training load. The app computes its **own Recovery Score** instead, blending your sleep, resting HR vs baseline, HRV, and your morning check-in.

**Tip:** exempt both *Garmin Connect* and *Health Connect* from battery optimisation (Settings → Apps → … → Battery → Unrestricted) so background sync never stalls.

## 4. Daily flow

- **Morning:** open the app → 10-second check-in (mood, energy, stress) → your Recovery Score is revealed with advice for the day.
- **Train:** start the day's session, tick exercises off (with easier alternatives one tap away), and get a confetti celebration on completion.
- **Fuel:** workout-day vs rest-day targets and meals, plus the honesty log for off-plan food.
- **Mind:** 4-7-8 breathing orb with haptic pacing, guided meditations, power-nap protocol.
- **Progress:** body composition trends, mood strip, coach's assessment, full Garmin activity history.

## 5. Shipping updates

1. Make changes.
2. Bump `versionCode` (+1) and `versionName` in `app/build.gradle.kts`.
3. Commit and push — GitHub builds, signs and republishes `TarangElite.apk` on the **latest** release.
4. Download and install over the top.

## 6. Project map

```
app/src/main/java/com/tarang/elite/
├── data/
│   ├── content/      Workouts, nutrition, mind content (your full programme)
│   ├── db/           Room database (check-ins, logs, metrics, synced health)
│   ├── health/       Health Connect reader + background sync worker
│   └── repo/         Repositories
├── domain/           Recovery Score + streak logic
├── di/               Manual dependency container
└── ui/
    ├── today/        Recovery card, check-in sheet, Garmin, water, streak
    ├── train/        Weekly schedule + exercise library
    ├── workout/      Live session screen with confetti
    ├── fuel/         Macros, meals, food guide, honesty log
    ├── mind/         Meditations, breathing screen, quotes, stories
    ├── progress/     Body comp, sparkline, mood strip, coach assessment
    ├── navigation/   5-tab bottom navigation
    └── theme/        Dark amber/slate Material 3 theme
```

**Notes**
- Dynamic colour (Material You) is off by default for the branded look. Flip `USE_DYNAMIC_COLOUR` in `ui/theme/Theme.kt` to follow your wallpaper instead.
- Keystore: `keystore/release.jks`, alias `tarang`, password `forgedElite2026`. Keep the repo private; back the file up — losing it means future builds can't update over the installed app.
- Min Android 8.0 (API 26), target Android 15 (API 35).
