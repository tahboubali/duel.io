# duel.io Web Embed

This folder contains a browser-hosted version of the Java 8 client.

## Build the web bundle

```bash
./scripts/build-web-bundle.sh
```

That produces:

- `web/app/duel.io.jar`
- dependency jars under `web/app/lib/`

## Run locally

```bash
python3 -m venv target/web-venv
source target/web-venv/bin/activate
python3 -m pip install aiohttp
python3 scripts/serve-web.py --port 4173
```

Then open:

- `http://127.0.0.1:4173/panel.html` for the game panel itself
- `http://127.0.0.1:4173/` for the iframe-style embed demo

## Notes

- The panel uses CheerpJ to run the Java client directly in-browser.
- The local web host proxies `/connect` to the Go WebSocket server at `ws://127.0.0.1:8080/connect`.
- `panel.html` is the page you can embed in another site with an iframe.
