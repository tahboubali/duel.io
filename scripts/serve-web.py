#!/usr/bin/env python3
from __future__ import annotations

import argparse
import asyncio
import json
from pathlib import Path

try:
    from aiohttp import ClientSession, WSMsgType, web
except ImportError as exc:
    raise SystemExit(
        "aiohttp is required for the web proxy server. "
        "Create a virtual environment and install it before running this script."
    ) from exc


ROOT = Path(__file__).resolve().parents[1] / "web"
BACKEND_WS = "ws://127.0.0.1:8080/connect"


def summarize_message(payload: str) -> str:
    try:
        data = json.loads(payload)
    except json.JSONDecodeError:
        return payload[:180]

    request_type = data.get("request_type")
    if request_type:
        return str(request_type)
    return str(data)[:180]


async def index(request: web.Request) -> web.FileResponse:
    return web.FileResponse(ROOT / "index.html")


async def status_report(request: web.Request) -> web.Response:
    try:
        data = await request.json()
    except json.JSONDecodeError:
        data = {"raw": await request.text()}
    client = data.get("client")
    prefix = f"[browser-status][{client}]" if client else "[browser-status]"
    print(prefix, data.get("message", data), flush=True)
    return web.Response(status=204)


async def websocket_proxy(request: web.Request) -> web.WebSocketResponse:
    client = request.query.get("client")
    prefix = f"[proxy][{client}]" if client else "[proxy]"
    browser_ws = web.WebSocketResponse()
    await browser_ws.prepare(request)
    print(f"{prefix} browser connected", flush=True)

    async with ClientSession() as session:
        async with session.ws_connect(BACKEND_WS) as backend_ws:
            print(f"{prefix} backend connected", flush=True)

            async def browser_to_backend() -> None:
                async for msg in browser_ws:
                    if msg.type == WSMsgType.TEXT:
                        print(f"{prefix} browser -> backend: {summarize_message(msg.data)}", flush=True)
                        await backend_ws.send_str(msg.data)
                    elif msg.type == WSMsgType.BINARY:
                        await backend_ws.send_bytes(msg.data)
                    elif msg.type == WSMsgType.CLOSE:
                        await backend_ws.close()

            async def backend_to_browser() -> None:
                async for msg in backend_ws:
                    if msg.type == WSMsgType.TEXT:
                        print(f"{prefix} backend -> browser: {summarize_message(msg.data)}", flush=True)
                        await browser_ws.send_str(msg.data)
                    elif msg.type == WSMsgType.BINARY:
                        await browser_ws.send_bytes(msg.data)
                    elif msg.type == WSMsgType.CLOSE:
                        await browser_ws.close()

            try:
                await asyncio.gather(browser_to_backend(), backend_to_browser())
            finally:
                if not backend_ws.closed:
                    await backend_ws.close()
                if not browser_ws.closed:
                    await browser_ws.close()
                print(f"{prefix} sockets closed", flush=True)

    return browser_ws


@web.middleware
async def request_logger(request: web.Request, handler):
    response = await handler(request)
    print(f"[web] {request.method} {request.path} -> {response.status}", flush=True)
    return response


def create_app() -> web.Application:
    app = web.Application(middlewares=[request_logger])
    app.router.add_get("/", index)
    app.router.add_post("/__status", status_report)
    app.router.add_get("/connect", websocket_proxy)
    app.router.add_static("/", ROOT, show_index=True)
    return app


def main() -> None:
    parser = argparse.ArgumentParser(description="Serve the duel.io web build locally.")
    parser.add_argument("--port", type=int, default=4173)
    args = parser.parse_args()

    print(f"Serving {ROOT} at http://127.0.0.1:{args.port}", flush=True)
    web.run_app(create_app(), host="127.0.0.1", port=args.port, print=None)


if __name__ == "__main__":
    main()
