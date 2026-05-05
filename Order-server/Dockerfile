# استخدام بيئة تشغيل جافا 21
FROM eclipse-temurin:21-jre-alpine

# تحديد مسار العمل داخل الحاوية
WORKDIR /app

# نسخ ملف الـ JAR الخاص بالـ Order-server
COPY target/Order-server-0.0.1-SNAPSHOT.jar app.jar

# كشف المنفذ 9091 (الخاص بخدمة الطلبات)
EXPOSE 9091

# أمر تشغيل السيرفر
ENTRYPOINT ["java", "-jar", "app.jar"]