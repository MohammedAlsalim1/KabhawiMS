# المرحلة الأولى: بناء المشروع وتوليد ملف الـ JAR داخل الحاوية
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# نسخ ملف إعدادات Maven (الاعتماديات)
COPY pom.xml .
# نسخ مجلد الأكواد المصدرية
COPY src ./src
# بناء المشروع وتخطي الاختبارات لتسريع عملية النشر
RUN mvn clean package -DskipTests

# المرحلة الثانية: بيئة التشغيل النهائية (النسخة الخفيفة)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# نسخ ملف الـ JAR من "المرحلة الأولى" بدلاً من البحث عنه في جهازك
COPY --from=build /app/target/*.jar app.jar
# كشف المنفذ 9091 (الخاص بخدمة الطلبات)
EXPOSE 1337
# أمر تشغيل السيرفر
ENTRYPOINT ["java", "-jar", "app.jar"]