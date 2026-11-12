/**
 * Every call goes to the API Gateway (port 8090), never to a microservice directly.
 * During `npm start`, proxy.conf.json forwards /api/** to http://localhost:8090, so the base stays empty.
 * For a production build served elsewhere, set it to the gateway URL, e.g. 'http://localhost:8090'.
 */
export const API_BASE = '';
