import http from 'k6/http';
import { check, sleep } from 'k6';

// k6 load test configuration (Guide Section 7 & 17)
export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],    // Error rate must be less than 1%
  },
};

const BASE_URL = __ENV.APP_URL || 'http://localhost:8080';

export default function () {
  // 1. Health check probe
  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  check(healthRes, {
    'health check status is 200': (r) => r.status === 200,
  });

  // 2. Fetch tasks list
  const tasksRes = http.get(`${BASE_URL}/api/tasks`);
  check(tasksRes, {
    'tasks query status is 200': (r) => r.status === 200,
    'tasks response is json': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });

  sleep(1);
}
