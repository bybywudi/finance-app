# HSBC面试


```bash
# 构建镜像
docker build -t finance-service .

# 运行容器
docker run -d -p 8080:8080 --name finance-app finance-service

# 验证运行状态
docker logs finance-app

# 创建交易
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 150.50, "type": "INCOME", "description": "工资"}'

# 查询交易
curl http://localhost:8080/transactions

# 部署到集群
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml