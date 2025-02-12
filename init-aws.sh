#!/bin/bash

echo "⏳ Очікуємо запуск LocalStack..."
MAX_TRIES=30
TRIES=0

# Перевірка здоров'я LocalStack
until curl -s -o /dev/null -w "%{http_code}" http://localhost:4566/_localstack/health | grep -q "200"; do
  sleep 2
  TRIES=$((TRIES + 1))
  if [ $TRIES -ge $MAX_TRIES ]; then
    echo "❌ LocalStack не запустився за відведений час!"
    exit 1
  fi
done

echo "✅ LocalStack запущено!"

# Змінні
S3_BUCKET_NAME="testhorizon"
AWS_ENDPOINT="http://localhost:4566"

# Встановлюємо облікові дані
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-2"

# Створюємо бакет у LocalStack
echo "🚀 Створюємо S3-бакет: $S3_BUCKET_NAME..."
aws --endpoint-url=$AWS_ENDPOINT s3 mb s3://$S3_BUCKET_NAME

# Перевіряємо, що бакет створено
echo "📂 Список бакетів:"
aws --endpoint-url=$AWS_ENDPOINT s3 ls

echo "✅ Ініціалізація завершена!"