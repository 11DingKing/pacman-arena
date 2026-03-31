#!/bin/bash

# 吃豆人游戏 - 本地开发环境启动脚本

echo "🎮 启动吃豆人游戏开发环境..."
echo ""

# 启动 MySQL
echo "📦 启动 MySQL 数据库..."
docker-compose -f docker-compose.dev.yml up -d

# 等待 MySQL 就绪
echo "⏳ 等待 MySQL 就绪..."
until docker exec pacman-mysql-dev mysqladmin ping -h localhost -uroot -proot123 --silent 2>/dev/null; do
    sleep 1
done
echo "✅ MySQL 已就绪!"
echo ""

# 显示连接信息
echo "=========================================="
echo "🗄️  MySQL 连接信息:"
echo "   Host: localhost"
echo "   Port: 3306"
echo "   Database: pacman"
echo "   Username: root"
echo "   Password: root123"
echo "=========================================="
echo ""
echo "📝 启动后端 (在 backend 目录):"
echo "   ./mvnw spring-boot:run"
echo ""
echo "📝 启动用户端前端 (在 frontend-user 目录):"
echo "   npm install && npm run dev"
echo ""
echo "📝 启动管理后台前端 (在 frontend-admin 目录):"
echo "   npm install && npm run dev"
echo ""
echo "🌐 访问地址:"
echo "   用户端: http://localhost:5173"
echo "   管理后台: http://localhost:5174"
echo "   后端API: http://localhost:8080"
echo ""
echo "🔑 测试账号: admin / admin123"
echo ""
