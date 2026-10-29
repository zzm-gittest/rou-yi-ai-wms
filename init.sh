#!/bin/bash
# RuoYi-AI-WMS 项目初始化脚本
# ============================================================================
# 用途: 快速启动开发环境，供 Claude AI 代理在新会话开始时使用
# 参考: https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents
# ============================================================================

cd /cygdrive/e/idea-work/RuoYi-AI-WMS

echo "=========================================="
echo "  RuoYi-AI-WMS 开发环境初始化"
echo "=========================================="

# 检查后端服务
echo "检查后端服务 (8080)..."
if netstat -an | grep -q ":8080.*LISTENING"; then
    echo "  ✓ 后端服务已运行"
else
    echo "  ○ 后端服务未运行"
fi

# 检查前端服务
echo "检查前端服务 (8081)..."
if netstat -an | grep -q ":8081.*LISTENING"; then
    echo "  ✓ 前端服务已运行"
else
    echo "  ○ 前端服务未运行"
fi

# 检查数据库
echo "检查数据库连接..."
if mysql -h localhost -u root -p123456 -e "SELECT 1" ry-vue &>/dev/null; then
    echo "  ✓ 数据库连接正常"
else
    echo "  ○ 数据库无法连接"
fi

echo ""
echo "=========================================="
echo "访问地址:"
echo "  前端: http://localhost:8081"
echo "  后端: http://localhost:8080"
echo ""
echo "下一步:"
echo "  1. cat claude-progress.txt 了解项目进度"
echo "  2. cat docs/07-会话管理/feature-list.json 查看功能状态"
echo "  3. git log --oneline -10 查看最近提交"
echo "=========================================="
