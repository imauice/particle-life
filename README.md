# Particle Life — Bio-Atom (CHNOPS)

Android Live Wallpaper based on the existing Bond Lab project.

## Model

- Six element categories: C, H, O, N, P, S, with approximate atomic masses 12.01, 1.008, 16.00, 14.01, 30.97, 32.06.
- Simplified common covalent valence caps: C=4, H=1, O=2, N=3, P=3, S=2. Real valences and bonding depend on chemical context.
- Visually scaled mass-dependent inertia, short-range repulsion, spring-like bonds, stochastic formation and breakage.
- These rules are **illustrative**, not a validated molecular dynamics simulation. No quantum mechanics, electron orbitals, charge states, reaction pathways or realistic time/energy scales.
- Default 420 atoms, 30 FPS. Tap wallpaper to add atoms.

## Build

Unzip and run Gradle `assembleDebug` using Android SDK 35 and Java 17, or upload this ZIP under the exact filename `ParticleLifeWallpaper_AndroidStudio.zip` to the existing GitHub Actions workflow.

The applicationId remains `com.particlelife.wallpaper` so it can replace the previous app if signed with the same key. GitHub Actions debug signing keys may differ between runs, so Android may require uninstalling the old app before installing the new one.
