# Roboroads Morphe Patches

My collection of [Morphe](https://github.com/MorpheApp) patches.

## 📲 Add this patch source to Morphe

Tap the button on your device, or add this URL as a patch source in Morphe Manager:
`https://github.com/Roboroads/morphe-patches`

<a href="https://morphe.software/add-source?github=Roboroads/morphe-patches">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Generated on release by release.yml. Do not edit by hand. -->

| App | Package | Patches |
|---|---|---|
| discovery+ (Fire TV) | `com.discovery.discoveryplus.firetv` | Remove ads |

<!-- PATCHES_END -->

## 🛠️ Development

- Work on the `dev` branch, merge to `main` for a stable release.
- Build locally with `./gradlew buildAndroid`; the bundle lands in `patches/build/libs/patches-*.mpp`.
- Apply it with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) like any other patch bundle.
- Use semantic commits (`feat:`, `fix:`, `chore:`); `release.yml` builds and publishes releases.
- Do not edit generated files by hand: `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`.

The build pulls the Morphe Gradle plugin from GitHub Packages, so it needs a GitHub token with
`read:packages` (`gpr.user`/`gpr.key` in `~/.gradle/gradle.properties`, or `GITHUB_ACTOR`/`GITHUB_TOKEN`).

## 📜 License

Roboroads Patches are licensed under the [GNU General Public License v3.0](LICENSE).
