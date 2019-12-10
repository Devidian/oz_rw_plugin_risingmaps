## [Unreleased]

## [0.3.0] - 2019-12-10
### Added
- map-tiles can now be send to a rendering service through webSockets
- setting `enableWebSocket` to enable websocket client
- setting `wsURI` websocket server uri
- setting `wsSendTiles` to enable sending map-tiles to websocket server
### Fixed
- `enableWebSocket` setting was ignored when creating WSEndpoint

## [0.2.0] - 2019-05-02
### Fixed
- Exception thrown when typing `/rm` without parameter
### Added
- Code for plugin update-auto-restart added  (`restartOnUpdate` option)
- Short-manual for installation
### changed
- default language of *.md files is now English

## [0.1.0] - 2019-03-18
### Added
- initial plugin, basic features
