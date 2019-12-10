## [Unreleased]

## [0.3.0] - 2019-12-10
### Added
- map-tiles können nun über webSockets zu einem externen rendering server gesendet werden
- Einstellung `enableWebSocket` um den websocket client zu aktivieren
- Einstellung `wsURI` websocket server uri
- Einstellung `wsSendTiles` um die Übertragung von map tiles zu aktivieren
### Fixed
- `enableWebSocket` Einstellung wurde ignoriert beim erstellen des WebSocket Servers

## [0.2.0] - 2019-05-02
### Fixed
- Exception bei Eingabe von `/rm` ohne parameter
### Added
- Code für Plugin update-auto-neustart hinzugefügt (`restartOnUpdate` option)
- Kurzanleitung zur Installation
### Changed
- Alle *.md files sind nun standardmäßig in englisch

## [0.1.0] - 2019-03-18
### Added
- initiales plugin, basis features
