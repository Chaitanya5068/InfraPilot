const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.ADMIN_PORT || 8085;
const HOST = process.env.ADMIN_HOST || '0.0.0.0';

const MIME_TYPES = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
  // Mock Health & Status Endpoint
  if (req.url === '/api/v1/admin/health') {
    res.writeHead(200, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
    res.end(JSON.stringify({
      status: 'HEALTHY',
      service: 'InfraPilot-Admin-Console',
      port: PORT,
      database: 'PostgreSQL-15 (Connected)',
      redis: 'Redis-7 (Connected)',
      timestamp: new Date().toISOString()
    }));
    return;
  }

  const cleanUrl = req.url === '/' ? 'index.html' : req.url.split('?')[0].replace(/^\//, '');
  let filePath = path.join(__dirname, '..', 'admin-portal', cleanUrl);

  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    filePath = path.join(__dirname, '..', 'admin-portal', 'index.html');
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      res.writeHead(500);
      res.end('Admin Console Server Error: ' + err.code);
    } else {
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content, 'utf-8');
    }
  });
});

server.listen(PORT, HOST, () => {
  console.log(`================================================================`);
  console.log(`InfraPilot Admin & Operations Portal running on http://${HOST}:${PORT}/`);
  console.log(`Dedicated Node/IP for User Records, Sessions, and Maintenance`);
  console.log(`================================================================`);
});
