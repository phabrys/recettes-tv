<p align="center">
  <img src="app/src/main/res/drawable-xhdpi/banner.png" alt="Recettes" width="320">
</p>

# Recettes TV

Application Android TV qui affiche en plein écran le recueil de recettes
[recette.d.perivol.net](https://recette.d.perivol.net/), pour suivre une recette
sur la télévision en cuisinant.

## Fonctionnalités

- **Plein écran, pensé pour la télécommande** : la navigation (flèches, OK) est
  gérée par le site ; le bouton Retour remonte l'historique, puis quitte
  l'application depuis l'accueil.
- **Écran toujours allumé** tant que l'application est ouverte.
- **Reprise automatique** : si le serveur ne répond pas, un écran d'erreur
  s'affiche et un nouvel essai a lieu toutes les 10 secondes (ou à la demande
  avec le bouton « Réessayer »).
- **Contenu à jour** : après plus de 30 minutes en arrière-plan, le site est
  rechargé au retour dans l'application.
- **Navigation restreinte** au site des recettes : les liens externes sont ignorés.
- **Lien `recettes://`** pour lancer l'application depuis une autre application,
  par exemple une télécommande Home Assistant.

## Installation

1. Télécharger `Recettes.apk` depuis la
   [dernière release](https://github.com/phabrys/recettes-tv/releases/latest).
2. L'installer sur l'appareil Android TV (autoriser l'installation depuis des
   sources inconnues), par exemple avec :

   ```sh
   adb install Recettes.apk
   ```

L'application apparaît ensuite dans le lanceur Android TV sous le nom « Recettes ».
Elle fonctionne aussi sur un téléphone ou une tablette (Android 7.0 minimum).

## Compilation

Prérequis : JDK 17 ou plus récent et le SDK Android (API 36), dont le chemin est
indiqué dans `local.properties` :

```properties
sdk.dir=/chemin/vers/Android/Sdk
```

Puis :

```sh
./gradlew assembleDebug
```

L'APK est produit dans `app/build/outputs/apk/debug/`.

## Structure

| Chemin | Contenu |
| --- | --- |
| `app/src/main/java/net/perivol/recettes/MainActivity.java` | L'activité unique : WebView, gestion du bouton Retour, écran d'erreur, rechargement |
| `app/src/main/AndroidManifest.xml` | Déclaration pour le lanceur Android TV et le lien `recettes://` |
| `app/src/main/res/` | Icône et bannière du lanceur |
| `art/` | Sources HTML de l'icône et de la bannière |

L'adresse du site affiché se change dans la constante `HOME_URL` de `MainActivity`.
