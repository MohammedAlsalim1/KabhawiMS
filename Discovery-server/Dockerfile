# استخدام بيئة تشغيل جافا 21
FROM eclipse-temurin:21-jre-alpine

# تحديد مسار العمل داخل الحاوية
WORKDIR /app

# نسخ ملف الـ JAR الخاص بالديسكفري سيرفر
COPY target/Discovery-server-0.0.1-SNAPSHOT.jar app.jar

# كشف المنفذ الخاص بـ Eureka
EXPOSE 8761

# أمر التشغيل
ENTRYPOINT ["java", "-jar", "app.jar"]