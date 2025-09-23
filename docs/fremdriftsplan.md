# Smart-TV — fremdriftsplan (Uke 37–44)

## Uke 37 — Oppstart & struktur
- [x] Opprett repo `smart-tv` (public) og legg til `.gitignore` for Java/Maven  
- [x] Sett korrekt basepakke: `edu.ntnu.sveiap.idata2304.smarttv`  
- [x] Legg inn multimodul-struktur: `common/`, `tv-server/`, `remote-client/`, `it-tests/`  
- [x] Parent POM + modul-POMer kompilerer (Java 21, JUnit 5)  
- [x] **Docs:** opprett `docs/architecture.md` og `docs/protocol.md` (malene er på plass)  
- [x] Placeholder `main`-klasser (server/klient) kjører via Maven/VS Code  
- [x] `mvn validate` og “Reload Maven Projects” i VS Code uten feil  
- [X] Første commit + tag `v0.0.1-init`

## Uke 38 — Del 1: Enkel Smart-TV + klient
- [x] Spesifiser **protokoll v1** i `docs/protocol.md` (kommandoer, svar, feilkoder, eksempler)  
- [x] `common/entity`: `TvState`, ev. `Channel`   
- [x] `common/logic`: `SmartTv` med `turnOn/turnOff/getNumberOfChannels/getChannel/setChannel`  
- [x] `common/protocol`: `Command`, `Message`, `Codec` (linjebasert UTF-8)  
- [ ] `tv-server/transport`: enkel TCP-server (lytt på port 1238, håndter én klient)  
- [X] `tv-server/adapter`: map kommandoer → `SmartTv` og bygg svar  
- [ ] `remote-client/transport`: enkel TCP-klient (koble til host:port)  
- [ ] `remote-client/ui`: minimal CLI (send kommando, vis svar)  
- [ ] Manuell test: ON/OFF/STATUS/CHANNELS/GET/SET fungerer ende-til-ende  
- [ ] Commit + tag `v0.1.0-simple`

## Uke 39 — Del 2: Refaktorering & enhetstester
- [ ] Sjekk lagdeling: `logic` uavhengig av `protocol`/`transport`  
- [ ] Gjør meldinger/kommandoer lett å endre (sentraliser i `protocol`)  
- [ ] **Tester (common/logic):** minst 3 (TV OFF default, valid/invalid channel, state rules)  
- [ ] **Tester (protocol/codec):** parsing av gyldige/ugyldige meldinger, feilkoder  
- [ ] **Tester (server/adapter):** kommando → stateendring (uten ekte sockets hvis mulig)  
- [ ] Oppdater `architecture.md` med klassediagram (enkelt)  
- [ ] Commit + tag `v0.2.0-refactor-tests`

## Uke 40 — Robusthet & feilhandtering
- [ ] Normaliser feilkoder (`400/401/404/409/500`) i server-svar  
- [ ] Trim/ignorer ekstra whitespace, håndter tomme linjer  
- [ ] Input-validering og grenser (maks linjelengde, kanalgrenser)  
- [ ] Logging: INFO/WARN/ERROR på server  
- [ ] Oppdater protokoll-eksempler i `docs/protocol.md`  
- [ ] Commit + tag `v0.3.0-robustness`

## Uke 41 — Klientopplevelse & kvalitet
- [ ] Forbedre CLI (hjelpetekst, statuslinje for aktiv kanal)  
- [ ] “Røyktest”-manus i README (copy-paste-kommandoer for rask verifikasjon)  
- [ ] Små UX-detaljer (f.eks. kommando-aliaser: `on/off/up/down`)  
- [ ] Commit + tag `v0.4.0-client-polish`

## Uke 42 — Forberedelser til multiklient
- [ ] Design **Broadcaster**-grensesnitt (push av `EVT CHANNEL <n>`)  
- [ ] Definer `SUB/UNSUB` og `PING` i protokollen (oppdater `docs/protocol.md`)  
- [ ] Klienten: støtt (men ignorer) ukjente `EVT` uten å knekke  
- [ ] Commit + tag `v0.5.0-event-design`

## Uke 43 — Implementer asynkrone events
- [ ] Server: implementer Broadcaster (trådsikker liste over abonnenter)  
- [ ] Server: send `EVT CHANNEL <n>` ved kanalendring til alle abonnenter  
- [ ] Klient: legg til lytter for asynkrone linjer parallelt med sync-kommandoer  
- [ ] Manuell test: to klienter; sett kanal i A → B får `EVT CHANNEL` umiddelbart  
- [ ] Commit + tag `v0.6.0-async-events`

## Uke 44 — Del 3: Multiklient & levering (frist fredag)
- [ ] Server: **multi-threading** (én klient pr. tråd / thread-pool)  
- [ ] Trådsikring i `SmartTv` for muterende operasjoner (kritiske seksjoner)  
- [ ] Tåle “busy” klient uten å blokkere andre (socket-timeouts, non-blocking IO eller separate tråder)  
- [ ] Integrasjonstester (`it-tests`): flere klienter, samtidig SET, event-distribusjon  
- [ ] Endelig gjennomgang av `docs/architecture.md` og `docs/protocol.md`  
- [ ] README: kjøre-instruksjoner (Maven/VS Code), kjent feil, versjonslogg  
- [ ] Leveranse: zip eller repo-lenke (sjekk at alt er offentlig/tilgjengelig)  
- [ ] Tag `v1.0.0` og push

## Ekstra (valgfritt)
- [ ] UDP-variant av transportlaget (uendret `logic`/`protocol`)  
- [ ] GUI for klient (JavaFX)  
- [ ] Keep-alive/idle-timeout på server
