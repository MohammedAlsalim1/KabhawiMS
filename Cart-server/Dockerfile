# استخدام بيئة تشغيل جافا 21 (نسخة خفيفة الحجم)
FROM eclipse-temurin:21-jre-alpine

# تحديد مسار العمل داخل الحاوية
WORKDIR /app

# نسخ ملف الـ JAR الذي قمت بإنشائه في إنتيليجي إلى داخل الحاوية
COPY target/Cart-server-0.0.1-SNAPSHOT.jar app.jar

# كشف المنفذ 9092
EXPOSE 9092

# أمر تشغيل السيرفر
ENTRYPOINT ["java", "-jar", "app.jar"]