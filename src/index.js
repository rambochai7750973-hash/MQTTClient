import { connect } from 'cloudflare:sockets';

const DEFAULT_CONFIG = {
  host: 'broker.emqx.io',
  port: 1883,
  tls: false,
  authToken: null,
};

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
};

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data, null, 2), {
    status,
    headers: { ...CORS_HEADERS, 'Content-Type': 'application/json' },
  });
}

function handleOptions(request) {
  if (request.method === 'OPTIONS') {
    return new Response(null, { status: 204, headers: CORS_HEADERS });
  }
  return null;
}

function getBrokerConfig(env, url) {
  const host = url.searchParams.get('host') || env.MQTT_BROKER_HOST || DEFAULT_CONFIG.host;
  const port = parseInt(url.searchParams.get('port') || env.MQTT_BROKER_PORT || DEFAULT_CONFIG.port, 10);
  const tls = (url.searchParams.get('tls') || env.MQTT_USE_TLS || (port === 8883 ? 'true' : 'false')) === 'true';
  return { host, port, tls };
}

function checkAuth(request, env, url) {
  const token = env.MQTT_AUTH_TOKEN;
  if (!token) return true;
  const auth = request.headers.get('Authorization');
  if (auth === `Bearer ${token}`) return true;
  const queryToken = url?.searchParams?.get('token');
  if (queryToken === token) return true;
  return false;
}

async function handleMqttWebSocket(request, env) {
  const corsCheck = handleOptions(request);
  if (corsCheck) return corsCheck;

  const url = new URL(request.url);

  if (!checkAuth(request, env, url)) {
    return jsonResponse({ error: 'Unauthorized' }, 401);
  }

  const upgrade = request.headers.get('Upgrade');
  if (!upgrade || upgrade.toLowerCase() !== 'websocket') {
    return jsonResponse({ error: 'This endpoint requires a WebSocket upgrade' }, 426);
  }
  const config = getBrokerConfig(env, url);
  const [client, server] = Object.values(new WebSocketPair());

  server.accept();

  const address = config.tls ? `${config.host}:${config.port}` : `${config.host}:${config.port}`;

  let tcpSocket;
  let tcpReader;
  let tcpWriter;
  let closed = false;

  async function cleanup() {
    if (closed) return;
    closed = true;
    try { server.close(); } catch (_) {}
    try { tcpWriter?.close(); } catch (_) {}
    try { tcpReader?.cancel(); } catch (_) {}
    try { tcpSocket?.close(); } catch (_) {}
  }

  try {
    tcpSocket = connect(address, { tls: config.tls ? {} : undefined });
    tcpReader = tcpSocket.readable.getReader();
    tcpWriter = tcpSocket.writable.getWriter();
  } catch (err) {
    server.close(1011, `Broker connection failed: ${err.message}`);
    return new Response(null, { status: 101, webSocket: client });
  }

  pumpTCPToWebSocket(tcpReader, server).catch((err) => {
    console.error('TCP->WS error:', err.message);
    cleanup();
  });

  server.addEventListener('message', async (event) => {
    if (closed) return;
    try {
      const data = event.data instanceof ArrayBuffer
        ? event.data
        : new TextEncoder().encode(event.data).buffer;
      await tcpWriter.write(new Uint8Array(data));
    } catch (err) {
      console.error('WS->TCP error:', err.message);
      cleanup();
    }
  });

  server.addEventListener('close', cleanup);
  server.addEventListener('error', cleanup);

  return new Response(null, { status: 101, webSocket: client });
}

async function pumpTCPToWebSocket(reader, ws) {
  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      ws.send(value.buffer.slice(value.byteOffset, value.byteOffset + value.byteLength));
    }
  } finally {
    try { ws.close(); } catch (_) {}
  }
}

export default {
  async fetch(request, env) {
    const corsCheck = handleOptions(request);
    if (corsCheck) return corsCheck;

    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, '') || '/';

    switch (path) {
      case '/health': {
        return jsonResponse({
          status: 'healthy',
          uptime: Date.now(),
          version: '1.0.0',
        });
      }

      case '/mqtt': {
        return handleMqttWebSocket(request, env);
      }

      case '/': {
        return jsonResponse({
          name: 'MQTT WebSocket Bridge',
          version: '1.0.0',
          description: 'Bridges MQTT over WebSocket connections to a TCP MQTT broker',
          docs: 'https://github.com/your-org/mqtt-websocket-bridge',
          endpoints: {
            'GET /': 'API info',
            'GET /health': 'Health check',
            'WS /mqtt': 'MQTT WebSocket endpoint (query: ?host=&port=&tls=)',
          },
          env_config: {
            MQTT_BROKER_HOST: 'Default MQTT broker hostname',
            MQTT_BROKER_PORT: 'Default MQTT broker port',
            MQTT_USE_TLS: 'Enable TLS (true/false)',
            MQTT_AUTH_TOKEN: 'Bearer token for client auth (optional)',
          },
        });
      }

      default:
        return jsonResponse({ error: 'Not Found', path }, 404);
    }
  },
};
