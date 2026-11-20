import settingsGradle from '../../settings.gradle.kts?raw';
import rootBuildGradle from '../../build.gradle.kts?raw';
import gradleProperties from '../../gradle.properties?raw';
import appBuildGradle from '../../app/build.gradle.kts?raw';
import androidManifest from '../../app/src/main/AndroidManifest.xml?raw';
import stringsXml from '../../app/src/main/res/values/strings.xml?raw';
import themesXml from '../../app/src/main/res/values/themes.xml?raw';
import booksJson from '../../app/src/main/assets/books.json?raw';
import initialStateJson from '../../app/src/main/assets/initial_state.json?raw';
import mainActivityKt from '../../app/src/main/java/com/ecosdelacoronarota/MainActivity.kt?raw';
import gameModelsKt from '../../app/src/main/java/com/ecosdelacoronarota/data/GameModels.kt?raw';
import catalogoMundoKt from '../../app/src/main/java/com/ecosdelacoronarota/data/CatalogoMundoInicial.kt?raw';
import validadorAccionesKt from '../../app/src/main/java/com/ecosdelacoronarota/data/ValidadorAcciones.kt?raw';
import gameRepositoryKt from '../../app/src/main/java/com/ecosdelacoronarota/data/GameRepository.kt?raw';
import llamaEngineKt from '../../app/src/main/java/com/ecosdelacoronarota/data/LlamaEngine.kt?raw';
import gameViewModelKt from '../../app/src/main/java/com/ecosdelacoronarota/viewmodel/GameViewModel.kt?raw';
import themeKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/theme/Theme.kt?raw';
import commonComponentsKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/components/CommonComponents.kt?raw';
import ecosAppKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/EcosApp.kt?raw';
import estadoScreenKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/screens/EstadoScreen.kt?raw';
import mapaScreenKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/screens/MapaScreen.kt?raw';
import bibliotecaScreenKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/screens/BibliotecaScreen.kt?raw';
import gmPanelScreenKt from '../../app/src/main/java/com/ecosdelacoronarota/ui/screens/GmPanelScreen.kt?raw';

export interface AndroidProjectFile {
  path: string;
  group: 'Kotlin (App)' | 'UI Compose' | 'Assets JSON' | 'Gradle y Manifest';
  content: string;
}

export const ANDROID_PROJECT_FILES: AndroidProjectFile[] = [
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/MainActivity.kt',
    group: 'Kotlin (App)',
    content: mainActivityKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/data/GameModels.kt',
    group: 'Kotlin (App)',
    content: gameModelsKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/data/CatalogoMundoInicial.kt',
    group: 'Kotlin (App)',
    content: catalogoMundoKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/data/ValidadorAcciones.kt',
    group: 'Kotlin (App)',
    content: validadorAccionesKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/data/GameRepository.kt',
    group: 'Kotlin (App)',
    content: gameRepositoryKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/data/LlamaEngine.kt',
    group: 'Kotlin (App)',
    content: llamaEngineKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/viewmodel/GameViewModel.kt',
    group: 'Kotlin (App)',
    content: gameViewModelKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/EcosApp.kt',
    group: 'UI Compose',
    content: ecosAppKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/theme/Theme.kt',
    group: 'UI Compose',
    content: themeKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/components/CommonComponents.kt',
    group: 'UI Compose',
    content: commonComponentsKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/screens/EstadoScreen.kt',
    group: 'UI Compose',
    content: estadoScreenKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/screens/MapaScreen.kt',
    group: 'UI Compose',
    content: mapaScreenKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/screens/BibliotecaScreen.kt',
    group: 'UI Compose',
    content: bibliotecaScreenKt,
  },
  {
    path: 'app/src/main/java/com/ecosdelacoronarota/ui/screens/GmPanelScreen.kt',
    group: 'UI Compose',
    content: gmPanelScreenKt,
  },
  {
    path: 'app/src/main/assets/initial_state.json',
    group: 'Assets JSON',
    content: initialStateJson,
  },
  {
    path: 'app/src/main/assets/books.json',
    group: 'Assets JSON',
    content: booksJson,
  },
  {
    path: 'app/build.gradle.kts',
    group: 'Gradle y Manifest',
    content: appBuildGradle,
  },
  {
    path: 'build.gradle.kts',
    group: 'Gradle y Manifest',
    content: rootBuildGradle,
  },
  {
    path: 'settings.gradle.kts',
    group: 'Gradle y Manifest',
    content: settingsGradle,
  },
  {
    path: 'gradle.properties',
    group: 'Gradle y Manifest',
    content: gradleProperties,
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    group: 'Gradle y Manifest',
    content: androidManifest,
  },
  {
    path: 'app/src/main/res/values/strings.xml',
    group: 'Gradle y Manifest',
    content: stringsXml,
  },
  {
    path: 'app/src/main/res/values/themes.xml',
    group: 'Gradle y Manifest',
    content: themesXml,
  },
];
