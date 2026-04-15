# Team 45 -- Naturhendelser

Android-app for Case 3 (Naturhendelser) i IN2000 ved UiO.
Appen lar huseiere og hytteeiere sjekke klimarisiko (flom, skred) og klimastatistikk for sitt omrade.

## Funksjonalitet

Appen har to hovedskjermer tilgjengelig via en bunnnavigasjonsbar:

### Kart (venstre knapp)
- Mapbox-kart over Norge
- NVE flomsone-overlay som viser flomsonekart oppaa kartet
- Overlayet oppdateres dynamisk nar brukeren flytter kartet

### Klimastatistikk (hoyre knapp)
- Sok etter vaerstasjon etter navn
- Viser klimanormaler (30-ars gjennomsnitt) for temperatur, nedbor og vind
- Faller tilbake til historiske observasjoner (2010-2024) om normaler ikke finnes

## Teknisk stack

- Kotlin + Jetpack Compose + Material 3
- Mapbox SDK for kart
- Ktor (CIO) + kotlinx.serialization for nettverkskall
- MVVM-arkitektur med datasource/repository/viewmodel-lag

## APIer

- **MET Frost API v0** - Klimadata (stasjoner, normaler, observasjoner)
- **NVE flomsone-tjeneste** - Flomsonekart som overlay

## Bygg og kjor

```bash
./gradlew assembleDebug
```

Krever:
- **Mapbox access token** i `app/src/main/res/values/mapbox_access_token.xml`
- **Frost client ID** i `app/src/main/res/values/frost_client_id.xml`
  - Registrer deg paa https://frost.met.no/auth/requestCredentials.html
