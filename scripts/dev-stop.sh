#!/bin/bash

# 停止开发环境

echo "🛑 停止开发环境..."
docker-compose -f docker-compose.dev.yml down
echo "✅ 已停止!"
