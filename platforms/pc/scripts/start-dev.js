#!/usr/bin/env node
'use strict';

const { spawn } = require('child_process');
const path = require('path');

const root = path.join(__dirname, '..');
const command = process.platform === 'darwin'
  ? path.join(root, '.dev-app/Shell++.app/Contents/MacOS/Shell++')
  : path.join(root, 'node_modules/.bin/electron');

const child = spawn(command, ['.'], {
  cwd: root,
  stdio: 'inherit'
});

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code == null ? 0 : code);
});
