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

## Algoritmer

### Stasjonsvalg for et stedssoek

Naar brukeren soeker paa et sted (f.eks. "Stavanger") henter vi en liste
med naerliggende vaerstasjoner sortert paa avstand. `StationPicker`
velger sa hvilke stasjoner som faktisk skal brukes:

1. Foerst leter vi etter en enkelt stasjon som dekker alle maalinger
   (temperatur, nedboer, vind, snoedybde) og har minst 50 aars historikk.
   For Oslo treffer Blindern her, og vi slipper aa blande data.
2. Hvis ingen enkelt stasjon er god nok, splitter vi per element og
   plukker stasjonen med lengst historikk for hvert maaleelement. Sola
   havner f.eks. paa temperatur (1935-) mens vinden hentes fra en annen
   stasjon for Stavanger.

Resultatet er en liten liste med `StationAssignment`, der hver stasjon
er tagget med akkurat de elementene den skal levere. Det holder
nettverkstrafikken nede.

### Dager med kraftig nedboer

Aarsstatistikken inkluderer "dager med >= 20 mm nedboer", omtrent
det nivaaet som overbelaster urban drenering. Vi henter daglige
sumverdier fra Frost for nedboerstasjonen og teller dagene per aar.
Tallene varierer kraftig mellom regioner (Bergen ~40 dager/aar,
Karasjok ~1), saa det fungerer som en konkret risikoindikator for
huseiere.

## Bygg og kjor

```bash
./gradlew assembleDebug
```

Krever:
- **Mapbox access token** i `app/src/main/res/values/mapbox_access_token.xml`
- **Frost client ID** i `app/src/main/res/values/frost_client_id.xml`
  - Registrer deg paa https://frost.met.no/auth/requestCredentials.html

## Viktig: Emulator-oppsett for tastatur

For at søkefeltet og tekstinntasting skal fungere korrekt i emulatoren,
kan det være nødvendig å endre følgende innstillinger på emulatoren:
- Gå til `Setting` -> `System` -> `Keyboard` -> `On-screen keyboard` -> `Gboard`   
- Klikk på `Physical keyboard` og skru PÅ bryteren for `Show on-screen keyboard`
- Gå ett steg tilbake (til Gboards innstillinger)
- Klikk på `Write in text fields` og skru AV for `Use stylus to write in text fields`